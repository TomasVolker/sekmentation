package numeriko.sekmentation.levelset

import numeriko.sekmentation.visualization.Grid2D
import numeriko.sekmentation.visualization.PanZoom
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

class LevelSet2DProgram(
    val algorithm: LevelSetAlgorithm
): Program() {

    val image get() = algorithm.image
    val phi get() = algorithm.phi

    val buffer by lazy { colorBuffer(image.shape0, image.shape1) }
    val bufferPhi by lazy { colorBuffer(image.shape0, image.shape1) }

    override fun setup() {

        backgroundColor = ColorRGBa.BLUE.shade(0.2)

        extend(PanZoom())
        extend(Grid2D())

        buffer.write(image.normalizeContrast())

        keyboard.keyUp.listen { onKeyEvent(it) }
        keyboard.keyRepeat.listen { onKeyEvent(it) }

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

            image(buffer)

            drawStyle.blendMode = BlendMode.ADD
            bufferPhi.writePhi(phi.elementWise { (it > 0).indicator() })
            image(bufferPhi)

        }

    }

    fun ColorBuffer.writePhi(image: DoubleArray2D) {

        shadow.buffer.rewind()
        image.forEachIndex { i0, i1 ->
            shadow[i0, i1] = ColorRGBa.RED.opacify(image[i0, i1] * 0.1).shade(0.5)
        }
        shadow.upload()

    }

}