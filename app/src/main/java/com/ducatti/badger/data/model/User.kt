package com.ducatti.badger.data.model

import com.ducatti.badger.utils.clearLowercase
import kotlinx.serialization.Serializable

@Serializable
data class User @JvmOverloads constructor(
    override var id: String = "",
    val name: String = "",
    val nameLowercase: String = name.clearLowercase(),
    val guests: Int = 0,
    val status: UserStatus = UserStatus.WAITING
) : IdModel

@Serializable
@JvmInline
value class UserStatus(val value: String) {
    companion object {
        val WAITING = UserStatus("waiting")
        val PRESENT = UserStatus("present")
    }
}
