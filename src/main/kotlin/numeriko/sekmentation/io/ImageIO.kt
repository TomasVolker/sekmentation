package numeriko.sekmentation.io

import tomasvolker.numeriko.core.interfaces.array2d.double.DoubleArray2D
import tomasvolker.numeriko.core.interfaces.factory.doubleArray2D
import java.io.File
import javax.imageio.ImageIO

val Int.gray get(): Double {
    val r = (this and 0x00FF0000) shr 16
    val g = (this and 0x0000FF00) shr 8
    val b = (this and 0x000000FF)
    return (r + g + b) / (255.0 * 3.0)
}

fun loadImage(path: String): DoubleArray2D {

    val image = ImageIO.read(File(path))
    return doubleArray2D(image.width, image.height) { x, y ->
        image.getRGB(x, y).gray * 255.0
    }
}