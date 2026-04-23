package com.tsapps.fitnessbodyrecomposition.data.model

import java.util.Date

data class User(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val age: String = "",
    val height: String = "",
    val weight: String = "",
    val targetWeight: String = "",
    val onboardingCompleted: Boolean = true,
    val fcmToken: String = "",
    val createdAt: Long = Date().time
)
