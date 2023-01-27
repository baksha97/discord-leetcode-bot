package main.leetcode

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.*
import main.Environment
import me.jakejmattson.discordkt.annotations.Service


@Service
class LeetcodeRemoteDatasource {
    suspend fun getStatistics(username: String): LeetcodeStatistic =
        Environment.client.fetchLeetcodeStatistics(username).toDomain(username)
}

private suspend fun HttpClient.fetchLeetcodeStatistics(username: String): LeetcodeStatisticResponse {
    val query = String.format(
        "{\"query\":\"query getUserProfile(\$username: String!) { allQuestionsCount { difficulty count } matchedUser(username: \$username) { contributions { points } profile { reputation ranking } submissionCalendar submitStats { acSubmissionNum { difficulty count submissions } totalSubmissionNum { difficulty count submissions } } } } \",\"variables\":{\"username\":\"%s\"}}",
        username
    )

    return get {
        url {
            protocol = URLProtocol.HTTPS
            host = "leetcode.com"
            method = HttpMethod.Post
            header("referer", "https://leetcode.com/$username/")
            header("Content-Type", "application/json")
            appendPathSegments("graphql")
            setBody(query)
        }
    }.body()
}

private fun LeetcodeStatisticResponse.toDomain(username: String): LeetcodeStatistic {
    fun findOrDefaultSolved(difficulty: String): Long =
        data.matchedUser.submitStats.acSubmissionNum.find { it.difficulty == difficulty }?.count ?: 0

    fun calculateAcceptanceRate(): Double {
        val actual = findOrDefaultSolved("All").takeIf { it != 0L } ?: return 0.0
        val total = data.matchedUser.submitStats.totalSubmissionNum.find { it.difficulty == "All" }?.count ?: return 0.0
        return (actual / total).toDouble()
    }

    return LeetcodeStatistic(
        username = username,
        totalSolved = findOrDefaultSolved("All"),
        easySolved = findOrDefaultSolved("Easy"),
        mediumSolved = findOrDefaultSolved("Medium"),
        hardSolved = findOrDefaultSolved("Hard"),
        acceptanceRate = calculateAcceptanceRate(),
        ranking = data.matchedUser.profile.ranking,
        contributionPoints = data.matchedUser.contributions.points
    )
}



@Serializable
private data class LeetcodeStatisticResponse (
    val data: Data
)

@Serializable
private data class Data (
    val allQuestionsCount: List<AllQuestionsCount>,
    val matchedUser: MatchedUser
)

@Serializable
private data class AllQuestionsCount (
    val difficulty: String,
    val count: Long
)

@Serializable
private data class MatchedUser (
    val contributions: Contributions,
    val profile: Profile,
    val submissionCalendar: String,
    val submitStats: SubmitStats
)

@Serializable
private data class Contributions (
    val points: Long
)

@Serializable
private data class Profile (
    val reputation: Long,
    val ranking: Long
)

@Serializable
private data class SubmitStats (
    val acSubmissionNum: List<SubmissionNum>,
    val totalSubmissionNum: List<SubmissionNum>
)

@Serializable
private data class SubmissionNum (
    val difficulty: String,
    val count: Long,
    val submissions: Long
)
