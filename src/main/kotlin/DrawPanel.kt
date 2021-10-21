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
    // Given the current squares in the "coordinates" set, updates the DrawPanel.
    public override fun paintComponent(g: Graphics) {
        g.color = Color.BLACK

        val size: Int = Integer.min(this.width, this.height) / GRID
        val h: Int = (this.height - size * GRID) / 2
        val w: Int = (this.width - size * GRID) / 2

        val gridSizeH = GRID * size + h
        val gridSizeW = GRID * size + w
        // Draw Box around simulation area.
        g.drawLine(w, h, w, gridSizeH) // Top Left to Bottom Left.
        g.drawLine(w, h, gridSizeW, h) // Top Left to Top Right.
        g.drawLine(gridSizeW, h, gridSizeW, gridSizeH) // Top Right to Bottom Right.
        g.drawLine(w, gridSizeH, gridSizeW, gridSizeH) // Bottom Left to Bottom Right.

        // Draw Squares for Game of Life Simulation.
        for (coordinate in this.board.coordinates) {
            g.fillRect(
                (coordinate % GRID) * size + w,
                (coordinate / GRID) * size + h,
                size,
                size
            )
        }
    }
}