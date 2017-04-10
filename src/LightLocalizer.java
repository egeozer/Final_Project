


import lejos.hardware.Sound;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.robotics.SampleProvider;

/**
 * @author Ege Ozer
 * 
 * LightLocalizer ensures that the robot localizes at 0,0 with the right heading by using two light sensors.
 */
public class LightLocalizer {
	/**
	 * Odometer type that stores odometer related information
	 */
	private Odometer odo;
	/**
	 * Navigation type that stores navigation related information
	 */
	private Navigation navi;
	/**
	 * SampleProvider type that is used to store right light general initial sensor information
	 */
	/**
	 * SampleProvider type that is used to store left light general initial sensor information
	 */
	private SampleProvider colorSensorRight,colorSensorLeft;
	/**
	 *  float[] type that is used to store right light fetching information
	 */
	/**
	 *  float[] type that is used to store left light fetching information
	 */
	private float[] colorDataRight,colorDataLeft;
	/**
	 * EV3LargeRegulatedMotor type that stores the left wheel motor information
	 */
	/**
	 * EV3LargeRegulatedMotor type that stores the left wheel motor information
	 */
	private EV3LargeRegulatedMotor leftMotor, rightMotor;
	/**
	 * double type that holds the distance to compensate for the distance from the wheelbase to the lightSensor
	 */
	double lightSensorDist = 6.00; 		//distance added to compensate for the distance from the wheelbase to the lightSensor, was 3.5cm

	
	/**
	 * @param odo odometer values are passed through Odometer type
	 * @param navi navigation values are passed though Navigation type
	 * @param colorSensorRight right light sensor general initial information is passed through SampleProvider type
	 * @param colorDataRight right light sensor fetching values are passed through float[] type
	 * @param colorSensorLeft left light sensor general initial information is passed through SampleProvider type
	 * @param colorDataLeft  left light sensor fetching values are passed through float[] type
	 */
	public LightLocalizer(Odometer odo, Navigation navi, SampleProvider colorSensorRight, float[] colorDataRight,SampleProvider colorSensorLeft, float[] colorDataLeft) {
		this.odo = odo;
		this.navi = navi;
		this.colorSensorRight = colorSensorRight;
		this.colorDataRight = colorDataRight;
		this.colorSensorLeft = colorSensorLeft;
		this.colorDataLeft = colorDataLeft;
		Sound.setVolume(50);
		EV3LargeRegulatedMotor[] motors = this.odo.getMotors();
		this.leftMotor = motors[0];
		this.rightMotor = motors[1];
	}
	
	/**
	 *  It first goes forward
	 * once USLocalizer finishes executing, then goes forward until it sees a black line. Robot aligns itself on the line, turns 
	 * exactly 90 degrees, goes forward until it sees a black line again. It corrects itself again, goes lightSensorDist once, and the robot it localized
	 */
	public void doLocalization() {
	
		leftMotor.setSpeed(150);
		rightMotor.setSpeed(150);
		
		
		lightRight right = new lightRight(colorSensorRight, colorDataRight, odo );
		lightLeft left = new lightLeft(colorSensorLeft, colorDataLeft, odo );
		
		// when facing the black line, wait 3 seconds and then start moving forward
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}		
		
		leftMotor.startSynchronization();
		leftMotor.forward();
		rightMotor.forward();
		leftMotor.endSynchronization();
		
		// starts light localization in the y direction
		right.start();		// stops right motor when right sensor sees a black line, then beeps
		left.start();		// stops left motor when left sensor sees a black line, then beeps
		
		right.scanLine=true;
		left.scanLine=true;
		
		try {
			right.join();
			left.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		right.scanLine=false;
		left.scanLine=false;
		
		
		// go to the zero on the y-axis
		navi.goForward(lightSensorDist);
		
		
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}		
		
		// set theta to 90 degrees as we now face the positive y-axis
		odo.setPosition(new double [] {0,0,90}, new boolean [] {false, false, true});		
						
		// turn to face the postive x-direction, wait 3 seconds and then start moving forward to the black line
		navi.turnTo(0, true);
		
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}	
		
		// starts light localization in the x direction
		right = new lightRight(colorSensorRight, colorDataRight, odo );
		left = new lightLeft(colorSensorLeft, colorDataLeft, odo );
		
		leftMotor.startSynchronization();
		leftMotor.forward();
		rightMotor.forward();
		leftMotor.endSynchronization();
		
		right.start();		// stops right motor when right sensor sees a black line, then beeps
		left.start();		// stops left motor when left sensor sees a black line, then beeps
		
		right.scanLine=true;
		left.scanLine=true;
		
		try {
			right.join();
			left.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		right.scanLine=false;
		left.scanLine=false;
		
		
		// go to the zero on the x-axis
		navi.goForward(lightSensorDist);
	}
		
}
		
		
	


