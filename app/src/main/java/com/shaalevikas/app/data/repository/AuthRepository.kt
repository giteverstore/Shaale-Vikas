package com.shaalevikas.app.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.shaalevikas.app.data.model.User
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {
    val currentUser: FirebaseUser? get() = auth.currentUser

    suspend fun loginAdmin(
        email: String,
        password: String
    ): Result<FirebaseUser> {
        return try {
            val result = auth
                .signInWithEmailAndPassword(email, password)
                .await()
            Result.success(result.user!!)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun registerAlumni(
        email: String,
        password: String,
        name: String,
        gradYear: Int,
        district: String,
        city: String
    ): Result<FirebaseUser> {
        return try {
            val result = auth
                .createUserWithEmailAndPassword(email, password)
                .await()
            val uid = result.user!!.uid
            result.user!!.sendEmailVerification().await()
            val userMap = hashMapOf(
                "uid"              to uid,
                "displayName"      to name,
                "email"            to email,
                "role"             to "alumni",
                "graduationYear"   to gradYear,
                "district"         to district,
                "city"             to city,
                "totalPledgeValue" to 0.0,
                "badgeTier"        to "Bronze",
                "profileImageUrl"  to "",
                "emailVerified"    to false
            )
            firestore.collection("users")
                .document(uid)
                .set(userMap)
                .await()
            Result.success(result.user!!)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun mapDocumentToUser(
        doc: DocumentSnapshot
    ): User? {
        return try {
            if (!doc.exists()) return null
            val uid = doc.id
            User(
                uid = doc.getString("uid") ?: uid,
                displayName = doc.getString("displayName") ?: "",
                email = doc.getString("email")
                    ?: auth.currentUser?.email ?: "",
                role = doc.getString("role") ?: "alumni",
                graduationYear = when (
                    val gy = doc.get("graduationYear")
                ) {
                    is Long   -> gy.toInt()
                    is Int    -> gy
                    is Double -> gy.toInt()
                    is String -> gy.toIntOrNull() ?: 0
                    else      -> 0
                },
                district = doc.getString("district") ?: "",
                city = doc.getString("city") ?: "",
                totalPledgeValue = when (
                    val tv = doc.get("totalPledgeValue")
                ) {
                    is Double -> tv
                    is Long   -> tv.toDouble()
                    is Float  -> tv.toDouble()
                    is String -> tv.toDoubleOrNull() ?: 0.0
                    else      -> 0.0
                },
                badgeTier = doc.getString("badgeTier") ?: "Bronze",
                profileImageUrl = doc.getString("profileImageUrl") ?: "",
                emailVerified = doc.getBoolean("emailVerified") ?: false
            )
        } catch (e: Exception) {
            null
        }
    }

    fun getUserDataFlow(): Flow<User?> = callbackFlow {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            trySend(null)
            close()
            return@callbackFlow
        }
        val listener = firestore
            .collection("users")
            .document(uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(null)
                    return@addSnapshotListener
                }
                trySend(snapshot?.let { mapDocumentToUser(it) })
            }
        awaitClose { listener.remove() }
    }

    suspend fun getCurrentUserData(): User? {
        val uid = auth.currentUser?.uid ?: return null
        return try {
            val doc = firestore
                .collection("users")
                .document(uid)
                .get()
                .await()
            mapDocumentToUser(doc)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getUserRole(): String {
        val uid = auth.currentUser?.uid ?: return "alumni"
        return try {
            val doc = firestore
                .collection("users")
                .document(uid)
                .get()
                .await()
            doc.getString("role")?.trim() ?: "alumni"
        } catch (e: Exception) {
            "alumni"
        }
    }

    suspend fun sendVerificationEmail(): Result<Unit> {
        return try {
            auth.currentUser?.sendEmailVerification()?.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun checkEmailVerified(): Boolean {
        auth.currentUser?.reload()?.await()
        return auth.currentUser?.isEmailVerified ?: false
    }

    fun signOut() {
        auth.signOut()
    }
}