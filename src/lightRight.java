import lejos.hardware.Sound;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.robotics.SampleProvider;

public class lightRight extends Thread {

	private SampleProvider colorSensorRight;
	private float[] colorDataRight;
	private EV3LargeRegulatedMotor leftMotor, rightMotor;
	boolean havePassed = false;
	boolean scanLine = false;
	//lightLeft left;
	
	
	lightRight(SampleProvider colorSensorRight,  float[] colorDataRight,Odometer odo ){
		
		this.colorSensorRight = colorSensorRight;
		this.colorDataRight = colorDataRight;
		EV3LargeRegulatedMotor[] motors = odo.getMotors();
		this.leftMotor = motors[0];
		this.rightMotor = motors[1];
		//this.left = left;
	
	}
	
	
	public void run(){
		
while(true){
	
	try{
		Thread.sleep(10);
	
		if(scanLine){
			
			colorSensorRight.fetchSample(colorDataRight, 0);
			if(colorDataRight[0]<0.3){	//if the robot crosses the black line, it will get the distance, pointA(X value from center to black line)
				havePassed = true;
				rightMotor.stop();
				Sound.beep();
				//wait();
		
				
				//break;
			
			}
	
		}
		
	}
	catch(InterruptedException e){
		
	}
		
}
	}

}
