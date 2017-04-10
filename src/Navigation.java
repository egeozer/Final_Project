
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.robotics.SampleProvider;

/**
 * This class is responsible for navigating the robot to specific coordinates.
 * 
 * @author Ege Ozer
 * @author Mitchell Keeley
 * 
 */

public class Navigation {
	
	/**
	 *  desired speeds and acceleration values in type int
	 */
	final static int FAST = 200, SLOW = 100, clawTurnSpeed = 125, ACCELERATION = 6000;
	/**
	 * max tolerated error constant values in type double
	 */
	final static double DEG_ERR = 2.0, CM_ERR = 1.0;
	/**
	 * Odometer type that stores odometer related information
	 */
	private Odometer odometer;
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
	 * boolean variable that is used to determine if robot went to dispenser
	 */
	boolean wentToDisp  = false;
	/**
	 * boolean variable that is used to determine if robot went to firing line
	 */
	boolean wentToFireLine = false;
	/**
	 * boolean variable that is used to determine if robot went to defense line
	 */
	boolean wentToDefLine = false;
	/**
	 * double variable that is used to determine the robots traveling angle
	 */
	double travelingAngle = 0;
	
	/**
	 * double type that holds the size of a square playing tile
	 */
	static double squareSize = 30.48;

	/**
	 * @param odo odometer values are passed through Odometer type
	 * @param colorSensorRight right light sensor general initial information is passed through SampleProvider type
	 * @param colorDataRight right light sensor fetching values are passed through float[] type
	 * @param colorSensorLeft left light sensor general initial information is passed through SampleProvider type
	 * @param colorDataLeft  left light sensor fetching values are passed through float[] type
	 */
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
	
	
	/**
	 * Function to set the motor speeds jointly
	 */
	public void setSpeeds(int lSpd, int rSpd) {
		
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
	
	}

	/**
	 * TravelTo function which takes as arguments the x and y position in cm Will travel to designated position, while
	 * constantly updating it's heading NOTE:** this method is not used in the last version of the code, travelXY is used instead
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
	
	/**
	 * This method is the main navigation method used for the final demo. This methods takes as parameters the x and y value
	 * of the final destination and it will go there by moving only straight in x or y directions. It will first travel along
	 * the x then go straight along the y direction. This way of navigation is strictly designed for avoiding restricted areas
	 * @param finalX x coordinate parameter
	 * @param finalY y coordinate parameter
	 * @param odo odometer values are passed through this parameter
	 */
	public void travelToXY(double finalX, double finalY, Odometer odo) {
		
		lightCorrector corrector = new lightCorrector(odo, this, colorSensorRight, colorDataRight,colorSensorLeft, colorDataLeft);
		//if there is collision, this section is ignored
		if(!odo.collision){
			double initialX = odo.getX();
			double initialY = odo.getY();
			double travelingAngle = 0;
			double currentX = odo.getX();
			double odoXCorrect;
			int squaresTravelledX = 0;
			
			// the difference in initial and final x is smaller than the fixed error, do nothing
			if(Math.abs(finalX - currentX) <= (CM_ERR*5)){
				
			//if current x has a smaller value than final x value then the destination is on the right and vice versa
			}else if(currentX < finalX){
				turnTo(0, true);
				travelingAngle = 0;
				odo.setTheta(travelingAngle);
			}else{
				turnTo(180, true);
				travelingAngle = 180;
				odo.setTheta(travelingAngle);
			}
			
			//  while the initial x is enough far from final x, it loops inside this while loop
			while (Math.abs(finalX - currentX) > (CM_ERR*5)){
				//if collision occurs during travel, break out
				if(odo.collision)
					break;
				//correct heading by scanning the first black lines seen
				corrector.travelCorrect();
				//increment sqaureTravelled since one correction = 1 tile passed
				squaresTravelledX++;
				//correct odometer
				odoXCorrect = squaresTravelledX*squareSize;
				//set odometer the appropriate corrected value in x
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
			
			// see X direction section , same logic applies
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
			
			}

	
		}

	
	}

	/**
	 * TurnTo function which takes an angle and boolean as arguments. The boolean controls whether or not to stop the
	 * motors when the turn is completed
	 * @param angle parameter that takes a specific angle for the robot to turn to
	 * @param stop boolean value that is set to true to stop motors, false otherwise
	 */
	public void turnTo(double angle, boolean stop) {
		
		if(angle<0)
			angle=angle+360;

		double error = angle - this.odometer.getAng();

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
			leftMotor.startSynchronization();
			leftMotor.stop();
			rightMotor.stop();
			leftMotor.endSynchronization();
		}
		
	}
	/**
	 * TurnTo function which takes an angle and boolean as arguments. The only difference between the normal turn to
	 * and this is that the track width value is different. The boolean controls whether or not to stop the
	 * motors when the turn is completed
	 * @param angle parameter that takes a specific angle for the robot to turn to
	 * @param stop boolean value that is set to true to stop motors, false otherwise
	 */
	public void clawOutTurnTo(double angle, boolean stop) {

		if(angle<0)
			angle = angle+360;
		
		double error = angle - this.odometer.getAng();
		
		if(Math.abs(error) >180 && Math.abs(error)<450)
			error = error-360;
		
		// change the trackWidth to compensate for the claw being out
		double initOdoWidth = odometer.getWidth();
		odometer.setWidth(13.2);
		
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
			leftMotor.startSynchronization();
			leftMotor.stop();
			rightMotor.stop();
			leftMotor.endSynchronization();
		}
		
		// reset the trackWidth to its normal value
		odometer.setWidth(initOdoWidth);
		
	}

	
	/**
	 * method to determine how the robot travels to the dispencer while avoiding
	 * the restricted areas of the playing field, and orients itself based on the
	 * specified orientation of the ball dispenser. Also indicates whether the robot
	 * has completed its attempt to reach the ball dispenser.
	 * 
	 * @param bx is the grid line position of the ball dispenser on the x-axis
	 * @param by is the grid line position of the ball dispenser on the y-axis
	 * @param fireLineY is the grid line position of the firing line on the y-axis
	 * @param dispOrientation is the cardinal orientation of the ball dispenser
	 */
	public void goToDisp(int bx, int by, int fireLineY , String dispOrientation){
					
		// last minute fix to correct the discrepancy in the clarifications for the competition
		if(dispOrientation.equals("E")){
			bx = bx +1;
		}
		else if(dispOrientation.equals("W")){
			bx = bx -1;
		}
		else if(dispOrientation.equals("N")){
			by = by +1;	
		}
		else if(dispOrientation.equals("S")){
			by = by-1;
		}
		
		// travel to the dispenser by avoiding restricted zones
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
		//if robot is at dispensor, set wentToDisp to true
		if(Math.abs(  odometer.getX() - bx*squareSize  )<3 &&Math.abs(  odometer.getY() - by*squareSize  )<3){
		
		wentToDisp = true;
		}
		
	}
	
	/**
	 * method to determine how the robot travels to the firing line and
	 * is correctly facing upfield, while avoiding the restricted areas 
	 * of the playing field. Also indicates whether the robot has completed
	 * its attempt to reach the firing line.
	 * 
	 * @param targetX is the grid line position of the target on the x-axis
	 * @param fireLineY is the grid line position of the firing line on the y-axis
	 */
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
		turnTo(90,true);
		if(Math.abs(  odometer.getX() - targetX*squareSize  )<3 && Math.abs(  odometer.getY() - (fireLineY-1)*squareSize  )<3){
			wentToFireLine = true;
	}
		
	}
	
	
	/**
	 * method to determine how the robot travels to the desired defense
	 * position and is correctly facing downfield, while avoiding the restricted areas 
	 * of the playing field. Also indicates whether the robot has completed
	 * its attempt to reach a defense position.
	 * 
	 * @param targetX is the grid line position of the target on the x-axis
	 * @param defLineY is the grid line position of the desired defense position on the y-axis
	 */
	public void goToDefLine(int targetX, int defLineY){
		
		// travel to the firing position, one tile below the firing line
		if( odometer.getY() > (defLineY-1)*squareSize && Math.abs(targetX*squareSize-odometer.getX()) > 1*squareSize){
			travelToXY(odometer.getX(), (defLineY-1)*squareSize, odometer);
			travelToXY(targetX*squareSize, (defLineY-1)*squareSize, odometer);
		}
		else if( odometer.getY() > (defLineY-1)*squareSize && Math.abs(targetX*squareSize-odometer.getX()) < 1*squareSize){
			travelToXY(targetX*squareSize, (defLineY-1)*squareSize, odometer);
		}
		else if( odometer.getY() < (defLineY-1)*squareSize && Math.abs(targetX*squareSize-odometer.getX()) > 1*squareSize){
			travelToXY(targetX*squareSize, (defLineY-1)*squareSize, odometer);
		}
		else if( odometer.getY() < (defLineY-1)*squareSize && Math.abs(targetX*squareSize-odometer.getX()) < 1*squareSize){
			travelToXY(targetX*squareSize, (defLineY-1)*squareSize, odometer);
		}
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e1) {
			
			e1.printStackTrace();
		}
		// turn towards the other robot
		turnTo(270,true);
		if(Math.abs(  odometer.getX() - targetX*squareSize  ) <3 && Math.abs(  odometer.getY() - (defLineY-1)*squareSize  )<3){
			wentToDefLine = true;
		}
		
	}
	
	/**
	 * Turn Immediate method is coded when it is required for the robot to turn a specific set angle, no matter
	 * where the initial angle is.
	 * @param angle parameter that takes an angle to turn the robot by
	 */
	public void turnImm(double angle) {

		leftMotor.rotate(convertAngle(odometer.getLeftRadius(), odometer.getWidth(), angle), true);
		rightMotor.rotate(-convertAngle(odometer.getLeftRadius(), odometer.getWidth(), angle), false);
		
	}
	
	/**
	 * Go forward a set distance in cm
	 * 
	 * @param distance is the distance to go forward
	 */
	public void goForward(double distance) {
		
		leftMotor.setSpeed(FAST);
		rightMotor.setSpeed(FAST);
		leftMotor.startSynchronization();
		leftMotor.rotate(convertDistance(odometer.getLeftRadius(), distance), true);
		rightMotor.rotate(convertDistance(odometer.getLeftRadius(), distance), false);
		leftMotor.endSynchronization();

	}
	/**
	 * Go backward a set distance in cm
	 * 
	 * @param distance is the distance to go backward
	 */
	public void goBackward(double distance) {
		leftMotor.setSpeed(FAST);
		rightMotor.setSpeed(FAST);
		leftMotor.startSynchronization();
		leftMotor.rotate(-convertDistance(odometer.getLeftRadius(), distance), true);
		rightMotor.rotate(-convertDistance(odometer.getLeftRadius(), distance), false);
		leftMotor.endSynchronization();

	}
	/**
	 * Converts a desired distance in cm into the radians required to move the robot said distance
	 * 
	 * @param radius is the radius of the wheels
	 * @param distance is the distance to be traveled
	 * @return
	 */
	private static int convertDistance(double radius, double distance) {
		return (int) ((180.0 * distance) / (Math.PI * radius));
	}

	/**
	 * Converts a desired change in heading in degrees into the radians required to rotate the robot said amount 
	 * 
	 * @param radius is the radius of the wheels
	 * @param width is the track width of the robot
	 * @param angle is the angle for the robot to turn
	 * @return
	 */
	private static int convertAngle(double radius, double width, double angle) {
		return convertDistance(radius, Math.PI * width * angle / 360.0);
	}

}
