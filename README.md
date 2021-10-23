# Game of Life Simulator

Conway's Game of Life is a classic one player game/simulation. Read more about it on [Wikipedia](https://en.wikipedia.org/wiki/Conway%27s_Game_of_Life).

The simulator comes with the following features:

* Zoomable and scrollable simulation playground
* Optimized simulator which only simulates areas with live cells
* Random board generation
* PNG Capturing of current board state
* Save the current board state and load it in later
* Record the board during animation and convert to video

# The App

<img src="Documentation/documentation.gif" alt="GIF Showing the functionality of the Game of Life Simulator app" />
<br />

### Buttons/Icons Descriptions

Icon | Key | Description
--- | --- | ---
<img src="src/Icons/next.png" alt="Next Icon" width=50px /> | `RIGHT ARROW` | Moves the simulation to the next generation according to the rules above.
<img src="src/Icons/reset.png" alt="Reset Icon" width=50px /> | `BACKSPACE` | Clears the simulation board.
<img src="src/Icons/random.png" alt="Random Icon" width=50px /> | `D` | Randomly fills the simulation board.
<img src="src/Icons/save.png" alt="Save Icon" width=50px /> | `S` | Saves the current state of the board as a `.golf` file.
<img src="src/Icons/snapshot.png" alt="Snapshot Icon" width=50px /> | `C` | Saves the current state of the board as a `.png` file.
<img src="src/Icons/recordrecording.gif" alt="Recording Icon" width=50px /> | `R` | Toggles Recording Mode. If recording mode is on, any changes made to the board by clicking **NEXT** or **START/STOP** are recorded and saved into a `.gif` file.
<img src="src/Icons/search.png" alt="Search Files Icon" width=50px /> | `F` | Opens all `.golf` files and allows the user to load in a new simulation state.
<img src="src/Icons/plus.png" alt="Zoom In Files Icon" width=50px /> | `F` | Zooms in on the simulation board.
<img src="src/Icons/minus.png" alt="Zoom Out Files Icon" width=50px /> | `F` | Zooms out on the simulation board.

## `golf` File Format and Loading Files

When the user presses the `Save` button, the current state of the board will be saved in a `golf` (`G`ame `O`f `L`ife `F`ile) file with the following format:

```
SIZE:index1,index2,index3,...
```

where `SIZE` is the size of the board (the board is always a `SIZE` by `SIZE` square) and `index1,index2,index3,...` represent the indices where cells are alive. The indices are "flattened". Given an `x` coordinate and a `y` coordinate on a board with size `size`, the formula to calculate the coordinate is `coordinate = x * size + y`. If you'd like to create your own designs and load them into the simulator, simply create a file in the format above, where you specify the size of your square board and the coordinates of every square you'd like to be on.

## Recording and `convert.py`

Once the user presses the `Record` button, a file called `Recording{X}.txt` (`X` is a number) is created. This file will contain the state of the board for every frame that was recorded. To convert this file to a video format, use `convert.py`.

## Gallery

<img src="Documentation/Snapshot.png" alt="Example Snapshot" width=50% />
<br />

<img src="Documentation/Recording.gif" alt="Recording GIF" width=50% />
<br />