import lejos.hardware.Sound;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.robotics.SampleProvider;

/**
 * This class controls the right light sensor, it runs on its own dedicated thread
 * @author Ege Ozer
 *
 */
public class lightRight extends Thread {

	/**
	 * SampleProvider type that is used to store right light general initial sensor information
	 */
	private SampleProvider colorSensorRight;
	/**
	 *  float[] type that is used to store right light fetching information
	 */
	private float[] colorDataRight;
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
	 * @param colorSensorRight right light sensor information is passed through SampleProvider type
	 * @param colorDataRight right light sensor fetching values are passed through float[] type
	 * @param odo odometer is passed for left and right wheel motors
	 */
	lightRight(SampleProvider colorSensorRight,  float[] colorDataRight,Odometer odo ){
		
		this.colorSensorRight = colorSensorRight;
		this.colorDataRight = colorDataRight;
		EV3LargeRegulatedMotor[] motors = odo.getMotors();
		this.leftMotor = motors[0];
		this.rightMotor = motors[1];
		Sound.setVolume(0);
		
	}
	
	public void run(){

		while(true){
			try{
				Thread.sleep(10);
				if(scanLine){	
					colorSensorRight.fetchSample(colorDataRight, 0);
				
					if(colorDataRight[0]<0.3){	
						havePassed = true;
						rightMotor.stop();
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
