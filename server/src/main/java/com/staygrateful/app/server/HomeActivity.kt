package com.staygrateful.app.server

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.staygrateful.app.server.databinding.ActivityHomeBinding
import com.staygrateful.app.server.utils.UtilsNetwork

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        registerBroadcast()

        bindView()

        bindEvent()

        startServerServices()
    }

    private fun bindView() {
        bindInfoView(false)
    }

    private fun bindInfoView(connected: Boolean) {
        binding.tvInfo.text = String.format(
            "IP Address \t: %s\nPort \t: %s\nStatus \t: %s",
            UtilsNetwork.getLocalIpAddress(this),
            TcpServerService.PORT,
            if (connected) "Connected" else "Not Connected"
        )
    }

    private fun bindEvent() {
        binding.btnSend.setOnClickListener {
            sendMessageToClient()
        }
    }

    private fun sendMessageToClient() {
        val log = binding.tvLog.text.toString()
        val text = binding.inputMsg.text.toString()
        binding.tvLog.text = "$log\nServer : $text".trim()
        TcpServerService.write(this, text)
        binding.inputMsg.setText("")
    }

    private fun startServerServices() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(Intent(applicationContext, TcpServerService::class.java))
        } else {
            startService(Intent(applicationContext, TcpServerService::class.java))
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterBroadcast()
    }

    private fun registerBroadcast() {
        LocalBroadcastManager.getInstance(this).registerReceiver(
            mServiceReceiver, IntentFilter(TcpServerService.SERVICE_NAME)
        )
    }

    private fun unregisterBroadcast() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(
            mServiceReceiver
        )
    }

    private var mServiceReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent != null) {
                val code = intent.getIntExtra(TcpServerService.KEY_CODE_STATE, 0)
                val data = intent.getSerializableExtra(TcpServerService.KEY_VALUE_STATE)

                if (code == TcpServerService.STATE_CODE_CONNECTED) {
                    bindInfoView(true)
                } else if (code == TcpServerService.STATE_CODE_READ) {
                    val text = binding.tvLog.text.toString()
                    binding.tvLog.text = "$text\nClient : $data".trim()
                }
            }
        }
    }
}