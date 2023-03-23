package blockchain

import ipc.Message

data class Block(
    val index: Long,
    val prevHash: String,
    val hash: String,
    val data: String,
    val nonce: Long,
    val isActualize: Boolean = false,
) : Message