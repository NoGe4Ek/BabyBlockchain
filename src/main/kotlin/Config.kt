import blockchain.Block
import blockchain.Node

object Config {
    const val INTERVAL_ACTUALIZE_BLOCK = 50
    const val FIRST_PROCESS_PORT = 8000
    const val SECOND_PROCESS_PORT = 8001
    const val THIRD_PROCESS_PORT = 8002
    const val PRIORITY_PROCESS_PORT = FIRST_PROCESS_PORT
    const val SOCKET_HOST = "localhost"
    val GENESIS = Block(
        0,
        "prev",
        "asdfas5df765asd7f6tas76df98a7s9d8f7",
        "data",
        Node.DEFAULT_NONCE,
        false
    )
}