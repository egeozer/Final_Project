

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
		WifiConnection conn = new WifiConnection(SERVER_IP, TEAM_NUMBER, ENABLE_DEBUG_WIFI_PRINT);
				
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
			forwardNum = ((Long) data.get("FWD_TEAM")).intValue();
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
		
		
		
		////////////////////////////////////////////////
		// Beginning of actual "functional" code
		///////////////////////////////////////////////
		
		// Robot will set up the sensors and display before is ready to begin receiving instructions from WiFi
		//Sound.setVolume(80);
		//Sound.beep();
		
		// important constants for testing the dispenser and ball launcher
		/*dispOrientation = "N";
		bx = 5;
		by = 0;
		w2 = 2;
		
		forwardNum = 9;
		defenderNum = 0;
		forwardStartPos = 1;
		*/
		int targetX = 5;			// final design: value is 5
		//d1 = 8;						// final design: [5,8]
		int fireLineY = 10-d1;		// final design: value is 10-d1
		int defLineY = 10-w2;
		
		///////////////////////////////////////////////
		// Robot will beep once it has received the Wifi instructions and is ready to localize
		Sound.setVolume(80);
		Sound.beep();
		
		// perform the ultrasonic localization using Falling Edge
		USLocalizer usl = new USLocalizer(odo, navi, usValue, usData, USLocalizer.LocalizationType.FALLING_EDGE);
		usl.doLocalization();
			
		// perform the light localization using two light sensors
		outer:while(true){			
			if (usl.isLocalized) {
				LightLocalizer lsl = new LightLocalizer(odo, navi, colorValueRight, colorDataRight,colorValueLeft, colorDataLeft);		
				lsl.doLocalization(odo, navi, colorValueRight, colorDataRight,colorValueLeft, colorDataLeft);
				
				// beep once to indicate localizaion has been completed
				Sound.setVolume(80);
				Sound.beep();
				
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
				break outer;
			}			
		}
		
		// set localized position coordinates based on the starting square
		if( forwardStartPos == 1){
			odo.setPosition(new double [] {0,0,0}, new boolean [] {true, true, true});
		}else if(forwardStartPos == 2){
			odo.setPosition(new double [] {10*squareSize,0,90}, new boolean [] {true, true, true});
		}else if(forwardStartPos == 3){
			odo.setPosition(new double [] {10*squareSize,10*squareSize,180}, new boolean [] {true, true, true});
		}else if(forwardStartPos == 4){
			odo.setPosition(new double [] {0,10*squareSize,270}, new boolean [] {true, true, true});
		}
		
		// initialize the launcher class
		demoTestMotors launch = new demoTestMotors(odo, navi, colorValueLeft, colorDataLeft, colorValueLeft, colorDataLeft, dispOrientation, bx,by);

		
		// activate obstacle avoidance
		wall.start();
		
		
		if(forwardNum == 9){
			
			while(true){	
				
				while(!odo.collision){
				
					// drive to the the ball dispenser
					if(!navi.wentToDisp){			
						navi.goToDisp(bx, by, fireLineY, dispOrientation);				
					}
					
					// load the 3 balls into the robot and get ready to fire them
					if(navi.wentToDisp && !launch.loaded3){				
						launch.load();			
					}
					
					// navigate to one tile below the firing line and then turn off the usSensor
					if(navi.wentToDisp && launch.loaded3 && !navi.wentToFireLine){			
						navi.goToFireLine(targetX, fireLineY);
						((EV3UltrasonicSensor) usSensor).disable();
					}
					
					//System.out.println(navi.wentToDisp+" "+ launch.loaded3 +" "+ navi.wentToFireLine+" "+  launch.fired3);
					// if the robot has received 3 balls and gone to one square below the firing line, shoot the 3 balls
					if(navi.wentToDisp && launch.loaded3 && navi.wentToFireLine && !launch.fired3){			
						launch.launcher3(targetX, fireLineY);
					}
					try {
						Thread.sleep(4000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
						
					// if the 3 balls have been successfully fired, reset the booleans indicating which steps have been completed
					// and turn the usSensor back on
					if(launch.fired3){							
						navi.wentToDisp = false;
						launch.loaded3 = false;
						navi.wentToFireLine = false;
						launch.fired3 = false;
						((EV3UltrasonicSensor) usSensor).enable();
						
						// wait for 4 seconds to ensure there are no threading conflicts
						try {
							Thread.sleep(4000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}			
			}
		}
		
		if(defenderNum == 9){
					
			while(true){	
				
				while(!odo.collision){
					if(!navi.wentToDefLine){
					navi.goToDefLine(targetX, defLineY);
					}
				}
			}
		}
			
	}
}
