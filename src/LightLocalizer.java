


import lejos.hardware.Sound;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.robotics.SampleProvider;

public class LightLocalizer {
	private Odometer odo;
	private Navigation navi;
	private SampleProvider colorSensorRight,colorSensorLeft;
	private float[] colorDataRight,colorDataLeft;
	private EV3LargeRegulatedMotor leftMotor, rightMotor;
	double lightSensorDist = 3.0; 		//distance added to compensate the distance from the center and to the lightSensor
	double x, y, xTheta, yTheta;
	double eucDistance, heading;
	int axisCounter;
	
	
	public LightLocalizer(Odometer odo, Navigation navi, SampleProvider colorSensorRight, float[] colorDataRight,SampleProvider colorSensorLeft, float[] colorDataLeft) {
		this.odo = odo;
		this.navi = navi;
		this.colorSensorRight = colorSensorRight;
		this.colorDataRight = colorDataRight;
		this.colorSensorLeft = colorSensorLeft;
		this.colorDataLeft = colorDataLeft;
		Sound.setVolume(50);
	}
	
	public void doLocalization(Odometer odo, Navigation navi, SampleProvider colorSensorRight, float[] colorDataRight,SampleProvider colorSensorLeft, float[] colorDataLeft) {
		this.odo = odo;
		this.navi = navi;
		this.colorSensorRight = colorSensorRight;
		this.colorDataRight = colorDataRight;
		this.colorSensorLeft = colorSensorLeft;
		this.colorDataLeft = colorDataLeft;
		EV3LargeRegulatedMotor[] motors = this.odo.getMotors();
		this.leftMotor = motors[0];
		this.rightMotor = motors[1];
		double pointA = 0;		//distances that we need to record for the calculation of 0,0
		double pointB = 0;
		
		leftMotor.setSpeed(150);
		rightMotor.setSpeed(150);
		
		
		//robot goes forward until it sees a black line
		//leftMotor.startSynchronization();
		leftMotor.forward();		
		rightMotor.forward();
		//leftMotor.endSynchronization();
		
		
		
		while(true){
			
			colorSensorRight.fetchSample(colorDataRight, 0);
			if(colorDataRight[0]<0.3){	//if the robot crosses the black line, it will get the distance, pointA(X value from center to black line)
				//Sound.beep();
				
				
				leftMotor.startSynchronization();
				leftMotor.stop();
				rightMotor.stop();
				
				navi.goForward(lightSensorDist*2);
				leftMotor.endSynchronization();
				
				
				
				// records the x distance between starting position and the y-axis
				pointA = odo.getY();
				
				//once the first distance is recorded, it goes back half that distance 
				//navi.goBackward(pointA/2);
				odo.setWidth(14.2);
				
				break;
			}
		}
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		navi.turnTo(0,true);	//we have an offset of approximately 0 degrees, due to our track being amazingly accurate
	
		//leftMotor.startSynchronization();
		navi.goForward(pointA);
		
		//leftMotor.endSynchronization();
		
		
	/*while(true){
			
			colorSensor.fetchSample(colorData, 0);	//second part where it first got pointA, now pointB will be obtained(Y value from center to black line)
			if(colorData[0]<0.3){
				Sound.beep();
				
				Sound.beep();
				navi.goForward(lightSensorDist);
				Sound.beep();
				
				
				leftMotor.stop();
				rightMotor.stop();
				
				
				// records the y distance between starting position and the x-axis
				pointB = odo.getY();
				
				//once the first distance is recorded, it goes back where it started
				//navi.goBackward(pointB);
				break;

			}		
		
		}
		*/
	
	
	// TODO:
	// TODO: This is where we set our coordinate system and turn to face the right direction to start the round
	// TODO: 
	
		
		
	//navi.turnTo(0, false);	
	//navi.goForward((pointA/2)+lightSensorDist);
		odo.setLeftRadius(2.05);
		odo.setRightRadius(2.05);
	
			// changed from 90 to 0
	//Sound.beep();
	
	
	
	}
		
}
		
		
	


