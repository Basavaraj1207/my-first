package com.example.data.repository

import com.example.data.dao.ScanDao
import com.example.data.model.ScanEntity
import com.example.firebase.FirestoreManager
import com.example.ml.DetectionResult
import com.example.ml.UrlFeatureExtractor
import kotlinx.coroutines.flow.Flow

class ScanRepository(
    private val scanDao: ScanDao,
    private val firestoreManager: FirestoreManager? = null
) {

    val allScans: Flow<List<ScanEntity>> = scanDao.getAllScans()

    suspend fun performScan(url: String, currentUserId: String? = null): DetectionResult {
        val result = UrlFeatureExtractor.analyze(url)
        val reasonsJoined = result.reasons.joinToString(";;")
        val featuresJoined = "Len:${result.features.urlLength}|Dots:${result.features.dotCount}|Https:${result.features.usesHttps}|Subdomains:${result.features.subdomainCount}|IP:${result.features.hasIpAddress}"

        val entity = ScanEntity(
            url = url,
            verdict = result.verdict,
            riskScore = result.riskScore,
            confidence = result.confidence,
            reasons = reasonsJoined,
            featuresSummary = featuresJoined,
            scannedAt = System.currentTimeMillis()
        )
        val insertedId = scanDao.insertScan(entity)
        val entityWithId = entity.copy(id = insertedId)

        // Sync with Firestore if user is logged in
        if (currentUserId != null && firestoreManager != null) {
            firestoreManager.saveScan(currentUserId, entityWithId)
        }

        return result
    }

    suspend fun deleteScan(id: Long) {
        scanDao.deleteScan(id)
    }

    suspend fun clearHistory() {
        scanDao.clearAll()
    }
}
