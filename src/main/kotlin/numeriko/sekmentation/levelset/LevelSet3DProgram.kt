package numeriko.sekmentation.levelset

import numeriko.sekmentation.visualization.write
import org.openrndr.KEY_SPACEBAR
import org.openrndr.KeyEvent
import org.openrndr.Program
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.*
import org.openrndr.extensions.Debug3D
import org.openrndr.math.Vector3
import org.openrndr.math.transforms.rotateX
import tomasvolker.numeriko.core.interfaces.array2d.double.DoubleArray2D
import tomasvolker.numeriko.core.interfaces.array2d.double.elementWise
import tomasvolker.numeriko.core.interfaces.array2d.generic.forEachIndex
import tomasvolker.numeriko.core.primitives.indicator

interface LevelSetAlgorithm {

    val image: DoubleArray2D
    val phi: DoubleArray2D

    val step: Int
    fun step()
    val finished: Boolean

}

class LevelSet3DProgram(
    val algorithm: LevelSetAlgorithm,
    val verticalFactor: Double = 1.0
): Program() {

    val image get() = algorithm.image
    val phi get() = algorithm.phi

    val buffer by lazy { colorBuffer(image.shape0, image.shape1) }
    val bufferPhi by lazy { colorBuffer(image.shape0, image.shape1) }

    override fun setup() {

        backgroundColor = ColorRGBa.BLUE.shade(0.2)

        extend(Debug3D())

        buffer.writeImage(image.normalizeContrast())

        keyboard.keyUp.listen { onKeyEvent(it) }
        keyboard.keyRepeat.listen { onKeyEvent(it) }

    }

    fun ColorBuffer.writeImage(image: DoubleArray2D) {

        shadow.buffer.rewind()
        image.forEachIndex { i0, i1 ->
            shadow[i0, i1] = ColorRGBa.WHITE.shade(image[i0, i1])
        }
        shadow.upload()

    }

    fun onKeyEvent(event: KeyEvent) {
        when(event.key) {
            KEY_SPACEBAR -> update()
        }
    }

    fun update() {
        algorithm.step()
        println("step: ${algorithm.step}")
    }

    override fun draw() {

        drawer.run {

            background(ColorRGBa.BLUE.shade(0.2))

            depthWrite = true
            depthTestPass = DepthTestPass.LESS_OR_EQUAL

            drawStyle.quality = DrawQuality.PERFORMANCE
            model = rotateX(-90.0)
/*
            isolated {
                bufferPhi.write(phi.elementWise { (it > 0).indicator() })
                translate(0.0, 0.0, -image.shape0.toDouble())
                image(bufferPhi)
            }
*/
            drawPhi()

            drawStyle.blendMode = BlendMode.MULTIPLY
            fill = ColorRGBa.WHITE.opacify(0.2)
            rectangle(buffer.bounds)
            drawStyle.blendMode = BlendMode.ADD
            image(buffer)

        }

    }

    fun Drawer.drawPhi() {

        stroke = ColorRGBa.RED

        lineStrips(
            (0 until image.shape0).map { x ->
                (0 until image.shape1).map { y ->
                    Vector3(x.toDouble(), y.toDouble(), phi[x, y] * verticalFactor)
                }
            }
        )

        lineStrips(
            (0 until image.shape1).map { y ->
                (0 until image.shape0).map { x ->
                    Vector3(x.toDouble(), y.toDouble(), phi[x, y] * verticalFactor)
                }
            }
        )

    }

}