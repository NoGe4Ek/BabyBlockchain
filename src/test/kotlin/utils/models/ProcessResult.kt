package utils.models

import Config
import org.junit.jupiter.api.Assertions.assertTrue

data class ProcessResult(
    val totalReceivedBlocks: Long,
    val totalReceivedActualizeBlocks: Long,
    val totalBroadcastActualizeBlocks: Long,
    val totalProducedBlocks: Long
)

fun ProcessResult.merge(processResult: ProcessResult): ProcessResult = ProcessResult(
    totalReceivedBlocks = this.totalReceivedBlocks + processResult.totalReceivedBlocks,
    totalReceivedActualizeBlocks = this.totalReceivedActualizeBlocks + processResult.totalReceivedActualizeBlocks,
    totalBroadcastActualizeBlocks = this.totalBroadcastActualizeBlocks + processResult.totalBroadcastActualizeBlocks,
    totalProducedBlocks = this.totalProducedBlocks + processResult.totalProducedBlocks,
)

fun ProcessResult.isActualizeBlocksWasBroadcasted() {
    val totalBlocks = this.totalProducedBlocks + this.totalReceivedBlocks
    assertTrue(this.totalReceivedActualizeBlocks == 0L)
    if (totalBlocks > Config.INTERVAL_ACTUALIZE_BLOCK)
        assertTrue(this.totalBroadcastActualizeBlocks > 0)
    else
        assertTrue(this.totalBroadcastActualizeBlocks == 0L)
}

fun ProcessResult.isActualizeBlocksWasReceived() {
    val totalBlocks = this.totalProducedBlocks + this.totalReceivedBlocks
    assertTrue(this.totalBroadcastActualizeBlocks == 0L)
    if (totalBlocks > Config.INTERVAL_ACTUALIZE_BLOCK)
        assertTrue(this.totalReceivedActualizeBlocks > 0)
    else
        assertTrue(this.totalReceivedActualizeBlocks == 0L)
}