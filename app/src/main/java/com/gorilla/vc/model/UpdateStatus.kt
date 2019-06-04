package com.gorilla.vc.model

import com.google.gson.annotations.SerializedName
import org.json.JSONArray
import org.json.JSONObject

class UpdateStatus {

    @SerializedName("host")
    var host: Int ?= null

    @SerializedName("isChatRoomEnabled")
    var isChatRoomEnabled: Int ?= null

    @SerializedName("isRecord")
    var isRecord: Boolean ?= null

    @SerializedName("participants")
    var participants: ArrayList<ParticipantStatus> ?= null

    @SerializedName("presentMode")
    var presentMode: Int ?= null

    @SerializedName("presenter1")
    var presenter1: Int ?= null

    @SerializedName("presenter2")
    var presenter2: Int ?= null

    /*  Must return JSON object with following order
        {
            "presentMode": -1,
            "presenter1": -1,
            "presenter2": -1,
            "isChatRoomEnabled": {0 or 1},
            "host": -1,
            "participants": [
                {
                    "id": 99,
                    "isMicEnabled": true,
                    "isCamEnabled": false,
                    "streamingContent": {-1, 0, 1, 2}
                    "rtspUrl": "",
                    "streamingContentStatus": 99
                }
            ],
            "isRecord": false
        }
     */
    private fun toJsonArray(list: ArrayList<ParticipantStatus>?): JSONArray {
        val array = JSONArray()
        list?.forEach { participant ->
            val jsonObject = JSONObject()
            jsonObject.put("id", participant.id)
            jsonObject.put("isMicEnabled", participant.isMicEnabled)
            jsonObject.put("isCamEnabled", participant.isCamEnabled)
            jsonObject.put("streamingContent", participant.streamingContent)
            jsonObject.put("rtspUrl", participant.rtspUrl)
            jsonObject.put("streamingContentStatus", participant.streamingContentStatus)
            array.put(jsonObject)
        }
        return array
    }

    private fun toJsonObject(status: UpdateStatus): JSONObject {
        val data = JSONObject()
        data.put("presentMode", status.presentMode)
        data.put("presenter1", status.presenter1)
        data.put("presenter2", status.presenter2)
        data.put("isChatRoomEnabled", status.isChatRoomEnabled)
        data.put("host", status.host)
        data.put("participants", toJsonArray(status.participants))
        data.put("isRecord", status.isRecord)
        return data
    }

    fun getControlChatRoomObject(status: UpdateStatus, isEnable: Boolean): JSONObject {
        if (isEnable) {
            status.isChatRoomEnabled = 1
        } else {
            status.isChatRoomEnabled = 0
        }
        return toJsonObject(status)
    }

    fun getChangeHostObject(status: UpdateStatus, hostId: Int): JSONObject {
        status.host = hostId
        return toJsonObject(status)
    }

    fun getControlParticipantObject(status: UpdateStatus, id: Int,
                                    isVideoEnable: Boolean, isAudioEnable: Boolean): JSONObject {
        status.participants?.forEach loop@ { participant ->
            if (participant.id == id) {
                participant.isCamEnabled = isVideoEnable
                participant.isMicEnabled = isAudioEnable
                return@loop
            }
        }

        return toJsonObject(status)
    }

    fun getControlStreamObject(status: UpdateStatus, id: Int, streamContent: Int, rtspUrl: String?): JSONObject {
         status.participants?.forEach loop@ { participant ->
             if (participant.id == id) {
                 participant.streamingContent = streamContent
                 participant.rtspUrl = rtspUrl
                 return@loop
             }
         }
        return toJsonObject(status)
    }

    /**
     * @param leftId Which participant's video will show on the left side
     * @param rightId In contrast, show on the right side
     */
    fun getHostChangeViewObject(status: UpdateStatus, mode: Int, leftId: Int, rightId: Int): JSONObject {
        status.presentMode = mode

        if (mode == PresentMode.SINGLE_HOST) {
            status.presenter1 = leftId
            status.presenter2 = -1
        }

        if (mode == PresentMode.DUAL_HOST) {
            status.presenter1 = leftId
            status.presenter2 = rightId
        }
        return toJsonObject(status)
    }
}