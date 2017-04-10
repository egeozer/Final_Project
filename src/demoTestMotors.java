
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.robotics.SampleProvider;
import lejos.hardware.Sound;

/**
 * Controls how the robot interacts with the balls during each round
 * @author Mitchell Keeley
 *
 */

public class demoTestMotors {
	
	/**
	 * Loading motor is assigned to port B, it controls the movement of the loading arm
	 */
	public static final EV3LargeRegulatedMotor loadingMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("B"));
	/**
	 * Winch motor is assigned to port C, it controls the winch mechanism used to pull back the elastic
	 */
	public static final EV3LargeRegulatedMotor winchMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("C"));
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
	 *  string used to indicate the cardinal orientation of the ball dispenser
	 */
	String dispOrientation;
	/**
	 * bx is the grid line position of the ball dispenser on the x-axis
	 */
	int bx;
	/**
	 * by is the grid line position of the ball dispenser on the y-axis
	 */	
	int by;
	/**
	 * double type that holds the size of a square playing tile
	 */
	final double squareSize = 30.48;
	/**
	 * double type that holds the desired distance between the robot and the dispenser, when facing the same orientation
	 */
	final double clearDist = 10.00;
	/**
	 * double type that holds the distance to compensate for the distance from the wheelbase to the lightSensor
	 */
	final double lightSensorDist = 6.00;
	/**
	 * boolean type that determines whether the robot has completed it's attempt to load 3 balls from the dispenser
	 */
	boolean loaded3 = false;
	/**
	 * boolean type that determines whether the robot has completed it's attempt to fire 3 balls at the target
	 */
	boolean fired3 = false;
	
	/**
	 * 
	 * @param odo odometer values are passed thourgh Odometer type
	 * @param navi navigation values are passed though Navigation type
	 * @param colorSensorRight right light sensor general initial information is passed through SampleProvider type
	 * @param colorDataRight right light sensor fetching values are passed through float[] type
	 * @param colorSensorLeft left light sensor general initial information is passed through SampleProvider type
	 * @param colorDataLeft  left light sensor fetching values are passed through float[] type
	 * @param dispOrientation is the cardinal orientation of the ball dispenser
	 * @param bx is the grid line position of the ball dispenser on the x-axis
	 * @param by is the grid line position of the ball dispenser on the y-axis
	 * 
	 */
	
	public demoTestMotors(Odometer odo, Navigation navi, SampleProvider colorSensorRight, float[] colorDataRight,SampleProvider colorSensorLeft,
			float[] colorDataLeft,String dispOrientation, int bx, int by){
		
		this.odo = odo;
		this.navi = navi;
		this.colorSensorRight = colorSensorRight;
		this.colorDataRight = colorDataRight;
		this.colorSensorLeft = colorSensorLeft;
		this.colorDataLeft = colorDataLeft;
		this.dispOrientation = dispOrientation;
		this.bx = bx;
		this.by = by;
		
	}
	
	/**
	 * Controls how the robot positions itself to receive balls from the dispenser 
	 * once it has arrived at the specified location. It also directs the loading
	 * arm to ensure it is positioned correctly during the loading procedure.
	 */		
	public void load(){
				
		lightCorrector corrector = new lightCorrector(odo, navi, colorSensorRight, colorDataRight, colorSensorLeft, colorDataLeft);
		
		Sound.setVolume(100);
		
		// set winchMotor acceleration and speed
		winchMotor.setAcceleration(300);
		winchMotor.setSpeed(300);
		
		// set loadingMotor initial acceleration and speed
		loadingMotor.setAcceleration(600);
		loadingMotor.setSpeed(200);
					
		try {
		    Thread.sleep(500);
		} catch(InterruptedException ex) {
		    Thread.currentThread().interrupt();
		}
		
		// once the robot is in place, go forward and correct the heading
		corrector.travelCorrect();
		
		try {
		    Thread.sleep(4000);
		} catch(InterruptedException ex) {
		    Thread.currentThread().interrupt();
		}
					
		// ready the loading arm to receive the ball, and make sure it doesn't hit the floor
		// execute the motion in two parts to ensure the claw lowers itself sufficiently to not hit the dispenser
		loadingMotor.rotate(115);
		
		try {
		    Thread.sleep(1000);
		} catch(InterruptedException ex) {
		    Thread.currentThread().interrupt();
		}
		
		loadingMotor.rotate(10);
		
		try {
		    Thread.sleep(4000);
		} catch(InterruptedException ex) {
		    Thread.currentThread().interrupt();
		}
							
		// once the claw is in place, go back 20cm to receive balls from the dispenser
		navi.goBackward(2*clearDist);
		
		// beep and then wait 2 seconds 3 times to receive the balls
		Sound.beep();
		
		try {
		    Thread.sleep(2000);
		} catch(InterruptedException ex) {
		    Thread.currentThread().interrupt();
		}
		
		Sound.beep();
		
		try {
		    Thread.sleep(2000);
		} catch(InterruptedException ex) {
		    Thread.currentThread().interrupt();
		}
		
		Sound.beep();
		
		try {
		    Thread.sleep(2000);
		} catch(InterruptedException ex) {
		    Thread.currentThread().interrupt();
		}
		
		// pull back the elastic until the arm can prevent it from firing
		winchMotor.rotate(1550);
							
		// wait for the winch to wind to the right position
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
				
		// go forward to clear the dispenser
		navi.goForward(2*clearDist);
			
		// load the ball into the launcher and hold the elastic in position
		loadingMotor.setAcceleration(650);				// with elastic, was 650 accel, 250 spd
		loadingMotor.setSpeed(250);
		loadingMotor.rotate(-125);		
		
		try {
			Thread.sleep(4000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
					
		// implement an odometry correction base on dispenser orientation to ensure proper traveling to the firing line
		if(dispOrientation.equals("E")){
			odo.setPosition(new double [] {(bx + 0)*squareSize, by*squareSize, 0}, new boolean [] {false, false, true});
		}
		else if(dispOrientation.equals("W")){
			odo.setPosition(new double [] {(bx - 0)*squareSize, by*squareSize, 180}, new boolean [] {false, false, true});
		}
		else if(dispOrientation.equals("N")){
			odo.setPosition(new double [] {bx*squareSize, (by + 0)*squareSize, 90}, new boolean [] {false, false, true});
		}
		else if(dispOrientation.equals("S")){
			odo.setPosition(new double [] {bx*squareSize, (by - 0)*squareSize, 270}, new boolean [] {true, true, true});
		}
		
		// indicates the robot has completed it's attempt at loading 3 balls from the dispenser
		loaded3 = true;

	}
		
	/**
	 * 
	 * Controls how the robot fires the 3 balls it has received from the dispenser.
	 * It reloads after each shot, and once all the balls have been fired, the robot resets
	 * the loading arm and winch position.
	 * 
	 * @param targetX is the grid line position of the target along the x-axis
	 * @param fireLineY is the grid line position of the firing line along the y-axis
	 * 
	 */
	public void launcher3(int targetX, int fireLineY){
		
		// set unwinding rotations based on the distance from the target
		int rotationOffset = 0;
		
		if(fireLineY == 5){
			rotationOffset = 600;
		}else if(fireLineY== 4){
			rotationOffset = 540;
		}else if(fireLineY == 3){
			rotationOffset = 450;
		}else if(fireLineY == 2){
			rotationOffset = 360;	
		}
		
		// use light correction to ensure we are directly facing the target
		navi.travelToXY(targetX*squareSize, fireLineY*squareSize, odo);
		navi.goBackward(3*lightSensorDist);

		// launch routine for balls 1 and 2
		for(int balls = 0; balls < 2; balls++){
			
			// unwind the winch to ensure the launcher can fire at the desired power
			winchMotor.rotate(-1550 + rotationOffset); 		// full unwind is (-1550)
			
			// set the loading arm to firing acceleration and speed, then release the ball
			loadingMotor.setAcceleration(9000);
			loadingMotor.setSpeed(1200);
			loadingMotor.rotate(100);
			try {
				Thread.sleep(500);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
			loadingMotor.rotate(50);
			
			// rewind the winch to fire
			winchMotor.rotate(1550 - rotationOffset);		// full wind is (1550)
			
			// load another ball into the launcher
			loadingMotor.setAcceleration(900);
			loadingMotor.setSpeed(300);
			loadingMotor.rotate(-150);	
			
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
						
		}
		
		// launch routine for ball 3, using less power since less weight in the claw	
		
		// unwind the winch to ensure the launcher can fire at full power
		winchMotor.rotate(-1550 + rotationOffset); 		// full unwind is (-1550)
		
		// set the loading arm to firing acceleration and speed, then release the ball
		loadingMotor.setAcceleration(9000);
		loadingMotor.setSpeed(1200);
		loadingMotor.rotate(100);
		
		try {
			Thread.sleep(500);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		
		loadingMotor.rotate(50);
		
		// wait 3 sec, then reset the arm acceleration and speed
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		// reset the loading arm and winch position
		loadingMotor.setAcceleration(400);
		loadingMotor.setSpeed(200);	
	
		// fully unwind the winch motor, as if there was no offset in the rotation
		winchMotor.rotate(0 - rotationOffset);		
		loadingMotor.rotate(-120);
		
		// indicate that the robot has finished trying to launch the 3 balls
		fired3 = true;
		
	}
}