package com.darthsanches.romaniannumbers

import android.content.*
import android.os.Bundle
import android.os.IBinder
import android.support.v4.content.LocalBroadcastManager
import android.support.v7.app.AppCompatActivity
import android.util.TypedValue
import android.view.View.TEXT_ALIGNMENT_CENTER
import android.widget.Button
import android.widget.TextSwitcher
import android.widget.TextView


class MainActivity : AppCompatActivity() {

    private var mService: Service? = null
    lateinit var textView: TextSwitcher
    lateinit var button: Button

    var isRunning = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        textView = findViewById(R.id.text_view)
        textView.setFactory {
            val t = TextView(this)
            t.textAlignment = TEXT_ALIGNMENT_CENTER
            t.textSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 20F, resources.displayMetrics)
            return@setFactory t
        }
        textView.setInAnimation(this, android.R.anim.slide_in_left)
        textView.setOutAnimation(this, android.R.anim.slide_out_right)
        button = findViewById(R.id.button)
        button.setOnClickListener {
            if (isRunning) {
                isRunning = false
                mService?.stop()
                button.text = getString(R.string.start)
            } else {
                isRunning = true
                mService?.start()
                button.text = getString(R.string.stop)
            }
        }
        if (savedInstanceState == null) {
            startService(Intent(this, Service::class.java))
        }
    }

    public override fun onResume() {
        super.onResume()
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(
                mMessageReceiver,
                IntentFilter("update_number")
            )
    }

    private val mMessageReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            textView.setText(intent.getStringExtra("number"))
        }
    }

    override fun onPause() {
        LocalBroadcastManager.getInstance(this)
            .unregisterReceiver(mMessageReceiver)
        super.onPause()
    }

    override fun onStart() {
        super.onStart()
        val intent = Intent(this, Service::class.java)
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE)
    }

    override fun onStop() {
        super.onStop()
        if (mService != null) {
            unbindService(mConnection)
            mService = null
        }
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        outState?.putBoolean("is_running", isRunning)
        outState?.putString("text", (textView.currentView as TextView).text.toString())
        super.onSaveInstanceState(outState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        (textView.currentView as TextView).text = savedInstanceState?.getString("text")
        isRunning = savedInstanceState?.getBoolean("is_running") ?: false
        button.text = if (isRunning) getString(R.string.stop) else getString(R.string.start)

        super.onRestoreInstanceState(savedInstanceState)
    }

    private val mConnection = object : ServiceConnection {

        override fun onServiceConnected(
            className: ComponentName,
            service: IBinder
        ) {
            val binder = service as Service.Binder
            mService = binder.getService()
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            mService = null
        }
    }
}
