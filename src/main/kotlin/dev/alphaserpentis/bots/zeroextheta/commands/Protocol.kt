package dev.alphaserpentis.bots.zeroextheta.commands

import dev.alphaserpentis.bots.zeroextheta.data.bot.UserSessionData
import dev.alphaserpentis.bots.zeroextheta.handlers.bot.UserSessionHandler
import dev.alphaserpentis.bots.zeroextheta.handlers.ethereum.DefiLlamaHandler
import dev.alphaserpentis.coffeecore.commands.ButtonCommand
import dev.alphaserpentis.coffeecore.data.bot.CommandResponse
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import net.dv8tion.jda.api.interactions.components.ItemComponent
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle
import retrofit2.HttpException
import java.awt.Color
import java.text.NumberFormat
import java.util.Locale
import java.util.Optional

class Protocol(
    private val userSessionHandler: UserSessionHandler
) : ButtonCommand<MessageEmbed, SlashCommandInteractionEvent>(
    BotCommandOptions("protocol", "Get info on a protocol")
        .setDeferReplies(true)
        .setUseRatelimits(true)
        .setRatelimitLength(30)
), AutocompleteCmd {
    data class ProtocolSession(
        val protocolName: String
    )

    private val nf = NumberFormat.getCurrencyInstance()
    private val defaultEmbedBuilder = EmbedBuilder()
        .setColor(Color.CYAN)
        .setFooter("Data Provided by DefiLlama")

    init {
        addButton("website", ButtonStyle.LINK, "Website", false)
        addButton("coingecko", ButtonStyle.LINK, "CoinGecko", false)
        addButton("coinmarketcap", ButtonStyle.LINK, "CoinMarketCap", false)
        addButton("token", ButtonStyle.PRIMARY, "Token", Emoji.fromUnicode("\uD83E\uDE99"), false)
        addButton("breakdown", ButtonStyle.PRIMARY, "Breakdown", false)
        addButton("back", ButtonStyle.SECONDARY, "Back", false)
    }

    override fun runCommand(userId: Long, event: SlashCommandInteractionEvent): CommandResponse<MessageEmbed> {
        val eb = EmbedBuilder(defaultEmbedBuilder)
        val protocolName = event.getOption("name")!!.asString.lowercase().replace(" ", "-")
        val userSession = UserSessionData(
            event.guild?.idLong ?: -1,
            name,
            "main",
            protocolSession = ProtocolSession(protocolName)
        )
        userSessionHandler.userData[userId] = userSession

        applyProtocolInfo(userSession, eb, protocolName)

        return CommandResponse(isOnlyEphemeral, eb.build())
    }

    override fun runButtonInteraction(event: ButtonInteractionEvent): Optional<Any> {

        return Optional.empty()
    }

    override fun addButtonsToMessage(event: SlashCommandInteractionEvent): MutableCollection<ItemComponent> {
        val userId = event.user.idLong
        val userSession = userSessionHandler.userData[userId] ?: return mutableListOf()

        return userSession.buttonsToAdd
    }

    override fun updateCommand(jda: JDA) {
        val nameOption = OptionData(OptionType.STRING, "name", "Name of the protocol")
            .setRequired(true)
            .setAutoComplete(true)

        jda
            .upsertCommand(name, description!!)
            .addOptions(nameOption)
            .queue { command -> setGlobalCommandId(command.idLong) }
    }

    override fun suggest(event: CommandAutoCompleteInteractionEvent): List<String> {
        val currentInput = event.focusedOption.value

        return DefiLlamaHandler.cachedProtocolNamesToSlug.keys
            .filter { it.contains(currentInput, ignoreCase = true) }
            .take(25)
            .sortedBy {
                if (it.startsWith(currentInput, ignoreCase = true)) { // Prioritize entries that start with the input
                    "0$it"
                } else {
                    it
                }
            }
    }

    private fun applyProtocolInfo(
        userSession: UserSessionData,
        eb: EmbedBuilder,
        protocolName: String,
        page: String = "main"
    ) {
        when (page) {
            "main" -> mainPage(eb, protocolName, userSession)
            "breakdown" -> {

            }
            else -> {
                TODO()
            }
        }
    }

    private fun mainPage(
        eb: EmbedBuilder,
        protocolName: String,
        userSession: UserSessionData
    ) {
        try {
            val specificInfo = DefiLlamaHandler.getProtocol(protocolName)
            val sb = StringBuilder()

            eb.setThumbnail(specificInfo.logo)

            sb.append("""
                        # [${specificInfo.name}](${"https://defillama.com/protocol/$protocolName"})
                        ## About
                        ${specificInfo.description}
                    """.trimIndent())

            // Show stats if available
            if (specificInfo.currentChainTvls.isNotEmpty()) {
                sb.appendLine("""
                    ## TVL
                    ${
                        specificInfo.currentChainTvls.entries
                            .filter { !it.key.contains("borrowed") }
                            .sortedBy { it.value }
                            .reversed()
                            .joinToString("\n", limit = 5) {
                                    (chain, tvl) -> "- **${humanReadableName(chain)}**: ${nf.format(tvl)}"
                            }
                    }
                            
                    ${
                        if (specificInfo.currentChainTvls.size > 5) {
                            "And ${specificInfo.currentChainTvls.size - 5} more"
                        } else { "" }
                    }
                """.trimIndent())
            }

            if (specificInfo.address != null) {
                userSession.buttonsToAdd
                    .add(getButton("token")!!
                        .withId("token_${specificInfo.address}"))
            }
            if (specificInfo.url != null) {
                userSession.buttonsToAdd
                    .add(getButton("website")!!
                        .withUrl(specificInfo.url))
            }

            userSession.buttonsToAdd
                .add(getButton("breakdown")!!)

            eb.setDescription(sb.toString())
        } catch (e: HttpException) {
            if (e.code() == 400) {
                eb.setTitle("Protocol Not Found")
                eb.setDescription("""
                            The protocol you are looking for may not exist. Double check your spelling and try again.
                            
                            If you believe this is an error, please reach out to us on our [**Discord**](https://discord.asrp.dev).
                        """.trimIndent())
                eb.setColor(Color.RED)
            } else {
                eb.setTitle("Error")
                eb.setDescription("""
                            An error occurred while trying to fetch the protocol information. Please try again later.
                            
                            ```
                            ${e.message ?: "No error message provided"}
                            ```
                            
                            If this issue persists, please reach out to us on our [**Discord**](https://discord.asrp.dev).
                        """.trimIndent())
                eb.setColor(Color.RED)
            }
        }
    }

    private fun humanReadableName(name: String): String {
        return name
            .replace("-", " ")
            .split(" ")
            .joinToString(" ") { s ->
                s.replaceFirstChar {
                    if (it.isLowerCase())
                        it.titlecase(Locale.getDefault())
                    else
                        it.toString()
                }
            }
    }

//    private fun getProtocolModule(slug: String): IModule<*, *>? {
//        return when (slug) {
//            "ens" -> ENS()
//            else -> null
//        }
//    }
}