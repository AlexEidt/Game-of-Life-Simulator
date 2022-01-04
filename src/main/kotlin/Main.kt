// Main Script
// Copyright 2022 by Alex Eidt

import java.io.File

const val DIR_ROOT_NAME = "GameOfLife"
val DIR = joinPath(DIR_ROOT_NAME)
val RECORDINGS_DIR = joinPath(DIR_ROOT_NAME, "Recordings")
val SNAPSHOTS_DIR = joinPath(DIR_ROOT_NAME, "Snapshots")
val SAVED_DIR = joinPath(DIR_ROOT_NAME, "Saved")

const val GRID = 200

fun main() {
    File(DIR).mkdir()
    File(RECORDINGS_DIR).mkdir()  // Directory for recording .golfr files
    File(SNAPSHOTS_DIR).mkdir()   // Directory for Snapshot .png files
    File(SAVED_DIR).mkdir()       // Directory for .golf files

    GUI(GRID)
}