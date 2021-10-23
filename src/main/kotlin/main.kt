// Game of Life Simulator
// Copyright 2021 by Alex Eidt

import java.awt.*
import java.awt.event.*
import java.io.File
import java.io.FileNotFoundException
import java.nio.file.Paths
import java.util.*
import javax.swing.*
import kotlin.math.ceil

var GRID = 20000 // Length of one side of Game of Life Grid.
var IS_RECORDING = false
const val KEEP_RECORDING = true

fun main() {
    if (GRID % 100 != 0) throw IllegalArgumentException("GRID must be divisible by 100")
    if (GRID !in 100 until 65500) throw IllegalArgumentException("GRID must be between 100 and 65500")

    val frame = JFrame("Conway's Game of Life")
    var panel = DrawPanel(Board(GRID, mutableSetOf()))

    // Set Simulation Playground to be Scrollable.
    panel.isOpaque = true
    panel.preferredSize = dimension()
    var scrollFrame = JScrollPane(panel)
    scrollFrame.verticalScrollBar.unitIncrement = 16
    scrollFrame.horizontalScrollBar.unitIncrement = 16
    panel.autoscrolls = true
    frame.add(scrollFrame)

    var gameIterator = panel.board.iterator()
    // Icons
    val iconFolder = joinPath("src", "Icons")
    val icons = mapOf(
        "File" to sizeIcon(ImageIcon("${iconFolder}file.png")),
        "Next" to sizeIcon(ImageIcon("${iconFolder}next.png")),
        "Reset" to sizeIcon(ImageIcon("${iconFolder}reset.png")),
        "Random" to sizeIcon(ImageIcon("${iconFolder}random.png")),
        "Search" to sizeIcon(ImageIcon("${iconFolder}search.png")),
        "Record" to sizeIcon(ImageIcon("${iconFolder}record.png")),
        "Recording" to sizeIcon(ImageIcon("${iconFolder}recording.png")),
        "Snapshot" to sizeIcon(ImageIcon("${iconFolder}snapshot.png")),
        "Save" to sizeIcon(ImageIcon("${iconFolder}save.png")),
        "Zoom In" to sizeIcon(ImageIcon("${iconFolder}plus.png")),
        "Zoom Out" to sizeIcon(ImageIcon("${iconFolder}minus.png"))
    )
    // Buttons
    val buttons = mapOf(
        "Next" to JButton("Next [→]", icons["Next"]),
        "Reset" to JButton("Reset [←]", icons["Reset"]),
        "Random" to JButton("Random [D]", icons["Random"]),
        "Save" to JButton("Save [Shift+S]", icons["Save"]),
        "Snapshot" to JButton("Snapshot [C]", icons["Snapshot"]),
        "Record" to JButton("Record [R]", icons["Record"]),
        "Search" to JButton("Open [F]", icons["Search"]),
        "Zoom In" to JButton("Zoom In [+]", icons["Zoom In"]),
        "Zoom Out" to JButton("Zoom Out [-]", icons["Zoom Out"])
    )
    // Key Bindings
    val keyBindings = mapOf(
        "Next" to KeyEvent.VK_RIGHT,
        "Reset" to KeyEvent.VK_BACK_SPACE,
        "Random" to KeyEvent.VK_D,
        "Save" to KeyEvent.VK_S,
        "Snapshot" to KeyEvent.VK_C,
        "Record" to KeyEvent.VK_R,
        "Search" to KeyEvent.VK_F,
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
                    File("__recording__.golf").appendText("${panel.board}\n")
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
        override fun actionPerformed(e: ActionEvent?) = Thread { snapshot(panel.board) }.start();
    })
    buttons["Save"]?.addActionListener(object : AbstractAction() {
        override fun actionPerformed(e: ActionEvent?) {
            var file = File("GameOfLife.golf")
            var index = 1
            while (file.exists()) {
                file = File("GameOfLife${index++}.golf")
            }
            file.writeText("$GRID:${panel.board}")
        }
    })
    buttons["Record"]?.addActionListener(object : AbstractAction() {
        override fun actionPerformed(e: ActionEvent?) {
            if (IS_RECORDING) {
                buttons["Record"]?.icon = icons["Record"]
                if (File("__recording__.golf").length() != 0L) {
                    Thread { convertToGIF(frame, GRID, KEEP_RECORDING) }.start()
                }
            } else {
                buttons["Record"]?.icon = icons["Recording"]
                File("__recording__.golf") // Create recording file.
            }
            IS_RECORDING = !IS_RECORDING
        }
    })
    buttons["Search"]?.addActionListener(object : AbstractAction() {
        override fun actionPerformed(e: ActionEvent?) {
            val fileFrame = JFrame("Load Game of Life Board")
            val filePanel = JPanel(GridLayout(-1, 1))
            for (file in getFiles()) {
                val fileButton = JButton(file.name, icons["File"])
                fileButton.addActionListener {
                    val boardString = file.readLines()[0].split(":")
                    val size = boardString[0].toInt()
                    val onSet = boardString[1].split(",").map { it.toInt() }.toMutableSet()
                    GRID = size
                    panel.board = Board(size, onSet)
                    gameIterator = panel.board.iterator()
                    frame.repaint()
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
            frame.repaint()
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
                frame.repaint()
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
    frame.isResizable = false
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
 * Finds all valid .golf (Game of Life Files) in the src directory. If a .golf file
 * is corrupted, it is not included in the returned list.
 *
 * @return A list of Files representing valid .golf files in the src directory.
 * @throws FileNotFoundException
 */
fun getFiles(): List<File> {
    val pattern = Regex("[0-9]+:([0-9]+,?)+")
    val dir = File(".").listFiles()
    return dir.filter { it.name.endsWith(".golf") }.filter { !hasError(it, pattern) }
}

/**
 * Determines if a .golf file is corrupted.
 *
 * @param file  the file to check.
 * @param regex the regex to check the contents of the given file.
 * @return      true if file is corrupted, false otherwise.
 */
fun hasError(file: File, pattern: Regex): Boolean {
    val fileReader = Scanner(file)
    // Check if file is empty.
    if (fileReader.hasNextLine()) {
        val line = fileReader.nextLine()
        // .golf file format is: "SIZE:index1,index2,index3,..."
        val data = line.split(":")
        val size = data[0].toInt() * data[0].toInt()
        // If file is not empty, check if the file contents match the regex exactly.
        var notValid = pattern.replace(line, "").isNotBlank() && !line.endsWith(",")
        // If the file contents match the regex exactly, check if every integer is in the given range
        // of 0 <= (integer) < size.
        if (!notValid) {
            notValid = !notValid && data[1].split(",").map {
                // If the given number is too big to fit in an int, detect this.
                try { it.toInt() } catch (e: Exception) { -1 }
            }.any { it >= size || it < 0 }
        }
        return notValid
    }
    return false;
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