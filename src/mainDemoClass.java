

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
	private static final Port colorPort = LocalEV3.get().getPort("S2");		
	
	private static final String SERVER_IP = "192.168.2.11";			//  Ege: 192.168.2.6
	private static final int TEAM_NUMBER = 9;

	// Enable/disable printing of debug info from the WiFi class
	private static final boolean ENABLE_DEBUG_WIFI_PRINT = true;
	static int bx;
	static int by;
	static int startPos;
	public static void main(String[] args) {
				
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
		
		/*WifiConnection conn = new WifiConnection(SERVER_IP, TEAM_NUMBER, ENABLE_DEBUG_WIFI_PRINT);
		
		// Connect to server and get the data, catching any errors that might occur
		
		try {
			
			Map data = conn.getData();

			// Example 1: Print out all received data
			System.out.println("Map:\n" + data);

			// Example 2 : Print out specific values
			int fwdTeam = ((Long) data.get("FWD_TEAM")).intValue();
			System.out.println("Forward Team: " + fwdTeam);

			int w1 = ((Long) data.get("w1")).intValue();
			System.out.println("Defender zone size w1: " + w1);
			bx = ((Long) data.get("bx")).intValue();
			by = ((Long) data.get("by")).intValue();
			startPos = ((Long) data.get("FWD_CORNER")).intValue();
			//	Odometer odo = new Odometer(leftMotor, rightMotor, 30, true);
			// Example 3: Compare value
			String orientation = (String) data.get("omega");
			if (orientation.equals("N")) {
				System.out.println("Orientation is North");
			}
			else {
				System.out.println("Orientation is not North");
			}

		} catch (Exception e) {
			System.err.println("Error: " + e.getMessage());
		}
		*/

		//Setup synchronized motors
		leftMotor.synchronizeWith(new EV3LargeRegulatedMotor[] {rightMotor});
		
		//Setup ultrasonic sensor
		// 1. Create a port object attached to a physical port (done above)
		// 2. Create a sensor instance and attach to port
		// 3. Create a sample provider instance for the above and initialize operating mode
		// 4. Create a buffer for the sensor data
		@SuppressWarnings("resource")							    	// Because we don't bother to close this resource
		SensorModes usSensor = new EV3UltrasonicSensor(usPort);
		SampleProvider usValue = usSensor.getMode("Distance");			// colorValue provides samples from this instance
		float[] usData = new float[usValue.sampleSize()];				// colorData is the buffer in which data are returned
		
		//Setup color sensor
		// 1. Create a port object attached to a physical port (done above)
		// 2. Create a sensor instance and attach to port
		// 3. Create a sample provider instance for the above and initialize operating mode
		// 4. Create a buffer for the sensor data
		@SuppressWarnings("resource")
		SensorModes colorSensor = new EV3ColorSensor(colorPort);
		SampleProvider colorValue = colorSensor.getMode("Red");			// colorValue provides samples from this instance
		float[] colorData = new float[colorValue.sampleSize()];			// colorData is the buffer in which data are returned
				
		// setup the odometer and display
		Odometer odo = new Odometer(leftMotor, rightMotor, 30, true, startPos);
		Navigation navi = new Navigation(odo);
		final TextLCD t = LocalEV3.get().getTextLCD();
		wallObstacle wall =new wallObstacle(leftMotor, rightMotor, odo, navi, usValue, usData);
		TestMotors launch = new TestMotors();
		
	
				
		//initialize display
		LCDInfo lcd = new LCDInfo(odo);
		int buttonChoice;
		Sound.setVolume(40);
		
		
		// Robot will beep once it has received the Wifi instructions and setup the sensors and display
		Sound.beep();
		
		// perform the ultrasonic localization using Falling Edge
		USLocalizer usl = new USLocalizer(odo, navi, usValue, usData, USLocalizer.LocalizationType.FALLING_EDGE);
		usl.doLocalization();
		
		outer:while(true){
			//buttonChoice = Button.waitForAnyPress();
			if (usl.isLocalized) {
				LightLocalizer lsl = new LightLocalizer(odo, navi, colorValue, colorData);		
				lsl.doLocalization(odo, navi, colorValue, colorData);
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				//Sound.beep();
				break outer;
			}
			
		}
			
			
		//wall.start();
		//navi.travelTo(bx,by);
		//navi.turnTo(0,true);
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		
		
		navi.travelTo(5*(30.48),1*(30.48));
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		navi.turnTo(90,true);
		//navi.turnTo(0,true);
		launch.launcher();

		//navi.turnTo(90,true);
		
		
		/*outer:while(true){
			if(odo.collisionAvoided && !odo.collision){
				odo.collisionAvoided=false;
				navi.travelTo(60,30);
				break outer;
					
					
				}
			}
		*/
		
		//}	******end of else block for falling edge*******
		
		// perform the light sensor localization upon pressing the up arrow
		
												
		//while (Button.waitForAnyPress() != Button.ID_ESCAPE);
			//System.exit(0);		
	}
}
