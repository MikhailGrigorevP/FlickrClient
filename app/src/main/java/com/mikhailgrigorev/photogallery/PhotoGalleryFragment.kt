package com.mikhailgrigorev.photogallery

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.*
import android.widget.ImageView
import android.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.mikhailgrigorev.photogallery.ThumbnailDownloader.ThumbnailDownloadListener


class PhotoGalleryFragment: Fragment() {

    private lateinit var mPhotoRecyclerView: RecyclerView
    private var items: MutableList<GalleryItem> = ArrayList()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
        setHasOptionsMenu(true)
        FetchItemsTask().execute()
        val responseHandler = Handler()
        mThumbnailDownloader = ThumbnailDownloader(responseHandler)
        mThumbnailDownloader!!.setThumbnailDownloadListener(
            object : ThumbnailDownloadListener<PhotoHolder> {
                override fun onThumbnailDownloaded(
                    target: PhotoHolder,
                    bitmap: Bitmap?
                ) {
                    val drawable: Drawable = BitmapDrawable(resources, bitmap)
                    target.bindDrawable(drawable)
                }
            }
        )
        mThumbnailDownloader!!.start()
        mThumbnailDownloader!!.looper
        Log.i(TAG, "Background thread started")

    }
    override fun onDestroyView() {
        super.onDestroyView()
        mThumbnailDownloader!!.clearQueue()
    }

    override fun onCreateOptionsMenu(menu: Menu, menuInflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, menuInflater)
        menuInflater.inflate(R.menu.fragment_photo_gallery, menu)
        val searchItem: MenuItem = menu.findItem(R.id.menu_item_search)
        val searchView: SearchView = searchItem.actionView as SearchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(s: String): Boolean {
                Log.d(TAG, "QueryTextSubmit: $s")
                QueryPreferences.setStoredQuery(activity, s)
                updateItems()
                return true
            }

            override fun onQueryTextChange(s: String): Boolean {
                Log.d(TAG, "QueryTextChange: $s")
                return false
            }
        })
        searchView.setOnSearchClickListener(View.OnClickListener {
            val query: String? = QueryPreferences.getStoredQuery(activity)
            searchView.setQuery(query, false)
        })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_item_clear -> {
                QueryPreferences.setStoredQuery(activity, null)
                updateItems()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun updateItems() {
        val query: String? = QueryPreferences.getStoredQuery(activity)
        FetchItemsTask(query).execute()
    }


    override fun onDestroy() {
        super.onDestroy()
        mThumbnailDownloader!!.quit()
        Log.i(TAG, "Background thread destroyed")
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

    @SuppressLint("StaticFieldLeak")
    private inner class FetchItemsTask(query: String? = null): AsyncTask<Void, Void, List<GalleryItem>>()
    {
        private var mQuery: String? = query

        override fun doInBackground(vararg params: Void?): List<GalleryItem> {
            return if (mQuery==null){
                FlickrFetchr().fetchRecentPhotos()
            } else {
                FlickrFetchr().searchPhotos(mQuery)
            }
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
        private var mThumbnailDownloader: ThumbnailDownloader<PhotoHolder>? = null
        private val TAG: String = "PhotoGalleryFragment"
        fun newInstance(): PhotoGalleryFragment {
            return PhotoGalleryFragment()
        }
    }

    private class PhotoHolder(itemView: View) : ViewHolder(itemView) {
        private val mItemImageView: ImageView = itemView.findViewById(R.id.item_image_view) as ImageView
        fun bindDrawable(drawable: Drawable){
            mItemImageView.setImageDrawable(drawable)
        }

    }

    private class PhotoAdapter(private val mGalleryItems: List<GalleryItem>) :
        RecyclerView.Adapter<PhotoHolder>() {
        private var context: Context? = null
        override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): PhotoHolder {
            val inflater = LayoutInflater.from(viewGroup.context)
            context = viewGroup.context
            val view: View =
                inflater.inflate(R.layout.galleryitem, viewGroup, false)
            return PhotoHolder(view)
        }

        override fun onBindViewHolder(photoHolder: PhotoHolder, position: Int) {
            val galleryItem = mGalleryItems[position]
            val placeholder: Drawable = context?.resources!!.getDrawable(R.drawable.bill_up_close)
            photoHolder.bindDrawable(placeholder)
            mThumbnailDownloader?.queueThumbnail(photoHolder, galleryItem.url)
        }

        override fun getItemCount(): Int {
            return mGalleryItems.size
        }

    }


}