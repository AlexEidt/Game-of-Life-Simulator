// Board Class
// Copyright 2021 by Alex Eidt

/**
 * Board Class
 *
 * Represents the Game of Life Simulation.
 *
 * @param size the size of the board. Board will always be a size x size square.
 * @param coordinates the cells of the board that are true.
 */
class Board(val size: Int, val coordinates: HashSet<Int>) {
    private val visited: HashSet<Int> = HashSet()
    private val set: MutableSet<Int> = mutableSetOf()

    private val neighbors1: Array<Int> = Array(8) { 0 }
    private val neighbors2: Array<Int> = Array(8) { 0 }
    private val neighbors3: Array<Int> = Array(8) { 0 }

    // Randomly fills in the board.
    fun random() {
        coordinates.clear()
        // Get a random density between 0 and 1
        val density = (0..1000).random() / 1000.0
        // Get a set of random numbers between 0 and size * size
        coordinates.addAll((0 until size * size).shuffled().take((size * size * density).toInt()))
    }

    // Adds a new square to the DrawPanel.
    fun addValue(value: Int) = coordinates.add(value)

    // Removes a square from the DrawPanel.
    fun removeValue(value: Int) = coordinates.remove(value)

    // Clears the board.
    fun clear() {
        coordinates.clear()
        visited.clear()
    }

    /**
     * To run the simulation, the Board class features an iterator which will calculate
     * which cells make it to the next generation. Once all cells are false, the iterator
     * stops.
     */
    fun iterator(): Iterator<Int> {
        return object : Iterator<Int> {

            override fun hasNext(): Boolean = coordinates.isNotEmpty()

            override fun next(): Int {
                visited.clear()
                set.clear()
                for (position in coordinates) {
                    if (checkCurrent(neighbors1, position)) {
                        set.add(position)
                    }
                    visited.add(position)
                    getNeighbors(neighbors2, position)
                    for (neighbor in neighbors2) {
                        if (neighbor >= 0 && neighbor !in visited) {
                            if (checkNeighbors(neighbors3, neighbor)) {
                                set.add(neighbor)
                            }
                            visited.add(neighbor)
                        }
                    }
                }
                coordinates.clear()
                coordinates.addAll(set)
                return 0 // Dummy return because iterator has to return something
            }
        }
    }

    private fun toInt(bool: Boolean) = if (bool) -1 else 1

    /**
     * Given a certain position on the board, returns the coordinates of all
     * neighbors. Positions on the boundaries of the board feature neighbors that
     * are false if the neighbor is out of bounds.
     */
    private fun getNeighbors(neighbors: Array<Int>, position: Int) {
        val isTop = position / size == 0
        val isBottom = position / size == size - 1
        val isLeft = position % size == 0
        val isRight = (position + 1) % size == 0

        val rowNext = position + size
        val rowPrev = position - size

        neighbors[0] = (position + 1) * toInt(isRight) // Right
        neighbors[1] = if (position == 0 || isLeft) -1 else position - 1 // Left
        neighbors[2] = rowNext * toInt(isBottom) // Bottom
        neighbors[3] = (rowNext + 1) * toInt(isRight || isBottom) // Downward Right Diagonal
        neighbors[4] = (rowNext - 1) * toInt(isLeft || isBottom) // Downward Left Diagonal
        neighbors[5] = rowPrev // Top
        neighbors[6] = if (rowPrev + 1 == 0 || isRight) -1 else rowPrev + 1 // Upward Right Diagonal
        neighbors[7] = (rowPrev - 1) * toInt(isLeft || isTop) // Upward Left Diagonal
    }

    /**
     * Given a certain position on the board, determines if the cell at that position
     * will make it to the next generation given its neighbors and the rules specified
     * in this method.
     */
    private fun checkNeighbors(neighbors: Array<Int>, position: Int): Boolean {
        getNeighbors(neighbors, position)
        val valid = neighbors.count { it >= 0 && it in coordinates }
        val self = position in coordinates
        // Any true cell with fewer than two true neighbors becomes false.
        // Any true cell with 2 or 3 true neighbors remains true.
        // Any true cell with more than 3 true neighbors becomes false.
        // Any false cell with exactly 3 neighbors becomes true.
        return (self && (valid == 2 || valid == 3)) || (!self && valid == 3)
    }

    /**
     * Optimized version of "checkNeighbors" for when the current positions
     * are looked over since we know all of these positions are "true".
     */
    private fun checkCurrent(neighbors: Array<Int>, position: Int): Boolean {
        getNeighbors(neighbors, position)
        val valid = neighbors.count { it >= 0 && it in coordinates }
        return valid == 2 || valid == 3
    }

    /**
     * Returns the board as the coordinates of the cells that are "true".
     */
    override fun toString(): String = coordinates.joinToString(separator = "\n")
}