package numeriko.som

import org.openrndr.Extension
import org.openrndr.MouseEvent
import org.openrndr.Program
import org.openrndr.draw.Drawer
import org.openrndr.math.Matrix44
import org.openrndr.math.Vector2
import org.openrndr.math.Vector4
import org.openrndr.math.transforms.transform
import kotlin.math.exp

private class Camera2D {

    var scrollSpeed: Double = 0.1

    var view = Matrix44.IDENTITY

    fun cameraToWorld(position: Vector2) = (view.inversed * position.xy01).xyz.xy
    fun worldToCamera(position: Vector2) = (view * position.xy01).xyz.xy

    fun mouseDragged(event: MouseEvent) {
        view *= transform { translate(event.dragDisplacement / view[0].x) }
    }

    fun mouseScrolled(event: MouseEvent) {
        val worldPosition = cameraToWorld(event.position)

        view *= transform {
            translate(worldPosition)
            scale(exp(scrollSpeed * event.rotation.y))
            translate(-worldPosition)
        }
    }
}

class PanZoom : Extension {

    override var enabled: Boolean = true

    private val camera = Camera2D()

    override fun setup(program: Program) {

        program.mouse.dragged.listen {
            if (!it.propagationCancelled) {
                camera.mouseDragged(it)
            }
        }

        program.mouse.scrolled.listen {
            if (!it.propagationCancelled) {
                camera.mouseScrolled(it)
            }
        }

    }
    override fun beforeDraw(drawer: Drawer, program: Program) {
        drawer.view = camera.view
    }
}