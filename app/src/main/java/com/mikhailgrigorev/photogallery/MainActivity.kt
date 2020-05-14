package com.mikhailgrigorev.photogallery

import androidx.fragment.app.Fragment

class MainActivity : SingleFragmentActivity() {

    override fun createFragment(): Fragment? {
        return PhotoGalleryFragment.newInstance()
    }
}
