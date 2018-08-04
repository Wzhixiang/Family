package com.wzx.family.utils

import android.annotation.TargetApi
import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.util.Log


/**
 * 更具图片地址获取bitmap
 *
 * @param path
 * @return
 */
fun decodeImage(path: String): Bitmap? {
    val res: Bitmap
    try {
        val exif = ExifInterface(path)
        val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)

        val op = BitmapFactory.Options()
        op.inSampleSize = 1
        op.inJustDecodeBounds = false
        //op.inMutable = true;
        res = BitmapFactory.decodeFile(path, op)
        //rotate and scale.
        val matrix = Matrix()

        if (orientation == ExifInterface.ORIENTATION_ROTATE_90) {
            matrix.postRotate(90f)
        } else if (orientation == ExifInterface.ORIENTATION_ROTATE_180) {
            matrix.postRotate(180f)
        } else if (orientation == ExifInterface.ORIENTATION_ROTATE_270) {
            matrix.postRotate(270f)
        }

        val temp = Bitmap.createBitmap(res, 0, 0, res.width, res.height, matrix, true)
        Log.d("image", "check target Image:" + temp.width + "X" + temp.height)

        if (temp != res) {
            res.recycle()
        }
        return temp
    } catch (e: Exception) {
        e.printStackTrace()
    }

    return null
}

/**
 * 将bitmap转ByteArray
 *
 * @param data 存放数据
 * @param bitmap 待转化位图
 */
fun bitmapToNV21(data: ByteArray, bitmap: Bitmap) {

    var argb = IntArray(bitmap.width * bitmap.height)

    // Copy pixel data from the Bitmap into the 'intArray' array
    bitmap.getPixels(argb, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)

    // Call to encoding function : convert intArray to Yuv Binary data
    encodeYUV420SP(data, argb, bitmap.width, bitmap.height)
}

/**
 * 将ByteArray转NV21
 *
 * @param data 存放数据
 * @param argb 传入argb数据
 * @param width 图片宽
 * @param height 图片高
 */
fun encodeYUV420SP(data: ByteArray, argb: IntArray, width: Int, height: Int) {

    val frameSize = width * height

    var yIndex = 0
    var uvIndex = frameSize

    var a: Int
    var R: Int
    var G: Int
    var B: Int
    var Y: Int
    var U: Int
    var V: Int
    var index = 0
    for (j in 0 until height) {
        for (i in 0 until width) {

            a = argb[index] and -0x1000000 shr 24 // a is not used obviously
            R = argb[index] and 0xff0000 shr 16
            G = argb[index] and 0xff00 shr 8
            B = argb[index] and 0xff shr 0

            // well known RGB to YUV algorithm
            Y = (66 * R + 129 * G + 25 * B + 128 shr 8) + 16
            U = (-38 * R - 74 * G + 112 * B + 128 shr 8) + 128
            V = (112 * R - 94 * G - 18 * B + 128 shr 8) + 128

            // NV21 has a plane of Y and interleaved planes of VU each sampled by a factor of 2
            //    meaning for every 4 Y pixels there are 1 V and 1 U.  Note the sampling is every other
            //    pixel AND every other scanline.
            data[yIndex++] = (if (Y < 0) 0 else if (Y > 255) 255 else Y).toByte()
            if (j % 2 == 0 && index % 2 == 0) {
                data[uvIndex++] = (if (V < 0) 0 else if (V > 255) 255 else V).toByte()
                data[uvIndex++] = (if (U < 0) 0 else if (U > 255) 255 else U).toByte()
            }

            index++
        }
    }
}


/**
 *
 */
@TargetApi(Build.VERSION_CODES.KITKAT)
fun getPath(context: Context, uri: Uri): String? {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
        if (DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                val type = split[0]

                if ("primary".equals(type, ignoreCase = true)) {
                    return Environment.getExternalStorageDirectory().absolutePath + "/" + split[1]
                }

                // TODO handle non-primary volumes
            } else if (isDownloadsDocument(uri)) {

                val id = DocumentsContract.getDocumentId(uri)
                val contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), java.lang.Long.valueOf(id)!!)

                return getDataColumn(context, contentUri, null, null)
            } else if (isMediaDocument(uri)) {
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                val type = split[0]

                var contentUri: Uri? = null
                if ("image" == type) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                } else if ("video" == type) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                } else if ("audio" == type) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                }

                val selection = "_id=?"
                val selectionArgs = arrayOf(split[1])

                return getDataColumn(context, contentUri, selection, selectionArgs)
            }
        }
    }
    val proj = arrayOf(MediaStore.Images.Media.DATA)
    val actualimagecursor = context.contentResolver.query(uri, proj, null, null, null)
    val actual_image_column_index = actualimagecursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
    actualimagecursor.moveToFirst()
    val img_path = actualimagecursor.getString(actual_image_column_index)
    val end = img_path.substring(img_path.length - 4)
    return if (0 != end.compareTo(".jpg", ignoreCase = true) && 0 != end.compareTo(".png", ignoreCase = true)) {
        null
    } else img_path
}

fun isExternalStorageDocument(uri: Uri): Boolean {
    return "com.android.externalstorage.documents".equals(uri.authority)
}

fun isDownloadsDocument(uri: Uri): Boolean {
    return "com.android.providers.downloads.documents".equals(uri.authority)
}

fun isMediaDocument(uri: Uri): Boolean {
    return "com.android.providers.media.documents".equals(uri.authority)
}

fun getDataColumn(context: Context, uri: Uri?, selection: String?, selectionArgs: Array<String>?): String? {
    var cursor: Cursor? = null
    val column = "_data"
    val projection = arrayOf(column)

    try {
        cursor = context.contentResolver.query(uri, projection, selection, selectionArgs, null)
        if (cursor != null && cursor!!.moveToFirst()) {
            val index = cursor!!.getColumnIndexOrThrow(column)
            return cursor!!.getString(index)
        }
    } finally {
        if (cursor != null)
            cursor!!.close()
    }
    return null
}