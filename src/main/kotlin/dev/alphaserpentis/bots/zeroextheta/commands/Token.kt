package dev.alphaserpentis.bots.zeroextheta.commands

import dev.alphaserpentis.bots.zeroextheta.data.bot.UserSessionData
import dev.alphaserpentis.bots.zeroextheta.data.web3.TokenInfo
import dev.alphaserpentis.bots.zeroextheta.handlers.bot.UserSessionHandler
import dev.alphaserpentis.bots.zeroextheta.handlers.ethereum.RPCHandler
import dev.alphaserpentis.bots.zeroextheta.handlers.ethereum.TokenHandler
import dev.alphaserpentis.bots.zeroextheta.handlers.ethereum.DefiLlamaHandler
import dev.alphaserpentis.coffeecore.commands.ButtonCommand
import dev.alphaserpentis.coffeecore.data.bot.CommandResponse
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import net.dv8tion.jda.api.interactions.components.ItemComponent
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.Function
import org.web3j.abi.datatypes.Utf8String
import org.web3j.abi.datatypes.generated.Uint256
import org.web3j.abi.datatypes.generated.Uint8
import java.awt.Color
import java.math.BigInteger
import java.util.Optional
import kotlin.math.pow

class Token(
    private val tokenHandler: TokenHandler,
    private val userSessionHandler: UserSessionHandler
) : ButtonCommand<MessageEmbed, SlashCommandInteractionEvent>(
    BotCommandOptions("token", "Get info on a token")
        .setDeferReplies(true)
        .setUseRatelimits(true)
        .setRatelimitLength(30)
) {
    data class TokenSession(
        val tokenInfo: TokenInfo,
        var lastInteraction: Long,
        var pageId: String = "main"
    )

    private val getTokenName: Function = Function(
        "name",
        emptyList(),
        listOf(object : TypeReference<Utf8String>() {})
    )
    private val getTokenSymbol: Function = Function(
        "symbol",
        emptyList(),
        listOf(object : TypeReference<Utf8String>() {})
    )
    private val getTokenDecimals: Function = Function(
        "decimals",
        emptyList(),
        listOf(object : TypeReference<Uint8>() {})
    )
    private val getTokenTotalSupply: Function = Function(
        "totalSupply",
        emptyList(),
        listOf(object : TypeReference<Uint256>() {})
    )

    init {
        addButton("etherscan", ButtonStyle.LINK, "Etherscan", false)
        addButton("chart", ButtonStyle.PRIMARY, "Chart", Emoji.fromUnicode("\uD83D\uDCCA"), false)
        addButton("compare", ButtonStyle.PRIMARY, "Compare", Emoji.fromUnicode("\uD83D\uDD00"), false)
        addButton("back", ButtonStyle.PRIMARY, "Go Back", Emoji.fromUnicode("\uD83D\uDD19"), false)
        addButton("add", ButtonStyle.PRIMARY, "Add to Watchlist", Emoji.fromUnicode("\uD83D\uDCD2"), false)
        addButton("remove", ButtonStyle.PRIMARY, "Remove from Watchlist", Emoji.fromUnicode("\uD83D\uDDD1"), false)
    }

    private val defaultEmbedBuilder = EmbedBuilder()
        .setColor(Color.CYAN)
        .setThumbnail("https://icons.llama.fi/chains/rsz_ethereum.jpg")
        .setFooter("Source: DefiLlama + On-Chain Data")

    override fun runButtonInteraction(event: ButtonInteractionEvent): Optional<Any> {
        when (val key = event.componentId.split("_")[1]) {
            "chart" -> {
                TODO()
            }
            "compare" -> {
                TODO()
            }
            "back" -> {
                TODO()
            }
            "add" -> {
                TODO()
            }
            "remove" -> {
                TODO()
            }
            else -> {
                try {
                    val split = key.split(":")
                    val chainId = split[0].toLong()
                    val address = split[1]
                } catch (e: IndexOutOfBoundsException) {
                    return Optional.empty()
                }
            }
        }

        return Optional.empty()
    }

    override fun addButtonsToMessage(event: SlashCommandInteractionEvent): Collection<ItemComponent> {
        val userSession = userSessionHandler.userData[event.user.idLong]?.tokenSession ?: return mutableListOf()

        when (userSession.pageId) {
            "main" -> {
                val etherscan = getButton("etherscan")!!
                val customEtherscan = etherscan.withUrl("https://etherscan.io/token/${userSession.tokenInfo.address}")

                return listOf(customEtherscan, getButton("chart")!!)
            }
            else -> {
                TODO()
            }
        }
    }

    override fun runCommand(userId: Long, event: SlashCommandInteractionEvent): CommandResponse<MessageEmbed> {
        val tokenInput: String = event.getOption("name")!!.asString
        val chainId: Long = event.getOption("chain")?.asLong ?: 1
        val eb = EmbedBuilder(defaultEmbedBuilder)
        val userSession = UserSessionData(
            event.guild?.idLong ?: -1,
            name,
            "main",
            tokenSession = TokenSession(getTokenInfo(tokenInput, chainId), System.currentTimeMillis())
        )

        userSessionHandler.userData[userId] = userSession

        putTokenInfoToEmbed(tokenInput, chainId, eb)

        return CommandResponse(isOnlyEphemeral, eb.build())
    }

    override fun updateCommand(jda: JDA) {
        val nameOption = OptionData(OptionType.STRING, "name", "Address, name, or symbol of the token")
            .setRequired(true)
        val chainId = OptionData(OptionType.INTEGER, "chain", "Chain ID to look up the token on")
            .setRequired(false)

        jda
            .upsertCommand(name, description!!)
            .addOptions(nameOption, chainId)
            .queue { command -> setGlobalCommandId(command.idLong) }
    }

    private fun putTokenInfoToEmbed(
        input: String,
        chainId: Long? = 1,
        eb: EmbedBuilder
    ) {
        // Check if the input is an address
        val address = if (input.length == 42 && input.startsWith("0x")) {
            input
        } else {
            // First check the trusted tokens
            val trustedToken = tokenHandler.lookupTrustedToken(name = input, symbol = input, chainId = chainId)

            // Try to call the DeFiLlama API to see if the name can match with a token
            if (trustedToken == null) {
                ""
            } else {
                eb.setThumbnail(trustedToken.second.second.logoUrl)

                trustedToken.second.first // The address
            }
        }

        val tokenInfo = getTokenInfo(address, chainId)

        tokenInfo.updatePrice()

        eb.setTitle("${tokenInfo.name} (${tokenInfo.symbol})")
        eb.addField(
            "Basic Info",
            """
                Price: $${tokenInfo.averagedPrice}
                FDV: $${tokenInfo.averagedPrice * tokenInfo.totalSupply.toDouble() / 10.0.pow(tokenInfo.decimals)}
                Total Supply: ${tokenInfo.getFormattedTotalSupply()}
            """,
            false
        )
    }

    private fun getTokenInfo(address: String, chainId: Long?): TokenInfo {
        val name = RPCHandler.executeCall(to = address, function = getTokenName)?.get(0)?.value as String
        val symbol = RPCHandler.executeCall(to = address, function = getTokenSymbol)?.get(0)?.value as String
        val totalSupply = RPCHandler.executeCall(to = address, function = getTokenTotalSupply)?.get(0)?.value as BigInteger
        val decimals = (RPCHandler.executeCall(to = address, function = getTokenDecimals)?.get(0)?.value as BigInteger).toInt()

        return TokenInfo(name, symbol, address, chainId, decimals, totalSupply)
    }
}
