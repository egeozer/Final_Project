
import lejos.hardware.Sound;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.robotics.SampleProvider;

public class wallObstacle extends Thread {
	private Odometer odo;
	private Navigation navi;
	private SampleProvider usSensor;
	private float[] usData;
	private double prevDist  = 0;
	EV3LargeRegulatedMotor leftMotor;
	EV3LargeRegulatedMotor rightMotor;
	
	
	
	public wallObstacle (EV3LargeRegulatedMotor leftMotor, EV3LargeRegulatedMotor rightMotor, Odometer odo, Navigation navi, SampleProvider usSensor, float[] usData ) {
		this.odo = odo;
		this.navi = navi;
		this.usSensor = usSensor;
		this.usData = usData;
		this.leftMotor = leftMotor;
		this.rightMotor = rightMotor;
		//leftMotor.setSpeed(450);
		//rightMotor.setSpeed(500);
		
	}
	

	public void run(){
		while(true){
			try {
				Thread.sleep(10);
				//System.out.println(getFilteredData());
				
				if(getFilteredData()<20){
					odo.collision = true;
					
					Thread.sleep(2000);
					
					odo.collision = false;
					
					// TODO: insert ifs to decide how to avoid obstacles based on current headin
					navi.travelToXY(odo.getX() - 1*30.48, odo.getY() + 3*30.48, odo);
				
					
					
					//navi.turnImm(-90);
					odo.collisionAvoided = true;
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
