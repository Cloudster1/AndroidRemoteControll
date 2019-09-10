package com.remotecontol

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.*
import com.remotecontol.Dialogs.PeerAdapter
import com.remotecontol.Dialogs.PeerConnectionDialog
import com.remotecontol.Views.JoystickView
import com.remotecontol.Views.VideoStreamView
import java.nio.charset.Charset
import kotlin.math.roundToInt


class FullscreenActivity : AppCompatActivity(), JoystickView.JoystickListener,
    PeerConnectionDialog.DialogListener {

    private var endpointId = ""
    private var endpoints: MutableList<List<String?>> = ArrayList()
    private lateinit var device_name: String

    private val mConnectionLifecycleCallback = object : ConnectionLifecycleCallback() {
        override fun onConnectionInitiated(endpointId: String, connectionInfo: ConnectionInfo) {
            Log.d("connect", "connected to ${connectionInfo.endpointName}")

            Nearby.getConnectionsClient(this@FullscreenActivity)
                .acceptConnection(endpointId, mPayloadCallback)
        }

        override fun onConnectionResult(endpointId: String, result: ConnectionResolution) {
            when (result.status.statusCode) {
                ConnectionsStatusCodes.STATUS_OK -> {
                    // We're connected! Can now start sending and receiving data.
                    Log.d("Connection Status", "ok")
                    this@FullscreenActivity.endpointId = endpointId
                }
                ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED -> {
                    // The connection was rejected by one or both sides.
                    Log.d("Connection Status", "rejected")
                }
                else -> {
                    // The connection was broken before it was accepted.
                    Log.d("Connection Status", "connection broke")
                    this@FullscreenActivity.endpointId = ""
                }
            }
        }

        override fun onDisconnected(endpointId: String) {
            // We've been disconnected from this endpoint. No more data can be
            // sent or received.
            Log.d("Connection Status", "disconnected from $endpointId")
            this@FullscreenActivity.endpointId = ""
        }
    }

    private val mEndpointDiscoveryCallback = object : EndpointDiscoveryCallback() {
        override fun onEndpointFound(endpointID: String?, info: DiscoveredEndpointInfo?) {
            Log.d("Endpoint", "Found ${info?.endpointName}")
            endpoints.add(listOf(endpointID, info?.endpointName))

            PeerConnectionDialog(PeerAdapter(endpoints, this@FullscreenActivity)).show(
                supportFragmentManager,
                "endpoints"
            )

        }


        override fun onEndpointLost(p0: String?) {
            Log.d("Endpoint", "Endpoint Lost")
        }
    }

    private val mPayloadCallback = object : PayloadCallback() {
        override fun onPayloadReceived(endpointId: String, payload: Payload) {
            val p = payload.asBytes()?.toString(Charset.defaultCharset())
            Log.d("Receive", "received ${p}")
        }

        override fun onPayloadTransferUpdate(endpointId: String, update: PayloadTransferUpdate) {
            // Payload progress has updated.
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        checkPermissions()

        // set the layout, hide action and support action bar and set keep screen on flag
        setContentView(R.layout.activity_fullscreen)
        supportActionBar?.hide()
        actionBar?.hide()
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)


        var leftJoystick = findViewById<JoystickView>(R.id.joystick_left)
        var rightJoystick = findViewById<JoystickView>(R.id.joystick_right)

        val videoView = findViewById<VideoStreamView>(R.id.videoView)

        val refresh = findViewById<ImageButton>(R.id.refreshButton)
        refresh.setOnClickListener {
            Toast.makeText(this, "yeah", Toast.LENGTH_LONG).show()
            discover()
        }

        device_name = BluetoothAdapter.getDefaultAdapter().name
        Log.d("device", device_name)
    }

    private fun discover() {
        Log.d("discover", "started discovering")
        Nearby.getConnectionsClient(this).startDiscovery(
            "Service",
            mEndpointDiscoveryCallback,
            DiscoveryOptions(Strategy.P2P_STAR)
        )
    }


    override fun onResume() {
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
    }


    // action to do if one of the joysticks is moved
    // TODO add the information transport to client
    override fun onJoystickMoved(xPercent: Float, yPercent: Float, source: Int) {
        when (source) {
            R.id.joystick_right -> {
                Log.d(
                    "Joystick right",
                    "X Percent: " + (xPercent * 100f).roundToInt() + "\tY Percent: " + (yPercent * 100f).roundToInt()
                )

                if (!endpointId.isBlank()) {
                    Nearby.getConnectionsClient(this)
                        .sendPayload(
                            endpointId,
                            Payload.fromBytes(
                                createBArray(
                                    1,
                                    (xPercent * 100f).roundToInt(),
                                    (yPercent * 100f).roundToInt()
                                )
                            )
                        )
                }
            }
            R.id.joystick_left -> {
                Log.d(
                    "Joystick left",
                    "X Percent: " + (xPercent * 100f).roundToInt() + "\tY Percent: " + (yPercent * 100f).roundToInt()
                )

                if (!endpointId.isBlank()) {
                    Nearby.getConnectionsClient(this)
                        .sendPayload(
                            endpointId,
                            Payload.fromBytes(
                                createBArray(
                                    2,
                                    (xPercent * 100f).roundToInt(),
                                    (yPercent * 100f).roundToInt()
                                )
                            )
                        )
                }
            }
        }
    }

    override fun onPeerSelect(peer: String?) {
        Log.d("Peer", "Selected Peer $peer")

        endpointId = peer!!

        Nearby.getConnectionsClient(this@FullscreenActivity)
            .requestConnection("1", endpointId, mConnectionLifecycleCallback)
            .addOnSuccessListener { Log.d("connection", "succesfully connected") }
            .addOnFailureListener { Log.d("connection", "connection failed") }
    }

    fun createBArray(joystick: Int, x: Int, y: Int): ByteArray {
        val b = ByteArray(3)
        b[0] = joystick.toByte()
        b[1] = x.toByte()
        b[2] = y.toByte()

        return b
    }


    private fun checkPermissions() {
        val PERMISSIONS_ALL = 1
        val PERMISSIONS = arrayOf(
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.BLUETOOTH,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.CHANGE_WIFI_STATE,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )

        if (!hasPermissions(this, *PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSIONS_ALL)
        }
    }

    private fun hasPermissions(context: Context?, vararg permissions: String): Boolean {
        if (context != null) {
            for (permission in permissions) {
                if (ActivityCompat.checkSelfPermission(
                        context,
                        permission
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    return false
                }
            }
        }
        return true
    }

    // show system bars
// TODO hide after a short period of time, instead of using one of them
    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) hideSystemUI()
    }

    private fun hideSystemUI() {
        // Enables regular immersive mode.
        // For "lean back" mode, remove SYSTEM_UI_FLAG_IMMERSIVE.
        // Or for "sticky immersive," replace it with SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE
                // Set the content to appear under the system bars so that the
                // content doesn't resize when the system bars hide and show.
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                // Hide the nav bar and status bar
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN)
    }

    // Shows the system bars by removing all the flags
// except for the ones that make the content appear under the system bars.
    private fun showSystemUI() {
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
    }
}
