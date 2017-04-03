


import lejos.hardware.Sound;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.sensor.BaseSensor;
import lejos.robotics.RegulatedMotor;
import lejos.robotics.SampleProvider;

public class Navigation {
	
	final static int FAST = 200, SLOW = 100, clawTurnSpeed = 125, ACCELERATION = 6000;
	final static double DEG_ERR = 3.0, CM_ERR = 1.0;
	private Odometer odometer;
	private EV3LargeRegulatedMotor leftMotor, rightMotor;
	private SampleProvider colorSensorRight,colorSensorLeft;
	private float[] colorDataRight,colorDataLeft;
	boolean wentToDisp  = false;
	boolean wentToFireLine = false;
	double travelingAngle = 0;
	
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
		
		//leftMotor.startSynchronization();
						
		
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
	
		
		//leftMotor.endSynchronization();
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
		//if(!odometer.isTravelling){
			//odometer.isTravelling=true;
		// travel in the x-direction
		
		if(!odo.collision){
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
				odo.setTheta(travelingAngle);
			}else{
				turnTo(180, true);
				travelingAngle = 180;
				odo.setTheta(travelingAngle);
			}
			
			// COMMENT
			while (Math.abs(finalX - currentX) > (CM_ERR*5)){
				if(odo.collision)
					break;
				
				corrector.travelCorrect();
				
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
				//System.out.println(currentX);	
				
			}
			
			
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
				odo.setTheta(travelingAngle);
			}
			else{
				turnTo(270, true);
				travelingAngle = 270;
				odo.setTheta(travelingAngle);
			}
			
			while (Math.abs(finalY - currentY) > (CM_ERR*5)){
				if(odo.collision)
					break;		
				
				corrector.travelCorrect();
				
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
				//System.out.println(currentY);
				//System.out.println(currentX);
			
		
			}

	}
	
		//}
		//odometer.isTravelling=false;
	
	}

	/*
	 * TurnTo function which takes an angle and boolean as arguments The boolean controls whether or not to stop the
	 * motors when the turn is completed
	 */
	
	public void turnTo(double angle, boolean stop) {

		double error = angle - this.odometer.getAng();
		
		if(Math.abs(error) >350 && Math.abs(error)<360)
				error = error-360;
		while (Math.abs(error) > DEG_ERR) {
			
			error = angle - this.odometer.getAng();

			if (error < -180.0) {
				this.setSpeeds(-FAST, FAST);
			} else if (error < 0.0) {
				this.setSpeeds(FAST, -FAST);
			} else if (error > 180.0) {
				this.setSpeeds(FAST, -FAST);
			} else {
				this.setSpeeds(-FAST, FAST);
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
	
	public void clawOutTurnTo(double angle, boolean stop) {

		double error = angle - this.odometer.getAng();
		
		// change the trackWidth to compensate for the claw being out
		double initOdoWidth = odometer.getWidth();
		odometer.setWidth(13.8);
		
		while (Math.abs(error) > DEG_ERR) {
			
			error = angle - this.odometer.getAng();

			if (error < -180.0) {
				this.setSpeeds(-FAST, FAST);
			} else if (error < 0.0) {
				this.setSpeeds(FAST, -FAST);
			} else if (error > 180.0) {
				this.setSpeeds(FAST, -FAST);
			} else {
				this.setSpeeds(-FAST, FAST);
			}
		}

		if (stop) {
			//this.setSpeeds(0, 0);
			leftMotor.startSynchronization();
			leftMotor.stop();
			rightMotor.stop();
			leftMotor.endSynchronization();
		}
		
		// reset the trackWidth to its normal value
		odometer.setWidth(initOdoWidth);
		
	}

	public void goToDisp(int bx, int by, int fireLineY , String dispOrientation){
				
		// travel to the dispenser
		if( odometer.getY() > fireLineY*squareSize && Math.abs(bx*squareSize-odometer.getX()) > 1*squareSize){
			travelToXY(odometer.getX(), (fireLineY-1)*squareSize, odometer);
			travelToXY(bx*squareSize, (fireLineY-1)*squareSize, odometer);
			travelToXY(bx*squareSize, by*squareSize, odometer);
		}
		else if( odometer.getY() > fireLineY*squareSize && Math.abs(bx*squareSize-odometer.getX()) < 1*squareSize){
			travelToXY(bx*squareSize, by*squareSize, odometer);
		}
		else if( odometer.getY() < fireLineY*squareSize && Math.abs(bx*squareSize-odometer.getX()) > 1*squareSize){
			travelToXY(bx*squareSize, by*squareSize, odometer);
		}
		else if( odometer.getY() < fireLineY*squareSize && Math.abs(bx*squareSize-odometer.getX()) < 1*squareSize){
			travelToXY(bx*squareSize, by*squareSize, odometer);
		}
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e1) {
				
				e1.printStackTrace();
			}
		// prepare to start loading balls from the dispenser, offsets are now 0 because of the change in disp. coord specs
		if(dispOrientation.equals("E")){
			turnTo(0, true);
			travelToXY((bx + 0)*squareSize, by*squareSize, odometer);
		}
		else if(dispOrientation.equals("W")){
			turnTo(180,true);
			travelToXY((bx - 0)*squareSize, by*squareSize, odometer);
		}
		else if(dispOrientation.equals("N")){
			turnTo(90,true);
			travelToXY(bx*squareSize, (by + 0)*squareSize, odometer);	
		}
		else if(dispOrientation.equals("S")){
			turnTo(270,true);
			travelToXY(bx*squareSize, (by - 0)*squareSize, odometer);
		}
		
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		if(Math.abs(  odometer.getX() - bx*squareSize  )<3 &&Math.abs(  odometer.getY() - by*squareSize  )<3){
		
		wentToDisp = true;
		}
		
	}
	
	public void goToFireLine(int targetX, int fireLineY){
		
		// travel to the firing position, one tile below the firing line
		if( odometer.getY() > fireLineY*squareSize && Math.abs(targetX*squareSize-odometer.getX()) > 1*squareSize){
			travelToXY(odometer.getX(), (fireLineY-1)*squareSize, odometer);
			travelToXY(targetX*squareSize, (fireLineY-1)*squareSize, odometer);
		}
		else if( odometer.getY() > fireLineY*squareSize && Math.abs(targetX*squareSize-odometer.getX()) < 1*squareSize){
			travelToXY(targetX*squareSize, (fireLineY-1)*squareSize, odometer);
		}
		else if( odometer.getY() < fireLineY*squareSize && Math.abs(targetX*squareSize-odometer.getX()) > 1*squareSize){
			travelToXY(targetX*squareSize, (fireLineY-1)*squareSize, odometer);
		}
		else if( odometer.getY() < fireLineY*squareSize && Math.abs(targetX*squareSize-odometer.getX()) < 1*squareSize){
			travelToXY(targetX*squareSize, (fireLineY-1)*squareSize, odometer);
		}
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e1) {
			
			e1.printStackTrace();
		}
		// turn towards the dispenser
		turnTo(90,true);
		
		wentToFireLine = true;
		
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
	 * Go foward or backward a set distance in cm
	 */
	public void goForward(double distance) {
		
		leftMotor.setSpeed(FAST);
		rightMotor.setSpeed(FAST);
		//leftMotor.startSynchronization();
		leftMotor.rotate(convertDistance(odometer.getLeftRadius(), distance), true);
		rightMotor.rotate(convertDistance(odometer.getLeftRadius(), distance), false);
		//leftMotor.endSynchronization();
		

	}
	
	public void goBackward(double distance) {
		leftMotor.setSpeed(FAST);
		rightMotor.setSpeed(FAST);
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
