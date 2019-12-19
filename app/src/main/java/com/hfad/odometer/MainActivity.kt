package com.hfad.odometer

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.os.Handler
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity() {

    lateinit var odometer: OdometerService
    private var bound = false
    val PERMISSION_REQUEST_CODE = 698
    val NOTIFICATION_ID = 423

    private val connection = object : ServiceConnection {

        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            odometer = (binder as OdometerService.OdometerBinder).getOdometer()
            bound = true
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            bound = false
        }

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.size > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED
            ) {
               val i=Intent(this,OdometerService::class.java)
                bindService(i,connection,Context.BIND_AUTO_CREATE)
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val CHANNEL_ID = "my_channel_01"
                    val name: CharSequence = "my_channel"
                    val importance = NotificationManager.IMPORTANCE_HIGH
                    val mChannel = NotificationChannel(CHANNEL_ID, name, importance)
                    (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
                        .createNotificationChannel(mChannel)

                    val builder = NotificationCompat.Builder(this, CHANNEL_ID)
                        .setSmallIcon(android.R.drawable.ic_menu_compass)
                        .setContentTitle(getString(R.string.app_name))
                        .setContentText(getString(R.string.permission_denied))
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setVibrate(longArrayOf(0, 1000))
                        .setAutoCancel(true)
                    val actionIntent = Intent(this, MainActivity::class.java)
                    val actionPendingIntent = PendingIntent.getActivity(
                        this,
                        0,
                        actionIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                    )
                    builder.setContentIntent(actionPendingIntent)
                    (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
                        .notify(NOTIFICATION_ID, builder.build())
                } else {
                    @Suppress("DEPRECATION")
                    val builder = NotificationCompat.Builder(this)
                        .setSmallIcon(android.R.drawable.sym_def_app_icon)
                        .setContentTitle(getString(R.string.app_name))
                        .setContentText(getString(R.string.permission_denied))
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setVibrate(longArrayOf(0, 1000))
                        .setAutoCancel(true)
                    val actionIntent = Intent(this, MainActivity::class.java)
                    val actionPendingIntent = PendingIntent.getActivity(
                        this,
                        0,
                        actionIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                    )
                    builder.setContentIntent(actionPendingIntent)
                    (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
                        .notify(NOTIFICATION_ID, builder.build())
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        displayDistance()
    }

    override fun onStart() {
        super.onStart()
        if (ContextCompat.checkSelfPermission(this, OdometerService().PERMISSION_STRING)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(OdometerService().PERMISSION_STRING), PERMISSION_REQUEST_CODE
            )
        } else {
            val i = Intent(this, OdometerService::class.java)
            bindService(i, connection, Context.BIND_AUTO_CREATE)
        }
    }

    override fun onStop() {
        super.onStop()
        if (bound) {
            unbindService(connection)
            bound = false
        }
    }

    private fun displayDistance() {
        val handler = Handler()
        handler.post(object : Runnable {
            override fun run() {
                var distance = 0.0
                if (bound && odometer != null) distance = odometer.getDistance()
                distanceView.text = (String.format(
                    Locale.getDefault(),
                    "%1$,.2f miles", distance
                ))
                handler.postDelayed(this, 1000)
            }

        })

    }
}
