package ar.edu.itba.criptog2.util;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.util.Arrays;

/**
 * Simple BMP writer. Initialized with basic BMP data, and can write out BMP to file.
 */
public class BmpWriter {

    private File file;
    private byte[] fileData;
    private String id;
    private int fileSize;
    byte[] reservedBytes;
    int pictureOffset;
    private int infoHeaderLength;
    private int width, height;
    private int numPlanes;
    private int bitsPerPixel;
    private int compressionType;
    private int pictureSize;
    private int horizontalResolution;
    private int verticalResolution;
    private int numUsedColors;
    private int numImportantColors;
    private byte[] pictureData;
    private byte[] extraHeaderBytes;
    private byte[] extraPictureBytes = new byte[0];

    public BmpWriter(String bitmapImagePath) throws IOException {
        if(bitmapImagePath == null || bitmapImagePath.isEmpty()) {
            throw new IllegalArgumentException("Invalid bitmap image path provided");
        }
        file = new File(bitmapImagePath);
        if (!file.exists()) {
            throw new IllegalArgumentException("Image does not exist");
        }
        if (file.isDirectory()) {
            throw new IllegalArgumentException("Provided path is a directory, not a file");
        }
        if (!file.canRead()) {
            throw new IllegalArgumentException("Image is not readable");
        }
        parse();
    }

    private BmpWriter(BmpWriterBuilder builder) {
        this.file = builder.file;
        this.fileData = builder.fileData;
        this.id = builder.id;
        this.fileSize = builder.fileSize;
        this.reservedBytes = builder.reservedBytes;
        this.pictureOffset = builder.pictureOffset;
        this.infoHeaderLength = builder.infoHeaderLength;
        this.width = builder.width;
        this.height = builder.height;
        this.numPlanes = builder.numPlanes;
        this.bitsPerPixel = builder.bitsPerPixel;
        this.compressionType = builder.compressionType;
        this.pictureSize = builder.pictureSize;
        this.horizontalResolution = builder.horizontalResolution;
        this.verticalResolution = builder.verticalResolution;
        this.numUsedColors = builder.numUsedColors;
        this.numImportantColors = builder.numImportantColors;
        this.pictureData = builder.pictureData;
        this.extraHeaderBytes = builder.extraHeaderBytes;
        this.extraPictureBytes = builder.extraPictureBytes;
    }

    /**
     * Parses the BMP this parser was initialized with, reading all header data and image data.
     *
     * @throws IOException If an I/O error occurs reading any of the file.
     * @see <a href="https://stackoverflow.com/a/10497780/2333689">Source for converting File to byte[]</a>
     */
    private void parse() throws IOException {
        fileData = Files.readAllBytes(file.toPath());
//        ByteArrayInputStream stream = new ByteArrayInputStream(fileData);
//        ArrayList<Byte> buffer = new ArrayList<>();
        id = new String(Arrays.copyOfRange(fileData, 0, 2));
        fileSize = bytesToInt(Arrays.copyOfRange(fileData, 2, 6));
        reservedBytes = Arrays.copyOfRange(fileData, 6, 10);
        pictureOffset = bytesToInt(Arrays.copyOfRange(fileData, 10, 14));
        infoHeaderLength = bytesToInt(Arrays.copyOfRange(fileData, 14, 18));
        width = bytesToInt(Arrays.copyOfRange(fileData, 18, 22));
        height = bytesToInt(Arrays.copyOfRange(fileData, 22, 26));
        numPlanes = bytesToInt(Arrays.copyOfRange(fileData, 26, 28));
        bitsPerPixel = bytesToInt(Arrays.copyOfRange(fileData, 28, 30));
        compressionType = bytesToInt(Arrays.copyOfRange(fileData, 30, 34));
        pictureSize = bytesToInt(Arrays.copyOfRange(fileData, 34, 38));
        horizontalResolution = bytesToInt(Arrays.copyOfRange(fileData, 38, 42));
        verticalResolution = bytesToInt(Arrays.copyOfRange(fileData, 42, 46));
        numUsedColors = bytesToInt(Arrays.copyOfRange(fileData, 46, 50));
        numImportantColors = bytesToInt(Arrays.copyOfRange(fileData, 50, 54));
        extraHeaderBytes = Arrays.copyOfRange(fileData, 54, pictureOffset);
        pictureData = Arrays.copyOfRange(fileData, pictureOffset, pictureOffset + pictureSize);
        if(pictureOffset + pictureSize < fileSize) {
            extraPictureBytes = Arrays.copyOfRange(fileData, pictureOffset + pictureSize, fileSize);
        }
    }

    /**
     * Writes the BMP to the same file this parser was initialized with.  Equivalent to {@code writeImage(file.getAbsolutePath())},
     * where {@code file} is the File representation of the file this parser was initialized with.
     */
    public void writeImage() throws IOException {
        writeImage(file.getAbsolutePath());
    }

    /**
     * Writes the BMP to the specified output file.  Only modifies the reserved bytes section, everything else is left as-is.
     *
     * @param outputPath The output file path.
     * @throws IOException If an I/O error occurs.
     */
    public void writeImage(String outputPath) throws IOException {
        //Update reserved bytes info
        int offset = 6;
        for (int i = 0; i < reservedBytes.length; i++) {
            fileData[i + offset] = reservedBytes[i];
        }
        Files.write(new File(outputPath).toPath(), fileData);
    }

    public String getId() {
        return id;
    }

    public int getFileSize() {
        return fileSize;
    }

    public byte[] getReservedBytes() {
        return reservedBytes;
    }

    public int getPictureOffset() {
        return pictureOffset;
    }

    public int getInfoHeaderLength() {
        return infoHeaderLength;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getNumPlanes() {
        return numPlanes;
    }

    public int getBitsPerPixel() {
        return bitsPerPixel;
    }

    public int getCompressionType() {
        return compressionType;
    }

    public int getPictureSize() {
        return pictureSize;
    }

    public int getHorizontalResolution() {
        return horizontalResolution;
    }

    public int getVerticalResolution() {
        return verticalResolution;
    }

    public int getNumUsedColors() {
        return numUsedColors;
    }

    public int getNumImportantColors() {
        return numImportantColors;
    }

    @Override
    public String toString() {
        return "BmpParser, parsed " + width + "x" + height + " bitmap image from " + file.getPath();
    }

    /**
     * Convert a byte array to int.
     *
     * @param bytes The bytes to convert to int.  {@code bytes.length} must be between 1 and 4.
     * @return The converted int.
     * @see <a href="https://stackoverflow.com/a/2383729/2333689">Source</a>
     */
    private int bytesToInt(byte[] bytes) {
        if (bytes.length > 4) {
            throw new IllegalArgumentException("Can only convert up to 4 bytes to int");
        } else if (bytes.length < 1) {
            throw new IllegalArgumentException("Need at least 1 byte to convert");
        }
        byte[] parsedBytes = new byte[4];
        for (int i = 0; i < bytes.length; i++) {
            parsedBytes[i] = bytes[i];
        }
        ByteBuffer bb = ByteBuffer.wrap(parsedBytes);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        return bb.getInt();
    }

    public static class BmpWriterBuilder {
        private File file;
        private byte[] fileData;
        private String id = "BM";
        private int fileSize;
        byte[] reservedBytes;
        int pictureOffset;
        private int infoHeaderLength;
        private int width, height;
        private int numPlanes;
        private int bitsPerPixel;
        private int compressionType;
        private int pictureSize;
        private int horizontalResolution;
        private int verticalResolution;
        private int numUsedColors;
        private int numImportantColors;
        private byte[] pictureData;
        private byte[] extraHeaderBytes;
        private byte[] extraPictureBytes = new byte[0];

        public BmpWriterBuilder file(File file) {
            this.file = file;
            return this;
        }

        public BmpWriterBuilder fileData(byte[] fileData) {
            this.fileData = fileData;
            return this;
        }

        public BmpWriterBuilder id(String id) {
            this.id = id;
            return this;
        }

        public BmpWriterBuilder fileSize(int fileSize) {
            this.fileSize = fileSize;
            return this;
        }

        public BmpWriterBuilder reservedBytes(byte[] reservedBytes) {
            this.reservedBytes = reservedBytes;
            return this;
        }

        public BmpWriterBuilder pictureOffset(int pictureOffset) {
            this.pictureOffset = pictureOffset;
            return this;
        }

        public BmpWriterBuilder infoHeaderLength(int infoHeaderLength) {
            this.infoHeaderLength = infoHeaderLength;
            return this;
        }

        public BmpWriterBuilder width(int width) {
            this.width = width;
            return this;
        }

        public BmpWriterBuilder height(int height) {
            this.height = height;
            return this;
        }

        public BmpWriterBuilder numPlanes(int numPlanes) {
            this.numPlanes = numPlanes;
            return this;
        }

        public BmpWriterBuilder bitsPerPixel(int bitsPerPixel) {
            this.bitsPerPixel = bitsPerPixel;
            return this;
        }

        public BmpWriterBuilder compressionType(int compressionType) {
            this.compressionType = compressionType;
            return this;
        }

        public BmpWriterBuilder pictureSize(int pictureSize) {
            this.pictureSize = pictureSize;
            return this;
        }

        public BmpWriterBuilder horizontalResolution(int horizontalResolution) {
            this.horizontalResolution = horizontalResolution;
            return this;
        }

        public BmpWriterBuilder verticalResolution(int verticalResolution) {
            this.verticalResolution = verticalResolution;
            return this;
        }

        public BmpWriterBuilder numUsedColors(int numUsedColors) {
            this.numUsedColors = numUsedColors;
            return this;
        }

        public BmpWriterBuilder numImportantColors(int numImportantColors) {
            this.numImportantColors = numImportantColors;
            return this;
        }

        public BmpWriterBuilder pictureData(byte[] pictureData) {
            this.pictureData = pictureData;
            return this;
        }

        public BmpWriterBuilder extraHeaderBytes(byte[] extraHeaderBytes) {
            this.extraHeaderBytes = extraHeaderBytes;
            return this;
        }

        public BmpWriterBuilder extraPictureBytes(byte[] extraPictureBytes) {
            this.extraPictureBytes = extraPictureBytes;
            return this;
        }

        public BmpWriter build() {
            return new BmpWriter(this);
        }
    }
}
