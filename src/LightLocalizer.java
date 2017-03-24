


import lejos.hardware.Sound;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.robotics.SampleProvider;

public class LightLocalizer {
	private Odometer odo;
	private Navigation navi;
	private SampleProvider colorSensor;
	private float[] colorData;
	private EV3LargeRegulatedMotor leftMotor, rightMotor;
	double lightSensorDist = 3.0; 		//distance added to compensate the distance from the center and to the lightSensor
	double x, y, xTheta, yTheta;
	double eucDistance, heading;
	int axisCounter;
	
	
	public LightLocalizer(Odometer odo, Navigation navi, SampleProvider colorSensor, float[] colorData) {
		this.odo = odo;
		this.navi = navi;
		this.colorSensor = colorSensor;
		this.colorData = colorData;
		Sound.setVolume(50);
	}
	
	public void doLocalization(Odometer odo, Navigation navi, SampleProvider colorSensor, float[] colorData) {
		this.odo = odo;
		this.navi = navi;
		this.colorSensor = colorSensor;
		this.colorData = colorData;
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
			
			colorSensor.fetchSample(colorData, 0);
			if(colorData[0]<0.3){	//if the robot crosses the black line, it will get the distance, pointA(X value from center to black line)
				Sound.beep();
				
				
				leftMotor.startSynchronization();
				leftMotor.stop();
				rightMotor.stop();
				leftMotor.endSynchronization();
				
				navi.goForward(lightSensorDist);
				
				// records the x distance between starting position and the y-axis
				pointA = odo.getY();
				
				//once the first distance is recorded, it goes back half that distance 
				//navi.goBackward(pointA/2);
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				break;
			}
		}
		odo.setWidth(14.1);
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
		odo.setLeftRadius(2);
		odo.setRightRadius(2);
	odo.setPosition(new double [] {0,0,0}, new boolean [] {true, true, true});
	
	//once everything is collected, the odometer is set to the updated position and now we can call it to go to 0,0,0 without any problem
	//odo.setPosition(new double [] {-((pointA/2)+lightSensorDist), 0, odo.getAng()}, new boolean [] {true, true, true});
	
	//navi.travelTo(0,0);
	//if(odo.startPos==1)
	//navi.turnTo(0,true);			// changed from 90 to 0
	Sound.beep();
	
	
	//else if(odo.startPos==2)
		//navi.turnTo(0,true);
	
	/*
	 * 
	 * Instead of changing where it turns to, we could just change what angle it sets it's heading at
	 * 
	 */
	
	//odo.setPosition(new double [] {odo.getX(), odo.getY(), odo.getAng()}, new boolean [] {true, true, true});
	
	
	//odo.setTheta();
	
	}
		
}
		
		
	


