package dev.alphaserpentis.bots.zeroextheta.data.api.alchemy

import dev.alphaserpentis.bots.zeroextheta.data.api.alchemy.request.AssetTransfersBody
import dev.alphaserpentis.bots.zeroextheta.data.api.alchemy.request.TokenMetadataBody
import dev.alphaserpentis.bots.zeroextheta.data.api.alchemy.response.GetAssetTransfers
import dev.alphaserpentis.bots.zeroextheta.data.api.alchemy.response.GetTokenMetadata
import io.reactivex.rxjava3.core.Single
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Path

interface AlchemyEndpoints {
    @Headers("Content-Type: application/json")
    @POST("{apiKey}")
    fun getAssetTransfers(
        @Path("apiKey") apiKey: String,
        @Body body: AssetTransfersBody
    ): Single<GetAssetTransfers>

    @Headers("Content-Type: application/json")
    @POST("{apiKey}")
    fun getTokenMetadata(
        @Path("apiKey") apiKey: String,
        @Body body: TokenMetadataBody
    ): Single<GetTokenMetadata>
}