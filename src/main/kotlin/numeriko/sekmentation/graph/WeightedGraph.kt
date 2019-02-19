package numeriko.sekmentation.graph

interface WeightedGraph {

    val size: Int

    fun support(node: Int): Iterable<Int> = 0 until size

    fun weight(from: Int, to: Int): Double

    fun forEachOnSupport(node: Int, block: (Int)->Unit) {
        support(node).forEach(block)
    }

}

