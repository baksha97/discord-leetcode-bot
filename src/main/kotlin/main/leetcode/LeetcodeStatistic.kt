package main.leetcode

import kotlinx.serialization.*

@Serializable
data class LeetcodeStatistic (
    val status: String,
    val message: String,
    val totalSolved: Long,
    val totalQuestions: Long,
    val easySolved: Long,
    val totalEasy: Long,
    val mediumSolved: Long,
    val totalMedium: Long,
    val hardSolved: Long,
    val totalHard: Long,
    val acceptanceRate: Double,
    val ranking: Long,
    val contributionPoints: Long,
    val reputation: Long,
    val submissionCalendar: Map<String, Long>
)