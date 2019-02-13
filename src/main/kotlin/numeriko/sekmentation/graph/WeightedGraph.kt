package numeriko.sekmentation.graph

interface WeightedGraph {

    val size: Int

    fun support(nodeIndex: Int): Iterable<Int> = 0 until size

    fun weight(from: Int, to: Int): Double

}

