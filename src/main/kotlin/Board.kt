// Board Class
// Copyright 2021 by Alex Eidt

/**
 * Board Class
 *
 * Represents the Game of Life Simulation.
 *
 * @param size          The size of the board. Board will always be a size x size square.
 */
class Board(val size: Int) {
    var coordinates: HashSet<Int> = HashSet()
    private val visited: HashSet<Int> = HashSet()
    private var next: HashSet<Int> = HashSet()

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

    fun addValue(value: Int) = coordinates.add(value)

    fun removeValue(value: Int) = coordinates.remove(value)

    fun clear() = coordinates.clear()

    fun isNotEmpty(): Boolean = coordinates.isNotEmpty()

    /**
     * To run the simulation, the Board class features an "iterator" which will calculate
     * which cells make it to the next generation. Once all cells are false, the iterator
     * stops. The "iterator" is implemented in the "hasNext()" and "next()" functions below.
     */
    fun hasNext(): Boolean = coordinates.isNotEmpty()

    fun next() {
        visited.clear()
        next.clear()
        for (position in coordinates) {
            if (position !in visited && checkCurrent(position)) {
                next.add(position)
            }
            visited.add(position)
            getNeighbors(neighbors2, position)
            for (neighbor in neighbors2) {
                if (neighbor >= 0 && neighbor !in visited) {
                    if (checkNeighbors(neighbors3, neighbor)) {
                        next.add(neighbor)
                    }
                    visited.add(neighbor)
                }
            }
        }
        val temp = coordinates
        coordinates = next
        next = temp
    }

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

        neighbors[0] = if (isRight) -1 else position + 1 // Right
        neighbors[1] = if (isLeft || position == 0) -1 else position - 1 // Left
        neighbors[2] = if (isBottom) -1 else rowNext // Bottom
        neighbors[3] = if (isRight || isBottom) -1 else rowNext + 1 // Downward Right Diagonal
        neighbors[4] = if (isLeft || isBottom) -1 else rowNext - 1 // Downward Left Diagonal
        neighbors[5] = rowPrev // Top
        neighbors[6] = if (isRight || rowPrev + 1 == 0) -1 else rowPrev + 1 // Upward Right Diagonal
        neighbors[7] = if (isLeft || isTop) -1 else rowPrev - 1 // Upward Left Diagonal
    }

    /**
     * Given a certain position on the board, determines if the cell at that position
     * will make it to the next generation given its neighbors and the rules specified
     * in this method.
     */
    private fun checkNeighbors(neighbors: Array<Int>, position: Int): Boolean {
        getNeighbors(neighbors, position)
        val valid = neighbors.count { it in coordinates }
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
    private fun checkCurrent(position: Int): Boolean {
        getNeighbors(neighbors1, position)
        val valid = neighbors1.count { it in coordinates }
        return valid == 2 || valid == 3
    }

    /**
     * Returns the board as the coordinates of the cells that are "true".
     */
    override fun toString(): String = coordinates.joinToString(separator = "\n")
}