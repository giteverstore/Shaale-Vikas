package com.shaalevikas.app.data

object RankSystem {

    // 1 PP = Rs. 100 (updated)
    const val RUPEES_PER_PP = 100.0

    // Convert rupees to pledge points
    fun toPledgePoints(rupees: Double): Int {
        return (rupees / RUPEES_PER_PP).toInt()
    }

    // Get rank from pledge points
    fun getRank(pledgePoints: Int): Rank {
        return when {
            pledgePoints < 400   -> Rank.UNRANKED
            pledgePoints < 500   -> Rank.BRONZE_III
            pledgePoints < 600   -> Rank.BRONZE_II
            pledgePoints < 700   -> Rank.BRONZE_I
            pledgePoints < 800   -> Rank.SILVER_III
            pledgePoints < 900   -> Rank.SILVER_II
            pledgePoints < 1000  -> Rank.SILVER_I
            pledgePoints < 1200  -> Rank.GOLD_IV
            pledgePoints < 1400  -> Rank.GOLD_III
            pledgePoints < 1600  -> Rank.GOLD_II
            pledgePoints < 1800  -> Rank.GOLD_I
            pledgePoints < 2100  -> Rank.PLATINUM_IV
            pledgePoints < 2400  -> Rank.PLATINUM_III
            pledgePoints < 2700  -> Rank.PLATINUM_II
            pledgePoints < 3000  -> Rank.PLATINUM_I
            pledgePoints < 3500  -> Rank.DIAMOND_V
            pledgePoints < 3800  -> Rank.DIAMOND_IV
            pledgePoints < 4200  -> Rank.DIAMOND_III
            pledgePoints < 4600  -> Rank.DIAMOND_II
            pledgePoints < 5000  -> Rank.DIAMOND_I
            else                 -> Rank.RUBY
        }
    }

    // Get rank from rupees directly
    fun getRankFromRupees(rupees: Double): Rank {
        return getRank(toPledgePoints(rupees))
    }

    // Get next rank info
    fun getNextRank(pledgePoints: Int): NextRankInfo? {
        val currentRank = getRank(pledgePoints)
        val nextRank = getNextRankEnum(currentRank) ?: return null
        val ppNeeded = getMinPPForRank(nextRank) - pledgePoints
        return NextRankInfo(nextRank, ppNeeded)
    }

    private fun getNextRankEnum(rank: Rank): Rank? {
        val values = Rank.values()
        val index = values.indexOf(rank)
        return if (index < values.size - 1) values[index + 1] else null
    }

    fun getMinPPForRank(rank: Rank): Int {
        return when (rank) {
            Rank.UNRANKED     -> 0
            Rank.BRONZE_III   -> 400
            Rank.BRONZE_II    -> 500
            Rank.BRONZE_I     -> 600
            Rank.SILVER_III   -> 700
            Rank.SILVER_II    -> 800
            Rank.SILVER_I     -> 900
            Rank.GOLD_IV      -> 1000
            Rank.GOLD_III     -> 1200
            Rank.GOLD_II      -> 1400
            Rank.GOLD_I       -> 1600
            Rank.PLATINUM_IV  -> 1800
            Rank.PLATINUM_III -> 2100
            Rank.PLATINUM_II  -> 2400
            Rank.PLATINUM_I   -> 2700
            Rank.DIAMOND_V    -> 3000
            Rank.DIAMOND_IV   -> 3500
            Rank.DIAMOND_III  -> 3800
            Rank.DIAMOND_II   -> 4200
            Rank.DIAMOND_I    -> 4600
            Rank.RUBY         -> 5000
        }
    }
}

data class NextRankInfo(
    val rank: Rank,
    val ppNeeded: Int
)

enum class Rank(
    val displayName: String,
    val emoji: String,
    val colorHex: String,
    val tier: String
) {
    UNRANKED(
        displayName = "Unranked",
        emoji       = "⬜",
        colorHex    = "#9E9E9E",
        tier        = "unranked"
    ),
    BRONZE_III(
        displayName = "Bronze III",
        emoji       = "🥉",
        colorHex    = "#CD7F32",
        tier        = "bronze"
    ),
    BRONZE_II(
        displayName = "Bronze II",
        emoji       = "🥉",
        colorHex    = "#CD7F32",
        tier        = "bronze"
    ),
    BRONZE_I(
        displayName = "Bronze I",
        emoji       = "🥉",
        colorHex    = "#CD7F32",
        tier        = "bronze"
    ),
    SILVER_III(
        displayName = "Silver III",
        emoji       = "🥈",
        colorHex    = "#9E9E9E",
        tier        = "silver"
    ),
    SILVER_II(
        displayName = "Silver II",
        emoji       = "🥈",
        colorHex    = "#9E9E9E",
        tier        = "silver"
    ),
    SILVER_I(
        displayName = "Silver I",
        emoji       = "🥈",
        colorHex    = "#9E9E9E",
        tier        = "silver"
    ),
    GOLD_IV(
        displayName = "Gold IV",
        emoji       = "🥇",
        colorHex    = "#FFD700",
        tier        = "gold"
    ),
    GOLD_III(
        displayName = "Gold III",
        emoji       = "🥇",
        colorHex    = "#FFD700",
        tier        = "gold"
    ),
    GOLD_II(
        displayName = "Gold II",
        emoji       = "🥇",
        colorHex    = "#FFD700",
        tier        = "gold"
    ),
    GOLD_I(
        displayName = "Gold I",
        emoji       = "🥇",
        colorHex    = "#FFD700",
        tier        = "gold"
    ),
    PLATINUM_IV(
        displayName = "Platinum IV",
        emoji       = "💠",
        colorHex    = "#00BCD4",
        tier        = "platinum"
    ),
    PLATINUM_III(
        displayName = "Platinum III",
        emoji       = "💠",
        colorHex    = "#00BCD4",
        tier        = "platinum"
    ),
    PLATINUM_II(
        displayName = "Platinum II",
        emoji       = "💠",
        colorHex    = "#00BCD4",
        tier        = "platinum"
    ),
    PLATINUM_I(
        displayName = "Platinum I",
        emoji       = "💠",
        colorHex    = "#00BCD4",
        tier        = "platinum"
    ),
    DIAMOND_V(
        displayName = "Diamond V",
        emoji       = "💎",
        colorHex    = "#64B5F6",
        tier        = "diamond"
    ),
    DIAMOND_IV(
        displayName = "Diamond IV",
        emoji       = "💎",
        colorHex    = "#64B5F6",
        tier        = "diamond"
    ),
    DIAMOND_III(
        displayName = "Diamond III",
        emoji       = "💎",
        colorHex    = "#64B5F6",
        tier        = "diamond"
    ),
    DIAMOND_II(
        displayName = "Diamond II",
        emoji       = "💎",
        colorHex    = "#64B5F6",
        tier        = "diamond"
    ),
    DIAMOND_I(
        displayName = "Diamond I",
        emoji       = "💎",
        colorHex    = "#64B5F6",
        tier        = "diamond"
    ),
    RUBY(
        displayName = "Ruby",
        emoji       = "❤️‍🔥",
        colorHex    = "#E53935",
        tier        = "ruby"
    )
}