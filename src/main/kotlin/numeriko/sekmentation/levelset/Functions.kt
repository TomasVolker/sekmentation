package numeriko.sekmentation.levelset

import tomasvolker.numeriko.core.primitives.squared
import kotlin.math.PI
import kotlin.math.atan

fun membership(x: Double, epsilon: Double, set: Int): Double =
    when(set) {
        0 -> softHeaviside(x, epsilon)
        1 -> 1 - softHeaviside(x, epsilon)
        else -> error("Illegal set")
    }

fun softHeaviside(x: Double, epsilon: Double): Double =
        0.5 * (1 + atan(x / epsilon) * 2 / PI )

fun softDelta(x: Double, epsilon: Double): Double =
        epsilon / (PI * (epsilon.squared() + x.squared()))

