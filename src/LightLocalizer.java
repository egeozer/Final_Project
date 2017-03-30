


import lejos.hardware.Sound;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.robotics.SampleProvider;

public class LightLocalizer {
	private Odometer odo;
	private Navigation navi;
	private SampleProvider colorSensorRight,colorSensorLeft;
	private float[] colorDataRight,colorDataLeft;
	private EV3LargeRegulatedMotor leftMotor, rightMotor;
	double lightSensorDist = 6.00; 		//distance added to compensate for the distance from the wheelbase to the lightSensor, was 3.5cm
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
		
		leftMotor.setSpeed(150);
		rightMotor.setSpeed(150);
		
		//lightRight right = null;
		//lightLeft left = null;
		lightRight right = new lightRight(colorSensorRight, colorDataRight, odo );
		lightLeft left = new lightLeft(colorSensorLeft, colorDataLeft, odo );
		
		// when facing the black line, wait 3 seconds and then start moving forward
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}		
		
		leftMotor.startSynchronization();
		leftMotor.forward();
		rightMotor.forward();
		leftMotor.endSynchronization();
		
		// starts light localization in the y direction
		right.start();		// stops right motor when right sensor sees a black line, then beeps
		left.start();		// stops left motor when left sensor sees a black line, then beeps
		
		right.scanLine=true;
		left.scanLine=true;
		
		try {
			right.join();
			left.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		right.scanLine=false;
		left.scanLine=false;
		
		//Sound.beep();
		//Sound.beep();
		
		// go to the zero on the y-axis
		navi.goForward(lightSensorDist);
		//odo.setPosition(new double [] {0,0,0}, new boolean [] {false, true, false});
		
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}		
		
		// set theta to 90 degrees as we now face the positive y-axis
		odo.setPosition(new double [] {0,0,90}, new boolean [] {false, false, true});		
						
		// turn to face the postive x-direction, wait 3 seconds and then start moving forward to the black line
		navi.turnTo(0, true);
		
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}	
		
		// starts light localization in the x direction
		right = new lightRight(colorSensorRight, colorDataRight, odo );
		left = new lightLeft(colorSensorLeft, colorDataLeft, odo );
		
		leftMotor.startSynchronization();
		leftMotor.forward();
		rightMotor.forward();
		leftMotor.endSynchronization();
		
		right.start();		// stops right motor when right sensor sees a black line, then beeps
		left.start();		// stops left motor when left sensor sees a black line, then beeps
		
		right.scanLine=true;
		left.scanLine=true;
		
		try {
			right.join();
			left.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		right.scanLine=false;
		left.scanLine=false;
		
		//Sound.beep();
		//Sound.beep();
		
		// go to the zero on the x-axis
		navi.goForward(lightSensorDist);
	}
		
}
		
		
	


