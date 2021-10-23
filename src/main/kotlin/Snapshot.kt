// Snapshot Class
// Copyright 2021 by Alex Eidt

import java.awt.Color
import java.awt.image.BufferedImage
import java.io.File
import java.io.IOException
import java.util.*
import javax.imageio.ImageIO
import javax.imageio.stream.FileImageOutputStream
import javax.imageio.stream.ImageOutputStream
import javax.swing.JFrame

const val RESOLUTION = 20
const val GIF_SPEED = 1
const val LOOP_CONTINUOUSLY = true

/**
 * Creates a PNG image of the board.
 *
 * @param board the board representing the current state of the Game of Life simulation.
 * @param size the size of the board. Always a size x size square.
 * @throws IOException
 */
@Throws(IOException::class)
fun snapshot(writer: GifSequenceWriter, board: List<Int>, size: Int) {
    val scale = RESOLUTION
    val gridScaled = size * scale
    val bufferedImage = BufferedImage(gridScaled, gridScaled, BufferedImage.TYPE_INT_ARGB)
    val g = bufferedImage.createGraphics()
    g.color = Color.BLACK
    for (coordinate in board) {
        g.fillRect(coordinate % size * scale, coordinate / size * scale, scale, scale)
    }
    writer.writeToSequence(bufferedImage)
}

/**
 * Creates a PNG image of the board.
 *
 * @param board the board representing the current state of the Game of Life simulation.
 * @throws IOException
 */
@Throws(IOException::class)
fun snapshot(board: Board) {
    var file = File("Snapshot.png")
    var index = 1
    while (file.exists()) {
        file = File("Snapshot${index++}.png")
    }
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

/**
 * Converts the information in the temporary "__recording__.golf" file to a series of images
 * and converts those to a GIF.
 *
 * @param frame JFrame to update to show progress.
 * @param size the size of the board. Always a size x size square.
 * @param keepRecording if true, keep the "__recording__.golf" file. Otherwise, delete it.
 * @throws IOException
 */
@Throws(IOException::class)
fun convertToGIF(frame: JFrame, size: Int, keepRecording: Boolean) {
    val recording = File("__recording__.golf")
    val file = Scanner(recording)
    // Create output gif file.
    var recorded = File("Recording.gif")
    var i = 1
    while (recorded.exists()) {
        recorded = File("Recording${i++}.gif")
    }
    val output: ImageOutputStream = FileImageOutputStream(recorded)
    // Create gif using frames.
    val writer = GifSequenceWriter(output, BufferedImage.TYPE_INT_ARGB, GIF_SPEED, LOOP_CONTINUOUSLY)
    frame.title = "Converting to GIF..."
    while (file.hasNextLine()) {
        snapshot(writer, file.nextLine().split(",").map { it.toInt() }, size)
    }
    frame.title = "Conway's Game of Life"
    file.close()
    writer.close()
    output.close()

    if (!keepRecording) recording.delete()
}