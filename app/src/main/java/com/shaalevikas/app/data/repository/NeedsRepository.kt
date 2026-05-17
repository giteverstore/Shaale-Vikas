package com.shaalevikas.app.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import com.shaalevikas.app.data.model.Need
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.io.InputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NeedsRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage
) {
    private val needsCollection = firestore.collection("needs")

    // Simplified - only one orderBy to avoid index requirement
    fun getActiveNeeds(): Flow<List<Need>> = callbackFlow {
        val listener = needsCollection
            .whereEqualTo("status", "active")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val needs = snapshot?.toObjects(Need::class.java) ?: emptyList()
                trySend(needs)
            }
        awaitClose { listener.remove() }
    }

    // Simplified - only one orderBy
    fun getFulfilledNeeds(): Flow<List<Need>> = callbackFlow {
        val listener = needsCollection
            .whereEqualTo("status", "fulfilled")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val needs = snapshot?.toObjects(Need::class.java) ?: emptyList()
                trySend(needs)
            }
        awaitClose { listener.remove() }
    }

    fun getNeedById(needId: String): Flow<Need?> = callbackFlow {
        val listener = needsCollection.document(needId)
            .addSnapshotListener { snapshot, error ->

                if (error != null) {
                    trySend(null)
                    return@addSnapshotListener
                }

                val need = snapshot?.toObject(Need::class.java)?.copy(
                    id = snapshot.id
                )

                trySend(need)
            }

        awaitClose { listener.remove() }
    }

    suspend fun createNeed(need: Need): Result<String> {
        return try {
            val needMap = hashMapOf(
                "schoolId"       to need.schoolId,
                "title"          to need.title,
                "category"       to need.category,
                "description"    to need.description,
                "costEstimate"   to need.costEstimate,
                "urgency"        to need.urgency,
                "targetAmount"   to need.targetAmount,
                "pledgedAmount"  to 0.0,
                "pledgeCount"    to 0,
                "status"         to "active",
                "beforePhotoUrl" to need.beforePhotoUrl,
                "createdAt"      to com.google.firebase.Timestamp.now(),
                "fulfilledAt"    to null
            )
            val ref = needsCollection.add(needMap).await()
            Result.success(ref.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateNeed(need: Need): Result<Unit> {
        return try {
            needsCollection.document(need.id).set(need).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteNeed(needId: String): Result<Unit> {
        return try {
            needsCollection.document(needId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun markFulfilled(
        needId: String,
        afterPhotoUrl: String
    ): Result<Unit> {
        return try {
            needsCollection.document(needId).update(
                mapOf(
                    "status" to "fulfilled",
                    "afterPhotoUrl" to afterPhotoUrl,
                    "fulfilledAt" to com.google.firebase.Timestamp.now()
                )
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun uploadPhoto(
        stream: InputStream,
        path: String
    ): Result<String> {
        return try {
            val ref = storage.reference.child(path)
            ref.putStream(stream).await()
            val url = ref.downloadUrl.await().toString()
            Result.success(url)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updatePledgedAmount(
        needId: String,
        delta: Double
    ): Result<Unit> {
        return try {
            firestore.runTransaction { transaction ->
                val ref = needsCollection.document(needId)
                val snapshot = transaction.get(ref)
                val current = snapshot.getDouble("pledgedAmount") ?: 0.0
                val count = (snapshot.getLong("pledgeCount") ?: 0L) + 1
                transaction.update(
                    ref, mapOf(
                        "pledgedAmount" to current + delta,
                        "pledgeCount" to count
                    )
                )
            }.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}