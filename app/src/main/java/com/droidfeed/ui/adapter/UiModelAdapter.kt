package com.droidfeed.ui.adapter

import android.support.v4.util.SparseArrayCompat
import android.support.v7.util.DiffUtil
import android.support.v7.util.ListUpdateCallback
import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import com.droidfeed.ui.adapter.diff.UiModelDiffCallback
import com.droidfeed.ui.common.BaseUiModel
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import org.jetbrains.anko.coroutines.experimental.bg


/**
 * Generic [RecyclerView.Adapter] for [BaseUiModel]s.
 *
 * Created by Dogan Gulcan on 11/2/17.
 */
@Suppress("UNCHECKED_CAST")
class UiModelAdapter constructor(
        private val dataInsertedCallback: DataInsertedCallback? = null
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val uiModels = ArrayList<BaseUiModelAlias>()
    private val viewTypes = SparseArrayCompat<BaseUiModelAlias>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
            viewTypes.get(viewType).getViewHolder(parent) as RecyclerView.ViewHolder

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        uiModels[position].bindViewHolder(holder)
    }

    override fun getItemCount(): Int = uiModels.size

    override fun getItemViewType(position: Int): Int {
        return if (position in 0..(itemCount - 1) && itemCount > 0) {
            uiModels[position]?.getViewType()
        } else {
            0
        }
    }

    fun addUiModels(uiModels: Collection<BaseUiModelAlias>?) {
        if (uiModels != null) {
            async(UI) {
                val oldItems = ArrayList(this@UiModelAdapter.uiModels)

                val diffResult = bg {
                    this@UiModelAdapter.uiModels.clear()
                    this@UiModelAdapter.uiModels.addAll(uiModels)

                    updateViewTypes(this@UiModelAdapter.uiModels)

                    DiffUtil.calculateDiff(UiModelDiffCallback(oldItems, uiModels as List<BaseUiModelAlias>))
                }


                diffResult.await().dispatchUpdatesTo(this@UiModelAdapter)
//                diffResult.await().dispatchUpdatesTo(object : ListUpdateCallback {
//                    override fun onInserted(position: Int, count: Int) {
////                        notifyItemRangeInserted(position, count)
//                        notifyDataSetChanged()
//                        dataInsertedCallback?.onDataInserted(position)
//                    }
//
//                    override fun onRemoved(position: Int, count: Int) {
//                        notifyItemRangeRemoved(position, count)
//                    }
//
//                    override fun onMoved(fromPosition: Int, toPosition: Int) {
//                        notifyItemMoved(fromPosition, toPosition)
//                    }
//
//                    override fun onChanged(position: Int, count: Int, payload: Any) {
//                        notifyItemRangeChanged(position, count, payload)
//                    }
//                })

                dataInsertedCallback?.onUpdated()
            }

        }
    }

    private fun updateViewTypes(uiModels: ArrayList<BaseUiModelAlias>) {
        uiModels.forEach {
            viewTypes.put(it.getViewType(), it)
        }
    }

    private val listUpdateCallBack = object : ListUpdateCallback {
        override fun onInserted(position: Int, count: Int) {
            notifyItemRangeInserted(position, count)
            dataInsertedCallback?.onDataInserted(position)
        }

        override fun onRemoved(position: Int, count: Int) {
            notifyItemRangeRemoved(position, count)
        }

        override fun onMoved(fromPosition: Int, toPosition: Int) {
            notifyItemMoved(fromPosition, toPosition)
        }

        override fun onChanged(position: Int, count: Int, payload: Any) {
            notifyItemRangeChanged(position, count, payload)
        }
    }

}

typealias BaseUiModelAlias = BaseUiModel<in RecyclerView.ViewHolder>