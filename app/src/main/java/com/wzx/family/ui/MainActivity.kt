package com.wzx.family.ui

import android.content.Intent
import android.os.Bundle
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.PopupMenu
import com.wzx.family.R
import com.wzx.family.base.BaseActivity
import com.wzx.family.databinding.ActivityMainBinding


class MainActivity : BaseActivity<ActivityMainBinding>(), PopupMenu.OnMenuItemClickListener {


    private var addMenu: PopupMenu? = null

    override fun getLayoutId(): Int {
        return R.layout.activity_main
    }

    override fun init(savedInstanceState: Bundle?) {
        dataBinding.titleBar?.ivAdd?.setOnClickListener { view ->
            initAddMenu(view)
        }

    }

    override fun initData() {

    }

    override fun onMenuItemClick(item: MenuItem?): Boolean {
        when (item!!.itemId) {
            R.id.add_by_album -> {

                return false
            }
            R.id.add_by_camera -> {

                return false
            }
            else -> return true
        }
    }

    private fun initAddMenu(view: View) {
        if (addMenu == null) {
            addMenu = PopupMenu(this, view)

            var inflater: MenuInflater = addMenu!!.menuInflater

            inflater.inflate(R.menu.main_menu, addMenu!!.menu)

            addMenu!!.setOnMenuItemClickListener(this)
        }

        addMenu?.show()
    }
}
