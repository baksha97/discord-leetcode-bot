package main.leetcode


import dev.kord.common.entity.Snowflake
import io.github.crackthecodeabhi.kreds.connection.Endpoint
import io.github.crackthecodeabhi.kreds.connection.newClient
import io.ktor.client.HttpClient
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import main.Environment
import me.jakejmattson.discordkt.annotations.Service


interface LeetcodeRegistry {
    suspend fun register(id: String, username: String)
    suspend fun contains(id: String): Boolean
    suspend fun get(id: String): String?
    suspend fun remove(id: String)

    suspend fun allUsers(): List<Pair<String, String>>

    suspend fun <T> get(id: T) = get("$id")
    suspend fun get(id: Snowflake) = get("${id.value}")
}

@Service
class RedisLeetcodeRegistry : LeetcodeRegistry {
    private val redis = newClient(Endpoint.from(Environment.redisUri))

    override suspend fun register(id: String, username: String): Unit =
        redis.use { client ->
            client.set(id, username)
        }


    override suspend fun contains(id: String): Boolean =
        redis.use { it.get(id) != null }

    override suspend fun get(id: String): String? =
        redis.use { it.get(id) }

    override suspend fun remove(id: String): Unit =
        redis.use { it.expire(id, 0u) }

    override suspend fun allUsers(): List<Pair<String, String>> =
        redis.use { client ->
            client
                .keys("*")
                .map {
                    Pair(it, client.get(it)!!)
                }
        }
}


@Service
class LeetcodeService(private val registry: LeetcodeRegistry) {

    private val client = HttpClient(CIO) {
        install(Logging) {
            logger = object: Logger {
                override fun log(message: String) {
                    println(message)
                }
            }
            level = LogLevel.HEADERS
        }
        install(ContentNegotiation) {
            json(
                Json {
                    prettyPrint = true
                    isLenient = true
                }
            )
        }
    }

    suspend fun allStatistics(): List<Pair<String, LeetcodeStatistic>> =
        registry.allUsers().map {
            Pair(it.first, getStatistics(it.second))
        }

    suspend fun getStatistics(username: String): LeetcodeStatistic =
        client.get {
            url {
                protocol = URLProtocol.HTTPS
                host = "leetcode-stats-api.herokuapp.com"
                appendPathSegments(username)
            }
        }.body()


    suspend fun register(id: String, username: String) = registry.register(id, username)
    suspend fun get(id: String): String? = registry.get(id)
    suspend fun remove(id: String) = registry.remove(id)

    suspend fun <T> get(id: T) = get("$id")
    suspend fun get(id: Snowflake) = get("${id.value}")

}
