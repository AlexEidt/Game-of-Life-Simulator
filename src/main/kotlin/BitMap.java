// Board Class
// Copyright 2021 by Alex Eidt

import java.util.Set;

/**
 * BitMap Class
 *
 * Represents a bitmap.
 */
public class BitMap {
    public static final int BITS = Integer.BYTES * 8;

    private final int[] bitmap;

    /**
     * BitMap Constructor.
     *
     * @param start the cells of the board that are true.
     * @param width the width of the bitmap.
     * @param height the height of the bitmap.
     */
    public BitMap(Set<Integer> start, int width, int height) {
        int total = width * height;
        int size = total / BITS;
        int remainder = total % BITS == 0 ? 0 : 1;
        this.bitmap = new int[size + remainder];

        int index = 0;
        for (int i = 0; i < this.bitmap.length; i++) {
            int bits = ~0;
            for (int j = 0; j < BITS && index < total; j++) {
                if (start.contains(index++)) bits &= ~(1 << j);
            }
            this.bitmap[i] = ~bits;
        }
    }

    // See if the value at the given position is true or not.
    // Overloads the "bitmap[position]" operator.
    public boolean get(int position) {
        return (this.bitmap[position / BITS] & (1 << (position % BITS))) != 0;
    }

    // Set the value to 0 or 1 (depending on "val") at the given position.
    // Overloads the "bitmap[position] = val" operator.
    public void set(int position, boolean val) {
        if (val) {
            this.bitmap[position / BITS] |= (1 << (position % BITS));
        } else {
            this.bitmap[position / BITS] &= ~(1 << (position % BITS));
        }
    }

    // Clears the board. Sets all values to 0.
    public void clear() {
        for (int i = 0; i < this.bitmap.length; i++)
            this.bitmap[i] = 0;
    }
}
