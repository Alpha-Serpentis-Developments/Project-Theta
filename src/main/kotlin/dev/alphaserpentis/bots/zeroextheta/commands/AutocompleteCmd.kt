package dev.alphaserpentis.bots.zeroextheta.commands

import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent

/**
 * Interface for BotCommand that can suggest autocompletions for a command
 */
interface AutocompleteCmd {
    fun suggest(event: CommandAutoCompleteInteractionEvent): List<String> = emptyList()
}