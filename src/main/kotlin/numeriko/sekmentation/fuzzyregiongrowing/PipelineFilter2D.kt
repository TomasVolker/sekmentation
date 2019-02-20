package numeriko.sekmentation.fuzzyregiongrowing

import tomasvolker.numeriko.core.interfaces.array2d.double.DoubleArray2D
import tomasvolker.numeriko.core.interfaces.array2d.double.MutableDoubleArray2D
import tomasvolker.numeriko.core.interfaces.factory.doubleZeros

interface Filter<T> {
    fun filter(input: T): T
}

interface PipelineFilter2D: Filter<DoubleArray2D> {
    override fun filter(input: DoubleArray2D): DoubleArray2D {
        val destination = doubleZeros(input.shape).as2D().asMutable()
        filter(input, destination)
        return destination
    }
    fun filter(input: DoubleArray2D, destination: MutableDoubleArray2D): Unit
}