"""
Alex Eidt
Converts recorded .golf files into video.
"""

import pandas as pd
import numpy as np
import imageio
from tqdm import tqdm


FILENAME = '__recording__.golf'
RESOLUTION = 10
GRID = 200


def convert():
    with imageio.save('golf.mp4', fps=30) as writer:
        canvas = np.empty(GRID * GRID, dtype=np.uint8)
        axis1 = np.empty((GRID, GRID * RESOLUTION))
        axis2 = np.empty((GRID * RESOLUTION, GRID * RESOLUTION))
        with open(FILENAME, mode='r') as f:
            for line in tqdm(f.readlines()):
                cells = np.fromstring(line, dtype=np.int32, sep=',')
                canvas.fill(255)
                canvas[cells] = 0
                frame = np.repeat(
                    np.repeat(canvas.reshape((GRID, GRID)), RESOLUTION, axis=1),
                    RESOLUTION,
                    axis=0
                )
                writer.append_data(frame)


if __name__ == '__main__':
    convert()