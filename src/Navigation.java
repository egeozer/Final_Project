


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
	
	// Constants
	static double squareSize = 30.48;

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
	
	public void travelToXY(double finalX, double finalY, Odometer odo) {
		
		lightCorrector corrector = new lightCorrector(odo, this, colorSensorRight, colorDataRight,colorSensorLeft, colorDataLeft);
		
		// travel in the x-direction
		double initialX = odo.getX();
		double initialY = odo.getY();
		double travelingAngle = 0;
		double currentX = odo.getX();
		double odoXCorrect;
		int squaresTravelledX = 0;
		
		// COMMENT
		if(Math.abs(finalX - currentX) <= (CM_ERR*5)){
			
		}else if(currentX < finalX){
			turnTo(0, true);
			travelingAngle = 0;
		}else{
			turnTo(180, true);
			travelingAngle = 180;
		}
		
		// COMMENT
		while (Math.abs(finalX - currentX) > (CM_ERR*5)){
			if(odo.collision)
				break;
			
			corrector.travelCorrect(odo, this, colorSensorRight, colorDataRight, colorSensorLeft, colorDataLeft);
			
			squaresTravelledX++;
			odoXCorrect = squaresTravelledX*squareSize;
			
			if(initialX < finalX){
				odo.setX(initialX+odoXCorrect);
			}else{
				odo.setX(initialX-odoXCorrect);
			}
			
			odo.setY(initialY);
			odo.setTheta(travelingAngle);
			
			currentX = odo.getX();
			System.out.println(currentX);
			
			
			/*// sets the odometer X position to the nearest gridline position
			if( Math.round((odo.getX()/squareSize)) < squaresTravelledX){
				
				odoXCorrect = Math.ceil((odo.getX()/squareSize)) * squareSize;
			}else{
				odoXCorrect = Math.floor((odo.getX()/squareSize)) * squareSize;
			}*/
			
			
		}
		
		// offset other localization
		//goForward(squareSize/2);
		
		// travel in the y-direction	
		initialX = odo.getX();
		initialY = odo.getY();
		double currentY = odo.getY();
		double odoYCorrect;
		int squaresTravelledY = 0;
		
		// COMMENT
		if(Math.abs(finalY - currentY) <= (CM_ERR*5)){
			
		}else if(currentY < finalY){
			turnTo(90, true);
			travelingAngle = 90;
		}
		else{
			turnTo(270, true);
			travelingAngle = 270;
		}
		
		while (Math.abs(finalY - currentY) > (CM_ERR*5)){
			if(odo.collision)
				break;		
			
			corrector.travelCorrect( odo, this, colorSensorRight, colorDataRight, colorSensorLeft, colorDataLeft);
			
			squaresTravelledY++;
			odoYCorrect = squaresTravelledY*squareSize;
			
			if(initialY < finalY){
				odo.setY(initialY+odoYCorrect);
			}else{
				odo.setY(initialY-odoYCorrect);
			}
			
			odo.setX(initialX);
			odo.setTheta(travelingAngle);
			
			currentY = odo.getY();
			System.out.println(currentY);
			System.out.println(currentX);
			
			
			/*currentY = odo.getY();
			
			// sets the odometer X position to the nearest gridline position
			if( Math.round((odo.getY()/squareSize)) < squaresTravelledY){
				
				odoYCorrect = Math.ceil((odo.getY()/squareSize)) * squareSize;
			}else{
				odoYCorrect = Math.floor((odo.getY()/squareSize)) * squareSize;
			}
			
			odo.setY(odoYCorrect);
			System.out.println(odoYCorrect);
			
			//System.out.println(currentY);
			//double odoYCorrect = odo.getY() - odo.getY()%squareSize;
			//System.out.println(odoYCorrect);*/
			
			
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
