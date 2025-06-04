package com.dudoji.tangvivor.matching.service

import android.content.Context
import android.util.Log
import com.dudoji.tangvivor.repository.UserRepository
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.AdvertisingOptions
import com.google.android.gms.nearby.connection.ConnectionInfo
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback
import com.google.android.gms.nearby.connection.ConnectionResolution
import com.google.android.gms.nearby.connection.ConnectionsStatusCodes
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo
import com.google.android.gms.nearby.connection.DiscoveryOptions
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback
import com.google.android.gms.nearby.connection.Payload
import com.google.android.gms.nearby.connection.PayloadCallback
import com.google.android.gms.nearby.connection.Strategy

class NearbyController(val context: Context,
                       private val payloadCallback: PayloadCallback, val onDiscoverChanged: () -> Unit) {
    companion object {
        private val SERVICE_ID = "com.dudoji.tangvivor.matching"
    }

    val connectedEndpointIds: MutableList<String> = mutableListOf()
    val discoveredEndpointIds : MutableList<String> = mutableListOf()

    private val endpointDiscoveryCallback: EndpointDiscoveryCallback =
    object : EndpointDiscoveryCallback() {
        override fun onEndpointFound(
            endpointId: String,
            p1: DiscoveredEndpointInfo
        ) {
            discoveredEndpointIds.add(endpointId)
            onDiscoverChanged()
        }

        override fun onEndpointLost(endpointId: String) {
            Log.d("NearbyController", "Endpoint lost: $endpointId")
            discoveredEndpointIds.remove(endpointId)
            onDiscoverChanged()
        }
    };

    private val connectionLifecycleCallback: ConnectionLifecycleCallback =
    object : ConnectionLifecycleCallback() {

        override fun onConnectionInitiated(endpointId: String, connectionInfo: ConnectionInfo) {
            Nearby.getConnectionsClient(context).acceptConnection(endpointId, payloadCallback);
        }

        override fun onConnectionResult(endpointId: String, result: ConnectionResolution) {
            when (result.getStatus().getStatusCode()) {
                ConnectionsStatusCodes.STATUS_OK -> {
                    connectedEndpointIds.add(endpointId)
                }
                ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED -> {
                    Log.d("NearbyController", "Connection rejected by the other endpoint.")
                }

                ConnectionsStatusCodes.STATUS_ERROR -> {
                    Log.d("NearbyController", "Connection error: ${result.getStatus().getStatusMessage()}")
                }

                else -> {
                    Log.d("NearbyController", "Unknown status code: ${result.getStatus().getStatusCode()}")
                }
            }
        }


        override fun onDisconnected(endpointId: String) {
            Log.d("NearbyController", "Disconnected from endpoint: $endpointId")
            connectedEndpointIds.remove(endpointId)
        }
    };

    fun connectToEndpoint(endpointId: String) {
        val userId = requireNotNull(UserRepository.me?.id) { "Authentication not initialized" }

        Nearby.getConnectionsClient(context)
            .requestConnection(userId, endpointId, connectionLifecycleCallback)
            .addOnSuccessListener {
                // Connection request sent successfully
            }
            .addOnFailureListener { e ->
                e.printStackTrace()
            }
    }

    fun sendPayload(endpointId: String, payload: Payload) {
        Nearby.getConnectionsClient(context)
            .sendPayload(endpointId, payload)
            .addOnFailureListener { e ->
                e.printStackTrace()
            }
    }

    fun disconnectFromEndpoint(endpointId: String) {
        Nearby.getConnectionsClient(context)
            .disconnectFromEndpoint(endpointId)
        connectedEndpointIds.remove(endpointId)
    }

    fun startAdvertising() {
        val userId = requireNotNull(UserRepository.me?.id) { "Authentication not initialized" }

        val advertisingOptions: AdvertisingOptions = AdvertisingOptions.Builder().setStrategy(Strategy.P2P_STAR).build()

        val connectionClient = Nearby.getConnectionsClient(context)

        connectionClient
            .startAdvertising(
                userId, SERVICE_ID, connectionLifecycleCallback, advertisingOptions)
            .addOnSuccessListener {
                // We're advertising!
            }
            .addOnFailureListener{ e ->
                e.printStackTrace()
            }
    }

    fun startDiscovery() {
        val discoveryOptions =
        DiscoveryOptions.Builder().setStrategy(Strategy.P2P_STAR).build();
        Nearby.getConnectionsClient(context)
            .startDiscovery(SERVICE_ID, endpointDiscoveryCallback, discoveryOptions)
            .addOnSuccessListener{

            }
            .addOnFailureListener{ e ->
                e.printStackTrace()
            }
    }
}