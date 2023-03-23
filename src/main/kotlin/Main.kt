import EventHandler.handleActualizeBlockBroadcast
import EventHandler.handleActualizeBlockReceived
import EventHandler.handleBlockReceived
import EventHandler.handleProduceBlock
import EventHandler.isActualizeBlockBroadcastEvent
import EventHandler.isActualizeBlockReceivedEvent
import EventHandler.isBlockReceivedEvent
import blockchain.Node
import ipc.Process
import utils.getProcessName

fun main(args: Array<String>) {
    val port = args[0].toInt()
    val process = Process(getProcessName(port), port)
    process.start()
    val node = Node()

    while (true) {
        val receivedBlock = SharedSpace.block?.copy()
        SharedSpace.block = null

        when {
            isBlockReceivedEvent(receivedBlock) -> handleBlockReceived(receivedBlock!!, node)
            isActualizeBlockReceivedEvent(receivedBlock, process) -> handleActualizeBlockReceived(receivedBlock!!, node)
            isActualizeBlockBroadcastEvent(node, process) -> handleActualizeBlockBroadcast(node, process)
            else -> handleProduceBlock(node, process)
        }
    }
    process.stop()
}