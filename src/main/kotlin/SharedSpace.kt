import blockchain.Block
import com.google.gson.Gson

object SharedSpace {
    var block: Block? = null
    var lastActualizeIndex = 0L
    val gson = Gson()
}