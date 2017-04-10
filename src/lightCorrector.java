
import lejos.hardware.Sound;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.robotics.SampleProvider;

/**
 * This class is responsible for the methods used by the robot to correct its heading
 * 
 * @author Ege Ozer
 * 
 */

public class lightCorrector {
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
	private SampleProvider colorSensorRight;
	/**
	 * SampleProvider type that is used to store left light general initial sensor information
	 */
	private SampleProvider colorSensorLeft;

	/**
	 *  float[] type that is used to store right light fetching information
	 */
	private float[] colorDataRight;
	/**
	 *  float[] type that is used to store left light fetching information
	 */
	private float[] colorDataLeft;
	/**
	 * EV3LargeRegulatedMotor type that stores the left wheel motor information
	 */
	private EV3LargeRegulatedMotor leftMotor;
	/**
	 * EV3LargeRegulatedMotor type that stores the right wheel motor information
	 */
	private EV3LargeRegulatedMotor rightMotor;
	/**
	 * double type that holds the distance to compensate for the distance from the wheelbase to the lightSensor
	 */
	double lightSensorDist = 6.00; 
	/**
	 * int type that stores the speed of the robot in lightCorrection
	 */
	int fast = 200;
	
	/**
	 * @param odo odometer values are passed thourgh Odometer type
	 * @param navi navigation values are passed though Navigation type
	 * @param colorSensorRight right light sensor general initial information is passed through SampleProvider type
	 * @param colorDataRight right light sensor fetching values are passed through float[] type
	 * @param colorSensorLeft left light sensor general initial information is passed through SampleProvider type
	 * @param colorDataLeft  left light sensor fetching values are passed through float[] type
	 */
	public lightCorrector(Odometer odo, Navigation navi, SampleProvider colorSensorRight, float[] colorDataRight,SampleProvider colorSensorLeft, float[] colorDataLeft) {
		this.odo = odo;
		this.navi = navi;
		this.colorSensorRight = colorSensorRight;
		this.colorDataRight = colorDataRight;
		this.colorSensorLeft = colorSensorLeft;
		this.colorDataLeft = colorDataLeft;
		EV3LargeRegulatedMotor[] motors = this.odo.getMotors();
		this.leftMotor = motors[0];
		this.rightMotor = motors[1];
		Sound.setVolume(40);
		leftMotor.setSpeed(fast);
		rightMotor.setSpeed(fast);
	}
	
	/**
	 * method that activates black line scanning when it is called with several Thread.sleep calls. 
	 * The robot goes forward until a black line is seen. The two light sensor threads starts and waits for the two to end
	 */
	public void correct() {
		
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}	

		lightRight right = new lightRight(colorSensorRight, colorDataRight, odo );
		lightLeft left = new lightLeft(colorSensorLeft, colorDataLeft, odo );
		
		// when facing the black line, wait 3 seconds
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}		
	
		
		leftMotor.forward();
		rightMotor.forward();
		
		
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
		
		
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}		
		
		navi.goForward(lightSensorDist);
		
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
	}
	
	/**
	 * method that activates black line scanning when it is called without making the robot sleep(Thread.sleep) 
	 */
	public void travelCorrect() {	
	
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}	
		
		lightRight right = new lightRight(colorSensorRight, colorDataRight, odo );
		lightLeft left = new lightLeft(colorSensorLeft, colorDataLeft, odo );	
		
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}	
		
		
		leftMotor.forward();
		rightMotor.forward();
		
		
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
		
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}	
		
		navi.goForward(lightSensorDist);
		
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}	
		
	}
	
	/**
	 * method that activates black line scanning when it is near the firing line. In addition to other correction methods, 
	 * the robot goes backwards to correct its heading then goes forward
	 */
	public void fireCorrect() {	
		
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}	

		lightRight right = new lightRight(colorSensorRight, colorDataRight, odo );
		lightLeft left = new lightLeft(colorSensorLeft, colorDataLeft, odo );
		
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}		
		
		navi.goBackward(lightSensorDist*2);
		
		// when facing the firing line, wait 3 seconds
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}		
	
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
		
		
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
	}
	
}
		
		
	
