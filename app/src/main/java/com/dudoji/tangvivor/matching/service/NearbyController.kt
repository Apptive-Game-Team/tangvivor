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
import java.util.concurrent.ConcurrentHashMap

class NearbyController(val context: Context,
                       private val payloadCallback: PayloadCallback, val onDiscoverChanged: () -> Unit) {
    companion object {
        private val SERVICE_ID = "com.dudoji.tangvivor.matching"
    }

    val connectedEndpoints: MutableMap<String, String> = ConcurrentHashMap()
    val discoveredEndpoints: MutableMap<String, String> = ConcurrentHashMap()

    private val endpointDiscoveryCallback: EndpointDiscoveryCallback =
    object : EndpointDiscoveryCallback() {
        override fun onEndpointFound(
            endpointId: String,
            p1: DiscoveredEndpointInfo
        ) {
            Log.d("NearbySystem", "Endpoint found: $endpointId")
            discoveredEndpoints.put(endpointId, p1.endpointName)
            onDiscoverChanged()
        }

        override fun onEndpointLost(endpointId: String) {
            Log.d("NearbySystem", "Endpoint lost: $endpointId")
            discoveredEndpoints.remove(endpointId)
            onDiscoverChanged()
        }
    };

    private val connectionLifecycleCallback: ConnectionLifecycleCallback =
    object : ConnectionLifecycleCallback() {

        override fun onConnectionInitiated(endpointId: String, connectionInfo: ConnectionInfo) {
            Log.d("NearbySystem", "Connection initiated with endpoint: $endpointId")
            Nearby.getConnectionsClient(context).acceptConnection(endpointId, payloadCallback);
        }

        override fun onConnectionResult(endpointId: String, result: ConnectionResolution) {
            when (result.getStatus().getStatusCode()) {
                ConnectionsStatusCodes.STATUS_OK -> {
                    connectedEndpoints.put(endpointId, discoveredEndpoints[endpointId] ?: "Unknown Endpoint")
                    Log.d("NearbySystem", "Connected to endpoint: $endpointId")
                }
                ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED -> {
                    Log.d("NearbySystem", "Connection rejected by the other endpoint.")
                }

                ConnectionsStatusCodes.STATUS_ERROR -> {
                    Log.d("NearbySystem", "Connection error: ${result.getStatus().getStatusMessage()}")
                }

                else -> {
                    Log.d("NearbySystem", "Unknown status code: ${result.getStatus().getStatusCode()}")
                }
            }
        }


        override fun onDisconnected(endpointId: String) {
            Log.d("NearbySystem", "Disconnected from endpoint: $endpointId")
            connectedEndpoints.remove(endpointId)
        }
    };

    fun connectToEndpoint(endpointId: String) {
        val userId = requireNotNull(UserRepository.me?.id) { "Authentication not initialized" }

        Nearby.getConnectionsClient(context)
            .requestConnection(userId, endpointId, connectionLifecycleCallback)
            .addOnSuccessListener {
                // Connection request sent successfully
                Log.d("NearbySystem", "Connection request sent to endpoint: $endpointId")
            }
            .addOnFailureListener { e ->
                Log.e("NearbySystem", "Failed to send connection request to endpoint: $endpointId", e)
            }
    }

    fun sendPayload(endpointId: String, payload: Payload) {
        Nearby.getConnectionsClient(context)
            .sendPayload(endpointId, payload)
            .addOnSuccessListener {
                Log.d("NearbySystem", "Payload sent successfully to endpoint: $endpointId")
            }
            .addOnFailureListener { e ->
                Log.e("NearbySystem", "Failed to send payload to endpoint: $endpointId", e)
            }
    }

    fun disconnectFromEndpoint(endpointId: String) {
        Nearby.getConnectionsClient(context)
            .disconnectFromEndpoint(endpointId)
        connectedEndpoints.remove(endpointId)
    }

    fun startAdvertising() {
        val userId = requireNotNull(UserRepository.me?.id) { "Authentication not initialized" }

        val advertisingOptions: AdvertisingOptions = AdvertisingOptions.Builder().setStrategy(Strategy.P2P_STAR).build()

        val connectionClient = Nearby.getConnectionsClient(context)

        connectionClient
            .startAdvertising(
                userId, SERVICE_ID, connectionLifecycleCallback, advertisingOptions)
            .addOnSuccessListener {
                Log.d("NearbySystem", "Started advertising with user ID: $userId")
            }
            .addOnFailureListener{ e ->
                Log.e("NearbySystem", "Failed to start advertising", e)
            }
    }

    fun startDiscovery() {
        val discoveryOptions =
        DiscoveryOptions.Builder().setStrategy(Strategy.P2P_STAR).build();
        Nearby.getConnectionsClient(context)
            .startDiscovery(SERVICE_ID, endpointDiscoveryCallback, discoveryOptions)
            .addOnSuccessListener{
                Log.d("NearbySystem", "Started discovery for service ID: $SERVICE_ID")
            }
            .addOnFailureListener{ e ->
                Log.e("NearbySystem", "Failed to start discovery", e)
            }
    }
}