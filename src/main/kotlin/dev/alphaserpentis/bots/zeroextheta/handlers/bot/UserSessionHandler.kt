package dev.alphaserpentis.bots.zeroextheta.handlers.bot

import dev.alphaserpentis.bots.zeroextheta.data.bot.UserSessionData
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

class UserSessionHandler {
    private val scheduledExecutor: ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor()
    val userData = mutableMapOf<Long, UserSessionData>()

    init {
        scheduledExecutor.scheduleAtFixedRate(this::cleanUp, 5, 5, TimeUnit.MINUTES)
    }

    private fun cleanUp() {
        val currentTime = System.currentTimeMillis()

        // Remove entries if they're older than 10 minutes
        userData.entries.removeIf { entry -> currentTime - entry.value.lastInteraction > 600000 }
    }
}