package com.shaalevikas.app.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

data class Pledge(
    @DocumentId
    val id: String = "",
    val needId: String = "",
    val alumniId: String = "",
    val alumniName: String = "",
    val pledgeType: String = "Funds", // Item, Funds
    val amount: Double = 0.0,
    val itemDescription: String = "",
    val message: String = "",
    val timestamp: Timestamp = Timestamp.now()
)