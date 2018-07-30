package com.wzx.family.ui

import android.os.Bundle
import android.support.v7.app.ActionBar
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast
import com.wzx.family.R


class MainActivity : AppCompatActivity() {


    private var actionBar: ActionBar? = null

    private var popupWindow: PopupWindow? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        actionBar = supportActionBar

        actionBar!!.title = getString(R.string.app_name)


    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item!!.itemId) {
            R.id.action_add -> {
                Toast.makeText(this@MainActivity, "添加", Toast.LENGTH_SHORT).show()
                if (popupWindow == null) {
                    popupWindow = createPopupWindow()
                }
                popupWindow?.showAsDropDown(actionBar!!.customView)
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }


    private fun createPopupWindow(): PopupWindow {
        val contentView = LayoutInflater.from(this@MainActivity).inflate(R.layout.layout_popupwindow, null)
        contentView.findViewById<TextView>(R.id.action_album).setOnClickListener { view ->
            Toast.makeText(this@MainActivity, "相册", Toast.LENGTH_SHORT).show()
        }
        contentView.findViewById<TextView>(R.id.action_phone).setOnClickListener { view ->
            Toast.makeText(this@MainActivity, "拍照", Toast.LENGTH_SHORT).show()
        }
        return PopupWindow(contentView)
    }


}
