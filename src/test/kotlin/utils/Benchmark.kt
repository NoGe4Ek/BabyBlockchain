package utils

object Benchmark {
    fun makePerformanceAssessment(steps: Int, nodesCount: Int, totalProducedBlocks: Int) =
        println(getCanonicalPerformance(steps, nodesCount) * 100 / totalProducedBlocks)

    private fun getCanonicalPerformance(steps: Int, nodesCount: Int): Int =
        (steps * HASH_GENERATING_PROBABILITY).toInt() * nodesCount

    private const val HASH_GENERATING_PROBABILITY = 0.0000152587890625
}