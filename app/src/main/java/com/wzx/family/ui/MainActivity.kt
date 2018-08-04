package com.wzx.family.ui

import android.Manifest
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Process
import android.provider.MediaStore
import android.support.annotation.RequiresApi
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.PopupMenu
import com.wzx.family.R
import com.wzx.family.base.BaseActivity
import com.wzx.family.databinding.ActivityMainBinding
import com.wzx.family.utils.getPath


//kotlin
class MainActivity : BaseActivity<ActivityMainBinding>(), PopupMenu.OnMenuItemClickListener {

    private val PermissionCamera = Manifest.permission.CAMERA
    private val PermissionWrite = Manifest.permission.WRITE_EXTERNAL_STORAGE

    private val CodePermissionCamera = 0x001
    private val CodePermissionWrite = 0x002
    private val CodePermissionVideo = 0x003

    private val CodeAlbum = 0x103
    private val CodeCamera = 0x104

    private var addMenu: PopupMenu? = null

    private var uri: Uri? = null

    override fun getLayoutId(): Int {
        return R.layout.activity_main
    }

    override fun init(savedInstanceState: Bundle?) {
        dataBinding.titleBar?.ivAdd?.setOnClickListener { view ->
            initAddMenu(view)
        }
        dataBinding.titleBar?.ivSearch?.setOnClickListener { view ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (requestPermission(PermissionCamera, CodePermissionVideo)) {
                    openDetecter()
                }
            }
        }

    }

    override fun initData() {

    }

    override fun onMenuItemClick(item: MenuItem?): Boolean {
        when (item!!.itemId) {
            R.id.add_by_album -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (requestPermission(PermissionWrite, CodePermissionWrite)) {
                        openAlbum()
                    }
                }
                return false
            }
            R.id.add_by_camera -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (requestPermission(PermissionCamera, CodePermissionCamera)) {
                        openCamera()
                    }
                }
                return false
            }
            else -> return true
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            CodePermissionWrite -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openAlbum()
                }
            }
            CodePermissionCamera -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openCamera()
                }
            }
            CodePermissionVideo ->{
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openDetecter()
                }
            }
            else -> {

            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            CodeAlbum -> {
                val mPath = data!!.data
                val file = getPath(this, mPath)

                goToRegister(file!!)
//                if (!TextUtils.isEmpty(file)) {
//                    val bmp = decodeImage(file!!)
//                    //前往注册
//
//                }
            }
            CodeCamera -> {
                val file = getPath(this, uri!!)

                goToRegister(file!!)

//                if (!TextUtils.isEmpty(file)) {
//                    val bmp = decodeImage(file!!)
//                    //前往注册
//                    goToRegister(file)
//                }
            }
        }
    }

    private fun goToRegister(file: String) {
        var intent = Intent(this, RegisterActivity::class.java)
        intent.putExtra("imagePath", file)
        startActivity(intent)
    }

    private fun openCamera() {
        val intent = Intent("android.media.action.IMAGE_CAPTURE")
        val values = ContentValues(1)
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri)
        startActivityForResult(intent, CodeCamera)
    }

    private fun openAlbum() {
        val getImageByalbum = Intent(Intent.ACTION_GET_CONTENT)
        getImageByalbum.addCategory(Intent.CATEGORY_OPENABLE)
        getImageByalbum.type = "image/jpeg"
        startActivityForResult(getImageByalbum, CodeAlbum)
    }

    private fun openDetecter(){
        var intent = Intent(this, DetecterActivity::class.java)
        startActivity(intent)
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

    /**
     * @param permission
     */
    @RequiresApi(Build.VERSION_CODES.M)
    private fun requestPermission(permission: String, code: Int): Boolean {
        if (PackageManager.PERMISSION_GRANTED != this.checkPermission(permission, Process.myPid(), Process.myUid())) {
            requestPermissions(arrayOf(permission), code)
            return false
        } else {
            return true
        }
    }

}
