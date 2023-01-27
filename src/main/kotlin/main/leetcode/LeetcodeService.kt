package main.leetcode

import dev.kord.common.entity.Snowflake
import me.jakejmattson.discordkt.annotations.Service


@Service
class LeetcodeService(
    private val storage: LeetcodePersistence,
    private val api: LeetcodeRemoteDatasource
) {

    suspend fun getStatistics(username: String): LeetcodeStatistic =
        api.getStatistics(username)

    suspend fun allStatistics(): List<Pair<String, LeetcodeStatistic>> =
        storage.allUsers().map {
            Pair(it.first, api.getStatistics(it.second))
        }


    suspend fun register(id: String, username: String) = storage.register(id, username)
    suspend fun get(id: String): String? = storage.get(id)
    suspend fun remove(id: String) = storage.remove(id)

    suspend fun <T> get(id: T) = get("$id")
    suspend fun get(id: Snowflake) = get("${id.value}")

}
