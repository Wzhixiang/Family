package com.wzx.family.base

import android.databinding.DataBindingUtil
import android.databinding.ViewDataBinding
import android.os.Bundle
import android.support.v7.app.AppCompatActivity

/**
 * 描述：
 *
 * 创建人： Administrator
 * 创建时间： 2018/8/2
 * 更新时间：
 * 更新内容：
 */
abstract class BaseActivity<T : ViewDataBinding> : AppCompatActivity() {

    protected lateinit var dataBinding: T

    abstract fun getLayoutId(): Int

    abstract fun init(savedInstanceState: Bundle?)

    abstract fun initData()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        dataBinding = DataBindingUtil.setContentView<T>(this, getLayoutId())

        init(savedInstanceState)
    }

    override fun onStart() {
        super.onStart()

        initData()
    }
}