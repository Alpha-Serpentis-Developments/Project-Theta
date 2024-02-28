package dev.alphaserpentis.bots.zeroextheta.exceptions

import org.web3j.protocol.core.Response

/**
 * The call attempted to run, but resulted in a revert
 *
 * This is NOT the same as an [java.io.IOException] if both providers fail to respond
 */
class EthCallException(
    val error: Response.Error
) : RuntimeException(error.message)