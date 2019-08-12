package com.yariksoffice.imagedecoderplayground

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.ImageDecoder
import android.graphics.ImageDecoder.OnHeaderDecodedListener
import android.graphics.Rect
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Html
import android.util.Size
import android.view.View
import android.widget.ImageView
import android.widget.ScrollView
import android.widget.TextView

class MainActivity : AppCompatActivity() {

    private lateinit var preview: ImageView
    private lateinit var initialSize: TextView
    private lateinit var finalSize: TextView
    private lateinit var scroll: ScrollView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initViews()
    }

    private fun initViews() {
        preview = findViewById(R.id.preview)
        initialSize = findViewById(R.id.initial_size)
        finalSize = findViewById(R.id.final_size)
        scroll = findViewById(R.id.scroll)
        findViewById<TextView>(R.id.description1).html(R.string.description_first)
        findViewById<TextView>(R.id.description2).html(R.string.description_second)

        findViewById<View>(R.id.button1).setOnClickListener { chooseImage(IMAGE_TARGET_SAMPLE_SIZE) }
        findViewById<View>(R.id.button2).setOnClickListener { chooseImage(IMAGE_TARGET_SIZE) }
    }

    private fun chooseImage(requestCode: Int) {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
            .setType(INTENT_IMAGE_TYPE)
        startActivityForResult(intent, requestCode)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && data != null) {
            val image = data.data!!
            when (requestCode) {
                IMAGE_TARGET_SIZE -> processWithTargetSize(image)
                IMAGE_TARGET_SAMPLE_SIZE -> processWithTargetSampleSize(image)
            }
            scroll.fullScroll(View.FOCUS_DOWN)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun processWithTargetSize(image: Uri) {
        val header = OnHeaderDecodedListener { decoder, info, _ ->
            val size = info.size
            val requiredWidth = minOf(TARGET_SCALED_WIDTH, size.width)
            val coefficient = requiredWidth / size.width.toDouble()
            val newHeight = (size.height * coefficient).toInt()
            decoder.setTargetSize(requiredWidth, newHeight)
            decoder.crop = Rect(0, 0, requiredWidth, newHeight / 2)

            initialSize.text = "Initial Size: $size"
        }
        decodeAndShow(image, header)
    }


    @SuppressLint("SetTextI18n")
    private fun processWithTargetSampleSize(image: Uri) {
        val header = OnHeaderDecodedListener { decoder, info, _ ->
            val size = info.size
            val sampleSize = TARGET_SAMPLE_SIZE
            val newWidth = size.width / sampleSize
            val newHeight = size.height / sampleSize
            decoder.setTargetSampleSize(sampleSize)
            decoder.crop = Rect(0, 0, newWidth, newHeight / 2)

            initialSize.text = "Initial Size: $size"
        }
        decodeAndShow(image, header)
    }

    @SuppressLint("SetTextI18n")
    private fun decodeAndShow(image: Uri, header: OnHeaderDecodedListener) {
        val source = ImageDecoder.createSource(contentResolver, image)
        // use main thread here for the sake of simplicity
        val bitmap = ImageDecoder.decodeBitmap(source, header)
        preview.setImageBitmap(bitmap)

        val finalSize = Size(bitmap.width, bitmap.height)
        this.finalSize.text = "Final Size: $finalSize"
    }

    companion object {
        private const val INTENT_IMAGE_TYPE = "image/*"

        private const val IMAGE_TARGET_SIZE = 666
        private const val IMAGE_TARGET_SAMPLE_SIZE = 777

        private const val TARGET_SCALED_WIDTH = 500
        private const val TARGET_SAMPLE_SIZE = 2
    }

    @Suppress("DEPRECATION")
    private fun TextView.html(stringRes: Int) {
        text = Html.fromHtml(context.getString(stringRes))
    }

}
