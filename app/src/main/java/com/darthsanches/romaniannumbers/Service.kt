package com.darthsanches.romaniannumbers

import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.support.v4.content.LocalBroadcastManager
import java.util.*
import java.util.concurrent.atomic.AtomicInteger

class Service : android.app.Service() {
    inner class Binder : android.os.Binder() {
        fun getService(): Service {
            return this@Service
        }
    }

    private val mBinder = Binder()
    private val handler = Handler(Looper.getMainLooper())

    companion object {
        const val UPDATE_PERIOD = 1000L
        private val map = TreeMap<Int, String>()

        init {
            map[1000] = "M"
            map[900] = "CM"
            map[500] = "D"
            map[400] = "CD"
            map[100] = "C"
            map[90] = "XC"
            map[50] = "L"
            map[40] = "XL"
            map[10] = "X"
            map[9] = "IX"
            map[5] = "V"
            map[4] = "IV"
            map[1] = "I"
        }
    }

    val counter = AtomicInteger(0)

    private val mRunnable = object : Runnable {
        override fun run() {
            if (counter.incrementAndGet() >= 100) counter.set(1)
            sendMessage(toRoman(counter.get()))
            handler.postDelayed(this, UPDATE_PERIOD)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        start()
        return flags
    }

    fun toRoman(number: Int): String? {
        val l = map.floorKey(number)
        return if (number == l) {
            map[number]
        } else map[l] + toRoman(number - l)
    }

    override fun onBind(intent: Intent): IBinder? {
        return mBinder
    }

    private fun sendMessage(message: String?) {
        val intent = Intent("update_number")
        intent.putExtra("number", message)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    fun stop() {
        handler.removeCallbacks(mRunnable)
    }

    fun start() {
        handler.postDelayed(mRunnable, UPDATE_PERIOD)
    }
}