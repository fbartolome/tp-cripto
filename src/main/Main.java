package main;

import util.BmpParser;

import java.io.IOException;
import java.util.Scanner;

/**
 * Main class, program runner.
 */
public class Main {

    public static void main(String[] args) {
        Scanner s = new Scanner(System.in);
        System.out.print("Enter bitmap image path: ");
        String path = s.nextLine();
        BmpParser parser = new BmpParser(path);
        try {
            byte[][] data = parser.readAllFiles();
            System.out.println(data);
        } catch (IOException e) {
            System.err.println("Could not read all files: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
