package dev.alphaserpentis.bots.zeroextheta.commands

import dev.alphaserpentis.bots.zeroextheta.data.bot.ThetaUserData
import dev.alphaserpentis.bots.zeroextheta.handlers.bot.ThetaDataHandler
import dev.alphaserpentis.coffeecore.commands.defaultcommands.Settings
import net.dv8tion.jda.api.EmbedBuilder

class CustomSettings : Settings() {
    override fun setUserFullError(userId: Long, eb: EmbedBuilder) {
        val dh = core.dataHandler as ThetaDataHandler<*>
        val userData = dh.getEntityData("user", userId) as ThetaUserData

        if (userData.showFullStackTrace) {
            userData.showFullStackTrace = false
            eb.setDescription("The bot will no longer show full stack traces.")
        } else {
            userData.showFullStackTrace = true
            eb.setDescription("The bot will now show full stack traces.")
        }

        dh.updateEntityData()
    }
}