package com.learning.image.detection.data

data class DetectedObject(val label: String, val score: Float) {
    val probabilityString = String.format("%.1f%%", score * 100.0f)

    override fun toString(): String {
        return "$label / $probabilityString"
    }
}