package util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * Simple BMP parser. Can convert BMP images to byte arrays and back.
 */
public class BmpParser {

    private final File[] files;

    public BmpParser(String... bitmapImagePaths) {
        files = new File[bitmapImagePaths.length];
        for (int i = 0; i < files.length; i++) {
            File f = new File(bitmapImagePaths[i]);
            if(!f.exists()) {
                throw new IllegalArgumentException("Image does not exist");
            }
            if(!f.canRead()) {
                throw new IllegalArgumentException("Image is not readable");
            }
            files[i] = f;
        }
    }

    /**
     * Reads all the files this parser was initialized with, converting each file to a byte[].
     *
     * @return An array of byte arrays, where each entry corresponds to a single file.
     * @throws IOException If an I/O error occurs reading any of the files.
     * @see <a href="https://stackoverflow.com/a/10497780/2333689">Source</a>
     */
    public byte[][] readAllFiles() throws IOException {
        byte[][] result = new byte[files.length][];
        for (int i = 0; i < files.length; i++) {
            result[i] = Files.readAllBytes(files[i].toPath());
        }
        return result;
    }

    public void writeAllFiles(byte[][] fileBytes) throws IOException {
        // TODO: Don't take arbitrary byte arrays as parameter, anybody can write anything (even different data) to the original files.
        for (int i = 0; i < files.length; i++) {
            Files.write(files[i].toPath(), fileBytes[i]);
        }
    }
}
