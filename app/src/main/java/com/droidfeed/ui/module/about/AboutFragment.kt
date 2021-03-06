package com.droidfeed.ui.module.about

import android.annotation.SuppressLint
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.droidfeed.R
import com.droidfeed.databinding.FragmentAboutBinding
import com.droidfeed.ui.adapter.BaseUiModelAlias
import com.droidfeed.ui.adapter.UiModelAdapter
import com.droidfeed.ui.common.BaseFragment
import com.droidfeed.util.CustomTab
import com.droidfeed.util.extention.startActivity
import com.droidfeed.util.glide.GlideApp
import javax.inject.Inject

@SuppressLint("ValidFragment")

class AboutFragment : BaseFragment() {

    private lateinit var binding: FragmentAboutBinding
    private lateinit var viewModel: AboutViewModel

    private val adapter: UiModelAdapter by lazy { UiModelAdapter() }

    @Inject
    lateinit var customTab: CustomTab

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAboutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders
            .of(this, viewModelFactory)
            .get(AboutViewModel::class.java)

        binding.viewModel = viewModel

        init()
        initObservers()
    }

    private fun init() {
        GlideApp.with(this)
            .load(R.drawable.df_blinking)
            .into(binding.imgAppLogo)

        binding.recyclerView.layoutManager = LinearLayoutManager(activity)
        binding.recyclerView.adapter = adapter
        binding.recyclerView.isNestedScrollingEnabled = false
    }

    @Suppress("UNCHECKED_CAST")
    private fun initObservers() {
        viewModel.licenceUiModels.observe(this, Observer {
            adapter.addUiModels(it as Collection<BaseUiModelAlias>)
        })

        viewModel.rateAppEvent.observe(this, Observer {
            activity?.let { it1 -> it?.startActivity(it1) }
        })

        viewModel.contactDevEvent.observe(this, Observer {
            activity?.let { it1 -> it?.startActivity(it1) }
        })

        viewModel.shareAppEvent.observe(this, Observer {
            activity?.let { it1 -> it?.startActivity(it1) }
        })

        viewModel.openLinkEvent.observe(this, Observer {
            it?.let { it1 -> customTab.showTab(it1) }
        })
    }
}