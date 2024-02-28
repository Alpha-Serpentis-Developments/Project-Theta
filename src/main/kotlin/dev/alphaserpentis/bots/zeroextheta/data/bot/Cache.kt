package dev.alphaserpentis.bots.zeroextheta.data.bot

import dev.alphaserpentis.bots.zeroextheta.data.web3.onchain.SimpleTransfer
import dev.alphaserpentis.coffeecore.data.entity.EntityData

data class Cache(
    /**
     * A map of addresses to symbols for each network.
     *
     * The first key is the chain ID pointing to a map of addresses to symbols.
     */
    val addressesToSymbols: MutableMap<Long, MutableMap<String, String>> = mutableMapOf(),
    val ensToAddresses: MutableMap<String, String> = mutableMapOf(),
    val transfers: MutableMap<Long, MutableMap<String, SimpleTransfer>>,
    val addressesOverLimit: MutableMap<Long, MutableList<String>>
) : EntityData() {
    fun wipeAllCaches() {
        wipeAddressesToSymbols()
        wipeEnsToAddresses()
    }

    fun wipeAddressesToSymbols() = addressesToSymbols.clear()

    fun wipeEnsToAddresses() = ensToAddresses.clear()

    fun wipeTransfers(chainId: Long? = null, addresses: List<String>? = null) {

    }
}
