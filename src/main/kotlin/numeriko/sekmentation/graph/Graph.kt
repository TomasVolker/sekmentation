package numeriko.sekmentation.graph

interface Graph {

    val size: Int

    fun areConnected(from: Int, to: Int): Boolean = to in neighbors(from)

    fun neighbors(nodeIndex: Int): Iterable<Int>

}