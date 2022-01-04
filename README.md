# Game of Life Simulator

Conway's Game of Life is a classic one player game/simulation. Read more about it on [Wikipedia](https://en.wikipedia.org/wiki/Conway%27s_Game_of_Life).

The simulator comes with the following features:

* Zoomable and scrollable simulation playground
* Optimized simulator which only simulates areas with live cells
* Random board generation
* Toggleable Grid Lines
* PNG Capturing of current board state
* Save the current board state and load it in later
* Record the board during animation and convert to video

# The App

<img src="Documentation/demo.gif" alt="Demo of Game of Life Simulator" />
<br />

### Buttons/Icons Descriptions

Icon | Key | Description
--- | --- | ---
<img src="src/main/resources/Icons/Next.png" alt="Next Icon" width=50px /> | `RIGHT ARROW` | Moves the simulation to the next generation.
<img src="src/main/resources/Icons/Reset.png" alt="Reset Icon" width=50px /> | `BACKSPACE` | Clears the simulation board.
<img src="src/main/resources/Icons/Random.png" alt="Random Icon" width=50px /> | `D` | Randomly fills the simulation board.
<img src="src/main/resources/Icons/Save.png" alt="Save Icon" width=50px /> | `S` | Saves the current state of the board as a `.golf` file.
<img src="src/main/resources/Icons/Snapshot.png" alt="Snapshot Icon" width=50px /> | `C` | Saves the current state of the board as a `.png` file in the `Snapshots` directory which is automatically generated on startup.
<img src="src/main/resources/Icons/recordrecording.gif" alt="Recording Icon" width=50px /> | `R` | Toggles Recording Mode. If recording mode is on, any changes made to the board by clicking **NEXT** are recorded.
<img src="src/main/resources/Icons/Open.png" alt="Search Files Icon" width=50px /> | `F` | Opens all `.golf` files and allows the user to load in a new simulation state.
<img src="src/main/resources/Icons/Zoom_In.png" alt="Zoom In Files Icon" width=50px /> | `=` | Zooms in on the simulation board.
<img src="src/main/resources/Icons/Zoom_Out.png" alt="Zoom Out Files Icon" width=50px /> | `-` | Zooms out on the simulation board.
<img src="src/main/resources/Icons/Grid_Lines.png" alt="Grid Lines Files Icon" width=50px /> | `G` | Toggles simulation board grid lines.
## `golf` File Format and Loading Files

When the user presses the `Save` button, the current state of the board will be saved in a `golf` (`G`ame `O`f `L`ife `F`ile) file with the following format:

```
SIZE
index1
index2
index3
...
```

where `SIZE` is the size of the board (the board is always a `SIZE` by `SIZE` square) and `index1`, `index2`, `index3`, ... represent the indices where cells are alive. The indices are flattened. Given an `x` coordinate and a `y` coordinate on a board with size `size`, the formula to calculate the coordinate is `coordinate = x * size + y`. If you'd like to create your own designs and load them into the simulator, simply create a file in the format above, where you specify the size of your square board and the coordinates of every square you'd like to be alive.

These files are saved in the `Saved` directory which is automatically generated on startup.
## `golfr` File Format

The `.golfr` file format is the s file format used to store multiple frames of simulation. The file format is the same as the `.golf` format, without the `SIZE` parameter at the front. The `SIZE` is specified on the first line of this file. It simply stores the coordinates of each cell as a comma separated list on every line in the file.

These files are saved in the `Recordings` directory which is automatically generated on startup.

## Recording and `convert.py`

Once the user presses the `Record` button, a file called `Recording{X}.golfr` (`X` is a number) is created. This file will contain the state of the board for every frame that was recorded. To convert this file to a video format, use `convert.py`.

`convert.py` usage:

```
cd Recordings
python convert.py -f FILENAME -t TYPE -r RESOLUTION -fps FPS
```

```
usage: convert.py [-h] [-f FILENAME] [-t TYPE] [-r RESOLUTION] [-fps FPS]

Convert recorded .golfr files into video.

optional arguments:
  -h, --help                                show this help message and exit
  -f FILENAME, --filename FILENAME          The filename of the .golf file to convert.
  -t TYPE, --filetype TYPE                  The file type of video to convert to.
  -r RESOLUTION, --resolution RESOLUTION    The size of the cells in the video.
  -fps FPS, --fps FPS                       The frames per second of the video.
```

`convert.py` dependencies:

* `Python 3.7+`
* `numpy`
* `imageio`
* `tqdm`

Note that `convert.py` is just one way of converting the data into video form. Modify the script to customize the video you get out of it.

## Gallery

<img src="Documentation/snapshot.png" alt="Example Snapshot" width=50% />
<br />

<img src="Documentation/recording.gif" alt="Recording GIF" width=50% />
<br />