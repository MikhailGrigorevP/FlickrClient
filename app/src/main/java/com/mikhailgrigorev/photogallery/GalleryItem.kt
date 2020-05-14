package com.mikhailgrigorev.photogallery


class GalleryItem {
    var caption: String? = null
    var id: String? = null
    var url: String? = null

    override fun toString(): String {
        return caption!!
    }
}
