package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "scans")
data class ScanEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val url: String,
    val verdict: String, // LEGITIMATE, SUSPICIOUS, PHISHING
    val riskScore: Int,   // 0 - 100
    val confidence: Int,  // 0 - 100
    val reasons: String,  // pipe-separated or comma-separated reasons
    val featuresSummary: String, // summary of key extracted features
    val scannedAt: Long = System.currentTimeMillis()
)
