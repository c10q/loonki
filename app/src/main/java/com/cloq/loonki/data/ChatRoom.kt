package com.cloq.loonki.data

class ChatRoom(
    val type: Int,
    val users: ArrayList<String>,
    val valid: Boolean = false,
    val deleted: Boolean = false
) {
    companion object {
        const val ROOM_NORMAL = 0
        const val ROOM_WHITE = 1
        const val ROOM_BLACK = 2
    }
}