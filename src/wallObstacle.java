
import lejos.hardware.Sound;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.robotics.SampleProvider;

/**
 * @author Ege Ozer
 * wall obstacle class that has its own dedicated Thread. This class constantly scans for any object closer than 20cm with the ultrasonic sesnsor.
 * Once an object is detected, it goes to either left/right, goes a tile distance, then turns 90 degrees and goes the distance of two tiles. 
 * The angle at which it turns depends on the robots location(left, right or down lane going up, down, left or right)
 *
 */
public class wallObstacle extends Thread {
	/**
	 * Odometer type that stores odometer related information
	 */
	private Odometer odo;
	/**
	 * Navigation type that stores navigation related information
	 */
	private Navigation navi;
	/**
	 * SampleProvider type that is used to store ultrasonic controller general initial sensor information
	 */
	private SampleProvider usSensor;
	/**
	 * float[] type that is used to store ultrasonic controller fetching information
	 */
	private float[] usData;
	/**
	 * double type that stores the previous filtered distance
	 */
	private double prevDist  = 0;
	/**
	 * EV3LargeRegulatedMotor type that stores the left wheel motor information
	 */
	EV3LargeRegulatedMotor leftMotor;
	/**
	 * EV3LargeRegulatedMotor type that stores the right wheel motor information
	 */
	EV3LargeRegulatedMotor rightMotor;
	/**
	 * double type that holds the distance from one side to the other side of a tile
	 */
	double squareSize =  30.48;
	
	
	
	/**
	 * @param leftMotor left motor values are passed through EV3LargeRegulatedMotor type
	 * @param rightMotor right motor values are passed through EV3LargeRegulatedMotor type
	 * @param odo odometer values are passed thourgh Odometer type
	 * @param navi navigation values are passed though Navigation type
	 * @param usSensor ultrasonic controller values are passed through SampleProvider type
	 * @param usData ultrasonic controller fetching values are passed through float[] type
	 */
	public wallObstacle (EV3LargeRegulatedMotor leftMotor, EV3LargeRegulatedMotor rightMotor, Odometer odo, Navigation navi, SampleProvider usSensor, float[] usData ) {
		this.odo = odo;
		this.navi = navi;
		this.usSensor = usSensor;
		this.usData = usData;
		this.leftMotor = leftMotor;
		this.rightMotor = rightMotor;
	
	}
	
	public void run(){
		while(true){
			try {
				Thread.sleep(10);
				
				if(getFilteredData() < 20){
					
					odo.collision = true;
					
					leftMotor.startSynchronization();
					leftMotor.stop();
					rightMotor.stop();
					leftMotor.endSynchronization();
					
					double initX =  odo.getX();
					double initY = odo.getY();
					double initAng = odo.getAng();
					
					
					Thread.sleep(5000);
					//right lane going up
					
					if(initX >= squareSize*8 && Math.abs(initAng - 90)<5){
					
						navi.turnTo(180, true);
						Thread.sleep(3000);
						
						navi.goForward(squareSize);
						Thread.sleep(3000);
						
						odo.setPosition(new double [] {initX - squareSize, initY, 180}, new boolean [] {true, false, true});
						Thread.sleep(3000);
						
						navi.turnTo(90, true);
						Thread.sleep(3000);
						navi.goForward(2*squareSize);
						Thread.sleep(3000);
						odo.setPosition(new double [] {initX - squareSize, initY + 2*squareSize, 90}, new boolean [] {true, true, true});
						
						Sound.beep();
				
					}
				
					//right lane going down
				
					else if(initX >= squareSize*8 && Math.abs(initAng -270)<5){
					
						navi.turnTo(180, true);
						Thread.sleep(3000);
						navi.goForward(squareSize);
						Thread.sleep(3000);
						odo.setPosition(new double [] {initX - squareSize, initY, 180}, new boolean [] {true, false, true});
						Thread.sleep(3000);
						
						navi.turnTo(270, true);
						Thread.sleep(3000);
						navi.goForward(2*squareSize);
						Thread.sleep(3000);
						odo.setPosition(new double [] {initX - squareSize, initY - 2*squareSize, 270}, new boolean [] {true, true, true});
						
						Sound.beep();
				}

					//left lane going up
				
					else if(initX <= squareSize*2 && Math.abs(initAng -90)<5){
					
						navi.turnTo(0, true);
						Thread.sleep(3000);
						navi.goForward(squareSize);
						Thread.sleep(3000);
						odo.setPosition(new double [] {initX + squareSize, initY, 0}, new boolean [] {true, false, true});
						Thread.sleep(3000);
						
						navi.turnTo(90, true);
						Thread.sleep(3000);
						navi.goForward(2*squareSize);
						Thread.sleep(3000);
						odo.setPosition(new double [] {initX + squareSize, initY + 2*squareSize, 90}, new boolean [] {true, true, true});
						
						Sound.beep();
				}
				
					//left lane going down
				
					else if(initX <= squareSize*2 && Math.abs(initAng -270)<5){
					
						navi.turnTo(0, true);
						Thread.sleep(5000);
						navi.goForward(squareSize);
						Thread.sleep(5000);
						odo.setPosition(new double [] {initX + squareSize, initY, 0}, new boolean [] {true, false, true});
						Thread.sleep(5000);
						
						navi.turnTo(270, true);
						Thread.sleep(5000);
						navi.goForward(2*squareSize);
						Thread.sleep(5000);
						odo.setPosition(new double [] {initX + squareSize, initY - 2*squareSize, 270}, new boolean [] {true, true, true});
						
						Sound.beep();
				}
				
					//down lane going right
				
					else if(initY <= squareSize*2 && Math.abs(initAng-0)<5){
					
						navi.turnTo(90, true);
						Thread.sleep(3000);
						navi.goForward(squareSize);
						Thread.sleep(3000);
						odo.setPosition(new double [] {initX, initY+squareSize, 90}, new boolean [] {true, true, true});
						Thread.sleep(3000);
						
						navi.turnTo(0, true);
						Thread.sleep(3000);
						navi.goForward(2*squareSize);
						Thread.sleep(3000);
						odo.setPosition(new double [] {initX+2*squareSize, initY+squareSize, 0}, new boolean [] {true, true, true});
						
						Sound.beep();
				}
					//down lane going left
					else if(initY <= squareSize*2 && Math.abs(initAng-180)<5){
					
						navi.turnTo(90, true);
						Thread.sleep(3000);
						navi.goForward(squareSize);
						Thread.sleep(3000);
						odo.setPosition(new double [] {initX , initY+squareSize, 90}, new boolean [] {true, true, true});
						Thread.sleep(3000);
						
						navi.turnTo(180, true);
						Thread.sleep(3000);
						navi.goForward(2*squareSize);
						Thread.sleep(3000);
						odo.setPosition(new double [] {initX - 2*squareSize, initY +squareSize, 180}, new boolean [] {true, true, true});
						
						Sound.beep();
				
					}
		
					odo.collision = false;
				
				}		
				else{
					odo.collision = false;
				}
				
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
	
	/**
	 * @return returns the filtered data
	 * @author Sean Lawlor, ECSE 211 - Design Principles and Methods, Head TA
	 * method that filters the fetched ultrasonic distances.
	 */
	private float getFilteredData() {
		
		usSensor.fetchSample(usData, 0);
		float distance =100* usData[0];
		int filterControl = 0;
		
		if (distance >= 255 && filterControl < 25) {
			// bad value, do not set the distance var, however do increment the
			// filter value
			filterControl++;
		} 
		else if (distance >= 255) {
			// We have repeated large values, so there must actually be nothing
			// there: leave the distance alone
			prevDist = distance;
		} 	
		else {
			// distance went below 255: reset filter and leave
			// distance alone.
			filterControl = 0;
			prevDist = distance;
		}
						
		return distance;
	}

}
