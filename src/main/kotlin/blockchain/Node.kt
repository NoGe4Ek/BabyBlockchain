package blockchain

import Config.GENESIS
import java.security.MessageDigest

class Node {
    companion object {
        const val DATA_LENGTH = 256
        const val DEFAULT_NONCE = 0L
    }

    private val genesis = GENESIS
    private var lastBlock: Block = genesis
    fun getLastBlock(): Block = lastBlock
    var producingBlock: Block? = null

    fun stepProduce(): Block? {
        lateinit var hash: String
        if (producingBlock == null) {
            val index = lastBlock.index + 1
            val prevHash = lastBlock.hash
            val data = getData()
            val nonce = DEFAULT_NONCE
            hash = getHash(index, prevHash, data, nonce)
            producingBlock = Block(
                index = lastBlock.index + 1,
                prevHash = lastBlock.hash,
                data = getData(),
                nonce = DEFAULT_NONCE,
                hash = hash
            )
        } else {
            producingBlock?.let {
                hash = getHash(it.index, it.prevHash, it.data, it.nonce + 1)
                producingBlock = it.copy(hash = hash, nonce = it.nonce + 1)
            }
        }
        return if (hash.isValidHash())
            producingBlock?.also { lastBlock = it; producingBlock = null }
        else null
    }

    fun isBlockValid(block: Block): Boolean = block.hash.isValidHash() && block.index == lastBlock.index + 1

    fun updateLastBlock(block: Block) {
        lastBlock = block
        producingBlock = null
    }

    private fun getHash(
        index: Long,
        prevHash: String,
        data: String,
        nonce: Long
    ): String = (index.toString() + prevHash + data + nonce.toString()).toSha256()

    private fun getData(): String = getRandomString(DATA_LENGTH)

    private fun getRandomString(length: Int): String {
        val charset = ('a'..'z') + ('A'..'Z') + ('0'..'9')
        return (1..length)
            .map { charset.random() }
            .joinToString("")
    }

    private fun String.isValidHash(): Boolean =
        this.takeLast(4) == "0000"

    private fun String.toSha256(): String {
        val bytes = this.toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return digest.fold("") { str, it -> str + "%02x".format(it) }
    }
}