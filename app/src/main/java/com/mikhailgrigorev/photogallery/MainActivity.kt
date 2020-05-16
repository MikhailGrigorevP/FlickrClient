package com.mikhailgrigorev.photogallery

import android.content.Context
import android.content.Intent
import androidx.fragment.app.Fragment


class MainActivity : SingleFragmentActivity() {

    override fun createFragment(): Fragment? {
        return PhotoGalleryFragment.newInstance()
    }
    fun newIntent(context: Context?): Intent? {
        return Intent(context, MainActivity::class.java)
    }
}
