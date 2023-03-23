import Config.INTERVAL_ACTUALIZE_BLOCK
import blockchain.Block
import blockchain.Node
import ipc.Process
import ipc.broadcastMessage
import ipc.isPriorityProcess

object EventHandler {
    fun isBlockReceivedEvent(receivedBlock: Block?): Boolean = receivedBlock != null && !receivedBlock.isActualize
    fun isActualizeBlockReceivedEvent(receivedBlock: Block?, process: Process): Boolean =
        receivedBlock != null && isPriorityProcess(process).not()

    fun isActualizeBlockBroadcastEvent(node: Node, process: Process): Boolean =
        node.getLastBlock().index % INTERVAL_ACTUALIZE_BLOCK == 0L && SharedSpace.lastActualizeIndex != node.getLastBlock().index && isPriorityProcess(
            process
        )

    fun handleBlockReceived(receivedBlock: Block, node: Node): Boolean {
        if (node.isBlockValid(receivedBlock)) {
            node.updateLastBlock(receivedBlock)
            return true
        }
        return false
    }

    fun handleActualizeBlockReceived(receivedBlock: Block, node: Node): Boolean {
        node.updateLastBlock(receivedBlock)
        return true
    }

    fun handleActualizeBlockBroadcast(node: Node, process: Process): Boolean {
        val blockToActualize = node.getLastBlock().copy(isActualize = true)
        SharedSpace.lastActualizeIndex = blockToActualize.index
        process.broadcastMessage(blockToActualize)
        return true
    }

    fun handleProduceBlock(node: Node, process: Process): Boolean {
        val producedBlock = node.stepProduce()
        if (producedBlock != null) {
            process.broadcastMessage(producedBlock)
            return true
        }
        return false
    }
}