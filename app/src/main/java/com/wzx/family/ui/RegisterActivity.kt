package com.wzx.family.ui

import android.graphics.*
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Log
import android.view.SurfaceHolder
import com.arcsoft.facedetection.AFD_FSDKEngine
import com.arcsoft.facedetection.AFD_FSDKError
import com.arcsoft.facedetection.AFD_FSDKFace
import com.arcsoft.facedetection.AFD_FSDKVersion
import com.arcsoft.facerecognition.AFR_FSDKEngine
import com.arcsoft.facerecognition.AFR_FSDKError
import com.arcsoft.facerecognition.AFR_FSDKFace
import com.arcsoft.facerecognition.AFR_FSDKVersion
import com.wzx.family.BuildConfig
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

    private val MSG_CODE = 0x1000
    private val MSG_EVENT_REG = 0x1001
    private val MSG_EVENT_NO_FACE = 0x1002
    private val MSG_EVENT_NO_FEATURE = 0x1003
    private val MSG_EVENT_FD_ERROR = 0x1004
    private val MSG_EVENT_FR_ERROR = 0x1005

    private lateinit var surfaceHolder: SurfaceHolder

    private var bitmap: Bitmap? = null

    private var src = Rect()
    private var dst = Rect()

    private var registerSDK: AFR_FSDKFace? = null


    private var thread: Thread = Thread(Runnable {
        if (surfaceHolder == null) {
            Thread.sleep(100)
        }

        val data = ByteArray(bitmap!!.width * bitmap!!.height * 3 / 2)

        bitmapToNV21(data, bitmap!!)

        var engine = AFD_FSDKEngine()
        var version = AFD_FSDKVersion()
        var result: ArrayList<AFD_FSDKFace> = ArrayList()
        var err = engine.AFD_FSDK_InitialFaceEngine(BuildConfig.appid, BuildConfig.fd_key,
                AFD_FSDKEngine.AFD_OPF_0_HIGHER_EXT, 16, 5)
        Log.d("ArcFace", "AFD_FSDK_InitialFaceEngine = " + err.code)

        if (err.code != AFD_FSDKError.MOK) {
            var message = Message.obtain()
            message.what = MSG_CODE
            message.arg1 = MSG_EVENT_FD_ERROR
            message.arg2 = err.code
            handler.sendMessage(message)
        }

        err = engine.AFD_FSDK_GetVersion(version)
        Log.d("ArcFace", "AFD_FSDK_GetVersion =" + version.toString() + ", " + err.code)
        err = engine.AFD_FSDK_StillImageFaceDetection(data, bitmap!!.width, bitmap!!.height,
                AFD_FSDKEngine.CP_PAF_NV21, result)
        Log.d("ArcFace", "AFD_FSDK_StillImageFaceDetection =" + err.code + "<" + result.size)

        while (surfaceHolder != null) {
            var canvas = surfaceHolder.lockCanvas()
            if (canvas != null) {
                var paint = Paint()
                var fit_horizontal = canvas.width / src.width() < canvas.height / src.height()
                var scale = 1.0f
                if (fit_horizontal) {
                    scale = canvas.width / src.width().toFloat()
                    dst.left = 0
                    dst.top = (canvas.height - src.height() * scale).toInt() / 2
                    dst.right = dst.left + canvas.width
                    dst.bottom = dst.top + (src.height() * scale).toInt()
                } else {
                    scale = canvas.height / src.height().toFloat()
                    dst.left = (canvas.width - (src.width() * scale)).toInt() / 2
                    dst.top = 0
                    dst.right = dst.left + (src.width() * scale).toInt()
                    dst.bottom = dst.top + canvas.height
                }
                canvas.drawBitmap(bitmap, src, dst, paint)
                canvas.save()
                canvas.scale(dst.width() / src.width().toFloat(), dst.height() / src.height().toFloat())
                canvas.translate(dst.left / scale, dst.top / scale)
                for (face: AFD_FSDKFace in result) {
                    paint.color = Color.RED
                    paint.strokeWidth = 10.0f
                    paint.style = Paint.Style.STROKE
                    canvas.drawRect(face.rect, paint)
                }
                canvas.restore()
                surfaceHolder.unlockCanvasAndPost(canvas)

                break
            }
        }
        if (!result.isEmpty()) {
            var version1 = AFR_FSDKVersion()
            var engine1 = AFR_FSDKEngine()
            var result1 = AFR_FSDKFace()
            var error1 = engine1.AFR_FSDK_InitialEngine(BuildConfig.appid, BuildConfig.fr_key)

            if (error1.code != AFD_FSDKError.MOK) {
                var reg = Message.obtain()
                reg.what = MSG_CODE
                reg.arg1 = MSG_EVENT_FR_ERROR
                reg.arg2 = error1.code
                handler.sendMessage(reg)
            }
            error1 = engine1.AFR_FSDK_GetVersion(version1)
            Log.d("ArcFace", "FR=" + version.toString() + "," + error1.code)
            error1 = engine1.AFR_FSDK_ExtractFRFeature(data, bitmap!!.width, bitmap!!.height, AFR_FSDKEngine.CP_PAF_NV21, Rect(result[0].rect), result[0].degree, result1)
            Log.d("ArcFace", "Face=" + result1.featureData[0] + "," + result1.featureData[1] + "," + result1.featureData[2] + "," + error1.code)

            if (error1.code == AFR_FSDKError.MOK) {
                registerSDK = result1.clone()
                val width = result[0].rect.width()
                val height = result[0].rect.height()
                val faceBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
                val faceCanvas = Canvas(faceBitmap)
                faceCanvas.drawBitmap(bitmap, result[0].rect, Rect(0, 0, width, height), null)
                var reg = Message.obtain()
                reg.what = MSG_CODE
                reg.arg1 = MSG_EVENT_REG
                reg.obj = faceBitmap
                handler.sendMessage(reg)
            } else {
                var reg = Message.obtain()
                reg.what = MSG_CODE
                reg.arg1 = MSG_EVENT_NO_FEATURE
                handler.sendMessage(reg)
            }
            error1 = engine1.AFR_FSDK_UninitialEngine()
            Log.d("ArcFace", "AFR_FSDK_UninitialEngine : " + error1.code)

        } else {
            val reg = Message.obtain()
            reg.what = MSG_CODE
            reg.arg1 = MSG_EVENT_NO_FACE
            handler.sendMessage(reg)
        }

        err = engine.AFD_FSDK_UninitialFaceEngine()
        Log.d("ArcFace", "AFD_FSDK_UninitialFaceEngine =" + err.code)
    })

    private var handler = Handler(Handler.Callback { msg ->
        when (msg.what) {
            MSG_CODE -> {
                false
            }
            MSG_EVENT_REG -> {
                false
            }
            MSG_EVENT_NO_FACE -> {
                false
            }
            MSG_EVENT_NO_FEATURE -> {
                false
            }
            MSG_EVENT_FD_ERROR -> {
                false
            }
            MSG_EVENT_FR_ERROR -> {
                false
            }
            else -> {
                true
            }
        }
    })

    override fun getLayoutId(): Int {
        return R.layout.activity_register
    }

    override fun init(savedInstanceState: Bundle?) {

        dataBinding.surfaceView.holder.addCallback(this)
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