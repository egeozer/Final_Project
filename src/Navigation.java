


import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.robotics.RegulatedMotor;

public class Navigation {
	final static int FAST = 160, SLOW = 100, ACCELERATION = 6000;
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
	}
	
	

	/*
	 * Functions to set the motor speeds jointly
	 */
	public void setSpeeds(float lSpd, float rSpd) {
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

	public void setSpeeds(int lSpd, int rSpd) {
		
		this.leftMotor.synchronizeWith(new RegulatedMotor[]{this.rightMotor});
		this.leftMotor.startSynchronization();
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
		this.leftMotor.endSynchronization();
	}

	/*
	 * Float the two motors jointly
	 */
	public void setFloat() {
		this.leftMotor.stop();
		this.rightMotor.stop();
		this.leftMotor.flt(true);
		this.rightMotor.flt(true);
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

		//this.setSpeeds(0, 0);
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
	public void turnTo(double angle, boolean stop) {

		double error = angle - this.odometer.getAng();

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
			leftMotor.stop();
			rightMotor.stop();
		}
	}
	public void turnImm(double angle) {
		
		leftMotor.setSpeed(SLOW);
		rightMotor.setSpeed(SLOW);
		leftMotor.startSynchronization();
		
		
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
		leftMotor.rotate(convertDistance(odometer.getLeftRadius(), distance), true);
		rightMotor.rotate(convertDistance(odometer.getLeftRadius(), distance), false);
	

	}
	public void goBackward(double distance) {
		
		leftMotor.setSpeed(SLOW);
		rightMotor.setSpeed(SLOW);
		leftMotor.rotate(-convertDistance(odometer.getLeftRadius(), distance), true);
		rightMotor.rotate(-convertDistance(odometer.getLeftRadius(), distance), false);
	

	}
	
	private static int convertDistance(double radius, double distance) {
		return (int) ((180.0 * distance) / (Math.PI * radius));
	}

	private static int convertAngle(double radius, double width, double angle) {
		return convertDistance(radius, Math.PI * width * angle / 360.0);
	}
}
