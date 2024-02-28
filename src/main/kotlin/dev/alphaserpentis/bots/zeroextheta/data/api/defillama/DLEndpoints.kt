package dev.alphaserpentis.bots.zeroextheta.data.api.defillama

import dev.alphaserpentis.bots.zeroextheta.data.api.defillama.response.CoinsToPrice
import dev.alphaserpentis.bots.zeroextheta.data.api.defillama.response.DLGeneralProtocol
import dev.alphaserpentis.bots.zeroextheta.data.api.defillama.response.DLSpecificProtocol
import io.reactivex.rxjava3.core.Single
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Path
import retrofit2.http.Query

interface DLEndpoints {
    @Headers("Content-Type: application/json")
    @GET("/protocols")
    fun getProtocols(): Single<List<DLGeneralProtocol>>

    @Headers("Content-Type: application/json")
    @GET("/protocol/{protocol}")
    fun getProtocol(@Path("protocol") protocol: String): Single<DLSpecificProtocol>

    @Headers("Content-Type: application/json")
    @GET("{url}")
    fun getPrice(
        @Path(value = "url", encoded = true) coin: String,
        @Query("searchWidth") searchWidth: String? = null
    ): Single<CoinsToPrice>
}