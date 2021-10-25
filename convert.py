"""
Alex Eidt
Converts recorded .golfr files into video.
"""

import os
import argparse
import numpy as np
import imageio
from tqdm import tqdm


GOLFR_EXTENSION = '.golfr'


def convert(filename, resolution, fps, filetype):
    """
    Converts a recorded .golf file into a video.
    """
    print(f'Converting {filename}...')
    with imageio.save(f'{filename.replace(GOLFR_EXTENSION, "")}.{filetype}', fps=fps) as writer:
        with open(filename, mode='r') as f:
            lines = f.readlines()
            if len(lines) < 2:
                print(f"Error detected in {filename}")
                return
            size = int(lines[0])
            image = np.empty(size * size, dtype=np.uint8)
            for line in tqdm(lines):
                image.fill(255)
                image[np.fromstring(line, dtype=np.int32, sep=',')] = 0
                writer.append_data(
                    np.repeat(np.repeat(image.reshape((size, size)), resolution, axis=1), resolution, axis=0)
                )


def convert_all(directory, resolution, fps, filetype):
    """
    Converts all .golf files in a directory into a video.
    """
    for filename in os.listdir(directory):
        if filename.endswith(GOLFR_EXTENSION):
            convert(os.path.join(directory, filename), resolution, fps, filetype)


def main():
    parser = argparse.ArgumentParser(description=f'Convert recorded {GOLFR_EXTENSION} files into video.')

    parser.add_argument('-f', '--filename', type=str, default='.', help='The filename of the .golf file to convert.')
    parser.add_argument('-t', '--filetype', type=str, default='mp4', help='The file type of video to convert to.')
    parser.add_argument('-r', '--resolution', type=int, default=10, help='The size of the cells in the video.')
    parser.add_argument('-fps', '--fps', type=int, default=30, help='The frames per second of the video.')

    args = parser.parse_args()

    convert_all(args.filename, args.resolution, args.fps, args.filetype)


if __name__ == '__main__':
    main()