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
    public static void snapshot(ArrayList<Boolean> board, int size, int recording) throws IOException {
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
                    if (!board.get(rowScale + col / scale)) {
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
        ArrayList<Boolean> board = new ArrayList<>(size * size);
        int index = 0;
        while (file.hasNextLine()) {
            String boardString = file.nextLine();
            for (char bit : boardString.toCharArray()) {
                board.add(bit == 49); // 49 is ASCII '1' character.
            }
            snapshot(board, size, index++);
            board.clear();
        }
        file.close();
        recording.delete();

        File firstFile = new File("_r0.png");
        BufferedImage firstImage = ImageIO.read(firstFile);

        // Create a new BufferedOutputStream with the last argument.
        File recorded = new File("Recording.gif");
        int i = 1;
        while (!recorded.createNewFile()) {
            recorded = new File("Recording" + i++ + ".gif");
        }

        ImageOutputStream output = new FileImageOutputStream(recorded);

        // Create a gif sequence with the type of the first image, 1 second
        // between frames, which loops continuously.
        GifSequenceWriter writer =
                new GifSequenceWriter(output, firstImage.getType(), GIF_SPEED, LOOP_CONTINUOUSLY);

        // Write out the first image to our sequence.
        writer.writeToSequence(firstImage);
        for(i = 1; i < index - 1; i++) {
            File temp = new File("_r" + i + ".png");
            BufferedImage nextImage = ImageIO.read(temp);
            writer.writeToSequence(nextImage);
            temp.delete();
        }
        firstFile.delete();
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
                    String boardString = fileReader.nextLine();
                    double squareRoot = Math.sqrt(boardString.length());
                    if (!boardString.isBlank()
                            && boardString
                            // Check if String is made up of only 0's and 1's.
                            .replaceAll("0", "")
                            .replaceAll("1", "")
                            .isBlank()
                            // Check if number of bits is a perfect square.
                            && squareRoot - Math.floor(squareRoot) == 0)
                    {
                        result.add(fileName);
                    } else { // If there is an error append "ERROR" to signify.
                        result.add("ERROR" + fileName);
                    }
                } else { // If there is an error append "ERROR" to signify.
                    result.add("ERROR" + fileName);
                }
            }
        }
        return result;
    }
}
