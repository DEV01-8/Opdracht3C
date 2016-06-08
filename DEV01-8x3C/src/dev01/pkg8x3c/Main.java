/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dev01.pkg8x3c;

import java.text.DecimalFormat;
import java.util.ArrayList;
import javax.swing.JOptionPane;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import processing.core.PApplet;
import static processing.core.PApplet.map;
import processing.core.PVector;

/**
 *
 * @author Johan Bos
 */
public class Main extends PApplet {

    private ArrayList<PVector> results = new ArrayList();
    private final ArrayList<PVector> mappings = new ArrayList();

    private final float START_X = 92799f;
    private final float START_Y = 436964f;
    private final float MIN_X = START_X - 1000f;
    private final float MAX_X = START_X + 1000f;
    private final float MIN_Y = START_Y - 1000f;
    private final float MAX_Y = START_Y + 1000f;
    float waterLevel = -5f;
    float raiseWater = 0.100f;
    int frames = 8;
    final Logger logger = Logger.getLogger(Main.class);
    boolean firstRun = true;
    boolean pause = false;

    @Override
    public void setup() {
        background(255, 255, 255);
        textSize(13);
        frameRate(1);
        surface.setTitle("Hoogtebestand Rotterdam Oost");

        results = CSVParser.read();  //Get all items from parseCSV
        startMap();                 //use map() method to convert RDX and RDY to pixels

        //Show message about controls
        JOptionPane.showMessageDialog(frame, "Controls:\n"
                + "S: Resume \n"
                + "P: Pause \n"
                + "R: Reset \n"
                + "Q: Quit \n"
                + "1: Slow speed \n"
                + "2: Medium speed \n"
                + "3: High speed"
        );
    }

    @Override
    public void settings() {
        size(680, 680);
    }

    public static void main(String[] args) {
        //Logger4J
        BasicConfigurator.configure();
        PApplet.main(new String[]{Main.class.getName()});
    }

    //Method to map xyz coordinates
    private void startMap() {
        float MIN_Z = CSVParser.MIN_Z;     //min value of Z ~ -16
        float MAX_Z = CSVParser.MAX_Z;     //max value of z ~ 215

        for (PVector result : results) {
            float mapX = map(result.x, MIN_X, MAX_X, 0, width);         //map x
            float mapY = map(result.y, MAX_Y, MIN_Y, 0, height);        //map y
            float mapZ = map(result.z, MIN_Z, MAX_Z, 0, 216);           //map z
            PVector mappedVector = new PVector(mapX, mapY, mapZ);       //PVector holding all mapped values
            mappings.add(mappedVector);                                 //ArrayList of PVectors holding mapped values
        }
    }

    private void createMap() {
        if (firstRun == true) {
            for (PVector mapping : mappings) {
                float mapX = mapping.x;
                float mapY = mapping.y;
                float mapZ = mapping.z;

                if (mapZ > 4.0f && mapZ < 21.5f) {      //Color of ground and roads
                    stroke(color(196, 193, 186));
                    fill(color(211, 208, 201));
                } else {                                //Color of top of building
                    stroke(color(247, 245, 239));
                    fill(color(242, 240, 234));
                }

                rect(mapX, mapY, 13f, 13f);            //create rect at points of mapped xy
            }

            firstRun = false;                       //Set firstRun to false to create water

        } else if (firstRun == false) {
            if (pause == false) {
                for (PVector mapping : mappings) {
                    float mapX = mapping.x;
                    float mapY = mapping.y;
                    float mapZ = mapping.z;

                    if (waterLevel > mapZ) {                     //Color of water
                        stroke(color(0, 153, 153));
                        fill(color(0, 153, 153));

                        ellipse(mapX, mapY, 2f, 2f);            //create ellipse at points of mapped x
                    }
                }
            }
        }
    }

    private void reset() {
        logger.info("Resetting...");
        firstRun = true;
        background(255, 255, 255);
        setup();
    }

    private void quit() {
        logger.info("Quitting...");
        System.exit(0);
    }

    @Override
    public void draw() {
        //Use DecimalFormat to only show two digits
        DecimalFormat df = new DecimalFormat();
        df.setMaximumFractionDigits(2);

        //Draw all points
        createMap();

        //Increase waterlevel every six frames and display value
        if (pause == false && frameCount % frames == 0) {
            fill(255, 255, 255);
            rect(0, 0, 168, 35);

            //Increase water level by 0.11
            waterLevel = waterLevel + raiseWater;

            //Show value waterLevel
            fill(0, 0, 0);
            text("Water Level (m): " + df.format(waterLevel), 32, 16);
        }

        //Show circle green if running and red if paused
        if (pause == false) {
            fill(0, 200, 0);
            ellipse(12, 11, 18, 18);
        } else {
            fill(200, 0, 0);
            ellipse(12, 11, 18, 18);
        }

        //Show current speed
        fill(0, 0, 0);
        switch (frames) {
            case 8:
                text("Speed: Slow", 32, 30);
                break;
            case 4:
                text("Speed: Medium", 32, 30);
                break;
            case 2:
                text("Speed: High", 32, 30);
                break;
            default:
                text("Speed: Slow", 32, 30);
                break;
        }
    }

    @Override
    public void keyPressed() {
        switch (key) {
            case 's':                       //Start, Resume
                pause = false;
                raiseWater = 0.11f;
                logger.info("Resuming...");
                break;
            case 'p':                       //Pause
                pause = true;
                raiseWater = 0.0f;
                logger.info("Paused...");
                break;
            case 'r':                       //Reset
                reset();
                break;
            case 'q':                       //Quit
                quit();
                break;
            case '1':                       //Speed 1: update every 6 frames
                frames = 8;
                break;
            case '2':                       //Speed 2: update every 4 frames
                frames = 4;
                break;
            case '3':                       //Speed 3: update every 2 frames
                frames = 2;
                break;
            default:
                frames = 6;
                pause = false;
                break;
        }
    }
}
