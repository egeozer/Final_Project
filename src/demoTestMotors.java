


import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
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
	
	//final static TextLCD t = LocalEV3.get().getTextLCD();
		
		public void load(Odometer odo, Navigation navi){
		
			this.odo = odo;
			this.navi = navi;
			
			// set winchMotor acceleration and speed
			winchMotor.setAcceleration(300);
			winchMotor.setSpeed(300);
			
			// set loadingMotor initial acceleration and speed
			loadingMotor.setAcceleration(600);
			loadingMotor.setSpeed(150);
			
			// desired distance from the dispenser
			double clearDist = 20.0;			// 20 too far
			
			Sound.setVolume(40);
			
			// TODO: implement light localization before picking up the ball to ensure we are at the correct heading
							
			// Get the position of the target from the user
							
			// TODO: get in front of the dispenser
				
			// ready the loading arm to receive the ball, and make sure it doesn't hit the floor
			loadingMotor.rotate(110);
			try {
			    Thread.sleep(2000);
			} catch(InterruptedException ex) {
			    Thread.currentThread().interrupt();
			}
			loadingMotor.rotate(20);
								
			// back up to get underneath the dispenser
			navi.goBackward(clearDist);
			
			// beep, and wait 5 seconds to receive the ball
			Sound.beep();
			Sound.beep();
			Sound.beep();
			
			try {
			    Thread.sleep(5000);
			} catch(InterruptedException ex) {
			    Thread.currentThread().interrupt();
			}
			
			// pull back the elastic until the arm can prevent it from firing
			winchMotor.rotate(1440);
								
			// go forward to clear the dispenser
			navi.goForward(clearDist);
			
			try {
			    Thread.sleep(5000);
			} catch(InterruptedException ex) {
			    Thread.currentThread().interrupt();
			}
			
			// bring the loading arm almost back to it's initial position
			//loadingMotor.rotate(-100);
			
			// load the ball into the launcher and hold the elastic in position
			loadingMotor.setAcceleration(650);				// with elastic, was 650 accel, 250 spd
			loadingMotor.setSpeed(250);
			loadingMotor.rotate(-130);		// -20 extra degrees to account for the wait of the balls
			
			// TODO: navigate to the firing line and turn to the firing position
			Sound.beep();
		}
		
		public void launcher3(){
			
		/*
		 * Balls 1 and 2
		 */
		
			for(int balls = 2; balls > 0; balls--){
			
				// unwind the winch to ensure the launcher can fire at full power
				winchMotor.rotate(-1440); 		// full unwind is (-1440), (-1000),(-900) = still slightly too far, (-800)
				
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
				winchMotor.rotate(1440);		// (1500-875)
				
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
			winchMotor.rotate(-1440); 		// full unwind is (-1440), (-1000),(-900) = still slightly too far, (-800)
			
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