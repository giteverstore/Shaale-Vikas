package com.shaalevikas.app.data.model

import com.google.firebase.firestore.DocumentId

data class User(
    @DocumentId
    val uid: String = "",
    val displayName: String = "",
    val email: String = "",
    val role: String = "alumni",
    val graduationYear: Int = 0,
    val district: String = "",
    val city: String = "",
    val totalPledgeValue: Double = 0.0,
    val badgeTier: String = "Bronze",
    val profileImageUrl: String = "",
    val emailVerified: Boolean = false
) {
    // Computed pledge points
    val pledgePoints: Int
        get() = com.shaalevikas.app.data.RankSystem
            .toPledgePoints(totalPledgeValue)

    // Computed rank
    val rank: com.shaalevikas.app.data.Rank
        get() = com.shaalevikas.app.data.RankSystem
            .getRankFromRupees(totalPledgeValue)

    companion object {
        fun computeBadge(totalValue: Double): String {
            return com.shaalevikas.app.data.RankSystem
                .getRankFromRupees(totalValue).displayName
        }
    }
}