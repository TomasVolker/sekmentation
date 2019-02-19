package numeriko.sekmentation.visualization

import org.openrndr.Extension
import org.openrndr.Program
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.Drawer
import org.openrndr.math.Matrix44
import org.openrndr.math.Vector2
import org.openrndr.shape.Rectangle
import tomasvolker.numeriko.core.primitives.modulo
import kotlin.math.floor

class Grid2D : Extension {

    override var enabled: Boolean = true

    var deltaX: Double = 100.0
    var deltaY: Double = 100.0

    var color = ColorRGBa.GRAY
    var strokeWeight = 1.0


    operator fun Matrix44.times(rectangle: Rectangle) =
        Rectangle(
            corner = (this * rectangle.corner.vector3().xyz1).xyz.xy,
            width = this.c0r0 * rectangle.width,
            height = this.c1r1 * rectangle.height
        )

    override fun beforeDraw(drawer: Drawer, program: Program) {

        drawer.stroke = color
        drawer.strokeWeight = strokeWeight

        val worldBounds = drawer.view.inversed * drawer.bounds

        val offsetX = worldBounds.x modulo deltaX
        val countX = floor((worldBounds.width - offsetX) / deltaX).toInt() + 2

        drawer.lineStrips(
            List(countX) { i ->
                val x = worldBounds.x - offsetX + (i+1) * deltaX
                listOf(
                    Vector2(x, worldBounds.y),
                    Vector2(x, worldBounds.y + worldBounds.height)
                )
            }
        )


        val offsetY = worldBounds.y modulo deltaY
        val countY = floor((worldBounds.height - offsetY) / deltaY).toInt() + 2

        drawer.lineStrips(
            List(countY) { i ->
                val y = worldBounds.y - offsetY + (i+1) * deltaY
                listOf(
                    Vector2(worldBounds.x, y),
                    Vector2(worldBounds.x + worldBounds.width, y)
                )
            }
        )

    }

}