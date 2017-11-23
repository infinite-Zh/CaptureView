package com.infinite.demo

import android.graphics.BitmapFactory
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.SeekBar
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private var rValue: Float = 0f
    var gValue: Float = 0f
    var bValue: Float = 0f
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        sr.setOnSeekBarChangeListener(listener)
        sg.setOnSeekBarChangeListener(listener)
        sb.setOnSeekBarChangeListener(listener)
        shotView.bringToFront()
    }

    private val listener = object : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
            when (seekBar.id) {
                R.id.sr -> {
                    rValue = i.toFloat()
                }
                R.id.sg -> {
                    gValue = i.toFloat()
                }
                R.id.sb -> {
                    bValue = i.toFloat()
                }
            }
            setImageColorFilter()
        }

        override fun onStartTrackingTouch(seekBar: SeekBar) {

        }

        override fun onStopTrackingTouch(seekBar: SeekBar) {

        }

    }

    private fun setImageColorFilter() {
        imageView.colorFilter = null
        Log.e("values", rValue.toString() + "  " + gValue.toString() + "  " + bValue.toString())
        val MATRIX_BROWN = floatArrayOf(
                -1f, 0f, 0f, 0f, rValue,
                0f, -1f, 0f, 0f, gValue,
                0f, 0f, -1f, 0f, bValue,
                0f, 0f, 0f, 1f, 0f)

        val colorMatrix = ColorMatrixColorFilter(MATRIX_BROWN)

        imageView.colorFilter = colorMatrix

    }

    private fun setImageColorYUV() {
//        imageView.colorFilter=null
        val cm = ColorMatrix()
        cm.setSaturation(10f)
        val cmcf = ColorMatrixColorFilter(cm)
        imageView.colorFilter = cmcf
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menu?.add(0, 1, 0, "change")
        menu?.add(0, 2, 1, "shot")
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item!!.itemId) {
            1 -> setImageColorYUV()
            2 -> {
                var target = shotView.shot(BitmapFactory.decodeResource(resources, R.mipmap.a))
                imgTarget.setImageBitmap(target)
            }
        }
        setImageColorYUV()
        return super.onOptionsItemSelected(item)
    }
}

