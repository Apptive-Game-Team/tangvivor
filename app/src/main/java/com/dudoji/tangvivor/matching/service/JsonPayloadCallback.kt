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
        val receivedBytes = payload.asBytes()
        val jsonStr = receivedBytes?.let { String(it) }
        if (jsonStr != null) {
            try {
                val jsonObject = JSONObject(jsonStr)
                Log.d("MatchingPayloadCallback", "Received JSON: $jsonObject")
                receiveCallback(jsonObject)
            } catch (e: JSONException) {
                Log.e("MatchingPayloadCallback", "Failed to parse JSON: $jsonStr", e)
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