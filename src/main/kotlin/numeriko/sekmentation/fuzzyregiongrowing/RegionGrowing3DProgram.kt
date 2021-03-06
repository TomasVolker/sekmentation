package numeriko.sekmentation.fuzzyregiongrowing

import com.github.tomasvolker.parallel.mapParallel
import numeriko.sekmentation.Resources
import numeriko.sekmentation.levelset.normalizeContrast
import numeriko.sekmentation.visualization.FPSDisplay
import org.openrndr.KEY_SPACEBAR
import org.openrndr.KeyEvent
import org.openrndr.Program
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.*
import org.openrndr.extensions.Debug3D
import org.openrndr.extensions.Screenshots
import org.openrndr.math.Matrix44
import org.openrndr.math.Vector3
import org.openrndr.math.transforms.rotateX
import tomasvolker.numeriko.core.interfaces.array2d.double.DoubleArray2D
import tomasvolker.numeriko.core.interfaces.array2d.generic.forEachIndex

val KEY_PLUS = 334
val KEY_MINUS = 333

class RegionGrowing3DProgram(
    val algorithm: FuzzyConnectedness,
    val verticalFactor: Double = 1.0
): Program() {

    val image get() = algorithm.image
    val connectednessMap get() = algorithm.connectivityMap

    val font by lazy { Resources.fontImageMap("IBMPlexMono-Bold.ttf", 16.0) }

    val buffer by lazy { colorBuffer(image.shape0, image.shape1) }

    var stepsPerFrame = 1
    enum class State { IDLE, RUNNING }
    var state = State.IDLE

    override fun setup() {

        backgroundColor = ColorRGBa.BLUE.shade(0.2)

        extend(FPSDisplay())
        extend(Screenshots()) {
            key = 'S'.toInt()
        }
        extend(Debug3D())

        extend { update() }

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
            KEY_SPACEBAR -> toggleState()
            KEY_PLUS -> stepsPerFrame++
            KEY_MINUS -> stepsPerFrame--
        }
    }

    fun toggleState() {
        state = when(state) {
            State.IDLE -> State.RUNNING
            State.RUNNING -> State.IDLE
        }
    }

    fun update() {
        when(state) {
            State.IDLE -> {}
            State.RUNNING -> {
                repeat(stepsPerFrame) {
                    algorithm.step()
                }
            }
        }

    }

    override fun draw() {

        drawer.run {

            background(ColorRGBa.BLUE.shade(0.2))

            depthWrite = true
            depthTestPass = DepthTestPass.LESS_OR_EQUAL

            drawStyle.quality = DrawQuality.PERFORMANCE
            model = rotateX(-90.0)

            drawMap()

            drawImage()

            drawStep()

        }

    }

    fun Drawer.drawMap() {

        stroke = ColorRGBa.RED

        lineStrips(
            (0 until image.shape0).mapParallel(50) { x ->
                (0 until image.shape1).map { y ->
                    Vector3(x.toDouble(), y.toDouble(), 0.1 + connectednessMap[x, y] * verticalFactor)
                }
            }
        )

        lineStrips(
            (0 until image.shape1).mapParallel(50) { y ->
                (0 until image.shape0).map { x ->
                    Vector3(x.toDouble(), y.toDouble(), 0.1 + connectednessMap[x, y] * verticalFactor)
                }
            }
        )

    }

    private fun Drawer.drawImage() {
        /*
        drawStyle.blendMode = BlendMode.MULTIPLY
        fill = ColorRGBa.WHITE.opacify(0.2)
        rectangle(buffer.bounds)
        drawStyle.blendMode = BlendMode.ADD
        */
        image(buffer)
    }

    private fun Drawer.drawStep() {

        isolated {
            ortho()
            view = Matrix44.IDENTITY
            model = Matrix44.IDENTITY

            fontMap = font

            fill = ColorRGBa.WHITE

            text("$state Steps per frame: $stepsPerFrame finished: ${algorithm.finished()}", x = 0.0, y = 16.0)

        }

    }

}