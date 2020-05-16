package com.mikhailgrigorev.photogallery

import android.app.AlarmManager
import android.app.IntentService
import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.net.ConnectivityManager
import android.os.SystemClock
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.mikhailgrigorev.photogallery.QueryPreferences.getLastResultId
import com.mikhailgrigorev.photogallery.QueryPreferences.getStoredQuery
import com.mikhailgrigorev.photogallery.QueryPreferences.setLastResultId


class PollService : IntentService(TAG) {
    override fun onHandleIntent(intent: Intent?) {
        if (!isNetworkAvailableAndConnected) {
            return
        }
        val query = getStoredQuery(this)
        val lastResultId = getLastResultId(this)
        val items: List<GalleryItem>
        items = if (query == null) {
            FlickrFetchr().fetchRecentPhotos()
        } else {
            FlickrFetchr().searchPhotos(query)
        }
        if (items.size == 0) {
            return
        }
        val resultId = items[0].id
        if (resultId == lastResultId) {
            Log.i(TAG, "Got an old result: $resultId")
        } else {
            Log.i(TAG, "Got a new result: $resultId")
            val resources: Resources = resources
            val i: Intent? = MainActivity().newIntent(this)
            val pi = PendingIntent
                .getActivity(this, 0, i, 0)
            val notification: Notification = NotificationCompat.Builder(this)
                .setTicker(resources.getString(R.string.new_pictures_title))
                .setSmallIcon(R.drawable.ic_menu_report_image)
                .setContentTitle(resources.getString(R.string.new_pictures_title))
                .setContentText(resources.getString(R.string.new_pictures_text))
                .setContentIntent(pi)
                .setAutoCancel(true)
                .build()
            val notificationManager =
                NotificationManagerCompat.from(this)
            notificationManager.notify(0, notification)
        }
        setLastResultId(this, resultId)
    }

    private val isNetworkAvailableAndConnected: Boolean
        private get() {
            val cm =
                getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val isNetworkAvailable = cm.activeNetworkInfo != null
            return isNetworkAvailable &&
                    cm.activeNetworkInfo.isConnected
        }

    companion object {
        private const val TAG = "PollService"
        private val POLL_INTERVAL_MS: Long = java.util.concurrent.TimeUnit.MINUTES.toMillis(15)
        fun newIntent(context: Context?): Intent {
            return Intent(context, PollService::class.java)
        }

        fun setServiceAlarm(context: Context, isOn: Boolean) {
            val i = newIntent(context)
            val pi = PendingIntent.getService(
                context, 0, i, 0
            )
            val alarmManager =
                context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (isOn) {
                alarmManager.setRepeating(
                    AlarmManager.ELAPSED_REALTIME,
                    SystemClock.elapsedRealtime(),
                    POLL_INTERVAL_MS,
                    pi
                )
            } else {
                alarmManager.cancel(pi)
                pi.cancel()
            }
        }

        fun isServiceAlarmOn(context: Context?): Boolean {
            val i = newIntent(context)
            val pi = PendingIntent
                .getService(context, 0, i, PendingIntent.FLAG_NO_CREATE)
            return pi != null
        }
    }
}
