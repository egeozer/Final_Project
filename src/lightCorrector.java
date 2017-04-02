


import lejos.hardware.Sound;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.sensor.BaseSensor;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.SensorModes;
import lejos.robotics.SampleProvider;

public class lightCorrector {
	private Odometer odo;
	private Navigation navi;
	private SampleProvider colorSensorRight,colorSensorLeft;
	private float[] colorDataRight,colorDataLeft;
	private EV3LargeRegulatedMotor leftMotor, rightMotor;
	double lightSensorDist = 6.00; 		//distance added to compensate for the distance from the wheelbase to the lightSensor, was 3.5cm
	int fast = 200;
	
	public lightCorrector(Odometer odo, Navigation navi, SampleProvider colorSensorRight, float[] colorDataRight,SampleProvider colorSensorLeft, float[] colorDataLeft) {
		this.odo = odo;
		this.navi = navi;
		this.colorSensorRight = colorSensorRight;
		this.colorDataRight = colorDataRight;
		this.colorSensorLeft = colorSensorLeft;
		this.colorDataLeft = colorDataLeft;
		Sound.setVolume(40);
	}
	
	public void correct(Odometer odo, Navigation navi, SampleProvider colorSensorRight, float[] colorDataRight,SampleProvider colorSensorLeft, float[] colorDataLeft) {
		this.odo = odo;
		this.navi = navi;
		this.colorSensorRight = colorSensorRight;
		this.colorDataRight = colorDataRight;
		this.colorSensorLeft = colorSensorLeft;
		this.colorDataLeft = colorDataLeft;
		EV3LargeRegulatedMotor[] motors = this.odo.getMotors();
		this.leftMotor = motors[0];
		this.rightMotor = motors[1];
			
		leftMotor.setSpeed(fast);
		rightMotor.setSpeed(fast);
		
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}	

		lightRight right = new lightRight(colorSensorRight, colorDataRight, odo );
		lightLeft left = new lightLeft(colorSensorLeft, colorDataLeft, odo );
		
		// when facing the black line, wait 3 seconds
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}		
	
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
		
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}		
		
		navi.goForward(lightSensorDist);
		
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
	}
	
	public void travelCorrect(Odometer odo, Navigation navi, SampleProvider colorSensorRight, float[] colorDataRight,SampleProvider colorSensorLeft, float[] colorDataLeft) {
		this.odo = odo;
		this.navi = navi;
		this.colorSensorRight = colorSensorRight;
		this.colorDataRight = colorDataRight;
		this.colorSensorLeft = colorSensorLeft;
		this.colorDataLeft = colorDataLeft;
		EV3LargeRegulatedMotor[] motors = this.odo.getMotors();
		this.leftMotor = motors[0];
		this.rightMotor = motors[1];
			
		leftMotor.setSpeed(fast);
		rightMotor.setSpeed(fast);
		
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}	
		
		lightRight right = new lightRight(colorSensorRight, colorDataRight, odo );
		lightLeft left = new lightLeft(colorSensorLeft, colorDataLeft, odo );	
		
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}	
		
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
		
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}	
		
		navi.goForward(lightSensorDist);
		
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}	
		
	}
	
	public void fireCorrect(Odometer odo, Navigation navi, SampleProvider colorSensorRight, float[] colorDataRight,SampleProvider colorSensorLeft, float[] colorDataLeft) {
		this.odo = odo;
		this.navi = navi;
		this.colorSensorRight = colorSensorRight;
		this.colorDataRight = colorDataRight;
		this.colorSensorLeft = colorSensorLeft;
		this.colorDataLeft = colorDataLeft;
		EV3LargeRegulatedMotor[] motors = this.odo.getMotors();
		this.leftMotor = motors[0];
		this.rightMotor = motors[1];
			
		leftMotor.setSpeed(fast);
		rightMotor.setSpeed(fast);
		
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}	

		lightRight right = new lightRight(colorSensorRight, colorDataRight, odo );
		lightLeft left = new lightLeft(colorSensorLeft, colorDataLeft, odo );
		
		// when facing the black line, wait 3 seconds
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}		
	
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
		
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}		
		
	}
	
}
		
		
	
