package numeriko.sekmentation.levelset

import numeriko.sekmentation.visualization.write
import org.openrndr.KEY_SPACEBAR
import org.openrndr.KeyEvent
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.DepthTestPass
import org.openrndr.draw.DrawQuality
import org.openrndr.draw.colorBuffer
import org.openrndr.extensions.Debug3D
import org.openrndr.math.Vector3
import org.openrndr.math.transforms.rotateX
import tomasvolker.numeriko.core.interfaces.array2d.double.DoubleArray2D
import tomasvolker.numeriko.core.interfaces.array2d.double.elementWise
import tomasvolker.numeriko.core.interfaces.array2d.generic.forEachIndex
import tomasvolker.numeriko.core.interfaces.factory.doubleArray2D
import tomasvolker.numeriko.core.primitives.indicator
import tomasvolker.numeriko.core.primitives.squared
import kotlin.math.absoluteValue
import kotlin.math.hypot
import kotlin.math.tanh

fun main() {

    val image = doubleArray2D(100, 100) { i0, i1 ->
        tanh((i1 - 50) / 5.0)
    } * 255

    val levelSet = LevelSet(
        image = image,
        deltaT = 1e-1
    )

    application {
        configure {
            width = 1000
            height = 800
            windowResizable = true
        }
        program {

            backgroundColor = ColorRGBa.BLUE.shade(0.2)

            extend(Debug3D())

            val buffer = colorBuffer(image.shape0, image.shape1)
            val bufferPhi = colorBuffer(image.shape0, image.shape1)

            buffer.write(image.normalizeContrast())

            val update = { event: KeyEvent ->
                if (event.key == KEY_SPACEBAR) {
                    levelSet.step()
                    println("step: ${levelSet.step}")
                }
            }

            keyboard.keyUp.listen(update)
            keyboard.keyRepeat.listen(update)

            extend {

                drawer.background(ColorRGBa.BLUE.shade(0.2))

                drawer.depthWrite = true
                drawer.depthTestPass = DepthTestPass.LESS_OR_EQUAL

                drawer.drawStyle.quality = DrawQuality.PERFORMANCE
                drawer.model = rotateX(-90.0)

                drawer.image(buffer)

                bufferPhi.write(levelSet.phi.elementWise { (it > 0).indicator() })
                drawer.image(bufferPhi, image.shape0.toDouble(), 0.0)

                bufferPhi.write(levelSet.force.normalizeContrast())
                drawer.image(bufferPhi, image.shape0.toDouble(), image.shape1.toDouble())

                drawer.stroke = ColorRGBa.RED

                drawer.lineStrips(
                    (0 until levelSet.width).map { x ->
                        (0 until levelSet.height).map { y ->
                            Vector3(x.toDouble(), y.toDouble(), levelSet.phi[x, y])
                        }
                    }
                )

                drawer.lineStrips(
                    (0 until levelSet.height).map { y ->
                        (0 until levelSet.width).map { x ->
                            Vector3(x.toDouble(), y.toDouble(), levelSet.phi[x, y])
                        }
                    }
                )

            }
        }
    }

}

class LevelSet(
    val image: DoubleArray2D,
    val deltaT: Double,
    val v: Double = 1.0
) {

    var step: Int = 0
        private set

    val width = image.shape0
    val height = image.shape1

    val force: DoubleArray2D
    val forceGradient: Pair<DoubleArray2D, DoubleArray2D>

    init {

        val (gradientX, gradientY) = image.computeGradients()

        force = doubleArray2D(width, height) { x, y ->
            1.0 / (1.0  + gradientX[x, y].squared() + gradientY[x, y].squared())
        }
        forceGradient = force.computeGradients()

    }

    val phi = doubleArray2D(width, height) { x, y ->
        if (hypot(x - width / 2.0, y - height / 2.0 - 20) < 10.0) 1.0 else -1.0
    }.asMutable()

    fun step() {

        val (gradient0, gradient1) = phi.computeGradients()

        val gradientNorm = elementWise(gradient0, gradient1) { x, y ->
            hypot(x, y)
        }

        val gradientDir0 = gradient0 / (gradientNorm + 1e-8)
        val gradientDir1 = gradient1 / (gradientNorm + 1e-8)

        val laplacian = phi.computeSecondD0() + phi.computeSecondD1()
        //val curvature = 2 * laplacian / (gradientNorm + 1e-8)

        val curvature = gradientDir0.computeGradient0() + gradientDir1.computeGradient1()

        phi.forEachIndex { x, y ->
            phi[x, y] += deltaT * (
                    gradientNorm[x, y] * (curvature[x, y] + force[x, y] * v) +
                    if(phi[x, y].absoluteValue > 1.1) -phi[x, y] else 0.0
            )
        }

        step++
    }

}