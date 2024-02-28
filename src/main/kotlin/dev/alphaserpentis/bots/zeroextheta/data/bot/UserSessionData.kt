package dev.alphaserpentis.bots.zeroextheta.data.bot

import dev.alphaserpentis.bots.zeroextheta.commands.Portfolio
import dev.alphaserpentis.bots.zeroextheta.commands.Protocol
import dev.alphaserpentis.bots.zeroextheta.commands.Token
import net.dv8tion.jda.api.interactions.components.ItemComponent

/**
 * User session data to keep track of the user's current state across different commands and pages
 */
data class UserSessionData(
    val focusedGuildId: Long,
    var focusedCommand: String,
    var pageId: String = "main",
    var previousCommand: String = "",
    var previousPageId: String = "",
    var tokenSession: Token.TokenSession? = null,
    var protocolSession: Protocol.ProtocolSession? = null,
    var portfolioSession: Portfolio.PortfolioSession? = null,
    var lastInteraction: Long = System.currentTimeMillis(),
    val buttonsToAdd: MutableList<ItemComponent> = mutableListOf()
)
