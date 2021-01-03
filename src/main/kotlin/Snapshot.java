// Snapshot Class
// Copyright 2021 by Alex Eidt

import javax.imageio.ImageIO;
import javax.imageio.stream.FileImageOutputStream;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;


public class Snapshot {
    public static final int RESOLUTION = 20;
    public static final int GIF_SPEED = 1;
    public static final boolean LOOP_CONTINUOUSLY = true;

    /**
     * Creates a PNG image of the board.
     *
     * @param board the board representing the current state of the Game of Life simulation.
     * @param size the size of the board. Always a size x size square.
     * @param recording if the user is recording, recording >= 0, otherwise recording = -1.
     * @throws IOException
     */
    public static void snapshot(Set<Integer> board, int size, int recording) throws IOException {
        File file;
        if (recording == -1) {
            file = new File("Snapshot.png");
            int index = 1;
            while (!file.createNewFile()) {
                file = new File("Snapshot" + index++ + ".png");
            }
        } else {
            file = new File("_r" + recording + ".png");
        }

        int scale = RESOLUTION;
        int gridScaled = size * scale;
        BufferedImage bufferedImage = new BufferedImage(gridScaled, gridScaled, BufferedImage.TYPE_BYTE_BINARY);
        for (int row = 0; row < gridScaled; row += scale) {
            int rowScale = row / scale * size;
            for (int sh = 0; sh < scale; sh++) {
                for (int col = 0; col < gridScaled; col += scale) {
                    if (!board.contains(rowScale + col / scale)) {
                        for (int sw = 0; sw < scale; sw++) {
                            bufferedImage.setRGB(col + sw, row + sh, 11111111);
                        }
                    }
                }
            }
        }
        ImageIO.write(bufferedImage, "PNG", file);
    }

    /**
     * Converts the information in the temporary "__recording__.golf" file to a series of PNGs
     * and converts those to a GIF.
     *
     * @param size the size of the board. Always a size x size square.
     * @throws IOException
     */
    public static void convertToGIF(int size) throws IOException {
        // Grab the output image type from the first image in the sequence.
        File recording = new File("__recording__.golf");
        Scanner file = new Scanner(recording);
        Set<Integer> board = new HashSet<>();
        int index = 0;
        while (file.hasNextLine()) {
            for (String coordinate : file.nextLine().split(",")) {
                board.add(Integer.parseInt(coordinate));
            }
            snapshot(board, size, index++);
            board.clear();
        }
        file.close();
        recording.delete();

        File firstFile = new File("_r0.png");
        BufferedImage firstImage = ImageIO.read(firstFile);
        firstFile.delete();

        // Create output gif file.
        File recorded = new File("Recording.gif");
        int i = 1;
        while (!recorded.createNewFile()) {
            recorded = new File("Recording" + i++ + ".gif");
        }

        ImageOutputStream output = new FileImageOutputStream(recorded);

        // Create gif using PNG frames.
        GifSequenceWriter writer =
                new GifSequenceWriter(output, firstImage.getType(), GIF_SPEED, LOOP_CONTINUOUSLY);

        writer.writeToSequence(firstImage);
        for(i = 1; i < index - 1; i++) {
            File temp = new File("_r" + i + ".png");
            writer.writeToSequence(ImageIO.read(temp));
            temp.delete();
        }
        new File("_r" + (index - 1) + ".png").delete();

        writer.close();
        output.close();
    }

    /**
     * Finds all valid .golf (Game of Life Files) in the src directory. If a .golf file
     * is corrupted, the filename is preceded by "ERROR".
     *
     * @return A list of Strings representing valid .golf files in the src directory.
     * @throws FileNotFoundException
     */
    public static ArrayList<String> getFiles() throws FileNotFoundException {
        ArrayList<String> result = new ArrayList<>();
        for (File file : new File(".").listFiles()) {
            String fileName = file.getName();
            if (fileName.endsWith(".golf")) {
                Scanner fileReader = new Scanner(file);
                if (fileReader.hasNextLine()) {
                    String line = fileReader.nextLine();
                    String[] data = line.split(":");
                    int size = Integer.parseInt(data[0]);
                    size *= size;
                    result.add(fileName);
                    for (String coordinate : data[1].split(",")) {
                        int c = Integer.parseInt(coordinate);
                        if (c >= size || c < 0) {
                            result.set(result.size() - 1, "ERROR" + fileName);
                            break;
                        }
                    }
                } else { // If there is an error append "ERROR" to signify.
                    result.add("ERROR" + fileName);
                }
            }
        }
        return result;
    }
}
