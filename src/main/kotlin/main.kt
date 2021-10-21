// Game of Life Simulator
// Copyright 2021 by Alex Eidt

import java.awt.*
import java.awt.event.*
import java.io.File
import java.io.FileNotFoundException
import java.lang.Integer.min
import java.util.*
import javax.swing.*
import java.awt.event.MouseMotionAdapter
import javax.swing.JPanel







var GRID = 50 // Length of one side of Game of Life Grid.
const val ICON_SIZE = 35 // Size of Icons.
var IS_RECORDING = false

fun main() {
    val frame = JFrame("Conways Game of Life")
    var panel = DrawPanel(Board(GRID, mutableSetOf()))
    var gameIterator = panel.board.iterator()
    // Icons
    val icons = mapOf(
        "File" to sizeIcon(ImageIcon("src/Icons/file.png")),
        "Next" to sizeIcon(ImageIcon("src/Icons/next.png")),
        "Reset" to sizeIcon(ImageIcon("src/Icons/reset.png")),
        "Search" to sizeIcon(ImageIcon("src/Icons/search.png")),
        "Record" to sizeIcon(ImageIcon("src/Icons/record.png")),
        "Recording" to sizeIcon(ImageIcon("src/Icons/recording.png")),
        "Snapshot" to sizeIcon(ImageIcon("src/Icons/snapshot.png")),
        "Save" to sizeIcon(ImageIcon("src/Icons/save.png")),
    )
    // Buttons
    val buttons = mapOf(
        "Next" to JButton("Next [→]", icons["Next"]),
        "Reset" to JButton("Reset [←]", icons["Reset"]),
        "Save" to JButton("Save [Shift+S]", icons["Save"]),
        "Snapshot" to JButton("Snapshot [C]", icons["Snapshot"]),
        "Record" to JButton("Record [R]", icons["Record"]),
        "Search" to JButton("Open [F]", icons["Search"]),
    )
    // Key Bindings
    val keyBindings = mapOf(
        "Next" to KeyEvent.VK_RIGHT,
        "Reset" to KeyEvent.VK_BACK_SPACE,
        "Save" to KeyEvent.VK_S,
        "Snapshot" to KeyEvent.VK_C,
        "Record" to KeyEvent.VK_R,
        "Search" to KeyEvent.VK_F,
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
                if (IS_RECORDING) File("__recording__.golf").appendText("${panel.board}\n")
            }
        }
    })
    buttons["Reset"]?.addActionListener(object : AbstractAction() {
        override fun actionPerformed(e: ActionEvent) {
            panel.board.clear()
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
                if (File("__recording__.golf").length() != 0L)
                    Thread { convertToGIF(frame, GRID) }.start()
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
                val fileButton = JButton(file, icons["File"])
                fileButton.addActionListener {
                    val boardString = File(file).readLines()[0].split(":")
                    val size = boardString[0].toInt()
                    val onSet = (boardString[1].split(",").map { it.toInt() }).toMutableSet()
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
            scrollPane.horizontalScrollBarPolicy = JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
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

    frame.add(panel)

    panel.addMouseListener(object : MouseAdapter() {
        override fun mouseClicked(e: MouseEvent) {
            val size = min(panel.width, panel.height) / GRID
            val h = (panel.height - size * GRID) / 2
            val w = (panel.width - size * GRID) / 2
            val coordinateX: Int = (e.xOnScreen - w) / size
            val coordinateY: Int = (e.yOnScreen - h) / size
            val coordinate = coordinateY * GRID + coordinateX - 2 * GRID
            val isLeftClick = SwingUtilities.isLeftMouseButton(e)
            if (isLeftClick) {
                if (coordinateX in 0 until GRID && coordinateY in 0 until GRID + GRID / 25)
                    panel.board.addValue(coordinate)
            } else {
                panel.board.removeValue(coordinate)
            }
            frame.repaint()
        }
    })
    panel.addMouseMotionListener(object : MouseAdapter() {
        override fun mouseDragged(e: MouseEvent) {
            val size = min(panel.width, panel.height) / GRID
            val h = (panel.height - size * GRID) / 2
            val w = (panel.width - size * GRID) / 2
            val coordinateX: Int = (e.xOnScreen - w) / size
            val coordinateY: Int = (e.yOnScreen - h) / size
            val coordinate = coordinateY * GRID + coordinateX - 2 * GRID
            val isLeftClick = SwingUtilities.isLeftMouseButton(e)
            if (isLeftClick && coordinate in 0 until GRID * GRID) {
                if (coordinateX in 0 until GRID && coordinateY in 0 until GRID + GRID / 25)
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
    return ImageIcon(icon.image.getScaledInstance(ICON_SIZE, ICON_SIZE, Image.SCALE_DEFAULT))
}

/**
 * Finds all valid .golf (Game of Life Files) in the src directory. If a .golf file
 * is corrupted, it is not included in the returned list.
 *
 * @return A list of Strings representing valid .golf files in the src directory.
 * @throws FileNotFoundException
 */
fun getFiles(): ArrayList<String> {
    val result = ArrayList<String>()
    val pattern = Regex("[0-9]+:([0-9]+,?)+")
    for (file in File(".").listFiles()) {
        if (file.name.endsWith(".golf")) {
            if (!hasError(file, pattern))
                result.add(file.name)
        }
    }
    return result
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
        var size = data[0].toInt() * data[0].toInt()
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