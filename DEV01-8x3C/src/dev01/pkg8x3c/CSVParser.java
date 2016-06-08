/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dev01.pkg8x3c;

import com.opencsv.CSVReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import org.apache.log4j.Logger;
import processing.core.PVector;

/**
 *
 * @author Johan Bos <Johan Bos at jhnbos.nl>
 */
public class CSVParser {

    private static final ArrayList<PVector> points = new ArrayList();
    public static float MAX_Z = Float.MIN_VALUE;
    public static float MIN_Z = Float.MAX_VALUE;
    private static final float START_X = 92799f;
    private static final float START_Y = 436964f;
    final static Logger logger = Logger.getLogger(CSVParser.class);

    public static ArrayList<PVector> read() {
        try {
            logger.info("Reading CSV...");

            File path = new File("C:\\dev\\oost.csv");
            char[] separator = {',', '\''};
            int skipLine = 1;

            //read csv using opencsv library
            CSVReader reader = new CSVReader(new FileReader(path), separator[0], separator[1], skipLine);
            String[] nextLine;

            logger.info("Going through CSV...");

            //start time to see how long it takes
            long startTime = System.currentTimeMillis();

            while ((nextLine = reader.readNext()) != null) {
                // nextLine[] is an array of values from the line
                float z = Float.parseFloat(nextLine[2]);
                PVector tempVector = new PVector(Float.parseFloat(nextLine[0]), Float.parseFloat(nextLine[1]), z);

                //Limit of Map
                float limitX = (START_X - tempVector.x);
                float limitY = (START_Y - tempVector.y);

                //if x and y are equal or lower add to arraylist
                if (limitX <= 1000 && limitY <= 1000) {
                    points.add(tempVector);
                }
                if (MIN_Z > z) {
                    MIN_Z = z;
                }
                if (MAX_Z < z) {
                    MAX_Z = z;
                }
            }

            reader.close();

            //stop timer
            long stopTime = System.currentTimeMillis();
            long elapsedTime = stopTime - startTime;

            logger.info("Done going through csv!");
            logger.info("Time elapsed: " + (elapsedTime / 1000) + " sec");
            logger.info("Amount of items in ArrayList: " + points.size());

        } catch (IOException | NumberFormatException e) {
            logger.info(e);
        }

        return points;
    }
}
