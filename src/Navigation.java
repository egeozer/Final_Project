


import lejos.hardware.Sound;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.sensor.BaseSensor;
import lejos.robotics.RegulatedMotor;
import lejos.robotics.SampleProvider;

public class Navigation {
	final static int FAST = 160, SLOW = 100, clawTurnSpeed = 150, ACCELERATION = 6000;
	final static double DEG_ERR = 3.0, CM_ERR = 1.0;
	private Odometer odometer;
	private EV3LargeRegulatedMotor leftMotor, rightMotor;

	public Navigation(Odometer odo) {
		this.odometer = odo;

		EV3LargeRegulatedMotor[] motors = this.odometer.getMotors();
		this.leftMotor = motors[0];
		this.rightMotor = motors[1];

		// set acceleration
		this.leftMotor.setAcceleration(ACCELERATION);
		this.rightMotor.setAcceleration(ACCELERATION);
		
		// set speed
		this.leftMotor.setSpeed(FAST);
		this.rightMotor.setSpeed(FAST);
	}
	
	

	/*
	 * Function to set the motor speeds jointly
	 */
	public void setSpeeds(int lSpd, int rSpd) {
		
		leftMotor.startSynchronization();
						
		
		this.leftMotor.setSpeed(lSpd);
		this.rightMotor.setSpeed(rSpd);
		if (lSpd < 0)
			this.leftMotor.backward();
		else
			this.leftMotor.forward();
		if (rSpd < 0)
			this.rightMotor.backward();
		else
			this.rightMotor.forward();
	
		
		leftMotor.endSynchronization();
	}

	/*
	 * TravelTo function which takes as arguments the x and y position in cm Will travel to designated position, while
	 * constantly updating it's heading
	 */
	public void travelTo(double x, double y) {
		if(!odometer.isTravelling){
			odometer.isTravelling=true;
			double minAng;
			outer:while (Math.abs(x - odometer.getX()) > CM_ERR || Math.abs(y - odometer.getY()) > CM_ERR) {
				if(odometer.collision)
					break outer;
				minAng = (Math.atan2(y - odometer.getY(), x - odometer.getX())) * (180.0 / Math.PI);
				if (minAng < 0)
					minAng += 360.0;
				this.turnTo(minAng, false);
				this.setSpeeds(FAST, FAST);
			}

		leftMotor.startSynchronization();
		leftMotor.stop();
		rightMotor.stop();
		leftMotor.endSynchronization();
		odometer.isTravelling=false;
		}
	}

	/*
	 * TurnTo function which takes an angle and boolean as arguments The boolean controls whether or not to stop the
	 * motors when the turn is completed
	 */
	public void lsTravelTo(double x, double y, Odometer odo, SampleProvider colorSensorRight, float[] colorDataRight, 
			SampleProvider colorSensorLeft, float[] colorDataLeft){
		
		double lightSensorDist = 6.0;
		double heading = Math.atan2(y, x);			// changed to account for different 0 degrees / coord-systems
		
		
		// x direction navigation
		if( (x-odo.getX()) >= 0){
			turnTo(0, true);
		}
		outer:while (Math.abs(x - odo.getX()) > CM_ERR){
				if(odo.collision)
					break outer;
				
				// ensure we are in the right color mode
				((BaseSensor) colorSensorRight).setCurrentMode("Red");			// colorValue provides samples from this instance
				((BaseSensor) colorSensorLeft).setCurrentMode("Red");
				
				lightRight right = new lightRight(colorSensorRight, colorDataRight, odo );
				lightLeft left = new lightLeft(colorSensorLeft, colorDataLeft, odo );
				
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
				
				Sound.beep();
				Sound.beep();
				Sound.beep();
				Sound.beep();
				
				// go to the zero on the x-axis
				leftMotor.startSynchronization();
				leftMotor.rotate(convertDistance(odometer.getLeftRadius(), lightSensorDist), true);
				rightMotor.rotate(convertDistance(odometer.getLeftRadius(), lightSensorDist), false);
				leftMotor.endSynchronization();
			}
		
		// y direction navigation
		if( (y-odo.getY()) >= 0){
			turnTo(90, true);
		}
		outer:while (Math.abs(y - odo.getY()) > CM_ERR){
				if(odo.collision)
					break outer;
				
				// ensure we are in the right color mode
				((BaseSensor) colorSensorRight).setCurrentMode("Red");			// colorValue provides samples from this instance
				((BaseSensor) colorSensorLeft).setCurrentMode("Red");
				
				lightRight right = new lightRight(colorSensorRight, colorDataRight, odo );
				lightLeft left = new lightLeft(colorSensorLeft, colorDataLeft, odo );
				
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
				
				//Sound.beep();
				//Sound.beep();
				
				// go to the zero on the x-axis
				goForward(lightSensorDist);
						
		}
	}
	
	
	
	
	public void turnTo(double angle, boolean stop) {

		double error = angle - this.odometer.getAng();
		
		//System.out.println("Err: " + Math.abs(error));
		//System.out.println("Ang: " + Math.abs(angle));
		
		if(Math.abs(error) >350 && Math.abs(error)<360)
				error = error-360;
		outer:while (Math.abs(error) > DEG_ERR) {
			if(odometer.collision){
				leftMotor.startSynchronization();
				leftMotor.stop();
				rightMotor.stop();
				leftMotor.endSynchronization();
				break outer ;
				
			}
			error = angle - this.odometer.getAng();

			if (error < -180.0) {
				this.setSpeeds(-SLOW, SLOW);
			} else if (error < 0.0) {
				this.setSpeeds(SLOW, -SLOW);
			} else if (error > 180.0) {
				this.setSpeeds(SLOW, -SLOW);
			} else {
				this.setSpeeds(-SLOW, SLOW);
			}
		}

		if (stop) {
			//this.setSpeeds(0, 0);
			leftMotor.startSynchronization();
			leftMotor.stop();
			rightMotor.stop();
			leftMotor.endSynchronization();
		}
	}
	
	public void clawTurnTo(double angle, boolean stop) {

		double error = angle - this.odometer.getAng();
		
		//System.out.println("Err: " + Math.abs(error));
		//System.out.println("Ang: " + Math.abs(angle));
		
		if(Math.abs(error) >350 && Math.abs(error)<360)
				error = error-360;
		outer:while (Math.abs(error) > DEG_ERR) {
			if(odometer.collision){
				leftMotor.startSynchronization();
				leftMotor.stop();
				rightMotor.stop();
				leftMotor.endSynchronization();
				break outer ;
				
			}
			error = angle - this.odometer.getAng();

			if (error < -180.0) {
				this.setSpeeds(-clawTurnSpeed, clawTurnSpeed);
			} else if (error < 0.0) {
				this.setSpeeds(clawTurnSpeed, -clawTurnSpeed);
			} else if (error > 180.0) {
				this.setSpeeds(clawTurnSpeed, -clawTurnSpeed);
			} else {
				this.setSpeeds(-clawTurnSpeed, clawTurnSpeed);
			}
		}

		if (stop) {
			//this.setSpeeds(0, 0);
			leftMotor.startSynchronization();
			leftMotor.stop();
			rightMotor.stop();
			leftMotor.endSynchronization();
		}
	}
	
	public void travelToXY(double x, double y, Odometer odo) {
		travelTo(x,odo.getY());
		travelTo(x,y);
		
	}
	public void turnImm(double angle) {
		leftMotor.startSynchronization();
		leftMotor.setSpeed(SLOW);
		rightMotor.setSpeed(SLOW);
		leftMotor.rotate(convertAngle(odometer.getLeftRadius(), odometer.getWidth(), angle), true);
		rightMotor.rotate(-convertAngle(odometer.getLeftRadius(), odometer.getWidth(), angle), false);
		leftMotor.endSynchronization();
	}
	
	
	/*
	 * Go foward a set distance in cm
	 */
	public void goForward(double distance) {
		leftMotor.setSpeed(SLOW);
		rightMotor.setSpeed(SLOW);
		leftMotor.startSynchronization();
		leftMotor.rotate(convertDistance(odometer.getLeftRadius(), distance), true);
		rightMotor.rotate(convertDistance(odometer.getLeftRadius(), distance), false);
		leftMotor.endSynchronization();

	}
	public void goBackward(double distance) {
		leftMotor.setSpeed(SLOW);
		rightMotor.setSpeed(SLOW);
		leftMotor.startSynchronization();
		leftMotor.rotate(-convertDistance(odometer.getLeftRadius(), distance), true);
		rightMotor.rotate(-convertDistance(odometer.getLeftRadius(), distance), false);
		leftMotor.endSynchronization();

	}
	
	private static int convertDistance(double radius, double distance) {
		return (int) ((180.0 * distance) / (Math.PI * radius));
	}

	private static int convertAngle(double radius, double width, double angle) {
		return convertDistance(radius, Math.PI * width * angle / 360.0);
	}
}
