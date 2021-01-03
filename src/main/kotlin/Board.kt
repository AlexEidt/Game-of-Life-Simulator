// Board Class
// Copyright 2021 by Alex Eidt

/**
 * Board Class
 *
 * Represents the Game of Life Simulation.
 *
 * @param size the size of the board. Board will always be a size x size square.
 * @param start the cells of the board that are true.
 */
class Board(var size: Int, var start: MutableSet<Int>) {
    var board: MutableList<Boolean> = mutableListOf()
    var remove: MutableSet<Int> = mutableSetOf()
    var visited: MutableSet<Int> = mutableSetOf()

    init {
        for (i in 0 until this.size * this.size) {
            this.board.add(i in start)
        }
    }

    /**
     * To run the simulation, the Board class features an iterator which will calculate
     * which cells make it to the next generation. Once all cells are false, the iterator
     * stops.
     */
    fun iterator(): Iterator<MutableSet<Int>> {
        return object : Iterator<MutableSet<Int>> {

            override fun hasNext(): Boolean = start.isNotEmpty()

            override fun next(): MutableSet<Int> {
                for (position in start) {
                    board[position] = true
                }
                for (position in remove) {
                    board[position] = false
                }
                remove.clear()
                visited.clear()
                val set: MutableSet<Int> = mutableSetOf()
                for (position in start) {
                    if (checkNeighbors(position)) {
                        set.add(position)
                    } else {
                        remove.add(position)
                    }
                    visited.add(position)
                    for (neighbor in getNeighbors(position)) {
                        if (neighbor >= 0 && neighbor !in visited) {
                            if (checkNeighbors(neighbor)) {
                                set.add(neighbor)
                            } else {
                                remove.add(neighbor)
                            }
                            visited.add(neighbor)
                        }
                    }
                }
                start = set
                return set
            }
        }
    }

    private fun toInt(bool: Boolean) = if (bool) -1 else 1

    /**
     * Given a certain position on the board, returns the coordinates of all
     * neighbors. Positions on the boundaries of the board feature neighbors that
     * are false if the neighbor is out of bounds.
     */
    private fun getNeighbors(position: Int): Array<Int> {
        val isTop: Boolean = position / this.size == 0
        val isBottom: Boolean = position / this.size == this.size - 1
        val isLeft: Boolean = position % this.size == 0
        val isRight: Boolean = (position + 1) % this.size == 0

        val rowNext: Int = position + this.size
        val rowPrev: Int = position - this.size
        return arrayOf(
            (position + 1) * toInt(isRight), // Right
            if (position == 0 || isLeft) -1 else position - 1, // Left
            rowNext * toInt(isBottom), // Bottom
            (rowNext + 1) * toInt(isRight || isBottom), // Downward Right Diagonal
            (rowNext - 1) * toInt(isLeft || isBottom), // Downward Left Diagonal
            rowPrev, // Top
            if (rowPrev + 1 == 0 || isRight) -1 else rowPrev + 1, // Upward Right Diagonal
            (rowPrev - 1) * toInt(isLeft || isTop), // Upward Left Diagonal
        )
    }

    /**
     * Given a certain position on the board, determines if the cell at that position
     * will make it to the next generation given its neighbors and the rules specified
     * in this method.
     */
    private fun checkNeighbors(position: Int): Boolean {
        var valid = 0
        for (neighbor in getNeighbors(position)) {
            if (neighbor >= 0 && this.board[neighbor]) {
                valid++
            }
        }
        val self: Boolean = this.board[position]
        // Any true cell with fewer than two true neighbors becomes false.
        // Any true cell with 2 or 3 true neighbors remains true.
        // Any true cell with more than 3 true neighbors becomes false.
        // Any false cell with exactly 3 neighbors becomes true.
        return (self && (valid == 2 || valid == 3)) || (!self && valid == 3)
    }

    /**
     * Returns the board as the coordinates of the cells that are "true".
     */
    override fun toString(): String = this.start.joinToString(separator = ",")
}