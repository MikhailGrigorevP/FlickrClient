package com.mikhailgrigorev.photogallery

import android.os.AsyncTask
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder

class PhotoGalleryFragment: Fragment() {

    private lateinit var mPhotoRecyclerView: RecyclerView
    private var items: MutableList<GalleryItem> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
        FetchItemsTask().execute()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.fragment_photo_gallery, container, false)
        mPhotoRecyclerView = v.findViewById(R.id.photo_recycler_view) as RecyclerView
        mPhotoRecyclerView.layoutManager = GridLayoutManager(activity, 3)
        setupAdapter()
        return v

    }

    private inner class FetchItemsTask: AsyncTask<Void, Void, List<GalleryItem>>()
    {
        override fun doInBackground(vararg params: Void?): List<GalleryItem> {
            return FlickrFetchr().fetchItems()
        }

        override fun onPostExecute(result: List<GalleryItem>?) {
            items = result as MutableList<GalleryItem>
            setupAdapter()
        }
    }

    private fun setupAdapter() {
        if (isAdded) {
            mPhotoRecyclerView.setAdapter(PhotoAdapter(items))
        }
    }

    companion object{
        private val TAG: String = "PhotoGalleryFragment"
        fun newInstance(): PhotoGalleryFragment {
            return PhotoGalleryFragment()
        }
    }

    private class PhotoHolder(itemView: View) : ViewHolder(itemView) {
        private val mTitleTextView: TextView = itemView as TextView
        fun bindGalleryItem(item: GalleryItem) {
            mTitleTextView.text = item.toString()
        }

    }

    private class PhotoAdapter(private val mGalleryItems: List<GalleryItem>) :
        RecyclerView.Adapter<PhotoHolder>() {
        override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): PhotoHolder {
            val textView = TextView(viewGroup.context)
            return PhotoHolder(textView)
        }

        override fun onBindViewHolder(photoHolder: PhotoHolder, position: Int) {
            val galleryItem = mGalleryItems[position]
            photoHolder.bindGalleryItem(galleryItem)
        }

        override fun getItemCount(): Int {
            return mGalleryItems.size
        }

    }

}