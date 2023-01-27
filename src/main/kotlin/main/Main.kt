package main

import dev.kord.common.annotation.KordPreview
import io.github.cdimascio.dotenv.dotenv
import io.github.crackthecodeabhi.kreds.connection.Endpoint
import io.github.crackthecodeabhi.kreds.connection.newClient
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import main.leetcode.LeetcodeService
import me.jakejmattson.discordkt.dsl.bot
import me.jakejmattson.discordkt.locale.Language

object Environment {
    private val dotenv = dotenv()

    val discordToken: String get() = dotenv.get("DISCORD_BOT_TOKEN")
    val redisUri: String get() = dotenv.get("REDIS")
}

@OptIn(KordPreview::class)
suspend fun main(args: Array<String>) {
    bot(Environment.discordToken) {
        onStart {
            val guilds = kord.guilds.toList()
            println("Guilds: ${guilds.joinToString { it.name }}")
        }

        onException {
            println("Exception $this")
        }

        localeOf(Language.EN) {
            helpName = "Help"
            helpCategory = "Utility"
            commandRecommendation = "Recommendation: {0}"
        }
    }
}

