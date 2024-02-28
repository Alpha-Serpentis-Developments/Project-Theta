package dev.alphaserpentis.bots.zeroextheta.handlers.bot

import dev.alphaserpentis.bots.zeroextheta.commands.AutocompleteCmd
import dev.alphaserpentis.coffeecore.handler.api.discord.commands.CommandsHandler
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

class AutocompleteHandler(
    private val mappingOfCommands: HashMap<String, AutocompleteCmd> = HashMap(),
    private val commandsHandler: CommandsHandler
) : ListenerAdapter() {

    override fun onCommandAutoCompleteInteraction(event: CommandAutoCompleteInteractionEvent) {
        val command = findCommand(event.name) ?: return
        val choices = command.suggest(event)

        event.replyChoiceStrings(choices).queue()
    }

    private fun findCommand(name: String): AutocompleteCmd? {
        val command = mappingOfCommands[name]

        if (command == null) {
            val searchedCmd = commandsHandler.getCommand(name)

            return if (searchedCmd is AutocompleteCmd) {
                mappingOfCommands[name] = searchedCmd
                searchedCmd
            } else {
                null
            }
        } else {
            return command
        }
    }
}