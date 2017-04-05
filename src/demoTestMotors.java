


import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.robotics.SampleProvider;
import lejos.hardware.Button;
import lejos.hardware.Sound;
import lejos.hardware.lcd.TextLCD;

public class demoTestMotors {
	
	// Loading motor is connected to output B
	public static final EV3LargeRegulatedMotor loadingMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("B"));
	// Gear motor is connected to output C
	public static final EV3LargeRegulatedMotor winchMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("C"));
	
	private Odometer odo;
	private Navigation navi;
	private SampleProvider colorSensorRight;
	private SampleProvider colorSensorLeft;
	private float[] colorDataRight;
	private float[] colorDataLeft;
	String dispOrientation;
	int bx; int by;
	final double squareSize = 30.48;
	final double lightSensorDist = 6.00;
	boolean loaded3 = false;
	boolean fired3 = false;
	
	public demoTestMotors(Odometer odo, Navigation navi, SampleProvider colorSensorRight, float[] colorDataRight,SampleProvider colorSensorLeft, float[] colorDataLeft,String dispOrientation, int bx, int by){
		
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
	
	//final static TextLCD t = LocalEV3.get().getTextLCD();
		
	public void load( ){
				
		lightCorrector corrector = new lightCorrector(odo, navi, colorSensorRight, colorDataRight, colorSensorLeft, colorDataLeft);
		
		
		double clearDist = 10.0;		// desired distance from the dispenser
		
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
		
		// get the angle to load from the dispenser
		double initAng = odo.getAng();
					
		// ready the loading arm to receive the ball, and make sure it doesn't hit the floor
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
		
		// beep 3 times, and wait 6 seconds to receive the balls
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
		winchMotor.rotate(1910);
							
		// wait for the winch to wind to the right position
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
				
		// turn away from the dispenser and wait 2 seconds
		//navi.clawOutTurnTo((initAng + 45), true);
		navi.goForward(2*clearDist);
		
		/*try {
			Thread.sleep(4000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}*/
			
		// load the ball into the launcher and hold the elastic in position
		loadingMotor.setAcceleration(650);				// with elastic, was 650 accel, 250 spd
		loadingMotor.setSpeed(250);
		loadingMotor.rotate(-125);		
		
		// turn back to the initial heading
		//navi.turnTo(initAng, true);
		
		try {
			Thread.sleep(4000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
				
		// move to the middle of the tile
		navi.goForward(clearDist/2);
					
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
		
		loaded3 = true;

	}
		
	public void launcher3(int targetX, int fireLineY){
		
		// set unwinding rotations based on the distance from the target
		int rotationOffset = 0;
		
		if(fireLineY == 5){
			rotationOffset = 720;
		}else if(fireLineY== 4){
			rotationOffset = 450;
		}else if(fireLineY == 3){
			rotationOffset = 360;
		}else if(fireLineY == 2){
			rotationOffset = 500;		// 540 too much
		}
		
		// use light correction to ensure we are directly facing the target
		//navi.travelToXY(targetX*squareSize, fireLineY*squareSize, odo);
		navi.goBackward(3*lightSensorDist);
		//odo.setPosition(new double [] {targetX*squareSize, (fireLineY-1)*squareSize, 90}, new boolean [] {true, true, true});
		//corrector.travelCorrect();
		
		
		// launch routine for balls 1 and 2
	
		//lightCorrector corrector = new lightCorrector(odometer, navigation, colorSensorRight, colorDataRight,colorSensorLeft, colorDataLeft);
		
		for(int balls = 0; balls < 2; balls++){
			
			// unwind the winch to ensure the launcher can fire at the desired power
			winchMotor.rotate(-1910 + rotationOffset); 		// full unwind is (-1550)
			
			// set the loading arm to firing acceleration and speed, then release the ball
			loadingMotor.setAcceleration(9000);
			loadingMotor.setSpeed(1200);
			loadingMotor.rotate(100);
			
			try {
				Thread.sleep(500);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
			loadingMotor.rotate(50);
			
			
			
			// wait 3 sec, then reset the arm acceleration, speed and position
			
			
			// rewind the winch to fire
			winchMotor.rotate(1910 - rotationOffset);		// full wind is (1550)
			
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
		
		/////////////////////////////////////////
		// launch routine for ball 3, using less power since less weight in the claw
		
		// use light correction to ensure we are directly facing the target
		//navi.travelToXY(targetX*squareSize, fireLineY*squareSize, odo);
		//navi.goBackward(3*lightSensorDist);
		//odo.setPosition(new double [] {targetX*squareSize, (fireLineY-1)*squareSize, 90}, new boolean [] {true, true, true});
		
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}	
		
		//corrector.fireCorrect();
		
		// unwind the winch to ensure the launcher can fire at full power
		winchMotor.rotate(-1910 + rotationOffset); 		// full unwind is (-1550)
		
		// set the loading arm to firing acceleration and speed, then release the ball
		loadingMotor.setAcceleration(9000);
		loadingMotor.setSpeed(1200);
		loadingMotor.rotate(100);
		
		try {
			Thread.sleep(500);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
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
		
		winchMotor.rotate(0 - rotationOffset);		// fully unwind as if it was (0)
		loadingMotor.rotate(-120);
		
		fired3 = true;
		
	}
}