// Game of Life Simulator
// Copyright 2021 by Alex Eidt

import java.awt.*
import java.awt.event.ActionEvent
import java.awt.event.KeyEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.io.File
import javax.swing.*

/**
 * GUI Class
 *
 * Represents the Game of Life Simulation with a Graphical User Interface.
 *
 * @param grid          The size of the board. Board will always be a grid x grid square.
 */
class GUI(var grid: Int) {
    private var isRecording = false
    private lateinit var recordingFile: File
    
    init {
        if (!assertGrid(grid)) throw IllegalArgumentException()

        val frame = JFrame("Conway's Game of Life")
        // Simulation panel
        var panel = DrawPanel(Board(grid))

        // Set Simulation Playground to be Scrollable.
        panel.preferredSize = dimension(grid)
        var scrollFrame = updateScrollFrame(frame, null, panel)

        val buttonKeys = mapOf(
            "Next" to "→",
            "Reset" to "←",
            "Random" to "D",
            "Save" to "S",
            "Snapshot" to "C",
            "Record" to "R",
            "Open" to "F",
            "Zoom In" to "+",
            "Zoom Out" to "-",
            "Grid Lines" to "G"
        )
        val iconNames = arrayOf(*buttonKeys.keys.toTypedArray(), "Error", "File", "Recording")
        // Map Icon Names to Icon Objects
        val icons = iconNames.associateBy({ it }, { sizeIcon(this.javaClass.getResource("/Icons/${it.replace(" ", "_")}.png")) })

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
            "Zoom Out" to KeyEvent.VK_MINUS,
            "Grid Lines" to KeyEvent.VK_G
        )
        // Menu Panel on the Right with all Labels/Buttons.
        val menuPanel = JPanel(GridLayout(buttons.size, 1));

        for (button in buttons.values) {
            button.isContentAreaFilled = false;
            menuPanel.add(button)
        }

        buttons["Next"]?.addActionListener(object : AbstractAction() {
            override fun actionPerformed(e: ActionEvent) {
                if (panel.board.hasNext()) {
                    panel.board.next()
                    frame.repaint()
                    if (isRecording) {
                        recordingFile.appendText("${panel.board}\n")
                    }
                }
            }
        })
        buttons["Reset"]?.addActionListener(object : AbstractAction() {
            override fun actionPerformed(e: ActionEvent) = run { panel.board.clear(); frame.repaint() }
        })
        buttons["Random"]?.addActionListener(object : AbstractAction() {
            override fun actionPerformed(e: ActionEvent) = run { panel.board.random(); frame.repaint() }
        })
        buttons["Snapshot"]?.addActionListener(object : AbstractAction() {
            override fun actionPerformed(e: ActionEvent?) = Thread {
                snapshot(
                    panel.board,
                    createFile("${SNAPSHOTS_DIR}Snapshot", "png"),
                    panel.gridlines
                )
            }.start();
        })
        buttons["Save"]?.addActionListener(object : AbstractAction() {
            override fun actionPerformed(e: ActionEvent?) {
                if (panel.board.coordinates.isNotEmpty()) {
                    createFile("${SAVED_DIR}GameOfLife", "golf").writeText("$grid\n${panel.board}")
                }
            }
        })
        buttons["Record"]?.addActionListener(object : AbstractAction() {
            override fun actionPerformed(e: ActionEvent?) {
                if (isRecording) {
                    buttons["Record"]?.icon = icons["Record"]
                } else {
                    buttons["Record"]?.icon = icons["Recording"]
                    val file = createFile("${RECORDINGS_DIR}Recording", GOLFR_EXTENSION)
                    file.appendText("$grid\n")
                    recordingFile = file
                }
                isRecording = !isRecording
            }
        })
        buttons["Open"]?.addActionListener(object : AbstractAction() {
            override fun actionPerformed(e: ActionEvent?) {
                val currentDir = File(SAVED_DIR).listFiles().filter { it.name.endsWith(GOLF_EXTENSION) }
                if (currentDir.isEmpty()) return
                val fileFrame = JFrame("Showing ${currentDir.size} Game of Life Boards")
                val filePanel = JPanel(GridLayout(-1, 1))
                for (file in currentDir) {
                    val fileButton = JButton(file.name, icons["File"])
                    fileButton.addActionListener {
                        val (size, coordinates) = getData(file)
                        // If size is 0, an error was detected.
                        if (size == 0) {
                            fileButton.icon = icons["Error"]
                            fileButton.isEnabled = false
                        } else {
                            grid = size
                            // Adjust zoom based on board size
                            panel.preferredSize = dimension(grid)
                            scrollFrame = updateScrollFrame(frame, scrollFrame, panel)
                            // Fill in board
                            panel.board = Board(grid, coordinates)
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
                panel.preferredSize = Dimension(currentSize.width + grid, currentSize.height + grid)
                scrollFrame = updateScrollFrame(frame, scrollFrame, panel)
            }
        })
        buttons["Zoom Out"]?.addActionListener(object : AbstractAction() {
            override fun actionPerformed(e: ActionEvent) {
                val currentSize = panel.preferredSize
                val width = scrollFrame.size.width
                val height = scrollFrame.size.height
                if (width.coerceAtLeast(height) < currentSize.width - grid) {
                    panel.preferredSize = Dimension(currentSize.width - grid, currentSize.height - grid)
                    scrollFrame = updateScrollFrame(frame, scrollFrame, panel)
                }
            }
        })
        buttons["Grid Lines"]?.addActionListener(object : AbstractAction() {
            override fun actionPerformed(e: ActionEvent?) = run { panel.gridlines = !panel.gridlines; frame.repaint() }
        })

        panel.addMouseMotionListener(object : MouseAdapter() {
            override fun mouseDragged(e: MouseEvent) {
                val labelStepW = grid.toFloat() / panel.width
                val labelStepH = grid.toFloat() / panel.height
                val cX = (e.x * labelStepW + 0.5f).toInt() // X coordinate on simulation board.
                val cY = (e.y * labelStepH + 0.5f).toInt() // Y coordinate on simulation board.
                val coordinate = cY * grid + cX

                if (SwingUtilities.isLeftMouseButton(e) && coordinate in 0 until grid * grid) {
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
        frame.isVisible = true
        frame.size = Dimension(
            Toolkit.getDefaultToolkit().screenSize.width / 2,
            Toolkit.getDefaultToolkit().screenSize.height / 2
        )
        // When window is resized, make sure simulation panel isn't bigger than the scroll frame.
        frame.addWindowStateListener {
            panel.preferredSize = dimension(grid)
            scrollFrame = updateScrollFrame(frame, scrollFrame, panel)
        }
        frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        frame.extendedState = JFrame.MAXIMIZED_BOTH
    }
}