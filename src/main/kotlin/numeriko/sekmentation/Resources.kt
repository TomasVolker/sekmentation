package numeriko.sekmentation

import numeriko.sekmentation.io.loadImage
import org.openrndr.draw.FontImageMap
import org.openrndr.resourceUrl
import tomasvolker.numeriko.core.interfaces.array2d.double.DoubleArray2D

object Resources {

    fun url(name: String) = resourceUrl(name, Resources::class.java)

    fun fontImageMap(
        name: String,
        size: Double,
        contentScale: Double = 1.0
    ) = FontImageMap.fromUrl(url(name), size, contentScale)

    fun image(name: String): DoubleArray2D = loadImage(
        javaClass.getResource(name)
    )

}