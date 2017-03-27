


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
		
		lightRight right = null;
		lightLeft left = null;
		 right = new lightRight(colorSensorRight, colorDataRight, odo );
		 left = new lightLeft(colorSensorLeft, colorDataLeft, odo );
		
		leftMotor.forward();
		rightMotor.forward();
		right.start();
		left.start();
		
		right.scanLine=true;
		left.scanLine=true;
		
		
	
		
	
	
	}
		
}
		
		
	


