// Utils
// Copyright 2022 by Alex Eidt

import java.awt.Dimension
import java.awt.Image
import java.awt.Toolkit
import java.io.File
import java.net.URL
import java.nio.file.Paths
import java.util.*
import javax.swing.ImageIcon
import javax.swing.JFrame
import javax.swing.JScrollPane
import kotlin.math.ceil
import kotlin.math.sqrt

const val GOLFR_EXTENSION = "golfr"
const val GOLF_EXTENSION = "golf"
const val ICON_SIZE = 40

/**
 * Resizes the given ImageIcon. This is used to scale button icons.
 *
 * @param icon  Image Icon for button.
 * @return scaled Image Icon.
 */
fun sizeIcon(filename: URL?): ImageIcon {
    val icon = ImageIcon(Toolkit.getDefaultToolkit().getImage(filename))
    return ImageIcon(icon.image.getScaledInstance(ICON_SIZE, ICON_SIZE, Image.SCALE_DEFAULT))
}

/**
 * Gets the board state from a ".golf" file.
 * If there is an error with the file, the first value in the returned Pair
 * is 0.
 *
 * @param file  The ".golf" file to read.
 * @return      Pair with size of board and set of coordinates representing cell locations.
 */
fun getData(file: File): Pair<Int, HashSet<Int>> {
    val scanner = Scanner(file)
    var size = 0
    val set: HashSet<Int> = HashSet()
    if (scanner.hasNextLine()) {
        val gridSize = scanner.nextLine().toIntOrNull()
        if (gridSize != null && assertGrid(gridSize)) {
            size = gridSize
            while (scanner.hasNextLine()) {
                scanner.nextLine().toIntOrNull()?.let { set.add(it) }
            }
        }
    }
    if (size % 100 != 0)
        return Pair(0, set)
    return Pair(size, set)
}

/**
 * Joins the given list of files together to their full file path.
 *
 * @param files     List of files/directories in the file path.
 * @return          The full file path to the given files.
 */
fun joinPath(vararg files: String): String? {
    val currentDir = Paths.get(System.getProperty("user.dir")).toString()
    var filePath = Paths.get(currentDir, *files).toString()
    if (files.isNotEmpty() && !files[files.size - 1].matches(Regex("\\.[A-Za-z\\d]+$"))) {
        filePath = Paths.get(filePath, "x").toString()
        return filePath.substring(0, filePath.length - 1)
    }
    return filePath
}

/**
 * Finds a suitable size for the simulator playground based on
 * the screen dimensions.
 *
 * @param grid  The grid value of the board.
 * @return      Dimension with the playground size.
 */
fun dimension(grid: Int): Dimension {
    var original = grid * 4.0
    val width = Toolkit.getDefaultToolkit().screenSize.width
    val height = Toolkit.getDefaultToolkit().screenSize.height
    val max = (if (width > height) width else height).toDouble()
    while (original <= max) {
        original *= 1.5
    }
    // Round "original" to the nearest multiple of "grid" for zooming.
    val size = ceil(original / grid).toInt() * grid
    return Dimension(size, size)
}

/**
 * Creates a file with the given "filename" and "extension".
 * If a file with "filename" already exists, the name will be changed
 * to "filename{x}.extension" where x is a number.
 */
fun createFile(filename: String, extension: String): File {
    var file = File("${filename}.${extension}")
    var index = 1
    while (file.exists()) {
        file = File("${filename}${index++}.${extension}")
    }
    return file
}

/**
 * Asserts that the given value for "GRID" is valid.
 *
 * @param grid  The grid value to test.
 * @return      true if "grid" is valid, otherwise false.
 */
fun assertGrid(grid: Int): Boolean {
    // GRID must be a multiple of 100
    if (grid % 100 != 0) {
        println("GRID must be divisible by 100")
        return false
    }
    // GRID must be between 100 and sqrt(2^31-1) since the simulation board will always
    // be a GRID x GRID square.
    val max = sqrt(Integer.MAX_VALUE.toDouble()).toInt()
    if (grid !in 100 until max) {
        println("GRID must be between 100 and $max")
        return false
    }
    return true
}

/**
 * Updates the scroll frame to adjust to the size of the simulation board when the user
 * zooms in/out.
 *
 * @param frame         Main Frame the simulation runs on.
 * @param scrollFrame   The scrollable frame the simulation playground is in.
 * @param panel         The simulation playground.
 * @return              The updated scroll frame.
 */
fun updateScrollFrame(frame: JFrame, scrollFrame: JScrollPane?, panel: DrawPanel): JScrollPane {
    if (scrollFrame != null) frame.remove(scrollFrame)
    val scrollFrame = JScrollPane(panel)
    scrollFrame.verticalScrollBar.unitIncrement = 16
    scrollFrame.horizontalScrollBar.unitIncrement = 16
    frame.add(scrollFrame)
    frame.revalidate()
    return scrollFrame
}