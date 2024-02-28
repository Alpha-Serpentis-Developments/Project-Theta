package dev.alphaserpentis.bots.zeroextheta.handlers.ethereum

import dev.alphaserpentis.bots.zeroextheta.data.api.ExecuteRetrofit
import dev.alphaserpentis.bots.zeroextheta.data.api.defillama.response.CoinsToPrice
import dev.alphaserpentis.bots.zeroextheta.data.api.defillama.DLEndpoints
import dev.alphaserpentis.bots.zeroextheta.data.api.defillama.response.DLGeneralProtocol
import io.reactivex.rxjava3.core.Single
import org.slf4j.LoggerFactory
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.Thread.sleep

object DefiLlamaHandler : ExecuteRetrofit {
    private val logger = LoggerFactory.getLogger(DefiLlamaHandler::class.java)
    private val api: DLEndpoints = Retrofit.Builder()
        .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
        .addConverterFactory(GsonConverterFactory.create())
        .baseUrl("https://api.llama.fi")
        .build()
        .create(DLEndpoints::class.java)
    private var lastUpdated = 0L
    private lateinit var cachedProtocols: HashMap<String, DLGeneralProtocol>
    var cachedProtocolNamesToSlug: HashMap<String, String>

    init {
        getProtocols()

        cachedProtocolNamesToSlug = HashMap(cachedProtocols.mapValues { it.value.name })

        logger.info("DefiLlamaModule initialized")
    }

    fun getProtocols(): List<DLGeneralProtocol> {
        val currentTime = System.currentTimeMillis() / 1000L

        if (currentTime - lastUpdated > 3600) {
            cachedProtocols = execute(api.getProtocols()).associateBy { it.name } as HashMap
            lastUpdated = currentTime
        }

        return cachedProtocols.values.toList()
    }

    fun getProtocol(protocol: String) = execute(api.getProtocol(protocol))

    fun getPrice(coin: String, searchWidth: String? = null): CoinsToPrice {
        val urlToPass = "https://coins.llama.fi/prices/current/$coin"

        return execute(api.getPrice(urlToPass, searchWidth))
    }

    override fun <T : Any> execute(
        call: Single<T>,
        retryOnRatelimit: Boolean,
        maxRetries: Int?
    ): T {
        try {
            return call.blockingGet()
        } catch (e: HttpException) {
            if (e.code() == 429) {
                return if (retryOnRatelimit) {
                    sleep(1500L)
                    execute(call, false)
                } else {
                    logger.error("Ratelimited while executing call", e)

                    throw e
                }
            } else {
                logger.error("Error while executing call", e)

                throw e
            }
        } catch (e: Exception) {
            logger.error("Error while executing call", e)

            throw e
        }
        // TODO: Implement error handling
    }
}