


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
	
	private Odometer odometer;
	private Navigation navigation;
	private SampleProvider colorSensorRight;
	private SampleProvider colorSensorLeft;
	private float[] colorDataRight;
	private float[] colorDataLeft;
	
	final double squareSize = 30.48;
	
	public demoTestMotors(Odometer odo, Navigation navi, SampleProvider colorSensorRight, float[] colorDataRight,SampleProvider colorSensorLeft, float[] colorDataLeft){
		
		this.odometer = odo;
		this.navigation = navi;
		this.colorSensorRight = colorSensorRight;
		this.colorDataRight = colorDataRight;
		this.colorSensorLeft = colorSensorLeft;
		this.colorDataLeft = colorDataLeft;
		
	}
	
	//final static TextLCD t = LocalEV3.get().getTextLCD();
		
		public void load(Odometer odo, Navigation navi, SampleProvider colorSensorRight, float[] colorDataRight, 
				SampleProvider colorSensorLeft, float[] colorDataLeft, String dispOrientation, int bx, int by){
					
			lightCorrector corrector = new lightCorrector(odo, navi, colorSensorRight, colorDataRight,colorSensorLeft, colorDataLeft);
			
			double initAng = odo.getAng();
			double clearDist = 9.0;		// desired distance from the dispenser
			
			// set winchMotor acceleration and speed
			winchMotor.setAcceleration(300);
			winchMotor.setSpeed(300);
			
			// set loadingMotor initial acceleration and speed
			loadingMotor.setAcceleration(600);
			loadingMotor.setSpeed(200);
						
			// once the robot is in place, go forward 10cm, and then turn away from the dispenser
			navi.goForward(clearDist);
			navi.turnTo((initAng + 30), true);
			
			// ready the loading arm to receive the ball, and make sure it doesn't hit the floor
			loadingMotor.rotate(110);
			
			try {
			    Thread.sleep(500);
			} catch(InterruptedException ex) {
			    Thread.currentThread().interrupt();
			}
			
			loadingMotor.rotate(25);
			
			try {
			    Thread.sleep(2000);
			} catch(InterruptedException ex) {
			    Thread.currentThread().interrupt();
			}
								
			// once the claw is in place, turn to receive balls from the dispenser
			navi.clawOutTurnTo((initAng), true);
			
			// beep, and wait 6 seconds to receive the balls
			Sound.beep();
			Sound.beep();
			Sound.beep();
			
			try {
			    Thread.sleep(6000);
			} catch(InterruptedException ex) {
			    Thread.currentThread().interrupt();
			}
			
			// pull back the elastic until the arm can prevent it from firing
			System.out.println(winchMotor.getSpeed());
			winchMotor.rotate(1550);
								
			// wait for the winch to wind to the right position
			
			try {
			    Thread.sleep(6000);
			} catch(InterruptedException ex) {
			    Thread.currentThread().interrupt();
			}
			
			// go forward to clear the dispenser and correct heading
			//corrector.correct(odo, navi, colorSensorLeft, colorDataLeft, colorSensorLeft, colorDataLeft);
			//navi.goForward(clearDist*3);
			navi.clawOutTurnTo((initAng + 30), true);
			
			// turn away from the dispenser
			//navi.clawTurnTo(initAng, true);
			
			try {
			    Thread.sleep(1000);
			} catch(InterruptedException ex) {
			    Thread.currentThread().interrupt();
			}
			
			// bring the loading arm almost back to it's initial position
			//loadingMotor.rotate(-100);
			
			// load the ball into the launcher and hold the elastic in position
			loadingMotor.setAcceleration(650);				// with elastic, was 650 accel, 250 spd
			loadingMotor.setSpeed(250);
			loadingMotor.rotate(-135);		// -20 extra degrees to account for the wait of the balls
			
			// turn back to the initial heading
			navi.turnTo(initAng, true);
			
			// move to the middle of the tile
			navi.goForward(clearDist/2);
						
			// implement an odometry correction base on dispenser orientation to ensure proper traveling to the firing line
			if(dispOrientation.equals("E")){
				odo.setPosition(new double [] {(bx + 0)*squareSize, by*squareSize, 0}, new boolean [] {true, true, true});
			}
			else if(dispOrientation.equals("W")){
				odo.setPosition(new double [] {(bx - 0)*squareSize, by*squareSize, 180}, new boolean [] {true, true, true});
			}
			else if(dispOrientation.equals("N")){
				odo.setPosition(new double [] {bx*squareSize, (by + 0)*squareSize, 90}, new boolean [] {true, true, true});
			}
			else if(dispOrientation.equals("S")){
				odo.setPosition(new double [] {bx*squareSize, (by - 0)*squareSize, 270}, new boolean [] {true, true, true});
			}
			
			// TODO: navigate to the firing line and turn to the firing position
			Sound.beep();
		}
		
		public void launcher3(){
			
		/*
		 * Balls 1 and 2
		 */
		
			for(int balls = 2; balls > 0; balls--){
			
				// unwind the winch to ensure the launcher can fire at full power
				winchMotor.rotate(-1550); 		// full unwind is (-1440), (-1000),(-900) = still slightly too far, (-800)
				
				// set the loading arm to firing acceleration and speed, then release the ball
				loadingMotor.setAcceleration(4000);
				loadingMotor.setSpeed(300);
				loadingMotor.rotate(80);
				
				// wait 3 sec, then reset the arm acceleration, speed and position
				try {
				    Thread.sleep(3000);
				} catch(InterruptedException ex) {
				    Thread.currentThread().interrupt();
				}
				
				// rewind the winch to fire
				winchMotor.rotate(1550);		// (1500-875)
				
				// reset the winch position
				//winchMotor.rotate(-625);		// fully unwind as if it was (-1440)
				
				// load another ball into the launcher
				loadingMotor.setAcceleration(800);
				loadingMotor.setSpeed(250);
				loadingMotor.rotate(-80);			
						
			}
			
		/*
		 * Ball 3, less weight so lower acceleration and speed
		 */
			
			// unwind the winch to ensure the launcher can fire at full power
			winchMotor.rotate(-1550); 		// full unwind is (-1440), (-1000),(-900) = still slightly too far, (-800)
			
			// set the loading arm to firing acceleration and speed, then release the ball
			loadingMotor.setAcceleration(4000);
			loadingMotor.setSpeed(300);
			loadingMotor.rotate(80);
			
			// wait 3 sec, then reset the arm acceleration and speed
			try {
			    Thread.sleep(3000);
			} catch(InterruptedException ex) {
			    Thread.currentThread().interrupt();
			}

			// reset the loading arm and winch position
			loadingMotor.setAcceleration(400);
			loadingMotor.setSpeed(200);	
			
			winchMotor.rotate(-0);		// fully unwind as if it was (-1440)
			loadingMotor.rotate(-80);
			
		}
		
		public void launcher(Odometer odo, Navigation navi){
			
			// unwind the winch to ensure the launcher can fire at full power
			winchMotor.rotate(-875); 		// full unwind is (-1440), (-1000),(-900) = still slightly too far, (-800)
			
			// set the loading arm to firing acceleration and speed, then release the ball
			loadingMotor.setAcceleration(2000);
			loadingMotor.setSpeed(400);
			loadingMotor.rotate(100);
			
			// wait 3 sec, then reset the arm acceleration, speed and position
			try {
			    Thread.sleep(3000);
			} catch(InterruptedException ex) {
			    Thread.currentThread().interrupt();
			}
			loadingMotor.setAcceleration(400);
			loadingMotor.setSpeed(100);
			loadingMotor.rotate(-100);
			
			// reset the winch position
			winchMotor.rotate(-625);		// fully unwind as if it was (-1440)
		}
		
}