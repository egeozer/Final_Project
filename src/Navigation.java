


import lejos.hardware.Sound;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.sensor.BaseSensor;
import lejos.robotics.RegulatedMotor;
import lejos.robotics.SampleProvider;

public class Navigation {
	final static int FAST = 160, SLOW = 100, clawTurnSpeed = 150, ACCELERATION = 6000;
	final static double DEG_ERR = 1.0, CM_ERR = 1.0;
	private Odometer odometer;
	private EV3LargeRegulatedMotor leftMotor, rightMotor;
	private SampleProvider colorSensorRight,colorSensorLeft;
	private float[] colorDataRight,colorDataLeft;

	public Navigation(Odometer odo,  SampleProvider colorSensorRight, float[] colorDataRight,SampleProvider colorSensorLeft, float[] colorDataLeft) {
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
		
		this.colorSensorRight = colorSensorRight;
		this.colorDataRight = colorDataRight;
		this.colorSensorLeft = colorSensorLeft;
		this.colorDataLeft = colorDataLeft;
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
			minAng = (Math.atan2(y - odometer.getY(), x - odometer.getX())) * (180.0 / Math.PI);
			if (minAng < 0)
				minAng += 360.0;
			this.turnTo(minAng, false);
			outer:while (Math.abs(x - odometer.getX()) > CM_ERR || Math.abs(y - odometer.getY()) > CM_ERR) {
				if(odometer.collision)
					break outer;
				
				this.setSpeeds(FAST, FAST);
			}

		leftMotor.startSynchronization();
		leftMotor.stop();
		rightMotor.stop();
		leftMotor.endSynchronization();
		odometer.isTravelling=false;
		}
	}
	
	public void travelToXY(double x, double y, Odometer odo) {
		
		lightCorrector corrector = new lightCorrector(odometer, this, colorSensorRight, colorDataRight,colorSensorLeft, colorDataLeft);
		
		double firstX = odo.getX();
		
		if(firstX<x)
			turnTo(0, true);
		else
			turnTo(180, true);
		
		//travelTo(x,odo.getY());
		//goForward(x-odo.getX());
		
		while (Math.abs(x - odometer.getX()) > CM_ERR){
			if(odometer.collision)
				break ;
		
			goForward(30.48/2);
			while(leftMotor.isMoving() || rightMotor.isMoving()){
				
				
			}
			corrector.correct( odometer,this, colorSensorRight, colorDataRight, colorSensorLeft, colorDataLeft);
		
		}
		
		
		
		double firstY = odo.getY();
		
		if(firstY<y)
			turnTo(90, true);
		else
			turnTo(270, true);
		
		goForward(y-odo.getY());
		
		while (Math.abs(y - odometer.getY()) > CM_ERR){
			if(odometer.collision)
				break ;		
			
			goForward(30.48/2);
	
			while(leftMotor.isMoving() || rightMotor.isMoving()){
				
				
			}
			corrector.correct( odometer,this, colorSensorRight, colorDataRight, colorSensorLeft, colorDataLeft);
		}
		
		//corrector.correct( odometer,this, colorSensorRight, colorDataRight, colorSensorLeft, colorDataLeft);
		//travelTo(x,y);
		
	}

	/*
	 * TurnTo function which takes an angle and boolean as arguments The boolean controls whether or not to stop the
	 * motors when the turn is completed
	 */
	
	public void turnTo(double angle, boolean stop) {

		double error = angle - this.odometer.getAng();
		
		//System.out.println("Err: " + Math.abs(error));
		//System.out.println("Ang: " + Math.abs(angle));
		
		//if(Math.abs(error) >350 && Math.abs(error)<360)
				//error = error-360;
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
	
	public void turnImm(double angle) {
		//leftMotor.startSynchronization();
	//	leftMotor.setSpeed(SLOW);
		//rightMotor.setSpeed(SLOW);
		leftMotor.rotate(convertAngle(odometer.getLeftRadius(), odometer.getWidth(), angle), true);
		rightMotor.rotate(-convertAngle(odometer.getLeftRadius(), odometer.getWidth(), angle), false);
		//leftMotor.endSynchronization();
	}
	
	
	/*
	 * Go foward a set distance in cm
	 */
	public void goForward(double distance) {
		odometer.isTravelling = true;
		leftMotor.setSpeed(FAST);
		rightMotor.setSpeed(FAST);
		//leftMotor.startSynchronization();
		leftMotor.rotate(convertDistance(odometer.getLeftRadius(), distance), true);
		rightMotor.rotate(convertDistance(odometer.getLeftRadius(), distance), false);
		//leftMotor.endSynchronization();
		odometer.isTravelling = false;

	}
	public void goBackward(double distance) {
		//leftMotor.setSpeed(SLOW);
		//rightMotor.setSpeed(SLOW);
		//leftMotor.startSynchronization();
		leftMotor.rotate(-convertDistance(odometer.getLeftRadius(), distance), true);
		rightMotor.rotate(-convertDistance(odometer.getLeftRadius(), distance), false);
		//leftMotor.endSynchronization();

	}
	
	private static int convertDistance(double radius, double distance) {
		return (int) ((180.0 * distance) / (Math.PI * radius));
	}

	private static int convertAngle(double radius, double width, double angle) {
		return convertDistance(radius, Math.PI * width * angle / 360.0);
	}
}
