import lejos.hardware.Sound;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.robotics.SampleProvider;

/**
 * This class controls the right light sensor, it runs on its own dedicated thread
 * @author Ege Ozer
 *
 */
public class lightLeft extends Thread {

	/**
	 * SampleProvider type that is used to store right light general initial sensor information
	 */
	private SampleProvider colorSensorLeft;
	/**
	 *  float[] type that is used to store left light fetching information
	 */
	private float[] colorDataleft;
	private EV3LargeRegulatedMotor leftMotor, rightMotor;
	/**
	 * boolean value for indicating that a black line has been passed
	 */
	boolean havePassed = false;
	/**
	 * boolean value that shows the status of black line search. True if it's being scanned, false otherwise
	 */
	boolean scanLine = false;
	

	/**
	 * @param colorSensorleft left light sensor information is passed through SampleProvider type
	 * @param colorDataleft left light sensor fetching values are passed through float[] type
	 * @param odo odometer is passed for left and left wheel motors
	 */
	lightLeft(SampleProvider colorSensorleft,  float[] colorDataleft,Odometer odo ){
		
		this.colorSensorLeft = colorSensorleft;
		this.colorDataleft = colorDataleft;
		EV3LargeRegulatedMotor[] motors = odo.getMotors();
		this.leftMotor = motors[0];
		this.leftMotor = motors[1];
		Sound.setVolume(0);
		
	}
	
	public void run(){

		while(true){
			try{
				Thread.sleep(10);
				if(scanLine){	
					colorSensorLeft.fetchSample(colorDataleft, 0);
				
					if(colorDataleft[0]<0.3){	
						havePassed = true;
						leftMotor.stop();
						Sound.beep();
						break;			
						
					}
			
				}
	
			}	
	
			catch(InterruptedException e){
		
	
			}
		

		}
	
	}

}
