package com.shaalevikas.app.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

data class Need(
    @DocumentId
    val id: String = "",
    val schoolId: String = "",
    val title: String = "",
    val category: String = "",
    val description: String = "",
    val costEstimate: Double = 0.0,
    val urgency: String = "Medium",
    val targetAmount: Double = 0.0,
    val pledgedAmount: Double = 0.0,
    val pledgeCount: Int = 0,
    val status: String = "active",
    val beforePhotoUrl: String = "",  // ← must exist
    val afterPhotoUrl: String = "",
    val createdAt: com.google.firebase.Timestamp =
        com.google.firebase.Timestamp.now(),
    val fulfilledAt: com.google.firebase.Timestamp? = null
) {
    val progressPercent: Int
        get() = if (targetAmount > 0)
            ((pledgedAmount / targetAmount) * 100)
                .toInt().coerceAtMost(100)
        else 0
}