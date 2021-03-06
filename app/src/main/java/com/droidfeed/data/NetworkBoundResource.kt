package com.droidfeed.data

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MediatorLiveData
import android.content.Context
import android.support.annotation.MainThread
import android.support.annotation.WorkerThread
import com.droidfeed.data.api.ApiResponse
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import org.jetbrains.annotations.Nullable

/**
 * Adapted from https://developer.android.com/topic/libraries/architecture/guide.html
 */
abstract class NetworkBoundResource<ResultType, RequestType>(val context: Context) {

    private val result = MediatorLiveData<Resource<ResultType?>>()

    init {
        result.value = Resource.loading()

        val dbSource = loadFromDb()

        result.addSource(dbSource) { data ->
            result.removeSource(dbSource)
            if (shouldFetch(data)) {
                fetchFromNetwork(dbSource)
            } else {
                result.addSource(dbSource) { newData -> result.setValue(Resource.success(newData)) }
            }
        }
    }

    private fun fetchFromNetwork(dbSource: LiveData<ResultType>) {
        val apiResponse: LiveData<ApiResponse<RequestType>> = createCall()

        // we re-attach dbSource as a new source, it will dispatch its latest value quickly
        result.addSource(dbSource) { newData ->
            result.setValue(Resource.loading(newData))
        }

        result.addSource(apiResponse) { response ->
            result.removeSource(apiResponse)
            result.removeSource(dbSource)

            if (response?.isSuccessful()!!) {
                launch {
                    response.let { processResponse(it)?.let { saveCallResult(it) } }
                }

                launch(UI) {
                    result.addSource(loadFromDb()) { newData ->
                        result.setValue(
                            Resource.success(
                                newData
                            )
                        )
                    }
                }
            } else {
                onFetchFailed()
                result.addSource(dbSource) { newData ->
                    result.setValue(
                        Resource.error(
                            response.errorMessage,
                            newData
                        )
                    )
                }
            }
        }
    }

    protected open fun onFetchFailed() {}

    @Suppress("UNCHECKED_CAST")
    fun asLiveData(): LiveData<Resource<ResultType>> = result as LiveData<Resource<ResultType>>

    @WorkerThread
    protected open fun processResponse(response: ApiResponse<RequestType>): RequestType? =
        response.body

    @WorkerThread
    protected abstract fun saveCallResult(item: RequestType)

    @MainThread
    protected abstract fun shouldFetch(@Nullable data: ResultType?): Boolean

    @MainThread
    protected abstract fun loadFromDb(): LiveData<ResultType>

    @MainThread
    protected abstract fun createCall(): LiveData<ApiResponse<RequestType>>
}