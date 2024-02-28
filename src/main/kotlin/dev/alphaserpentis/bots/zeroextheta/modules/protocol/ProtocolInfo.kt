package dev.alphaserpentis.bots.zeroextheta.modules.protocol

import dev.alphaserpentis.bots.zeroextheta.data.web3.Networks
import net.dv8tion.jda.api.EmbedBuilder
import java.util.Optional

/**
 * Interface to provide supplementary information about a protocol
 */
interface ProtocolInfo<in T : Any, out R : Any> {
    fun getProtocolName(): String
    fun getProtocolSlug(): String? = null
    fun getSupportedNetworks(): List<Networks> = emptyList()
    fun getWebsite(): String? = null
    /**
     * Return a list of token addresses that the protocol uses for ERC20/ERC721/ERC1155 tokens
     *
     * Default empty if the protocol does not have its own tokens
     */
    fun getTokenAddresses(): List<String> = emptyList()
    fun getData(input: T? = null): Optional<*> = Optional.empty<Void>()
    fun putPageOnEmbed(eb: EmbedBuilder, pageKey: String) {}
}