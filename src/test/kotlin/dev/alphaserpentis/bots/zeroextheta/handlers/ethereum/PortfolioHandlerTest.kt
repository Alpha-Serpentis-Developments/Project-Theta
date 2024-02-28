package dev.alphaserpentis.bots.zeroextheta.handlers.ethereum

import dev.alphaserpentis.bots.zeroextheta.handlers.bot.ThetaDataHandler
import dev.alphaserpentis.coffeecore.data.entity.EntityData
import dev.alphaserpentis.coffeecore.serialization.EntityDataDeserializer
import io.github.cdimascio.dotenv.Dotenv
import org.junit.jupiter.api.Test
import kotlin.io.path.Path

class PortfolioHandlerTest {

    @Test
    fun getAddresses() {
    }

    @Test
    fun getTokensInAddress() {

    }

    @Test
    fun getAllTransfers() {
        val transfers = portfolioHandler.getAllTransfers(
            "0xf5E962C4Df3C870266aEab048335184Ee0D9ad66",
            listOf(1)
        )

        println(
            portfolioHandler.calculatePnl(
                "0xf5E962C4Df3C870266aEab048335184Ee0D9ad66",
                transfers
            )
        )
    }

    @Test
    fun calculatePnl() {
    }

    @Test
    fun getPriceOfToken() {
    }

    companion object {
        val portfolioHandler: PortfolioHandler<EntityData>

        init {
            val dotenv = Dotenv.load()
            portfolioHandler = PortfolioHandler(
                TokenHandler(
                    Path(dotenv["TRUSTED_TOKEN_DATA_PATH"]),
                ),
                ThetaDataHandler(
                    Path(dotenv["CACHE_PATH"]),
                    Path(dotenv["DATA_PATH"]),
                    EntityDataDeserializer<EntityData>()
                )
            )
        }
    }
}