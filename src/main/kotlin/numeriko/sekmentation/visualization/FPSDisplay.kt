package numeriko.sekmentation.visualization

import numeriko.sekmentation.Resources
import org.openrndr.Extension
import org.openrndr.Program
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.Drawer
import org.openrndr.draw.FontImageMap
import org.openrndr.draw.isolated
import org.openrndr.math.Matrix44

class FPSDisplay : Extension {
    override var enabled: Boolean = true

    var lastTime: Double = 0.0

    val font: FontImageMap = Resources.fontImageMap("IBMPlexMono-Bold.ttf", 16.0)

    override fun setup(program: Program) {
        lastTime = program.seconds
    }

    override fun afterDraw(drawer: Drawer, program: Program) {

        val now = program.seconds

        drawer.isolated {
            drawer.fontMap = font

            drawer.view = Matrix44.IDENTITY
            drawer.model = Matrix44.IDENTITY
            drawer.ortho()

            fill = ColorRGBa.WHITE
            drawer.translate(10.0, height.toDouble() - 10.0)
            drawer.text("fps: %.2f".format(1.0 / (now - lastTime)))
        }

        lastTime = now
    }
}