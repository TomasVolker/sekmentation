package numeriko.sekmentation.visualization

import org.openrndr.Extension
import org.openrndr.Program
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.Drawer
import org.openrndr.draw.isolated
import org.openrndr.math.Matrix44
import org.openrndr.math.Vector2
import kotlin.math.floor

class Grid2D : Extension {

    override var enabled: Boolean = true

    var deltaX: Double = 100.0
    var deltaY: Double = 100.0

    var color = ColorRGBa.GRAY
    var gridWeight = 1.0

    infix fun Double.modulo(other: Double) = ((this % other) + other) % other

    override fun beforeDraw(drawer: Drawer, program: Program) {

        drawer.run {

            stroke = color
            strokeWeight = gridWeight

            val gridXDelta = view.c0r0 * deltaX
            val gridXOffset = view.c3r0 modulo gridXDelta
            val countX = floor(drawer.width / gridXDelta).toInt() + 1

            val gridYDelta = view.c1r1 * deltaY
            val gridYOffset = view.c3r1 modulo gridYDelta
            val countY = floor(drawer.width / gridYDelta).toInt() + 1

            isolated {
                model = Matrix44.IDENTITY
                view = Matrix44.IDENTITY
                ortho()

                lineStrips(
                    List(countX) { i ->
                        val x = gridXOffset + i * gridXDelta
                        listOf(
                            Vector2(x, 0.0),
                            Vector2(x, height.toDouble())
                        )
                    }
                )

                lineStrips(
                    List(countY) { i ->
                        val y = gridYOffset + i * gridYDelta
                        listOf(
                            Vector2(0.0, y),
                            Vector2(width.toDouble(), y)
                        )
                    }
                )

            }

        }

    }

}