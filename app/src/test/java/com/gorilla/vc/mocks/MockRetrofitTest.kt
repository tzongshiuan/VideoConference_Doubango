import com.gorilla.vc.api.ApiService
import com.gorilla.vc.mocks.MockRetrofit
import com.gorilla.vc.model.MockApiServiceAsset
import io.reactivex.Observable
import org.junit.Test

class MockRetrofitTest {

    private val testId = "11751"

    /**
     * Make sure our fake participants data are get successfully
     */
    @Test
    fun participantsAssetTest() {
        val participants = MockApiServiceAsset.readFile(MockApiServiceAsset.PARTICIPANTS_DATA).replace("\r", "")
        val expectData =
                "[\n" +
                        "  {\n" +
                        "    \"id\": 11751,\n" +
                        "    \"name\": \"hsuan\",\n" +
                        "    \"password\": \"default\",\n" +
                        "    \"emailAddress\": \"tsunghsuanlai@gorilla-technology.com\",\n" +
                        "    \"selfieImageLocation\": \"defaultImg\",\n" +
                        "    \"isValid\": 1,\n" +
                        "    \"createDate\": \"2018-12-14T16:41:30.492+08:00\",\n" +
                        "    \"memberId\": \"A11041300F-9131436f-aca0-4ce3-a16b-d48d5d368a3e\",\n" +
                        "    \"meetingRoomMapParticipants\": [\n" +
                        "      {\n" +
                        "        \"id\": 16301,\n" +
                        "        \"isOnline\": 0\n" +
                        "      },\n" +
                        "      {\n" +
                        "        \"id\": 17301,\n" +
                        "        \"isOnline\": 0\n" +
                        "      }\n" +
                        "    ],\n" +
                        "    \"roleId\": 10002,\n" +
                        "    \"rank\": null,\n" +
                        "    \"duties\": null,\n" +
                        "    \"unit\": null\n" +
                        "  },\n" +
                        "  {\n" +
                        "    \"id\": 11902,\n" +
                        "    \"name\": \"hsuan2\",\n" +
                        "    \"password\": \"default\",\n" +
                        "    \"emailAddress\": \"tsunghsuanlai@gorilla-technology.com\",\n" +
                        "    \"selfieImageLocation\": \"defaultImg\",\n" +
                        "    \"isValid\": 1,\n" +
                        "    \"createDate\": \"2018-12-04T18:38:31.886+08:00\",\n" +
                        "    \"memberId\": \"A11041300F-0a6c6844-7015-4888-97f1-9b00395dc699\",\n" +
                        "    \"meetingRoomMapParticipants\": [\n" +
                        "      {\n" +
                        "        \"id\": 16302,\n" +
                        "        \"isOnline\": 0\n" +
                        "      },\n" +
                        "      {\n" +
                        "        \"id\": 17302,\n" +
                        "        \"isOnline\": 0\n" +
                        "      }\n" +
                        "    ],\n" +
                        "    \"roleId\": 10002,\n" +
                        "    \"rank\": null,\n" +
                        "    \"duties\": null,\n" +
                        "    \"unit\": null\n" +
                        "  }\n" +
                        "]"

        Observable.just(participants)
                .test()
                .assertValue(expectData)
    }

    @Test
    fun participantIdAssetTest() {
        val participant = MockApiServiceAsset.readFile(MockApiServiceAsset.PARTICIPANT_ID_DATA).replace("\r", "")
        val expectData =
                "{\n" +
                "  \"id\": 11751,\n" +
                "  \"name\": \"hsuan\",\n" +
                "  \"password\": \"default\",\n" +
                "  \"emailAddress\": \"tsunghsuanlai@gorilla-technology.com\",\n" +
                "  \"selfieImageLocation\": \"defaultImg\",\n" +
                "  \"isValid\": 1,\n" +
                "  \"createDate\": \"2018-12-14T16:41:30.492+08:00\",\n" +
                "  \"memberId\": \"A11041300F-9131436f-aca0-4ce3-a16b-d48d5d368a3e\",\n" +
                "  \"meetingRoomMapParticipants\": [\n" +
                "    {\n" +
                "      \"id\": 16301,\n" +
                "      \"isOnline\": 0\n" +
                "    },\n" +
                "    {\n" +
                "      \"id\": 17301,\n" +
                "      \"isOnline\": 0\n" +
                "    }\n" +
                "  ],\n" +
                "  \"roleId\": 10002,\n" +
                "  \"rank\": null,\n" +
                "  \"duties\": null,\n" +
                "  \"unit\": null\n" +
                "}"

        Observable.just(participant)
                .test()
                .assertValue(expectData)
    }

    @Test
    fun meetingRoomsAssetTest() {
        val meetingRooms = MockApiServiceAsset.readFile(MockApiServiceAsset.MEETING_ROOMS_DATA).replace("\r", "")
        val expectData =
                "[\n" +
                "  {\n" +
                "    \"id\": 18001,\n" +
                "    \"name\": \"VC_ANDROID_20181123\",\n" +
                "    \"password\": \"default\",\n" +
                "    \"startDate\": \"2018-11-23T11:00:00+08:00\",\n" +
                "    \"endDate\": \"2018-11-23T23:00:00+08:00\",\n" +
                "    \"hostParticipantId\": 11751,\n" +
                "    \"creatorParticipantId\": 11751,\n" +
                "    \"recordDefault\": 0,\n" +
                "    \"hasRecord\": 0,\n" +
                "    \"agenda\": \"VC_ANDROID_20181123\",\n" +
                "    \"status\": 1,\n" +
                "    \"createDate\": \"2018-11-23T10:49:27.337+08:00\",\n" +
                "    \"sip\": {\n" +
                "      \"id\": 1001,\n" +
                "      \"proxyIp\": \"192.168.11.26\",\n" +
                "      \"proxyPort\": \"20080\",\n" +
                "      \"videoCodecs\": \"H.263\",\n" +
                "      \"audioCodecs\": \"mp3\"\n" +
                "    },\n" +
                "    \"meetingRoomMapParticipants\": [\n" +
                "      {\n" +
                "        \"id\": 16301,\n" +
                "        \"isOnline\": 0\n" +
                "      },\n" +
                "      {\n" +
                "        \"id\": 16302,\n" +
                "        \"isOnline\": 1\n" +
                "      },\n" +
                "      {\n" +
                "        \"id\": 16303,\n" +
                "        \"isOnline\": 0\n" +
                "      }\n" +
                "    ]\n" +
                "  },\n" +
                "  {\n" +
                "    \"id\": 18002,\n" +
                "    \"name\": \"VC_ANDROID_20181124\",\n" +
                "    \"password\": \"default\",\n" +
                "    \"startDate\": \"2018-11-24T11:00:00+08:00\",\n" +
                "    \"endDate\": \"2018-11-24T23:00:00+08:00\",\n" +
                "    \"hostParticipantId\": 11751,\n" +
                "    \"creatorParticipantId\": 11751,\n" +
                "    \"recordDefault\": 0,\n" +
                "    \"hasRecord\": 0,\n" +
                "    \"agenda\": \"VC_ANDROID_20181124\",\n" +
                "    \"status\": 1,\n" +
                "    \"createDate\": \"2018-11-24T10:49:27.337+08:00\",\n" +
                "    \"sip\": {\n" +
                "      \"id\": 1001,\n" +
                "      \"proxyIp\": \"192.168.11.26\",\n" +
                "      \"proxyPort\": \"20080\",\n" +
                "      \"videoCodecs\": \"H.263\",\n" +
                "      \"audioCodecs\": \"mp3\"\n" +
                "    },\n" +
                "    \"meetingRoomMapParticipants\": [\n" +
                "      {\n" +
                "        \"id\": 17301,\n" +
                "        \"isOnline\": 0\n" +
                "      },\n" +
                "      {\n" +
                "        \"id\": 17302,\n" +
                "        \"isOnline\": 1\n" +
                "      },\n" +
                "      {\n" +
                "        \"id\": 17303,\n" +
                "        \"isOnline\": 0\n" +
                "      }\n" +
                "    ]\n" +
                "  }\n" +
                "]"

        Observable.just(meetingRooms)
                .test()
                .assertValue(expectData)
    }

    @Test
    fun getCameraAssetTest() {
        val cameras = MockApiServiceAsset.readFile(MockApiServiceAsset.GET_CAMERA_DATA).replace("\r", "")
        val expectData =
                "[\n" +
                "  {\n" +
                "    \"channel\": 1,\n" +
                "    \"name\": \"test01\",\n" +
                "    \"description\": \"description01\",\n" +
                "    \"location\": \"location01\",\n" +
                "    \"url\": \"rtsp://192.168.11.187:8554/live/ch1?stream=0\",\n" +
                "    \"cameraIp\": \"10.10.10.10\",\n" +
                "    \"onlineTime\": \"00:00-00:00\",\n" +
                "    \"repairStatus\": false\n" +
                "  },\n" +
                "  {\n" +
                "    \"channel\": 2,\n" +
                "    \"name\": \"test02\",\n" +
                "    \"description\": \"description02\",\n" +
                "    \"location\": \"location02\",\n" +
                "    \"url\": \"rtsp://192.168.11.187:8554/live/ch2?stream=0\",\n" +
                "    \"cameraIp\": \"10.10.10.10\",\n" +
                "    \"onlineTime\": \"00:00-00:00\",\n" +
                "    \"repairStatus\": false\n" +
                "  }\n" +
                "]"

                Observable.just(cameras)
                        .test()
                        .assertValue(expectData)
    }

    /**
     * To make sure that Retrofit could intercept API request, and return local mock data
     */
    @Test
    fun mockRetrofitTest() {
        val retrofit = MockRetrofit()
        val service = retrofit.create(ApiService::class.java)

        /**
         * Corresponding to [participantsAssetTest]
         */
        retrofit.path = MockApiServiceAsset.PARTICIPANTS_DATA
        service.getParticipants()
                .test()
                .assertValue { it ->
                    val participants = it.body()

                    participants?.size == 2
                            && participants[0].name.equals("hsuan")
                            && participants[1].name.equals("hsuan2")
                }

        /**
         * Corresponding to [participantIdAssetTest]
         */
        retrofit.path = MockApiServiceAsset.PARTICIPANT_ID_DATA
        service.getParticipant(testId)
                .test()
                .assertValue { it ->
                    val participant = it.body()

                    participant?.id.equals(testId)
                }

        /**
         * Corresponding to [meetingRoomsAssetTest]
         */
        retrofit.path = MockApiServiceAsset.MEETING_ROOMS_DATA
        service.getSessionList()
                .test()
                .assertValue { it ->
                    val sessions = it.body()

                    sessions?.size == 2
                            && sessions[0].id.equals("18001")
                            && sessions[0].hostId.equals(testId)
                            && sessions[1].id.equals("18002")
                            && sessions[1].hostId.equals(testId)
                }

        /**
         * Corresponding to [getCameraAssetTest]
         */
        retrofit.path = MockApiServiceAsset.GET_CAMERA_DATA
        service.getRtspList(testId)
                .test()
                .assertValue { it ->
                    val rtspList = it.body()

                    rtspList?.size == 2
                }
    }
}