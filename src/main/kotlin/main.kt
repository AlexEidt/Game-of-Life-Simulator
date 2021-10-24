// Game of Life Simulator
// Copyright 2021 by Alex Eidt

import java.awt.*
import java.awt.event.ActionEvent
import java.awt.event.KeyEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.io.File
import java.nio.file.Paths
import java.util.*
import javax.swing.*
import kotlin.collections.HashSet
import kotlin.math.ceil
import kotlin.math.sqrt

var GRID = 200 // Length of one side of Game of Life Grid.
var RECORDING_FILE = "" // Recording file name
var RECORDINGS_DIR = joinPath("Recordings")
var SNAPSHOTS_DIR = joinPath("Snapshots")
var SAVED_DIR = joinPath("Saved")

var IS_RECORDING = false

fun main() {
    if (!assertGrid(GRID)) return
    File(RECORDINGS_DIR).mkdir()  // Directory for recording .golfr files
    File(SNAPSHOTS_DIR).mkdir()   // Directory for Snapshot .png files
    File(SAVED_DIR).mkdir()       // Directory for .golf files

    val frame = JFrame("Conway's Game of Life")
    var panel = DrawPanel(Board(GRID, HashSet()))

    // Set Simulation Playground to be Scrollable.
    panel.isOpaque = true
    panel.preferredSize = dimension()
    var scrollFrame = JScrollPane(panel)
    scrollFrame.verticalScrollBar.unitIncrement = 16
    scrollFrame.horizontalScrollBar.unitIncrement = 16
    panel.autoscrolls = true
    frame.add(scrollFrame)

    var gameIterator = panel.board.iterator()
    // Map Icon Names to Icon Objects
    val icons = File(joinPath("src", "Icons")).listFiles().map { it.path }.associateBy(
        // Get Icon Name from file path
        { it.substring(it.lastIndexOf(File.separator) + 1, it.lastIndexOf(".")) },
        { sizeIcon(ImageIcon(it)) }
    )
    val buttonKeys = mapOf(
        "Next" to "→",
        "Reset" to "←",
        "Random" to "D",
        "Save" to "S",
        "Snapshot" to "C",
        "Record" to "R",
        "Open" to "F",
        "Zoom In" to "+",
        "Zoom Out" to "-"
    )
    // Buttons
    val buttons = buttonKeys.keys.associateBy({ it }, { JButton("$it [${buttonKeys[it]}]", icons[it]) })
    // Key Bindings
    val keyBindings = mapOf(
        "Next" to KeyEvent.VK_RIGHT,
        "Reset" to KeyEvent.VK_BACK_SPACE,
        "Random" to KeyEvent.VK_D,
        "Save" to KeyEvent.VK_S,
        "Snapshot" to KeyEvent.VK_C,
        "Record" to KeyEvent.VK_R,
        "Open" to KeyEvent.VK_F,
        "Zoom In" to KeyEvent.VK_EQUALS,
        "Zoom Out" to KeyEvent.VK_MINUS
    )
    // Menu Panel on the Right with all Labels/Buttons.
    val menuPanel = JPanel(GridLayout(buttons.size, 1));

    for (button in buttons.values) {
        button.isContentAreaFilled = false;
        menuPanel.add(button)
    }

    buttons["Next"]?.addActionListener(object : AbstractAction() {
        override fun actionPerformed(e: ActionEvent) {
            if (gameIterator.hasNext()) {
                gameIterator.next()
                frame.repaint()
                if (IS_RECORDING) {
                    File(RECORDING_FILE).appendText("${panel.board}\n")
                }
            }
        }
    })
    buttons["Reset"]?.addActionListener(object : AbstractAction() {
        override fun actionPerformed(e: ActionEvent) {
            panel.board.clear()
            frame.repaint()
        }
    })
    buttons["Random"]?.addActionListener(object : AbstractAction() {
        override fun actionPerformed(e: ActionEvent) {
            panel.board.random()
            frame.repaint()
        }
    })
    buttons["Snapshot"]?.addActionListener(object : AbstractAction() {
        override fun actionPerformed(e: ActionEvent?) = Thread {
            val file = createFile("${SNAPSHOTS_DIR}Snapshot", "png")
            snapshot(panel.board, file)
        }.start();
    })
    buttons["Save"]?.addActionListener(object : AbstractAction() {
        override fun actionPerformed(e: ActionEvent?) {
            if (panel.board.coordinates.isNotEmpty()) {
                createFile("${SAVED_DIR}GameOfLife", "golf").writeText("$GRID\n${panel.board}")
            }
        }
    })
    buttons["Record"]?.addActionListener(object : AbstractAction() {
        override fun actionPerformed(e: ActionEvent?) {
            if (IS_RECORDING) {
                buttons["Record"]?.icon = icons["Record"]
            } else {
                buttons["Record"]?.icon = icons["Recording"]
                val file = createFile("${RECORDINGS_DIR}Recording", "golfr")
                file.appendText("$GRID\n")
                RECORDING_FILE = file.name
            }
            IS_RECORDING = !IS_RECORDING
        }
    })
    buttons["Open"]?.addActionListener(object : AbstractAction() {
        override fun actionPerformed(e: ActionEvent?) {
            val fileFrame = JFrame("Load Game of Life Board")
            val filePanel = JPanel(GridLayout(-1, 1))
            val currentDir = File(SAVED_DIR).listFiles().filter { it.name.endsWith(".golf") }
            for (file in currentDir) {
                val fileButton = JButton(file.name, icons["File"])
                fileButton.addActionListener {
                    val data = getData(file)
                    // data.first contains the size of the board. If it is 0, an error was detected.
                    if (data.first == 0) {
                        fileButton.icon = icons["Error"]
                        fileButton.isEnabled = false
                    } else {
                        GRID = data.first
                        // Adjust zoom based on board size
                        panel.preferredSize = dimension()
                        frame.remove(scrollFrame)
                        scrollFrame = JScrollPane(panel)
                        scrollFrame.verticalScrollBar.unitIncrement = 16
                        scrollFrame.horizontalScrollBar.unitIncrement = 16
                        frame.add(scrollFrame)
                        // Fill in board
                        panel.board = Board(data.first, data.second)
                        gameIterator = panel.board.iterator()
                        frame.revalidate()
                    }
                }
                fileButton.isContentAreaFilled = false;
                filePanel.add(fileButton)
            }
            val scrollPane = JScrollPane(filePanel)
            scrollPane.verticalScrollBarPolicy = JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED
            scrollPane.verticalScrollBar.unitIncrement = 16
            // Set Default View position to start at the top of the panel
            SwingUtilities.invokeLater { scrollPane.viewport.viewPosition = Point(0, 0) }
            fileFrame.add(scrollPane)
            fileFrame.defaultCloseOperation = JFrame.HIDE_ON_CLOSE
            fileFrame.size = Dimension(frame.width / 3, frame.height / 2)
            fileFrame.setLocationRelativeTo(null)
            fileFrame.isResizable = false
            fileFrame.isVisible = true
        }
    })
    buttons["Zoom In"]?.addActionListener(object : AbstractAction() {
        override fun actionPerformed(e: ActionEvent) {
            val currentSize = panel.preferredSize
            panel.preferredSize = Dimension(currentSize.width + GRID, currentSize.height + GRID)
            frame.remove(scrollFrame)
            scrollFrame = JScrollPane(panel)
            scrollFrame.verticalScrollBar.unitIncrement = 16
            scrollFrame.horizontalScrollBar.unitIncrement = 16
            frame.add(scrollFrame)
            frame.revalidate()
        }
    })
    buttons["Zoom Out"]?.addActionListener(object : AbstractAction() {
        override fun actionPerformed(e: ActionEvent) {
            val currentSize = panel.preferredSize
            val width = scrollFrame.size.width
            val height = scrollFrame.size.height
            val max = if (width > height) width else height
            if (max < currentSize.width - GRID) {
                panel.preferredSize = Dimension(currentSize.width - GRID, currentSize.height - GRID)
                frame.remove(scrollFrame)
                scrollFrame = JScrollPane(panel)
                scrollFrame.verticalScrollBar.unitIncrement = 16
                scrollFrame.horizontalScrollBar.unitIncrement = 16
                frame.add(scrollFrame)
                frame.revalidate()
            }
        }
    })

    panel.addMouseMotionListener(object : MouseAdapter() {
        override fun mouseDragged(e: MouseEvent) {
            val labelStepW = GRID.toFloat() / panel.width
            val labelStepH = GRID.toFloat() / panel.height
            val cX = (e.x * labelStepW + 0.5f).toInt() // X coordinate on simulation board.
            val cY = (e.y * labelStepH + 0.5f).toInt() // Y coordinate on simulation board.
            val coordinate = cY * GRID + cX

            if (SwingUtilities.isLeftMouseButton(e) && coordinate in 0 until GRID * GRID) {
                panel.board.addValue(coordinate)
            } else {
                panel.board.removeValue(coordinate)
            }
            frame.repaint()
        }
    })

    // Add Key Bindings
    for ((key, keyEvent) in keyBindings) {
        buttons[key]?.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)?.clear()
        buttons[key]?.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)?.put(KeyStroke.getKeyStroke(keyEvent, 0), "KEY_BINDING")
        buttons[key]?.actionMap?.put("KEY_BINDING", buttons[key]?.actionListeners?.get(0) as Action)
    }

    frame.add(menuPanel, BorderLayout.EAST)
    frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
    frame.extendedState = JFrame.MAXIMIZED_BOTH
    frame.isVisible = true
}

/**
 * Resizes the given ImageIcon. This is used to scale button icons.
 *
 * @param icon  Image Icon for button.
 * @return scaled Image Icon.
 */
fun sizeIcon(icon: ImageIcon): ImageIcon {
    return ImageIcon(icon.image.getScaledInstance(40, 40, Image.SCALE_DEFAULT))
}

/**
 * Gets the board state from a ".golf" file.
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
 * @return      Dimension with the playground size.
 */
fun dimension(): Dimension {
    var original = GRID * 4.0
    val width = Toolkit.getDefaultToolkit().screenSize.width
    val height = Toolkit.getDefaultToolkit().screenSize.height
    val max = (if (width > height) width else height).toDouble()
    while (original <= max) {
        original *= 1.5
    }
    // Round "original" to nearest multiple of GRID for zooming.
    val size = ceil(original / GRID).toInt() * GRID
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