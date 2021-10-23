// Snapshot Class
// Copyright 2021 by Alex Eidt

import java.awt.Color
import java.awt.image.BufferedImage
import java.io.File
import java.io.IOException
import javax.imageio.ImageIO

const val RESOLUTION = 20

/**
 * Creates a PNG image of the board.
 *
 * @param board the board representing the current state of the Game of Life simulation.
 * @throws IOException
 */
@Throws(IOException::class)
fun snapshot(board: Board, file: File) {
    val scale = RESOLUTION
    val gridScaled = board.size * scale
    val bufferedImage = BufferedImage(gridScaled, gridScaled, BufferedImage.TYPE_INT_ARGB)
    val g = bufferedImage.createGraphics()
    g.color = Color.BLACK
    for (coordinate in board.coordinates) {
        g.fillRect(coordinate % board.size * scale, coordinate / board.size * scale, scale, scale)
    }
    ImageIO.write(bufferedImage, "PNG", file)
}