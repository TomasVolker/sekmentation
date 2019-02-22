package numeriko.sekmentation

import numeriko.sekmentation.fuzzyregiongrowing.runFuzzyRegionGrowingDemo
import numeriko.sekmentation.levelset.runLevelSetDemo

fun readNumber(requirement: (Int)->Boolean = { true }): Int? {

    while(true) {

        val line = readLine() ?: return null

        line.toIntOrNull()?.let {
            if(requirement(it))
                return it
        }

        println("Invalid number, try again")

    }

}

fun main() {

    println("""
        Sekmentation V1.0

        Insert one of the following:
        1 - Level Set Demo
        2 - Fuzzy Region Growing Demo

    """.trimIndent())

    when(readNumber { it in 1..2 } ?: return) {
        1 -> runLevelSetDemo()
        2 -> runFuzzyRegionGrowingDemo()
    }

}