package com.cloq.loonki.data

class UserRoom(
    val type: Int,
    val time: Long,
    val users: ArrayList<String>,
    val alert: Boolean = true,
    val valid: Boolean = false,
    val deleted: Boolean = false
) {

}