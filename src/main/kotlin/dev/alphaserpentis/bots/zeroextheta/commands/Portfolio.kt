package dev.alphaserpentis.bots.zeroextheta.commands

import dev.alphaserpentis.bots.zeroextheta.data.bot.UserSessionData
import dev.alphaserpentis.bots.zeroextheta.handlers.bot.UserSessionHandler
import dev.alphaserpentis.bots.zeroextheta.handlers.ethereum.PortfolioHandler
import dev.alphaserpentis.coffeecore.commands.ButtonCommand
import dev.alphaserpentis.coffeecore.commands.ModalCommand
import dev.alphaserpentis.coffeecore.data.bot.CommandResponse
import dev.alphaserpentis.coffeecore.data.entity.EntityData
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData
import net.dv8tion.jda.api.interactions.commands.build.SubcommandGroupData
import net.dv8tion.jda.api.interactions.components.ItemComponent
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle
import java.util.Optional

class Portfolio(
    private val userSessionHandler: UserSessionHandler,
    private val portfolioHandler: PortfolioHandler<EntityData>
) : ButtonCommand<MessageEmbed, SlashCommandInteractionEvent>(
    BotCommandOptions("portfolio", "Get info on your portfolio")
        .setOnlyEphemeral(true)
        .setDeferReplies(true)
        .setUseRatelimits(true)
        .setRatelimitLength(60)
), ModalCommand {
    data class PortfolioSession(
        val tempVariable: String
    )

    private val defaultViewEmbedBuilder = EmbedBuilder()
        .setColor(0x00FF00)
        .setTitle("Your Positions")
        .setFooter("Positions calculated via LIFO method")

    init {
        addButton("remove", ButtonStyle.DANGER, "Remove", false)
        addButton("notify", ButtonStyle.PRIMARY, "Notify", false)
        addButton("next", ButtonStyle.SECONDARY, "Next", false)
        addButton("back", ButtonStyle.SECONDARY, "Back", false)
        addButton("protocolview", ButtonStyle.PRIMARY, "View", false)
    }

    override fun runCommand(userId: Long, event: SlashCommandInteractionEvent): CommandResponse<MessageEmbed> {
        val eb = EmbedBuilder()
        val userSession = UserSessionData(
            event.guild?.idLong ?: -1,
            name,
            "main",
            portfolioSession = PortfolioSession("temp")
        )
        val subcommand = event.subcommandName!!
        userSessionHandler.userData[userId] = userSession

        when (subcommand) {
            "view" -> loadViewPage(eb, userId)
        }

        return CommandResponse(onlyEphemeral, eb.build())
    }

    override fun addButtonsToMessage(event: SlashCommandInteractionEvent): MutableCollection<ItemComponent> {
        val userId = event.user.idLong
        val userSession = userSessionHandler.userData[userId] ?: UserSessionData(
            event.guild?.idLong ?: -1,
            name,
            "main",
            portfolioSession = PortfolioSession("temp")
        )

        
        TODO("Not yet implemented")
    }

    override fun updateCommand(jda: JDA) {
        val viewGroup = SubcommandGroupData("view", "Use view-related commands")
        val manageGroup = SubcommandGroupData("manage", "Manage what you are tracking")
        val addCommand = SubcommandData("add", "Add a new address to track")
            .addOption(OptionType.STRING, "address", "The address to track", true)
        val removeCommand = SubcommandData("remove", "Remove an address from tracking")
            .addOption(OptionType.STRING, "address", "The address to remove", true)

        manageGroup.addSubcommands(addCommand, removeCommand)

        jda
            .upsertCommand(name, description)
            .addSubcommandGroups(viewGroup, manageGroup)
            .queue { command -> setGlobalCommandId(command.idLong) }
    }

    override fun runButtonInteraction(event: ButtonInteractionEvent): Optional<*> {
        val key = event.componentId.split("_")[1]

        when (key) {
            "add" -> {

            }
            "remove" -> {
                portfolioHandler.removeAddress("address", event.user.idLong)
            }
            "notify" -> {
                // Open notification settings
            }
        }

        return Optional.empty<Any>()
    }

    override fun runModalInteraction(event: ModalInteractionEvent): Optional<*> {
        TODO("Not yet implemented")
    }

    private fun loadViewPage(eb: EmbedBuilder, userId: Long) {
        // Pull the user's addresses
        val addresses = portfolioHandler.getAddresses(userId)

        if (addresses.isEmpty()) {
            eb.setDescription("You're currently not tracking any addresses! Perhaps you should add some \uD83E\uDD14")
        } else {

        }
    }

    private fun loadManageView(eb: EmbedBuilder, userId: Long) {

    }

    private fun loadAddView(
        eb: EmbedBuilder,
        userId: Long,
        address: String
    ) {

    }

    private fun loadRemoveView(
        eb: EmbedBuilder,
        userId: Long,
        address: String
    ) {

    }
}