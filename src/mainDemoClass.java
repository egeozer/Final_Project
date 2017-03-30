

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Map;

import org.json.simple.parser.ParseException;

import lejos.hardware.*;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.lcd.TextLCD;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.port.Port;
import lejos.hardware.sensor.*;
import lejos.robotics.SampleProvider;

public class mainDemoClass {

	// Static Resources:
	// Left motor connected to output A
	// Right motor connected to output D
	// Ultrasonic sensor port connected to input S1
	// Color sensor port connected to input S2
	private static final EV3LargeRegulatedMotor leftMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("A"));
	private static final EV3LargeRegulatedMotor rightMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("D"));
	private static final Port usPort = LocalEV3.get().getPort("S1");		
	private static final Port colorPortRight = LocalEV3.get().getPort("S2");		
	private static final Port colorPortLeft = LocalEV3.get().getPort("S3");		
	private static final String SERVER_IP = "192.168.2.14";			//  TA Server: 192.168.2.3
	private static final int TEAM_NUMBER = 9;
	

	// Enable/disable printing of debug info from the WiFi class
	private static final boolean ENABLE_DEBUG_WIFI_PRINT = true;
	
	// Constants
	static double squareSize = 30.48;
	
	// Initialize Variables given by WiFi
	// Player variables:
	static int forwardNum;
	static int forwardStartPos;
	static int defenderNum;
	static int defenderStartPos;
	
	// Playing field variables:
	static int d1;					// forward line position
	static int w1;					// defender zone dimensions
	static int w2;
	
	// Ball dispenser variables:
	static int bx;
	static int by;
	static String dispOrientation;
	
	public static void main(String[] args) {
				
		// Setup ultrasonic sensor
		// 1. Create a port object attached to a physical port (done above)
		// 2. Create a sensor instance and attach to port
		// 3. Create a sample provider instance for the above and initialize operating mode
		// 4. Create a buffer for the sensor data
		@SuppressWarnings("resource")							    	// Because we don't bother to close this resource
		SensorModes usSensor = new EV3UltrasonicSensor(usPort);
		SampleProvider usValue = usSensor.getMode("Distance");			// colorValue provides samples from this instance
		float[] usData = new float[usValue.sampleSize()];				// colorData is the buffer in which data are returned
		
		// Setup color sensor
		// 1. Create a port object attached to a physical port (done above)
		// 2. Create a sensor instance and attach to port
		// 3. Create a sample provider instance for the above and initialize operating mode
		// 4. Create a buffer for the sensor data
		SensorModes colorSensorRight = new EV3ColorSensor(colorPortRight);
		SensorModes colorSensorLeft = new EV3ColorSensor(colorPortLeft);
		SampleProvider colorValueRight = colorSensorRight.getMode("Red");			// colorValue provides samples from this instance
		SampleProvider colorValueLeft = colorSensorLeft.getMode("Red");
		float[] colorDataRight = new float[colorValueRight.sampleSize()];			// colorData is the buffer in which data are returned
		float[] colorDataLeft = new float[colorValueLeft.sampleSize()];
			
		// Setup synchronized motors
		leftMotor.synchronizeWith(new EV3LargeRegulatedMotor[] {rightMotor});
		
		// Setup the odometer and display
		Odometer odo = new Odometer(leftMotor, rightMotor, 30, true, forwardStartPos);
		Navigation navi = new Navigation(odo,colorValueRight, colorDataRight,colorValueLeft, colorDataLeft);
		wallObstacle wall = new wallObstacle(leftMotor, rightMotor, odo, navi, usValue, usData);
		demoTestMotors launch = new demoTestMotors();
		
		// initialize the light sensor classes
		lightLeft left = new lightLeft(colorSensorLeft, colorDataLeft, odo );
		lightRight right = new lightRight(colorSensorRight, colorDataRight, odo );
		lightCorrector corrector = new lightCorrector(odo, navi, colorValueRight, colorDataRight,colorValueLeft, colorDataLeft);	
		
		
		// initialize display
		LCDInfo lcd = new LCDInfo(odo);
		int buttonChoice;
		Sound.setVolume(40);
				
		/*
		 * getData() will connect to the server and wait until the user/TA
		 * presses the "Start" button in the GUI on their laptop with the
		 * data filled in. Once it's waiting, you can kill it by
		 * pressing the upper left hand corner button (back/escape) on the EV3.
		 * getData() will throw exceptions if it can't connect to the server
		 * (e.g. wrong IP address, server not running on laptop, not connected
		 * to WiFi router, etc.). It will also throw an exception if it connects 
		 * but receives corrupted data or a message from the server saying something 
		 * went wrong. For example, if TEAM_NUMBER is set to 1 above but the server expects
		 * teams 17 and 5, this robot will receive a message saying an invalid team number 
		 * was specified and getData() will throw an exception letting you know.
		 */
		
		// Initialize the wifi connection class
		/*WifiConnection conn = new WifiConnection(SERVER_IP, TEAM_NUMBER, ENABLE_DEBUG_WIFI_PRINT);
				
		// Connect to server and get the data, catching any errors that might occur
		try {
			
			Map data = conn.getData();

			// Example 1: Print out all received data
			//System.out.println("Map:\n" + data);

			// Example 2 : Print out specific values
			//int fwdTeam = ((Long) data.get("FWD_TEAM")).intValue();
			//System.out.println("Forward Team: " + fwdTeam);
			//System.out.println("Defender zone size w1: " + w1);
			
			// Example 3: Compare value
			/*String orientation = (String) data.get("omega");
			if (orientation.equals("N")) {
				System.out.println("Orientation is North");
			}
			else {
				System.out.println("Orientation is not North");
			}*/
			
			// Variables given by Wifi
			// Player variables:
		/*	forwardNum = ((Long) data.get("FWD_TEAM")).intValue();
			forwardStartPos = ((Long) data.get("FWD_CORNER")).intValue();
			defenderNum = ((Long) data.get("DEF_TEAM")).intValue();
			defenderStartPos = ((Long) data.get("DEF_CORNER")).intValue();
			
			// Playing field variables:
			d1 = ((Long) data.get("d1")).intValue();			// forward line position
			w1 = ((Long) data.get("w1")).intValue();					// defender zone dimensions
			w2 = ((Long) data.get("w2")).intValue();
			
			// Ball dispenser variables:
			int omega = 0;
			int initTheta = 0;
			bx = ((Long) data.get("bx")).intValue();
			by = ((Long) data.get("by")).intValue();
			dispOrientation = (String) data.get("omega");			

		} catch (Exception e) {
			System.err.println("Error: " + e.getMessage());
		}	
		
		*/
		/*
		 *  Beginning of actual "functional" code
		 */
		
		// Robot will beep once it has set up the sensors and display and is ready to begin receiving instructions from WiFi
		Sound.setVolume(60);
		Sound.beep();
		
		// important constants for dispenser and ball launcher
		int omega = 0;
		double initAng = 0;
		dispOrientation = "E";
		bx = -1;
		by = 8;
		
		int targetX = 5;
		d1 = 8;
		int targetY = 10-d1;
		
		// Mitchell's testing lines, aka trash code
		
		
		//navi.travelToXY((4-1)*squareSize, (2)*squareSize, odo);
		//navi.lsTravelTo(1*squareSize, 3*squareSize, odo, colorSensorRight, colorDataRight, colorSensorLeft, colorDataLeft);
		//launch.load(odo, navi, omega, initAng);
		//launch.launcher3();
		
		//Sound.beep();
		//LightLocalizer lsl = new LightLocalizer(odo, navi, colorValueRight, colorDataRight,colorValueLeft, colorDataLeft);
		//lsl.doLocalization(odo, navi, colorValueRight, colorDataRight,colorValueLeft, colorDataLeft);
		//launch.load(odo, navi);
		//launch.launcher3();
		//navi.clawTurnTo(0, true);
		//Sound.beep();
		//navi.clawTurnTo(180,true);
		//Sound.beep();
		//navi.clawTurnTo(90, true);
		//Sound.beep();
		
		
			
		// Robot will beep once it has received the Wifi instructions and is ready to localize
		Sound.beep();
		
		// perform the ultrasonic localization using Falling Edge
		USLocalizer usl = new USLocalizer(odo, navi, usValue, usData, USLocalizer.LocalizationType.FALLING_EDGE);
		usl.doLocalization();
		
		
		// perform the light localization using two light sensors
		outer:while(true){
			
			if (usl.isLocalized) {
				LightLocalizer lsl = new LightLocalizer(odo, navi, colorValueRight, colorDataRight,colorValueLeft, colorDataLeft);		
				lsl.doLocalization(odo, navi, colorValueRight, colorDataRight,colorValueLeft, colorDataLeft);
				Sound.beep();
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				Sound.beep();
				break outer;
			}
			
		}
		
		// set position to (0,0,0), and then beep twice
		odo.setPosition(new double [] {0,0,0}, new boolean [] {true, true, true});
		Sound.beep();
		Sound.beep();
		
		// start driving towards the ball dispenser
		//corrector.correct(odo, navi, colorSensorRight, colorDataRight, colorSensorLeft, colorDataLeft);
		//wall.start();
		
		if(dispOrientation.equals("E")){
			
			Sound.beep();
			navi.travelToXY(2*squareSize, 0*squareSize, odo);
			
			navi.travelToXY(2*squareSize, 6*squareSize, odo);
			
			navi.travelToXY(0*squareSize, 6*squareSize, odo);
			
			navi.travelToXY(0*squareSize, 0*squareSize, odo);
			//navi.travelToXY((bx + 1.7)*squareSize, (by-0.5)*squareSize, odo);
			//corrector.correct(odo, navi, colorSensorRight, colorDataRight, colorSensorLeft, colorDataLeft);
			//odo.setPosition(new double [] {0,0,90}, new boolean [] {false, false, true});
			//navi.travelToXY((bx + 1)*squareSize, (by)*squareSize, odo);
			//navi.travelToXY((1-0.5)*squareSize, (5-0.5)*squareSize, odo);
			//omega = 0;
			//initAng= odo.getAng();
			//navi.turnTo(0,true);
			//launch.load(odo, navi, omega, initAng);
			
			Sound.beep();

		}
		else if(dispOrientation.equals("W")){
			navi.travelToXY((bx - 1.33)*squareSize, by*squareSize, odo);
			navi.turnTo(180,true);
		}
		else if(dispOrientation.equals("N")){
			navi.travelToXY(bx*squareSize, (by + 1.33)*squareSize, odo);
			navi.turnTo(90,true);
		}
		else if(dispOrientation.equals("S")){
			navi.travelToXY(bx*squareSize, (by - 1.33)*squareSize, odo);
			navi.turnTo(270,true);
		}
		
		////////////////////////////////////////////
		// Mitchell's preliminary code for getting to the firing line and shooting
		
		// travel to lauching position, with an offset to allow for localization
		//navi.travelToXY(targetX - (0.5*squareSize), targetY - (0.5*squareSize), odo);
		
		// redo localization at launching position
		//LightLocalizer lsl = new LightLocalizer(odo, navi, colorValueRight, colorDataRight,colorValueLeft, colorDataLeft);		
		//lsl.doLocalization(odo, navi, colorValueRight, colorDataRight,colorValueLeft, colorDataLeft);
		//corrector.correct(odo, navi, colorSensorRight, colorDataRight, colorSensorLeft, colorDataLeft);
		//launch.launcher3();
		
  	  	/////////////////////////////////////////////
		
		
		
		
	}
}
