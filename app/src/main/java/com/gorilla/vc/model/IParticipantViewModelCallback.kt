package com.gorilla.vc.model

interface IParticipantViewModelCallback {
    fun onLoadParticipants(participants: ArrayList<Participant>)
    fun onLoadParticipantsFailed()
}