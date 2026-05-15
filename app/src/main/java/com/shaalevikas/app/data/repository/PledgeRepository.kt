package com.shaalevikas.app.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.shaalevikas.app.data.model.Pledge
import com.shaalevikas.app.data.model.User
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PledgeRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private val pledgesCollection = firestore.collection("pledges")
    private val usersCollection = firestore.collection("users")

    suspend fun makePledge(pledge: Pledge): Result<Unit> {
        return try {
            pledgesCollection.add(pledge).await()

            // Update user total pledge value and badge
            val userRef = usersCollection.document(pledge.alumniId)
            firestore.runTransaction { transaction ->
                val snap = transaction.get(userRef)
                val current = when (val tv = snap.get("totalPledgeValue")) {
                    is Double -> tv
                    is Long   -> tv.toDouble()
                    is String -> tv.toDoubleOrNull() ?: 0.0
                    else      -> 0.0
                }
                val newTotal = current + pledge.amount
                transaction.update(
                    userRef, mapOf(
                        "totalPledgeValue" to newTotal,
                        "badgeTier"        to User.computeBadge(newTotal)
                    )
                )
            }.await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    fun getPledgesForNeed(needId: String): Flow<List<Pledge>> = callbackFlow {
        val listener = pledgesCollection
            .whereEqualTo("needId", needId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                try {
                    val pledges = snapshot?.documents?.mapNotNull { doc ->
                        try {
                            Pledge(
                                id = doc.id,
                                needId = doc.getString("needId") ?: "",
                                alumniId = doc.getString("alumniId") ?: "",
                                alumniName = doc.getString("alumniName") ?: "",
                                pledgeType = doc.getString("pledgeType") ?: "Funds",
                                amount = when (val a = doc.get("amount")) {
                                    is Double -> a
                                    is Long -> a.toDouble()
                                    is String -> a.toDoubleOrNull() ?: 0.0
                                    else -> 0.0
                                },
                                itemDescription = doc.getString("itemDescription") ?: "",
                                message = doc.getString("message") ?: "",
                                timestamp = doc.getTimestamp("timestamp")
                                    ?: com.google.firebase.Timestamp.now()
                            )
                        } catch (e: Exception) {
                            null
                        }
                    } ?: emptyList()
                    trySend(pledges)
                } catch (e: Exception) {
                    trySend(emptyList())
                }
            }
        awaitClose { listener.remove() }
    }

    fun getMyPledges(alumniId: String): Flow<List<Pledge>> = callbackFlow {
        val listener = pledgesCollection
            .whereEqualTo("alumniId", alumniId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                try {
                    val pledges = snapshot?.documents?.mapNotNull { doc ->
                        try {
                            Pledge(
                                id = doc.id,
                                needId = doc.getString("needId") ?: "",
                                alumniId = doc.getString("alumniId") ?: "",
                                alumniName = doc.getString("alumniName") ?: "",
                                pledgeType = doc.getString("pledgeType") ?: "Funds",
                                amount = when (val a = doc.get("amount")) {
                                    is Double -> a
                                    is Long -> a.toDouble()
                                    is String -> a.toDoubleOrNull() ?: 0.0
                                    else -> 0.0
                                },
                                itemDescription = doc.getString("itemDescription") ?: "",
                                message = doc.getString("message") ?: "",
                                timestamp = doc.getTimestamp("timestamp")
                                    ?: com.google.firebase.Timestamp.now()
                            )
                        } catch (e: Exception) {
                            null
                        }
                    } ?: emptyList()

                    // Sort by timestamp descending
                    val sorted = pledges.sortedByDescending {
                        it.timestamp.seconds
                    }
                    trySend(sorted)
                } catch (e: Exception) {
                    trySend(emptyList())
                }
            }
        awaitClose { listener.remove() }
    }

    fun getTopPledgers(): Flow<List<User>> = callbackFlow {
        val listener = usersCollection
            .whereEqualTo("role", "alumni")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    android.util.Log.e(
                        "PledgeRepository",
                        "Error: ${error.message}"
                    )
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                try {
                    val users = snapshot?.documents?.mapNotNull { doc ->
                        try {
                            User(
                                uid = doc.getString("uid") ?: doc.id,
                                displayName = doc.getString("displayName") ?: "",
                                email = doc.getString("email") ?: "",
                                role = doc.getString("role") ?: "alumni",
                                // Safe graduationYear conversion
                                graduationYear = when (
                                    val gy = doc.get("graduationYear")
                                ) {
                                    is Long -> gy.toInt()
                                    is Int -> gy
                                    is Double -> gy.toInt()
                                    is String -> gy.toIntOrNull() ?: 0
                                    else -> 0
                                },
                                city = doc.getString("city") ?: "",
                                // Safe totalPledgeValue conversion
                                totalPledgeValue = when (
                                    val tv = doc.get("totalPledgeValue")
                                ) {
                                    is Double -> tv
                                    is Long -> tv.toDouble()
                                    is Float -> tv.toDouble()
                                    is String -> tv.toDoubleOrNull() ?: 0.0
                                    else -> 0.0
                                },
                                badgeTier = doc.getString("badgeTier")
                                    ?: "Bronze"
                            )
                        } catch (e: Exception) {
                            android.util.Log.e(
                                "PledgeRepository",
                                "Skip user ${doc.id}: ${e.message}"
                            )
                            null
                        }
                    } ?: emptyList()

                    // Sort in memory
                    val sorted = users.sortedByDescending {
                        it.totalPledgeValue
                    }
                    trySend(sorted)

                } catch (e: Exception) {
                    android.util.Log.e(
                        "PledgeRepository",
                        "Fatal error: ${e.message}"
                    )
                    trySend(emptyList())
                }
            }
        awaitClose { listener.remove() }
    }

    fun getAllPledgeCounts(): Flow<Map<String, Int>> = callbackFlow {
        val listener = pledgesCollection
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyMap())
                    return@addSnapshotListener
                }
                val counts = mutableMapOf<String, Int>()
                snapshot?.documents?.forEach { doc ->
                    val alumniId = doc.getString("alumniId") ?: return@forEach
                    counts[alumniId] = (counts[alumniId] ?: 0) + 1
                }
                trySend(counts)
            }
        awaitClose { listener.remove() }
    }
}