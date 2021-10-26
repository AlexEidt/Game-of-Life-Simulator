// DrawPanel Class
// Copyright 2021 by Alex Eidt

import java.awt.Color
import java.awt.Graphics
import javax.swing.JPanel

/**
 * The DrawPanel represents the "Playground" or "Simulation Area" on the
 * UI. It is the box that shows the simulation.
 *
 * @param board     The Game of Life board.
 * @param gridlines If true, draw gridlines on the simulation board.
 */
class DrawPanel(var board: Board, var gridlines: Boolean = false) : JPanel() {
    override fun paint(g: Graphics) {
        g.color = Color.BLACK
        // width and height are always the same since DrawPanel is always a square
        val size = width / board.size
        // Draw GridLines
        if (gridlines) {
            for (i in 0 until width step size) {
                g.drawLine(i, 0, i, width) // Top to Bottom
                g.drawLine(0, i, width, i) // Left to Right
            }
        }
        // Draw Squares for Game of Life Simulation.
        for (coordinate in board.coordinates) {
            g.fillRect(coordinate % board.size * size, coordinate / board.size * size, size, size)
        }
    }
}