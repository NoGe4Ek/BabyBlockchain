package ipc

import SharedSpace
import blockchain.Block
import com.google.gson.JsonSyntaxException
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.ServerSocket
import java.net.Socket
import java.net.SocketException

data class Process(val name: String, val port: Int) {
    private lateinit var serverSocket: ServerSocket
    private lateinit var clientSocket: Socket
    private val clientSockets = mutableListOf<Socket>()
    private var serverThread: Thread? = null
    private var clientThread: Thread? = null
    private val clientThreads = mutableListOf<Thread>()

    // Save all out-coming peer sockets
    val peerSockets = mutableMapOf<Int, Socket>()

    // Start the process
    fun start() {
        // Create a server socket to listen for incoming connections
        serverSocket = ServerSocket(port)
        // Thread to listen for incoming connections
        serverThread = Thread {
            // Check thread is not interrupted yet
            while (!Thread.currentThread().isInterrupted) {
                try {
                    Thread.sleep(100)
                    // Wait for a client to connect
                    clientSocket = serverSocket.accept()
                    // Save all client sockets
                    clientSockets.add(clientSocket)
                    // Thread to handle the client connection
                    clientThread = Thread {
                        val input = BufferedReader(InputStreamReader(clientSocket.getInputStream()))
                        // Check thread is not interrupted yet
                        while (!Thread.currentThread().isInterrupted) {
                            try {
                                Thread.sleep(100)
                                // Read messages from the client
                                val message = input.readLine()
                                try {
                                    val block = SharedSpace.gson.fromJson(message, Block::class.java) ?: break
                                    SharedSpace.block = block
                                    if (block.isActualize)
                                        println("$name actualize block: $block")
                                    else
                                        println("$name received block: $block")
                                } catch (e: JsonSyntaxException) {
                                    println("$name can't parse message: $message")
                                }
                            } catch (e: SocketException) {
                                Thread.currentThread().interrupt()
                                println("Client socket was closed")
                            } catch (e: InterruptedException) {
                                println("${Thread.currentThread().name} was interrupted")
                            }
                        }
                    }
                    clientThread?.let {
                        // Start a thread to handle the client connection
                        it.start()
                        // Save all client threads
                        clientThreads.add(it)
                    }
                } catch (e: SocketException) {
                    Thread.currentThread().interrupt()
                    println("Server socket was closed")
                } catch (e: InterruptedException) {
                    println("${Thread.currentThread().name} was interrupted")
                }
            }
        }
        serverThread?.start()
    }

    // Stop the process
    fun stop() {
        // Interrupt threads just in case
        clientThreads.forEach { it.interrupt() }
        clientThreads.clear()
        serverThread?.interrupt()

        // Close all sockets
        clientSockets.forEach { it.close() }
        serverSocket.close()
    }
}