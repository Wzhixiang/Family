package com.wzx.family.ui

import android.graphics.Bitmap
import android.graphics.Rect
import android.os.Bundle
import android.view.SurfaceHolder
import com.wzx.family.R
import com.wzx.family.base.BaseActivity
import com.wzx.family.databinding.ActivityRegisterBinding
import com.wzx.family.utils.bitmapToNV21
import com.wzx.family.utils.decodeImage


/**
 * 描述：
 *
 * 创建人： Administrator
 * 创建时间： 2018/8/3
 * 更新时间：
 * 更新内容：
 */
class RegisterActivity : BaseActivity<ActivityRegisterBinding>(), SurfaceHolder.Callback {


    private lateinit var surfaceHolder: SurfaceHolder

    private var bitmap: Bitmap? = null

    private var src = Rect()
    private var dst = Rect()

    private var thread: Thread = Thread(object : Runnable {
        override fun run() {
            if (surfaceHolder == null) {
                Thread.sleep(100)
            }

            val data = ByteArray(bitmap!!.width * bitmap!!.height * 3 / 2)


        }
    })

    override fun getLayoutId(): Int {
        return R.layout.activity_register
    }

    override fun init(savedInstanceState: Bundle?) {


    }

    override fun initData() {
        val imagePath = intent.getStringExtra("imagePath")
        bitmap = decodeImage(imagePath)

        if (bitmap == null) {
            return
        }
        src.set(0, 0, bitmap!!.width, bitmap!!.height)

        var data = ByteArray(bitmap!!.width * bitmap!!.height * 3 / 2)

        bitmapToNV21(data, bitmap!!)


    }

    override fun surfaceChanged(surfaceHolder: SurfaceHolder?, p1: Int, p2: Int, p3: Int) {

    }

    override fun surfaceDestroyed(surfaceHolder: SurfaceHolder?) {
        try {
            thread.join()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }

    }

    override fun surfaceCreated(surfaceHolder: SurfaceHolder?) {
        this.surfaceHolder = surfaceHolder!!

        thread.start()
    }


}