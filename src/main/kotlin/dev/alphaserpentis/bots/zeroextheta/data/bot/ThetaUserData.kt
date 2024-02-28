package dev.alphaserpentis.bots.zeroextheta.data.bot

import dev.alphaserpentis.coffeecore.data.entity.UserData

/**
 * User data for the Theta bot
 */
data class ThetaUserData(
    var tokensToTrack: MutableList<String> = mutableListOf(),
    var addressesToTrack: MutableList<String> = mutableListOf(),
    var addressedFlaggedAsSpam: MutableList<String> = mutableListOf(),
    var protocolsToTrack: MutableList<String> = mutableListOf(),
    var saveAddresses: Boolean = true,
    var optOutOfAnalytics: Boolean = false
) : UserData()
