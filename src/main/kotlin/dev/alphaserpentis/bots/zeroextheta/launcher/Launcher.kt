package dev.alphaserpentis.bots.zeroextheta.launcher

import dev.alphaserpentis.bots.zeroextheta.commands.CustomSettings
import dev.alphaserpentis.bots.zeroextheta.commands.Notify
import dev.alphaserpentis.bots.zeroextheta.commands.Portfolio
import dev.alphaserpentis.bots.zeroextheta.commands.Protocol
import dev.alphaserpentis.bots.zeroextheta.commands.Token
import dev.alphaserpentis.bots.zeroextheta.handlers.bot.AutocompleteHandler
import dev.alphaserpentis.bots.zeroextheta.handlers.bot.ThetaDataHandler
import dev.alphaserpentis.bots.zeroextheta.handlers.bot.UserSessionHandler
import dev.alphaserpentis.bots.zeroextheta.handlers.ethereum.PortfolioHandler
import dev.alphaserpentis.bots.zeroextheta.handlers.ethereum.TokenHandler
import dev.alphaserpentis.bots.zeroextheta.handlers.ethereum.DefiLlamaHandler
import dev.alphaserpentis.bots.zeroextheta.handlers.ethereum.RPCHandler
import dev.alphaserpentis.coffeecore.commands.defaultcommands.About
import dev.alphaserpentis.coffeecore.commands.defaultcommands.Help
import dev.alphaserpentis.coffeecore.core.CoffeeCore
import dev.alphaserpentis.coffeecore.core.CoffeeCoreBuilder
import dev.alphaserpentis.coffeecore.data.bot.AboutInformation
import dev.alphaserpentis.coffeecore.data.bot.BotSettings
import dev.alphaserpentis.coffeecore.data.entity.EntityData
import dev.alphaserpentis.coffeecore.serialization.EntityDataDeserializer
import io.github.cdimascio.dotenv.Dotenv
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder
import org.slf4j.LoggerFactory
import java.awt.Color
import kotlin.io.path.Path

object Launcher {
    private val logger = LoggerFactory.getLogger(Launcher::class.java)
    lateinit var core: CoffeeCore
    lateinit var tokenHandler: TokenHandler
    lateinit var autocompleteHandler: AutocompleteHandler
    lateinit var userSessionHandler: UserSessionHandler
    lateinit var portfolioHandler: PortfolioHandler<EntityData>

    @JvmStatic
    fun main(args: Array<String>) {
        val dotenv = Dotenv.load()

        initializeCoffeeCore(dotenv)
        initializeComponents(dotenv)
        initializeCommands()
    }

    private fun initializeCoffeeCore(dotenv: Dotenv) {
        val aboutInfo = AboutInformation(
            """
                **0xTheta** is a multi-chain wallet/address tracking bot informing on not only your position, but protocols, NFTs, and more!
                
                ## Note
                We may __suggest__ certain actions (e.g., token revocations). However, the bot does not require you to sign or place any transactions. If you get a message stating that you're required to sign a message/transaction, it's a scam!
                
                ## Acknowledgements
                **0xTheta** uses both on-chain and off-chain resources to deliver you the most accurate and up-to-date information.
               
                - DefiLlama for protocol, token information, and icons
                
                ## Support/Feedback
                If you are having any issues or have feedback, you can join our Discord server for assistance or you can open an issue on our GitHub repository.
                
                [**GitHub Repo**](https://github.com/Alpha-Serpentis-Developments)
                [**Support Discord**](https://discord.asrp.dev)
            """.trimIndent(),
            "wagmi?",
            Color.PINK,
            true,
            true
        )
        val botSettings = BotSettings(
            dotenv["BOT_OWNER_ID"].toLong(),
            dotenv["DATA_PATH"],
            dotenv["UPDATE_COMMANDS"].toBoolean(),
            false,
            aboutInfo
        )
        val entityDataDeserializer = EntityDataDeserializer<EntityData>()
        val dataHandler = ThetaDataHandler(
            Path(dotenv["CACHE_PATH"]),
            Path(dotenv["DATA_PATH"]),
            entityDataDeserializer
        )
        val builder = CoffeeCoreBuilder<DefaultShardManagerBuilder>()
            .setDataHandler(dataHandler)
            .setSettings(botSettings)
            .enableSharding(true)

        core = builder.build(dotenv["BOT_TOKEN"])
        core.commandsHandler.setHandleInteractionError(this::logInteractionError)

        logger.info("Coffee Core has been initialized")
    }

    @Suppress("UNCHECKED_CAST")
    private fun initializeComponents(dotenv: Dotenv) {
        RPCHandler // Initialize RPCHandler
        DefiLlamaHandler // Initialize DefiLlamaHandler
        this.tokenHandler = TokenHandler(Path(dotenv["TRUSTED_TOKEN_DATA_PATH"]))
        this.portfolioHandler = PortfolioHandler(tokenHandler, core.dataHandler as ThetaDataHandler<EntityData>)
        this.autocompleteHandler = AutocompleteHandler(commandsHandler = core.commandsHandler)
        this.userSessionHandler = UserSessionHandler()

        core.addEventListenersToContainer(autocompleteHandler)

        logger.info("Components have been initialized")
    }

    private fun initializeCommands() {
        core.registerCommands(
            Notify(),
            Portfolio(userSessionHandler, portfolioHandler),
            Token(tokenHandler, userSessionHandler),
            Protocol(userSessionHandler),
            Help(),
            About(),
            CustomSettings()
        )

        logger.info("Commands have been registered")
    }

    private fun logInteractionError(e: Throwable) = logger.error("Error while executing interaction", e)
}
