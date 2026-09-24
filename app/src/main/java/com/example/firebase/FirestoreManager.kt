package com.example.firebase

import android.content.Context
import android.util.Log
import com.example.data.model.ScanEntity
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirestoreManager(private val context: Context) {

    private var firestore: FirebaseFirestore? = null

    init {
        try {
            if (FirebaseApp.getApps(context).isNotEmpty()) {
                firestore = FirebaseFirestore.getInstance()
            } else {
                Log.w("FirestoreManager", "FirebaseApp not initialized. Firestore offline mode.")
            }
        } catch (e: Exception) {
            Log.e("FirestoreManager", "Firestore init error: ${e.message}")
        }
    }

    suspend fun saveScan(userId: String, scan: ScanEntity): Boolean {
        val db = firestore ?: return false
        return try {
            val scanMap = hashMapOf(
                "url" to scan.url,
                "verdict" to scan.verdict,
                "riskScore" to scan.riskScore,
                "confidence" to scan.confidence,
                "reasons" to scan.reasons,
                "featuresSummary" to scan.featuresSummary,
                "scannedAt" to scan.scannedAt,
                "userId" to userId
            )
            // Save to user's private scans sub-collection
            db.collection("users")
                .document(userId)
                .collection("scans")
                .document(scan.id.toString())
                .set(scanMap)
                .await()

            // Also mirror to global telemetry for Admin intelligence dashboard
            db.collection("global_telemetry")
                .document("${userId}_${scan.id}")
                .set(scanMap)
                .await()

            true
        } catch (e: Exception) {
            Log.e("FirestoreManager", "Error saving scan to Firestore: ${e.message}")
            false
        }
    }

    fun observeUserScans(userId: String): Flow<List<ScanEntity>> = callbackFlow {
        val db = firestore
        if (db == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val listener = db.collection("users")
            .document(userId)
            .collection("scans")
            .orderBy("scannedAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("FirestoreManager", "Firestore listener error: ${error.message}")
                    return@addSnapshotListener
                }

                val list = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        val id = doc.id.toLongOrNull() ?: 0L
                        val url = doc.getString("url") ?: return@mapNotNull null
                        val verdict = doc.getString("verdict") ?: "UNKNOWN"
                        val riskScore = doc.getLong("riskScore")?.toInt() ?: 0
                        val confidence = doc.getLong("confidence")?.toInt() ?: 0
                        val reasons = doc.getString("reasons") ?: ""
                        val featuresSummary = doc.getString("featuresSummary") ?: ""
                        val scannedAt = doc.getLong("scannedAt") ?: System.currentTimeMillis()

                        ScanEntity(
                            id = id,
                            url = url,
                            verdict = verdict,
                            riskScore = riskScore,
                            confidence = confidence,
                            reasons = reasons,
                            featuresSummary = featuresSummary,
                            scannedAt = scannedAt
                        )
                    } catch (e: Exception) {
                        null
                    }
                } ?: emptyList()

                trySend(list)
            }

        awaitClose { listener.remove() }
    }
}
