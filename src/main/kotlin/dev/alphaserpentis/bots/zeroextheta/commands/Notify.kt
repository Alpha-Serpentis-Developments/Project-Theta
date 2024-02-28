package dev.alphaserpentis.bots.zeroextheta.commands

import dev.alphaserpentis.coffeecore.commands.ButtonCommand
import dev.alphaserpentis.coffeecore.data.bot.CommandResponse
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.interactions.components.ItemComponent
import java.util.Optional

class Notify : ButtonCommand<MessageEmbed, SlashCommandInteractionEvent>(
    BotCommandOptions("notify", "Notify me when a token reaches a certain price")
        .setDeferReplies(true)
        .setUseRatelimits(true)
        .setRatelimitLength(30)
) {

    override fun runCommand(userId: Long, event: SlashCommandInteractionEvent): CommandResponse<MessageEmbed> {
        TODO("Not yet implemented")
    }

    override fun runButtonInteraction(event: ButtonInteractionEvent): Optional<*> {
        TODO("Not yet implemented")
    }

    override fun addButtonsToMessage(event: SlashCommandInteractionEvent): MutableCollection<ItemComponent> {
        TODO("Not yet implemented")
    }
}