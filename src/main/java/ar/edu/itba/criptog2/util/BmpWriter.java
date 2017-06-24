package ar.edu.itba.criptog2.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Simple BMP writer. Initialized with basic BMP data, and can write out BMP to file.
 */
public class BmpWriter {

    private File file;
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

    private BmpWriter(BmpWriterBuilder builder) {
        this.file = builder.outputFile;
        this.id = builder.id;
        this.fileSize = 14 + 40 + extraPictureBytes.length + builder.pictureData.length;
        this.reservedBytes = builder.reservedBytes;
        this.pictureOffset = 14 + 40 + extraPictureBytes.length;
        this.infoHeaderLength = 40;
        this.width = builder.width;
        this.height = builder.height;
        this.numPlanes = builder.numPlanes;
        this.bitsPerPixel = builder.bitsPerPixel;
        this.compressionType = builder.compressionType;
        this.pictureSize = builder.pictureData.length;
        this.horizontalResolution = builder.horizontalResolution;
        this.verticalResolution = builder.verticalResolution;
        this.numUsedColors = builder.numUsedColors;
        this.numImportantColors = builder.numImportantColors;
        this.pictureData = builder.pictureData;
        this.extraHeaderBytes = builder.extraHeaderBytes;
        this.extraPictureBytes = new byte[0];
    }

    /**
     * Writes the BMP to the file this writer was initialized with.
     *
     * @throws IOException If an I/O error occurs.
     */
    public void writeImage() throws IOException {
        FileOutputStream fos = new FileOutputStream(this.file);
        fos.write(id.getBytes());
        fos.write(intToBytes(fileSize));
        fos.write(reservedBytes);
        fos.write(intToBytes(pictureOffset));
        fos.write(intToBytes(infoHeaderLength));
        fos.write(intToBytes(width));
        fos.write(intToBytes(height));
        fos.write(intToBytes(numPlanes));
        fos.write(intToBytes(bitsPerPixel));
        fos.write(intToBytes(compressionType));
        fos.write(intToBytes(pictureSize));
        fos.write(intToBytes(horizontalResolution));
        fos.write(intToBytes(verticalResolution));
        fos.write(intToBytes(numUsedColors));
        fos.write(intToBytes(numImportantColors));
        fos.write(extraHeaderBytes);
        fos.write(pictureData);
        fos.close();
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
     * Convert an int to byte array. See {@link BmpParser#bytesToInt(byte[])} for original source.
     *
     * @param number The int to convert to bytes.
     * @return The converted byte array.
     */
    private byte[] intToBytes(int number) {
        ByteBuffer bb = ByteBuffer.allocate(4);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        bb.putInt(number);
        return bb.array();
    }

    public static class BmpWriterBuilder {
        private File outputFile;
        private String id = "BM";
        byte[] reservedBytes;
//        int pictureOffset;
//        private int infoHeaderLength;
        private int width;
        private int height;
        private int numPlanes = 1;
        private int bitsPerPixel = 8;
        private int compressionType = 0;
        private int horizontalResolution = 0;
        private int verticalResolution = 0;
        private int numUsedColors = 256;
        private int numImportantColors = 0;
        private byte[] pictureData;
        private byte[] extraHeaderBytes = new byte[] {0, 0, 0, 0, 1, 1, 1, 0, 2, 2, 2, 0, 3, 3, 3, 0, 4, 4, 4, 0, 5, 5, 5, 0, 6, 6, 6, 0, 7, 7, 7, 0, 8, 8, 8, 0, 9, 9, 9, 0, 10, 10, 10, 0, 11, 11, 11, 0, 12, 12, 12, 0, 13, 13, 13, 0, 14, 14, 14, 0, 15, 15, 15, 0, 16, 16, 16, 0, 17, 17, 17, 0, 18, 18, 18, 0, 19, 19, 19, 0, 20, 20, 20, 0, 21, 21, 21, 0, 22, 22, 22, 0, 23, 23, 23, 0, 24, 24, 24, 0, 25, 25, 25, 0, 26, 26, 26, 0, 27, 27, 27, 0, 28, 28, 28, 0, 29, 29, 29, 0, 30, 30, 30, 0, 31, 31, 31, 0, 32, 32, 32, 0, 33, 33, 33, 0, 34, 34, 34, 0, 35, 35, 35, 0, 36, 36, 36, 0, 37, 37, 37, 0, 38, 38, 38, 0, 39, 39, 39, 0, 40, 40, 40, 0, 41, 41, 41, 0, 42, 42, 42, 0, 43, 43, 43, 0, 44, 44, 44, 0, 45, 45, 45, 0, 46, 46, 46, 0, 47, 47, 47, 0, 48, 48, 48, 0, 49, 49, 49, 0, 50, 50, 50, 0, 51, 51, 51, 0, 52, 52, 52, 0, 53, 53, 53, 0, 54, 54, 54, 0, 55, 55, 55, 0, 56, 56, 56, 0, 57, 57, 57, 0, 58, 58, 58, 0, 59, 59, 59, 0, 60, 60, 60, 0, 61, 61, 61, 0, 62, 62, 62, 0, 63, 63, 63, 0, 64, 64, 64, 0, 65, 65, 65, 0, 66, 66, 66, 0, 67, 67, 67, 0, 68, 68, 68, 0, 69, 69, 69, 0, 70, 70, 70, 0, 71, 71, 71, 0, 72, 72, 72, 0, 73, 73, 73, 0, 74, 74, 74, 0, 75, 75, 75, 0, 76, 76, 76, 0, 77, 77, 77, 0, 78, 78, 78, 0, 79, 79, 79, 0, 80, 80, 80, 0, 81, 81, 81, 0, 82, 82, 82, 0, 83, 83, 83, 0, 84, 84, 84, 0, 85, 85, 85, 0, 86, 86, 86, 0, 87, 87, 87, 0, 88, 88, 88, 0, 89, 89, 89, 0, 90, 90, 90, 0, 91, 91, 91, 0, 92, 92, 92, 0, 93, 93, 93, 0, 94, 94, 94, 0, 95, 95, 95, 0, 96, 96, 96, 0, 97, 97, 97, 0, 98, 98, 98, 0, 99, 99, 99, 0, 100, 100, 100, 0, 101, 101, 101, 0, 102, 102, 102, 0, 103, 103, 103, 0, 104, 104, 104, 0, 105, 105, 105, 0, 106, 106, 106, 0, 107, 107, 107, 0, 108, 108, 108, 0, 109, 109, 109, 0, 110, 110, 110, 0, 111, 111, 111, 0, 112, 112, 112, 0, 113, 113, 113, 0, 114, 114, 114, 0, 115, 115, 115, 0, 116, 116, 116, 0, 117, 117, 117, 0, 118, 118, 118, 0, 119, 119, 119, 0, 120, 120, 120, 0, 121, 121, 121, 0, 122, 122, 122, 0, 123, 123, 123, 0, 124, 124, 124, 0, 125, 125, 125, 0, 126, 126, 126, 0, 127, 127, 127, 0, -128, -128, -128, 0, -127, -127, -127, 0, -126, -126, -126, 0, -125, -125, -125, 0, -124, -124, -124, 0, -123, -123, -123, 0, -122, -122, -122, 0, -121, -121, -121, 0, -120, -120, -120, 0, -119, -119, -119, 0, -118, -118, -118, 0, -117, -117, -117, 0, -116, -116, -116, 0, -115, -115, -115, 0, -114, -114, -114, 0, -113, -113, -113, 0, -112, -112, -112, 0, -111, -111, -111, 0, -110, -110, -110, 0, -109, -109, -109, 0, -108, -108, -108, 0, -107, -107, -107, 0, -106, -106, -106, 0, -105, -105, -105, 0, -104, -104, -104, 0, -103, -103, -103, 0, -102, -102, -102, 0, -101, -101, -101, 0, -100, -100, -100, 0, -99, -99, -99, 0, -98, -98, -98, 0, -97, -97, -97, 0, -96, -96, -96, 0, -95, -95, -95, 0, -94, -94, -94, 0, -93, -93, -93, 0, -92, -92, -92, 0, -91, -91, -91, 0, -90, -90, -90, 0, -89, -89, -89, 0, -88, -88, -88, 0, -87, -87, -87, 0, -86, -86, -86, 0, -85, -85, -85, 0, -84, -84, -84, 0, -83, -83, -83, 0, -82, -82, -82, 0, -81, -81, -81, 0, -80, -80, -80, 0, -79, -79, -79, 0, -78, -78, -78, 0, -77, -77, -77, 0, -76, -76, -76, 0, -75, -75, -75, 0, -74, -74, -74, 0, -73, -73, -73, 0, -72, -72, -72, 0, -71, -71, -71, 0, -70, -70, -70, 0, -69, -69, -69, 0, -68, -68, -68, 0, -67, -67, -67, 0, -66, -66, -66, 0, -65, -65, -65, 0, -64, -64, -64, 0, -63, -63, -63, 0, -62, -62, -62, 0, -61, -61, -61, 0, -60, -60, -60, 0, -59, -59, -59, 0, -58, -58, -58, 0, -57, -57, -57, 0, -56, -56, -56, 0, -55, -55, -55, 0, -54, -54, -54, 0, -53, -53, -53, 0, -52, -52, -52, 0, -51, -51, -51, 0, -50, -50, -50, 0, -49, -49, -49, 0, -48, -48, -48, 0, -47, -47, -47, 0, -46, -46, -46, 0, -45, -45, -45, 0, -44, -44, -44, 0, -43, -43, -43, 0, -42, -42, -42, 0, -41, -41, -41, 0, -40, -40, -40, 0, -39, -39, -39, 0, -38, -38, -38, 0, -37, -37, -37, 0, -36, -36, -36, 0, -35, -35, -35, 0, -34, -34, -34, 0, -33, -33, -33, 0, -32, -32, -32, 0, -31, -31, -31, 0, -30, -30, -30, 0, -29, -29, -29, 0, -28, -28, -28, 0, -27, -27, -27, 0, -26, -26, -26, 0, -25, -25, -25, 0, -24, -24, -24, 0, -23, -23, -23, 0, -22, -22, -22, 0, -21, -21, -21, 0, -20, -20, -20, 0, -19, -19, -19, 0, -18, -18, -18, 0, -17, -17, -17, 0, -16, -16, -16, 0, -15, -15, -15, 0, -14, -14, -14, 0, -13, -13, -13, 0, -12, -12, -12, 0, -11, -11, -11, 0, -10, -10, -10, 0, -9, -9, -9, 0, -8, -8, -8, 0, -7, -7, -7, 0, -6, -6, -6, 0, -5, -5, -5, 0, -4, -4, -4, 0, -3, -3, -3, 0, -2, -2, -2, 0, -1, -1, -1, 0};

        public BmpWriterBuilder file(File file) {
            this.outputFile = file;
            return this;
        }

        public BmpWriterBuilder id(String id) {
            this.id = id;
            return this;
        }

        public BmpWriterBuilder reservedBytes(byte[] reservedBytes) {
            this.reservedBytes = reservedBytes;
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

        public BmpWriterBuilder compressionType(int compressionType) {
            this.compressionType = compressionType;
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

        public BmpWriter build() {
            return new BmpWriter(this);
        }
    }
}
