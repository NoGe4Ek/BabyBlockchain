package utils

import EventHandler.handleActualizeBlockBroadcast
import EventHandler.handleActualizeBlockReceived
import EventHandler.handleBlockReceived
import EventHandler.handleProduceBlock
import EventHandler.isActualizeBlockBroadcastEvent
import EventHandler.isActualizeBlockReceivedEvent
import EventHandler.isBlockReceivedEvent
import blockchain.Block
import blockchain.Node
import ipc.Process
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import utils.models.ProcessResult
import kotlin.test.assertNotEquals

val PLACEHOLDER_BLOCK = Block(123, "123", "123", "123", 123L, false)

fun getRandomLong(): Long = (0..Long.MAX_VALUE).random()

fun Node.makeStepWithoutGenerating(): Block? {
    var producingBlock: Block? = this.stepProduce()
    while (producingBlock != null) {
        producingBlock = this.stepProduce()
    }
    return this.producingBlock?.copy()
}

fun Node.generateBlock(): Block {
    var producingBlock: Block? = null
    while (producingBlock == null) {
        producingBlock = this.stepProduce()
    }
    return producingBlock
}

fun isProducingStepsValid(vararg blocks: Block?) {
    assertEquals(blocks.contains(null), false)
    for (blockIndex in 0..blocks.size - 2) {
        val block1 = blocks[blockIndex]
        val block2 = blocks[blockIndex + 1]

        assertEquals(block1!!.index, block2!!.index)
        assertEquals(block1.prevHash, block2.prevHash)
        assertEquals(block1.data, block2.data)
        assertTrue(block1.nonce < block2.nonce)
        assertNotEquals(block1.hash, block2.hash)
    }
}

fun isChainValid(vararg blocks: Block?) {
    assertEquals(blocks.contains(null), false)
    for (blockIndex in 0..blocks.size - 2) {
        val block1 = blocks[blockIndex]
        val block2 = blocks[blockIndex + 1]

        assertEquals(block1!!.index + 1, block2!!.index)
        assertEquals(block1.hash, block2.prevHash)
    }
}

fun Process.handleEvent(receivedBlock: Block?, node: Node): ProcessResult {
    val process = this
    var totalReceivedBlocks = 0L
    var totalReceivedActualizeBlocks = 0L
    var totalBroadcastActualizeBlocks = 0L
    var totalProducedBlocks = 0L
    when {
        isBlockReceivedEvent(receivedBlock) ->
            if (handleBlockReceived(receivedBlock!!, node)) totalReceivedBlocks++

        isActualizeBlockReceivedEvent(receivedBlock, process) ->
            if (handleActualizeBlockReceived(receivedBlock!!, node)) totalReceivedActualizeBlocks++

        isActualizeBlockBroadcastEvent(node, process) ->
            if (handleActualizeBlockBroadcast(node, process)) totalBroadcastActualizeBlocks++

        else -> if (handleProduceBlock(node, process)) totalProducedBlocks++
    }
    return ProcessResult(
        totalReceivedBlocks = totalReceivedBlocks,
        totalReceivedActualizeBlocks = totalReceivedActualizeBlocks,
        totalBroadcastActualizeBlocks = totalBroadcastActualizeBlocks,
        totalProducedBlocks = totalProducedBlocks,
    )
}