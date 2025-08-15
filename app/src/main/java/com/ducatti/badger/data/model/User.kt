package com.ducatti.badger.data.model

import kotlinx.serialization.Serializable

@Serializable
data class User @JvmOverloads constructor(
    override var id: String = "",
    val name: String = "",
    val nameLowercase: String = "",
    val guests: Int = 0,
    val status: UserStatus = UserStatus.WAITING
) : IdModel {
    var guestString: String = ""
}

@Serializable
enum class UserStatus {
    WAITING,
    PRESENT
}
