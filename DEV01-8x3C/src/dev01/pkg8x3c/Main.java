/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dev01.pkg8x3c;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
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
    //ArrayLists
    private ArrayList<PVector> results = new ArrayList();
    private final ArrayList<PVector> mappings = new ArrayList();
    //Floats
    private final float START_X = 92799f;
    private final float START_Y = 436964f;
    private final float MIN_X = START_X - 1000f;
    private final float MAX_X = START_X + 1000f;
    private final float MIN_Y = START_Y - 1000f;
    private final float MAX_Y = START_Y + 1000f;
    float waterLevel = -5f;
    float raiseWater = 0.100f;
    //Integers
    int frames = 8;
    int limit = 1000;
    //Logger
    final Logger logger = Logger.getLogger(Main.class);
    //Booleans
    boolean firstRun = true;
    boolean pause = false;

    
    @Override
    public void setup() {
        background(255, 255, 255);
        textSize(13);
        frameRate(1);
        surface.setTitle("Simulatie Overstroming Rotterdam Oost Hoogtebestand 2012");

        results = CSVParser.read(limit);    //Get all items from parseCSV
        startMap();                         //use map() method to convert RDX and RDY to pixels
        
        //Show message about controls
        JOptionPane.showMessageDialog(frame, "Controls:\n"
                + "S: Resume \n"
                + "P: Pause \n"
                + "R: Reset \n"
                + "Q: Quit \n"
                + "1: Slow speed \n"
                + "2: Medium speed \n"
                + "3: High speed \n"
                + "4: Size 500x500 \n"
                + "5: Size 1000x1000 \n"
                + "6: Take screenshot"
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

        results.clear();
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
        setup();
    }

    private void quit() {
        logger.info("Quitting...");
        JOptionPane.showMessageDialog(frame, "Goodbye!");
        System.exit(0);
    }
    
    private void takeScreenshot(){
        Date date = new Date();
        saveFrame("../Images/screenshot_" + date.getTime() + ".png");
        logger.info("Screenshot taken @ " + date.getTime());
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
            //White rectangle
            fill(255, 255, 255);
            rect(250, 0, 168, 50);
        
            //Increase water level by 0.10
            waterLevel = waterLevel + raiseWater;

            //Show value waterLevel
            fill(0, 0, 0);
            text("Water Level (m): " + df.format(waterLevel), 282, 16);
        }

        //Show circle green if running and red if paused
        if (pause == false) {
            fill(0, 200, 0);
            ellipse(262, 21, 18, 18);
        } else {
            fill(200, 0, 0);
            ellipse(262, 21, 18, 18);
        }

        fill(0, 0, 0);

        //Show size
        if (limit == 500) {
            text("Size: 500x500", 282, 45);
        } else {
            text("Size: 1000x1000", 282, 45);
        }

        //Show current speed
        fill(0, 0, 0);
        switch (frames) {
            case 8:
                text("Speed: Slow", 282, 30);
                break;
            case 4:
                text("Speed: Medium", 282, 30);
                break;
            case 2:
                text("Speed: High", 282, 30);
                break;
            default:
                text("Speed: Slow", 282, 30);
                break;
        }
    }

    @Override
    //Controls
    public void keyPressed() {
        switch (key) {
            case 's':                       //Start, Resume
                pause = false;
                raiseWater = 0.100f;
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
            case '4':                       //500x500
                limit = 500;
                reset();
                break;
            case '5':                       //1000x1000
                limit = 1000;
                reset();
                break;
            case '6':                       //Save screenshot
                takeScreenshot();
                break;
            default:
                frames = 6;
                pause = false;
                limit = 1000;
                break;
        }
    }
}
