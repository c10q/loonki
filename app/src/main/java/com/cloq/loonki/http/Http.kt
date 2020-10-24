package com.cloq.loonki.http

import com.github.kittinunf.fuel.httpPost

class Http() {
    fun randomMatchRequest(
        token: String,
        title: String,
        message: String,
        uid: String,
        type: Int
    ) {
        val bodyJson = """
                        |{
                        |"to" : "$token",
                        |"data" : {
                        |    "title" : "$title",
                        |    "body" : "$message",
                        |    "uid" : "$uid"
                        |    "type" : "$type"
                        |    }
                        |}
                        """.trimMargin()
        "https://fcm.googleapis.com/fcm/send".httpPost()
            .header(
                mapOf(
                    "Content-Type" to "application/json",
                    "Authorization" to "key=AAAAwLm5Wa0:APA91bFWU0DkxnahHjvLRIP0X4Rejh88RefA2w_u2pmYLvpKZDNU6cgAqDfRqcXX3IytsDkqRU0AI3jcTQyjXGyuBsL91j4TLv4WUh0gj7HgNeZmOT8_jYKM9EYwBeN-3bZnKWX20maF"
                )
            )
            .body(bodyJson)
            .response { request, response, result ->
            }
    }

    fun chatSendRequest(
        token: String,
        title: String,
        message: String,
        uid: String,
        type: String,
        room: String
    ) {
        val bodyJson = """
                        |{
                        |"to" : "$token",
                        |"data" : {
                        |    "title" : "$title",
                        |    "body" : "$message",
                        |    "uid" : "$uid"
                        |    "type" : "$type"
                        |    "room" : "$room"
                        |    }
                        |}
                        """.trimMargin()
        "https://fcm.googleapis.com/fcm/send".httpPost()
            .header(
                mapOf(
                    "Content-Type" to "application/json",
                    "Authorization" to "key=AAAAwLm5Wa0:APA91bFWU0DkxnahHjvLRIP0X4Rejh88RefA2w_u2pmYLvpKZDNU6cgAqDfRqcXX3IytsDkqRU0AI3jcTQyjXGyuBsL91j4TLv4WUh0gj7HgNeZmOT8_jYKM9EYwBeN-3bZnKWX20maF"
                )
            )
            .body(bodyJson)
            .response { request, response, result -> }
    }
}