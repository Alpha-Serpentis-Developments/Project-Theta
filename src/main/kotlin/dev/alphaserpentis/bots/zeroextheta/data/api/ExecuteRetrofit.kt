package dev.alphaserpentis.bots.zeroextheta.data.api

import io.reactivex.rxjava3.core.Single

/**
 * Interface used to execute Retrofit calls
 */
interface ExecuteRetrofit {
    /**
     * Executes a Retrofit call
     * @param call The call to execute
     * @param retryOnRatelimit Whether to retry the call if it fails due to a ratelimit
     * @param maxRetries The maximum number of retries to attempt
     * @return The result of the call
     */
    fun <T : Any> execute(call: Single<T>, retryOnRatelimit: Boolean = false, maxRetries: Int? = null): T {
        return call.blockingGet()
    }
}