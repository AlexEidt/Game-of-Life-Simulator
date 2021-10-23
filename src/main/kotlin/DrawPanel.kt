// DrawPanel Class
// Copyright 2021 by Alex Eidt

import java.awt.Color
import java.awt.Graphics
import javax.swing.JPanel

/**
 * The DrawPanel represents the "Playground" or "Simulation Area" on the
 * UI. It is the box that shows the simulation.
 *
 * @param board the Game of Life board.
 */
class DrawPanel(var board: Board) : JPanel() {
    override fun paint(g: Graphics) {
        g.color = Color.BLACK
        // width and height are always the same since DrawPanel is always a square
        val size = width / board.size
        // Draw Squares for Game of Life Simulation.
        for (coordinate in board.coordinates) {
            g.fillRect((coordinate % board.size) * size, (coordinate / board.size) * size, size, size)
        }
    }
}