package com.cryptostore.client;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.BitSet;

public class SteganographyManager {

    public static byte[] hide(String imgPath, byte[] fileBytes) throws IOException {
        BufferedImage bi = ImageIO.read(new File(imgPath));
        int[][][] imgArray = getImageArray(bi);
        int[][][] modifiedImgArray = encrypt(fileBytes, imgArray);

        File f = buildImgFromArray(imgPath, modifiedImgArray);
        byte[] out = Files.readAllBytes(f.toPath());
        f.delete();

        return out;
    }

    public static byte[] retrieve(String filePath) throws IOException {
            BufferedImage mbi = ImageIO.read(new File(filePath));
            int[][][] retrievedModifiedImageArray = getImageArray(mbi);

            return decrypt(retrievedModifiedImageArray);
    }

    private static int[][][] getImageArray(BufferedImage bi) {
        int[][][] imageArray = new int[bi.getWidth()][bi.getHeight()][3];

        for (int x=0; x<bi.getWidth(); x++) {
            for (int y=0; y<bi.getHeight(); y++) {
                Color c = new Color(bi.getRGB(x, y));
                imageArray[x][y][0] = c.getRed();
                imageArray[x][y][1] = c.getGreen();
                imageArray[x][y][2] = c.getBlue();
            }
        }

        return imageArray;
    }

    private static int[][][] encrypt(byte[] bytesIn, int[][][] imageArray) throws IOException {
        BitSet msg = BitSet.valueOf(bytesIn);
        int msgIndex = 0;

        BitSet sizeInBits = BitSet.valueOf(new long[]{Long.valueOf(msg.length()).longValue()});
        int sizeIndex = 0;

        for (int j=imageArray[0].length-1; j>=0; j--) {
            for (int k=imageArray[0][0].length-1; k>=0; k--) {
                if (sizeIndex < sizeInBits.length()) {
                    imageArray[0][j][k] = changeNthSignificantBit(imageArray[0][j][k], 1, sizeInBits.get(sizeIndex));
                    sizeIndex++;
                } else {
                    imageArray[0][j][k] = changeNthSignificantBit(imageArray[0][j][k], 1, false);
                }
            }
        }

        startOfLoop:
        for (int i=1; i<imageArray.length; i++) {
            for (int j=0; j<imageArray[0].length; j++) {
                for (int k=0; k<imageArray[0][0].length; k++) {
                    if (msgIndex < msg.length()) {
                        imageArray[i][j][k] = changeNthSignificantBit(imageArray[i][j][k], 1, msg.get(msgIndex));
                        msgIndex++;
                    } else {
                        break startOfLoop;
                    }
                }
            }
        }

        return imageArray;
    }

    private static byte[] decrypt(int[][][] imageArray) throws IOException {
        BitSet sizeBits = new BitSet();
        boolean foundFirstTrue = false;

        for (int j=0; j<imageArray[0].length; j++) {
            for (int k=0; k<imageArray[0][0].length; k++) {
                if (foundFirstTrue || getNthSignificantBit(imageArray[0][j][k], 1)) {
                    int length = imageArray[0].length;
                    int depth = imageArray[0][0].length;
                    sizeBits.set(((length*depth - j*depth)-k-1), getNthSignificantBit(imageArray[0][j][k], 1));
                    foundFirstTrue = true;
                }
            }
        }

        int size = bitSetToInt(sizeBits);
        BitSet fileBits = new BitSet();
        int fileIndex = 0;

        startOfLoop:
        for (int i=1; i<imageArray.length; i++) {
            for (int j=0; j<imageArray[0].length; j++) {
                for (int k=0; k<imageArray[0][0].length; k++) {
                    if (fileIndex < size) {
                        fileBits.set(fileIndex, getNthSignificantBit(imageArray[i][j][k], 1));
                        fileIndex++;
                    } else {
                        break startOfLoop;
                    }
                }
            }
        }

        return fileBits.toByteArray();
    }

    private static int bitSetToInt(BitSet bitSet) {
        int bitInteger = 0;

        for (int i = 0; i < 32; i++){
            if (bitSet.get(i)) {
                bitInteger |= (1 << i);
            }
        }
        return bitInteger;
    }

    private static int changeNthSignificantBit(int x, int n, boolean newValue) { //boolean representing 1 or 0
        String currentValue = Integer.toBinaryString(x);

        if (n==1) {
            return Integer.parseInt(currentValue.substring(0, currentValue.length() - 1) + (newValue ? 1 : 0), 2);
        } else if (n==2) {
            return Integer.parseInt(currentValue.substring(0, currentValue.length() - 2) + (newValue ? 1 : 0) + currentValue.substring(currentValue.length() - 1), 2);
        } else if (n==3) {
            return Integer.parseInt(currentValue.substring(0, currentValue.length() - 3) + (newValue ? 1 : 0) + currentValue.substring(currentValue.length() - 2), 2);
        } else {
            return -1;
        }
    }

    private static boolean getNthSignificantBit(int value, int index) {
        String currentValue = Integer.toBinaryString(value);

        return Integer.parseInt(String.valueOf(currentValue.charAt(currentValue.length()-index))) == 1;
    }

    private static File buildImgFromArray(String destination, int[][][] imageArray) throws IOException {
        BufferedImage image = new BufferedImage(imageArray.length, imageArray[0].length, BufferedImage.TYPE_3BYTE_BGR);

        for (int x=0; x<image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
               image.setRGB(x, y, new Color(imageArray[x][y][0], imageArray[x][y][1], imageArray[x][y][2]).getRGB());
            }
        }

        File f = new File("MOD"+destination);
        ImageIO.write(image, "png", f);

        return f;
    }
}