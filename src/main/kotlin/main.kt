// Game of Life Simulator
// Copyright 2021 by Alex Eidt

import Snapshot.snapshot
import java.awt.*
import java.awt.Font
import java.awt.event.*
import java.io.File
import java.lang.Integer.min
import javax.swing.*
import javax.swing.border.EmptyBorder
import javax.swing.SwingUtilities

import java.awt.Dimension
import java.lang.IllegalArgumentException

import javax.swing.JScrollPane
import kotlin.math.sqrt
import javax.swing.KeyStroke

import javax.swing.JComponent

var GRID = 50 // Length of one side of Game of Life Grid.
const val ICON_SIZE = 35 // Size of Icons.
var IS_RUNNING = false
var IS_DRAWING = false
var IS_RECORDING = false
var STARTED_RECORDING = false
var SLIDER = 200

fun main()
{
    if (GRID < 10) {
        throw IllegalArgumentException("$GRID must be greater than or equal to 10")
    }
    val frame = JFrame("Conways Game of Life")
    var set = mutableSetOf<Int>()
    var board = Board(GRID, set)
    var panel = DrawPanel(set)
    var gameIterator = board.iterator()
    // Icons
    val icons = mapOf(
        "Draw" to sizeIcon(ImageIcon("src/Icons/draw.png")),
        "Erase" to sizeIcon(ImageIcon("src/Icons/eraser.png")),
        "Error" to sizeIcon(ImageIcon("src/Icons/error.png")),
        "File" to sizeIcon(ImageIcon("src/Icons/file.png")),
        "Next" to sizeIcon(ImageIcon("src/Icons/next.png")),
        "Stop" to sizeIcon(ImageIcon("src/Icons/pause.png")),
        "Start" to sizeIcon(ImageIcon("src/Icons/play.png")),
        "Start/Stop" to sizeIcon(ImageIcon("src/Icons/startstop.gif")),
        "Reset" to sizeIcon(ImageIcon("src/Icons/reset.png")),
        "Search" to sizeIcon(ImageIcon("src/Icons/search.png")),
        "Speed1" to sizeIcon(ImageIcon("src/Icons/speed1.png")),
        "Speed2" to sizeIcon(ImageIcon("src/Icons/speed2.png")),
        "Speed3" to sizeIcon(ImageIcon("src/Icons/speed3.png")),
        "Record" to sizeIcon(ImageIcon("src/Icons/record.png")),
        "Recording" to sizeIcon(ImageIcon("src/Icons/recording.png")),
        "Record/Recording" to sizeIcon(ImageIcon("src/Icons/recordrecording.gif")),
        "Snapshot" to sizeIcon(ImageIcon("src/Icons/snapshot.png")),
        "Save" to sizeIcon(ImageIcon("src/Icons/save.png")),
        "Key Bindings" to sizeIcon(ImageIcon("src/Icons/keyboard.png"))
    )
    // Buttons
    val buttons = mapOf(
        "Next" to JButton("Next", icons["Next"]),
        "Reset" to JButton("Reset", icons["Reset"]),
        "Start/Stop" to JButton("Start", icons["Start"]),
        "Erase" to JButton("Erase", icons["Erase"]),
        "Draw" to JButton("Draw", icons["Draw"]),
        "Save" to JButton("Save", icons["Save"]),
        "Snapshot" to JButton("Snapshot", icons["Snapshot"]),
        "Record" to JButton("Record", icons["Record"]),
        "Search" to JButton("Open", icons["Search"]),
        "Key Bindings" to JButton("Key Bindings", icons["Key Bindings"])
    )
    // Key Bindings
    val keyBindings = mapOf(
        "Next_RIGHT ARROW" to KeyEvent.VK_RIGHT,
        "Reset_BACK SPACE" to KeyEvent.VK_BACK_SPACE,
        "Start/Stop_SPACE" to KeyEvent.VK_SPACE,
        "Erase_E" to KeyEvent.VK_E,
        "Draw_D" to KeyEvent.VK_D,
        "Save_Shift+S" to KeyEvent.VK_S,
        "Snapshot_C" to KeyEvent.VK_C,
        "Record_R" to KeyEvent.VK_R,
        "Search_F" to KeyEvent.VK_F,
        "Key Bindings_K" to KeyEvent.VK_K
    )
    // Menu Panel on the Right with all Labels/Buttons.
    val menuPanel = JPanel()
    val title = JLabel("Conways Game of Life", JLabel.CENTER)
    title.font = Font(title.font.name, Font.BOLD, 25)
    title.border = EmptyBorder(10, 10, 30, 10)
    title.alignmentX = Component.CENTER_ALIGNMENT
    menuPanel.add(title)
    // buttonPanel contains all the buttons for the simulation.
    val buttonPanels = arrayOf(JPanel(), JPanel())
    buttonPanels[0].layout = BoxLayout(buttonPanels[0], BoxLayout.Y_AXIS)
    buttonPanels[1].layout = BoxLayout(buttonPanels[1], BoxLayout.Y_AXIS)

    var index = 0
    for ((_, button) in buttons) {
        button.isContentAreaFilled = false;
        button.border = EmptyBorder(10, 35, 10, 35)
        buttonPanels[index++ / (buttons.size / 2)].add(button)
    }
    val buttonPanel = JPanel()
    buttonPanel.layout = BoxLayout(buttonPanel, BoxLayout.X_AXIS)
    buttonPanel.add(buttonPanels[0])
    buttonPanel.add(buttonPanels[1])
    menuPanel.add(buttonPanel)
    // AdjustPanel is the panel containing the slider for simulation speed.
    val adjustPanel = JPanel()
    adjustPanel.layout = BoxLayout(adjustPanel, BoxLayout.Y_AXIS)
    val speedLabel = JLabel("   Simulation Speed", JLabel.CENTER)
    speedLabel.icon = icons["Speed2"]
    speedLabel.alignmentX = Component.CENTER_ALIGNMENT
    adjustPanel.add(speedLabel)
    val slider = JSlider(0, SLIDER, SLIDER / 2)
    // Change speedometer icon when slider changes.
    slider.addChangeListener {
        frame.cursor = Cursor.getDefaultCursor();
        val sliderThird = SLIDER / 3
        when (slider.value.toLong()) {
            in 0 until sliderThird -> speedLabel.icon = icons["Speed1"]
            in sliderThird until sliderThird * 2 -> speedLabel.icon = icons["Speed2"]
            else -> {
                speedLabel.icon = icons["Speed3"]
            }
        }
    }
    adjustPanel.add(slider)
    adjustPanel.border = EmptyBorder(20, 10, 0, 10)

    menuPanel.add(adjustPanel)
    menuPanel.border = EmptyBorder(0, 20, 0, 20)
    menuPanel.layout = BoxLayout(menuPanel, BoxLayout.Y_AXIS)

    var thread = Thread {
        while (gameIterator.hasNext()) {
            buttons["Next"]?.doClick()
            Thread.sleep((slider.value.toLong() - SLIDER) * -1)
        }
    }

    buttons["Next"]?.addActionListener(object : AbstractAction() {
        override fun actionPerformed(e: ActionEvent) {
            if (gameIterator.hasNext()) {
                set = gameIterator.next()
                panel.updateSet(set)
                frame.repaint()
                if (!IS_RUNNING) {
                    buttons["Snapshot"]?.isEnabled = true
                    buttons["Save"]?.isEnabled = true
                }
                if (IS_RECORDING) {
                    File("__recording__.golf").appendText("$board\n")
                }
            }
            frame.cursor = Cursor.getDefaultCursor();
            if (!IS_RUNNING) {
                buttons["Erase"]?.isEnabled = true
                buttons["Draw"]?.isEnabled = true
            }
            IS_DRAWING = false
        }
    })
    buttons["Reset"]?.addActionListener(object : AbstractAction() {
        override fun actionPerformed(e: ActionEvent) {
            panel.clear()
            frame.repaint()
            buttons["Start/Stop"]?.isEnabled = false
            buttons["Reset"]?.isEnabled = false
            buttons["Next"]?.isEnabled = false
            buttons["Erase"]?.isEnabled = false
            buttons["Draw"]?.isEnabled = true
            buttons["Snapshot"]?.isEnabled = false
            buttons["Save"]?.isEnabled = false
            frame.cursor = Cursor.getDefaultCursor();
            IS_DRAWING = false
        }
    })
    buttons["Start/Stop"]?.addActionListener(object : AbstractAction() {
        override fun actionPerformed(e: ActionEvent) {
            if (IS_RECORDING && !STARTED_RECORDING) {
                File("__recording__.golf").appendText("${set.joinToString(separator = ",")}\n")
                STARTED_RECORDING = true
            }
            if (IS_RUNNING) {
                if (thread.isAlive) {
                    thread.stop()
                }
                thread = Thread {
                    while (gameIterator.hasNext()) {
                        buttons["Next"]?.doClick()
                        Thread.sleep((slider.value.toLong() - SLIDER) * -1)
                    }
                }
                buttons["Start/Stop"]?.text = "Start"
                buttons["Start/Stop"]?.icon = icons["Start"]
                buttons["Reset"]?.isEnabled = true
                buttons["Erase"]?.isEnabled = true
                buttons["Draw"]?.isEnabled = true
                buttons["Snapshot"]?.isEnabled = true
                buttons["Save"]?.isEnabled = true
                buttons["Record"]?.isEnabled = true
                buttons["Search"]?.isEnabled = true
                frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
            } else {
                thread.start()
                buttons["Start/Stop"]?.text = "Stop"
                buttons["Start/Stop"]?.icon = icons["Stop"]
                buttons["Reset"]?.isEnabled = false
                buttons["Erase"]?.isEnabled = false
                buttons["Draw"]?.isEnabled = false
                buttons["Snapshot"]?.isEnabled = false
                buttons["Save"]?.isEnabled = false
                buttons["Record"]?.isEnabled = false
                buttons["Search"]?.isEnabled = false
                frame.defaultCloseOperation = JFrame.DO_NOTHING_ON_CLOSE
            }
            IS_RUNNING = !IS_RUNNING
            IS_DRAWING = false
            frame.cursor = Cursor.getDefaultCursor();
        }
    })
    buttons["Erase"]?.addActionListener(object : AbstractAction() {
        override fun actionPerformed(e: ActionEvent?) {
            IS_DRAWING = false
            buttons["Erase"]?.isEnabled = false
            buttons["Draw"]?.isEnabled = true
            // Set cursor to be Eraser Icon
            frame.cursor = Toolkit.getDefaultToolkit().createCustomCursor(
                icons["Erase"]?.image,
                Point(0, 0), "ERASER"
            )
        }
    })
    buttons["Draw"]?.addActionListener(object : AbstractAction() {
        override fun actionPerformed(e: ActionEvent?) {
            IS_DRAWING = true
            buttons["Erase"]?.isEnabled = true
            buttons["Draw"]?.isEnabled = false
            // Set cursor to be Paint Brush Icon
            frame.cursor = Toolkit.getDefaultToolkit().createCustomCursor(
                icons["Draw"]?.image,
                Point(0, 0), "DRAW"
            )
        }
    })
    buttons["Snapshot"]?.addActionListener(object : AbstractAction() {
        override fun actionPerformed(e: ActionEvent?) {
            snapshot(set, GRID, -1);
            frame.cursor = Cursor.getDefaultCursor();
            buttons["Snapshot"]?.isEnabled = false
            buttons["Erase"]?.isEnabled = true
            buttons["Draw"]?.isEnabled = true
            IS_DRAWING = false
        }
    })
    buttons["Save"]?.addActionListener(object : AbstractAction() {
        override fun actionPerformed(e: ActionEvent?) {
            var file = File("GameOfLife.golf")
            var index = 1
            while (!file.createNewFile()) {
                file = File("GameOfLife${index++}.golf")
            }
            file.writeText("$GRID:${set.joinToString(separator = ",")}")
            frame.cursor = Cursor.getDefaultCursor();
            buttons["Save"]?.isEnabled = false
            buttons["Erase"]?.isEnabled = true
            buttons["Draw"]?.isEnabled = true
            IS_DRAWING = false
        }
    })
    buttons["Record"]?.addActionListener(object : AbstractAction() {
        override fun actionPerformed(e: ActionEvent?) {
            if (IS_RECORDING) {
                frame.isEnabled = false
                buttons["Record"]?.text = "Record"
                buttons["Record"]?.icon = icons["Record"]
                if (File("__recording__.golf").length() != 0L) {
                    val progressFrame = JFrame("Converting to Gif...")
                    progressFrame.setLocationRelativeTo(null)
                    progressFrame.defaultCloseOperation = JFrame.DO_NOTHING_ON_CLOSE
                    progressFrame.size = Dimension(frame.width / 3, frame.height / 6)
                    progressFrame.isResizable = false
                    progressFrame.isVisible = true
                    Snapshot.convertToGIF(GRID)
                    progressFrame.dispose()
                }
                frame.isEnabled = true
                STARTED_RECORDING = false
            } else {
                buttons["Record"]?.text = "Recording"
                buttons["Record"]?.icon = icons["Recording"]
                File("__recording__.golf")
            }
            frame.cursor = Cursor.getDefaultCursor();
            buttons["Erase"]?.isEnabled = true
            buttons["Draw"]?.isEnabled = true
            IS_RECORDING = !IS_RECORDING
            IS_DRAWING = false
        }
    })
    buttons["Search"]?.addActionListener(object : AbstractAction() {
        override fun actionPerformed(e: ActionEvent?) {
            val fileSearch = JFrame("Load Old Game of Life Board")
            val files = Snapshot.getFiles()
            val filePanel = JPanel(GridLayout(files.size, 1))
            for (file in files) {
                val fileButton = JButton("   ${file.replace("ERROR", "")}")
                val error = file.startsWith("ERROR")
                fileButton.isEnabled = !error
                if (error) {
                    fileButton.icon = icons["Error"]
                } else {
                    fileButton.icon = icons["File"]
                    fileButton.addActionListener(object : AbstractAction() {
                        override fun actionPerformed(e: ActionEvent?) {
                            panel.clear()
                            val boardString = File(file).readLines()[0].split(":")
                            val size = boardString[0].toInt()
                            val onSet = (boardString[1].split(",").map {
                                it.toInt()
                            }).toMutableSet()
                            if (onSet.size > 0) {
                                buttons["Start/Stop"]?.isEnabled = true
                                buttons["Reset"]?.isEnabled = true
                                buttons["Next"]?.isEnabled = true
                                buttons["Snapshot"]?.isEnabled = true
                                buttons["Save"]?.isEnabled = true
                            }
                            if (size != GRID) {
                                GRID = size
                                set = onSet
                                board = Board(GRID, set)
                                gameIterator = board.iterator()
                            }
                            for (coordinate in onSet) {
                                panel.addValue(coordinate)
                            }
                            frame.repaint()
                        }
                    })
                }
                fileButton.isContentAreaFilled = false;
                filePanel?.add(fileButton)
            }
            val scrollPane = JScrollPane(filePanel)
            scrollPane.verticalScrollBarPolicy = JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED
            scrollPane.horizontalScrollBarPolicy = JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
            scrollPane.verticalScrollBar.unitIncrement = 16
            scrollPane.border = null
            // Set Default View position to start at the top of the panel
            SwingUtilities.invokeLater { scrollPane.viewport.viewPosition = Point(0, 0) }
            fileSearch.add(scrollPane)
            fileSearch.defaultCloseOperation = JFrame.HIDE_ON_CLOSE
            fileSearch.size = Dimension(frame.width / 3, frame.height / 2)
            fileSearch.setLocationRelativeTo(buttonPanel)
            fileSearch.isResizable = false
            fileSearch.isVisible = true

            IS_DRAWING = false
            buttons["Erase"]?.isEnabled = true
            buttons["Draw"]?.isEnabled = true
            frame.cursor = Cursor.getDefaultCursor();
        }
    })
    buttons["Key Bindings"]?.addActionListener(object : AbstractAction() {
        override fun actionPerformed(e: ActionEvent?) {
            val keys = JFrame("Key Bindings")
            val labelPanel = JPanel()
            for ((key, _) in keyBindings) {
                val keyData = key.split("_")
                val label = JLabel("${keyData[0]} | ${keyData[1]}")
                if (keyData[0] == "Record") {
                    label.icon = icons["Record/Recording"]
                } else {
                    label.icon = icons[keyData[0]]
                }
                label.border = EmptyBorder(3, 20, 3, 20)
                labelPanel.add(label)
            }
            labelPanel.layout = BoxLayout(labelPanel, BoxLayout.Y_AXIS)
            keys.add(labelPanel)
            keys.defaultCloseOperation = JFrame.HIDE_ON_CLOSE
            keys.size = Dimension(frame.width / 4, frame.height / 3 * 2)
            keys.setLocationRelativeTo(null)
            keys.isResizable = false
            keys.isVisible = true

            IS_DRAWING = false
            buttons["Erase"]?.isEnabled = true
            buttons["Draw"]?.isEnabled = true
            frame.cursor = Cursor.getDefaultCursor();
        }
    })

    buttons["Start/Stop"]?.isEnabled = false
    buttons["Reset"]?.isEnabled = false
    buttons["Next"]?.isEnabled = false
    buttons["Erase"]?.isEnabled = false
    buttons["Snapshot"]?.isEnabled = false
    buttons["Save"]?.isEnabled = false
    frame.add(panel)

    panel.addMouseListener(object : MouseAdapter() {
        override fun mouseClicked(e: MouseEvent) {
            if (!IS_RUNNING) {
                val size = min(panel.width, panel.height) / GRID
                val h = (panel.height - size * GRID) / 2
                val w = (panel.width - size * GRID) / 2
                val coordinateX: Int = (e.xOnScreen - w) / size
                val coordinateY: Int = (e.yOnScreen - h) / size
                if (IS_DRAWING) {
                    if (coordinateX in 0 until GRID && coordinateY in 0 until GRID + GRID / 25) {
                        panel.addValue(coordinateY * GRID + coordinateX - 2 * GRID)
                        buttons["Start/Stop"]?.isEnabled = true
                        buttons["Reset"]?.isEnabled = true
                        buttons["Next"]?.isEnabled = true
                        buttons["Snapshot"]?.isEnabled = true
                        buttons["Save"]?.isEnabled = true
                    }
                } else {
                    panel.removeValue(coordinateY * GRID + coordinateX - 2 * GRID)
                }
                frame.repaint()
            }
        }
    })
    panel.addMouseMotionListener(object : MouseAdapter() {
        override fun mouseDragged(e: MouseEvent) {
            if (!IS_RUNNING) {
                val size = min(panel.width, panel.height) / GRID
                val h = (panel.height - size * GRID) / 2
                val w = (panel.width - size * GRID) / 2
                val coordinateX: Int = (e.xOnScreen - w) / size
                val coordinateY: Int = (e.yOnScreen - h) / size
                val coordinate = coordinateY * GRID + coordinateX - 2 * GRID
                if (IS_DRAWING && coordinate in 0 until GRID * GRID) {
                    if (coordinateX in 0 until GRID && coordinateY in 0 until GRID + GRID / 25) {
                        panel.addValue(coordinate)
                        buttons["Start/Stop"]?.isEnabled = true
                        buttons["Reset"]?.isEnabled = true
                        buttons["Next"]?.isEnabled = true
                        buttons["Snapshot"]?.isEnabled = true
                        buttons["Save"]?.isEnabled = true
                    }
                } else {
                    panel.removeValue(coordinate)
                }
                frame.repaint()
            }
        }
    })

    frame.addWindowStateListener {
        fun windowDeiconifided(e: WindowEvent) {
            if (IS_DRAWING) {
                frame.cursor = Toolkit.getDefaultToolkit().createCustomCursor(
                    icons["Draw"]?.image,
                    Point(0, 0), "DRAW"
                )
            } else {
                frame.cursor = Toolkit.getDefaultToolkit().createCustomCursor(
                    icons["Erase"]?.image,
                    Point(0, 0), "ERASER"
                )
            }
        }
    }

    // Add Key Bindings
    for ((key, keyEvent) in keyBindings) {
        val keyName = key.split("_")[0]
        buttons[keyName]?.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)?.clear()
        buttons[keyName]?.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
            ?.put(KeyStroke.getKeyStroke(keyEvent, 0), "KEY_BINDING")
        buttons[keyName]?.actionMap
            ?.put("KEY_BINDING", buttons[keyName]?.actionListeners?.get(0) as Action)
    }
    buttons["Save"]?.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)?.clear()
    buttons["Save"]?.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
        ?.put(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.SHIFT_DOWN_MASK), "KEY_BINDING")
    buttons["Save"]?.actionMap
        ?.put("KEY_BINDING", buttons["Save"]?.actionListeners?.get(0) as Action)

    frame.add(menuPanel, BorderLayout.EAST)
    frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
    frame.extendedState = JFrame.MAXIMIZED_BOTH
    frame.isVisible = true
}

fun sizeIcon(icon: ImageIcon): ImageIcon {
    return ImageIcon(icon.image.getScaledInstance(ICON_SIZE, ICON_SIZE, Image.SCALE_DEFAULT))
}