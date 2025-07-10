package com.receiptr.domain.model

data class User(
    val id: String = "",
    val email: String? = null,
    val phoneNumber: String? = null,
    val displayName: String? = null,
    val photoUrl: String? = null,
    val isEmailVerified: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val firstName: String? = null,
    val lastName: String? = null,
    val isActive: Boolean = true,
    val preferences: Map<String, Any> = emptyMap()
) {
    // Convert to map for Firestore
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "id" to id,
            "email" to email,
            "phoneNumber" to phoneNumber,
            "displayName" to displayName,
            "photoUrl" to photoUrl,
            "isEmailVerified" to isEmailVerified,
            "createdAt" to createdAt,
            "updatedAt" to updatedAt,
            "firstName" to firstName,
            "lastName" to lastName,
            "isActive" to isActive,
            "preferences" to preferences
        )
    }

    companion object {
        // Create User from Firestore document
        fun fromMap(map: Map<String, Any?>): User {
            return User(
                id = map["id"] as? String ?: "",
                email = map["email"] as? String,
                phoneNumber = map["phoneNumber"] as? String,
                displayName = map["displayName"] as? String,
                photoUrl = map["photoUrl"] as? String,
                isEmailVerified = map["isEmailVerified"] as? Boolean ?: false,
                createdAt = map["createdAt"] as? Long ?: System.currentTimeMillis(),
                updatedAt = map["updatedAt"] as? Long ?: System.currentTimeMillis(),
                firstName = map["firstName"] as? String,
                lastName = map["lastName"] as? String,
                isActive = map["isActive"] as? Boolean ?: true,
                preferences = when (val prefs = map["preferences"]) {
                    is Map<*, *> -> prefs.entries
                        .filter { it.key is String && it.value != null }
                        .associate { (it.key as String) to it.value!! }
                    else -> emptyMap()
                }
            )
        }
    }
}
