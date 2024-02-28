package dev.alphaserpentis.bots.zeroextheta.handlers.ethereum;

import dev.alphaserpentis.bots.zeroextheta.data.api.ExecuteRetrofit
import dev.alphaserpentis.bots.zeroextheta.data.api.alchemy.AlchemyEndpoints;
import dev.alphaserpentis.bots.zeroextheta.data.api.alchemy.request.AssetTransfersBody
import dev.alphaserpentis.bots.zeroextheta.data.web3.Networks
import dev.alphaserpentis.bots.zeroextheta.exceptions.RatelimitException
import io.github.cdimascio.dotenv.Dotenv
import io.reactivex.rxjava3.core.Single
import org.slf4j.LoggerFactory
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

object AlchemyHandler : ExecuteRetrofit {
    private val logger = LoggerFactory.getLogger(AlchemyHandler::class.java)
    private val apiKey = Dotenv.load()["ALCHEMY_API_KEY"]
    val apis: MutableMap<Long, AlchemyEndpoints> = mutableMapOf()

    init {
        Networks.entries.forEach {
            apis[it.chainId] = Retrofit.Builder()
                .baseUrl("https://${it.alchemy}.g.alchemy.com/v2/")
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
                .build()
                .create(AlchemyEndpoints::class.java)
        }
    }

    fun getAllTransfers(
        from: String? = null,
        to: String? = null,
        chainId: Long,
        category: List<String> = listOf("external", "erc20", "erc721", "erc1155", "specialnft"),
        fromBlock: String = "0x0",
        toBlock: String = "latest",
        pageKey: String? = null
    ) = execute(
        apis[chainId]!!.getAssetTransfers(
            apiKey,
            AssetTransfersBody(
                params = listOf(
                    AssetTransfersBody.Parameter(
                        fromAddress = from,
                        toAddress = to,
                        category = category,
                        fromBlock = fromBlock,
                        toBlock = toBlock,
                        pageKey = pageKey
                    )
                )
            )
        )
    )

    override fun <T : Any> execute(
        call: Single<T>,
        retryOnRatelimit: Boolean,
        maxRetries: Int?
    ): T {
        return try {
            call.blockingGet()
        } catch (e: HttpException) {
            if (e.code() == 429) {
                if (retryOnRatelimit && maxRetries != null && maxRetries > 0) {
                    Thread.sleep(250L)

                    execute(call, true, maxRetries - 1)
                } else {
                    logger.error("Ratelimited on Alchemy API")

                    throw RatelimitException()
                }
            } else {
                logger.error("Error on Alchemy API", e)

                throw e
            }
        } catch (e: Exception) {
            logger.error("Error executing Alchemy call", e)

            throw e
        }
    }
}
