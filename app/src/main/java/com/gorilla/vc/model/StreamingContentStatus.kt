package com.gorilla.vc.model


class StreamingContentStatus {
    companion object {
        // Read fail at playing status
        val FAILED_PLAYING = -2
        val FAILED = -1
        val SUCCESS = 1
    }
}