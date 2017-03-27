import lejos.hardware.Sound;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.robotics.SampleProvider;

public class lightLeft extends Thread {

	private SampleProvider colorSensorLeft;
	private float[] colorDataLeft;
	private EV3LargeRegulatedMotor leftMotor, rightMotor;
	boolean havePassed = false;
	boolean scanLine = false;
	//lightRight right;
	
	lightLeft(SampleProvider colorSensorLeft,  float[] colorDataLeft,Odometer odo ){
		
		this.colorSensorLeft = colorSensorLeft;
		this.colorDataLeft = colorDataLeft;
		EV3LargeRegulatedMotor[] motors = odo.getMotors();
		this.leftMotor = motors[0];
		this.rightMotor = motors[1];
		//this.right = right;
	}
	
	
	public void run(){
while(true){
	try{
	Thread.sleep(10);
	if(scanLine){
			
		colorSensorLeft.fetchSample(colorDataLeft, 0);
			if(colorDataLeft[0]<0.3){	//if the robot crosses the black line, it will get the distance, pointA(X value from center to black line)
				havePassed = true;
				leftMotor.stop();
				Sound.beep();
				break;
					
				}
				
		
				
				//break;
			}
	}
		
	catch(InterruptedException e){
		
	}
		
}
	}
}
