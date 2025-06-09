package com.dudoji.tangvivor.matching.service

import android.util.Log
import com.google.android.gms.nearby.connection.Payload
import com.google.android.gms.nearby.connection.PayloadCallback

import com.google.android.gms.nearby.connection.PayloadTransferUpdate
import org.json.JSONException
import org.json.JSONObject

class JsonPayloadCallback(val receiveCallback: (JSONObject) -> Unit): PayloadCallback() {
    override fun onPayloadReceived(
        endpointId: String,
        payload: Payload
    ) {
        Log.d("NearbySystem", "Payload received from endpoint: $endpointId")
        val receivedBytes = payload.asBytes()
        val jsonStr = receivedBytes?.let { String(it) }
        Log.d("NearbySystem", "Received bytes: $jsonStr")
        if (jsonStr != null) {
            try {
                val jsonObject = JSONObject(jsonStr)
                Log.d("NearbySystem", "Received JSON: $jsonObject")
                receiveCallback(jsonObject)
            } catch (e: JSONException) {
                Log.e("NearbySystem", "Failed to parse JSON: $jsonStr", e)
            }
        } else {
            Log.e("MatchingPayloadCallback", "Received payload is null or not a valid byte array.")
        }
    }

    override fun onPayloadTransferUpdate(
        endpointId: String,
        payloadTransferUpdate: PayloadTransferUpdate
    ) {

    }
}