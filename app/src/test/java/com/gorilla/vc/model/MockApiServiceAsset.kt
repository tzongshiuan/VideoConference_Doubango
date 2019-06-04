package com.gorilla.vc.model

import java.io.File
import java.nio.charset.Charset

object MockApiServiceAsset {
    private val BASE_PATH = "${System.getProperty("user.dir")}\\src\\test\\java\\com\\gorilla\\vc\\mocks\\data"

    // User API corresponding file path
    val PARTICIPANTS_DATA = "$BASE_PATH\\Participants_Json_Test"
    val PARTICIPANT_ID_DATA = "$BASE_PATH\\Participant_Id_Json_Test"
    val MEETING_ROOMS_DATA = "$BASE_PATH\\Meeting_Rooms_Json_Test"
    val GET_CAMERA_DATA = "$BASE_PATH\\Get_Camera_Json_Test"

    // Read data through file path
    fun readFile(path: String): String {
        return file2String(File(path))
    }

    //kotlin丰富的I/O API,我们可以通过file.readText（charset）直接获取结果
    private fun file2String(f: File, charset: String = "UTF-8"): String {
        return f.readText(Charset.forName(charset))
    }
}