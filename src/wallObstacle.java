
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
	double squareSize =  30.48;
	
	
	
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
				
				if(getFilteredData() < 20){
					odo.collision = true;
					
					//leftMotor.startSynchronization();
					leftMotor.stop();
					rightMotor.stop();
					//leftMotor.endSynchronization();
					
					double initX =  odo.getX();
					double initY = odo.getY();
					double initAng = odo.getAng();
					
					//if (initX < 2 * squareSize && initY < 2 * squareSize){
						
					//}
					
					//if (initX < 2 * squareSize && initY < 2 * squareSize)
					
					//if()
					Thread.sleep(5000);
				
					navi.turnTo(180, true);
					Thread.sleep(3000);
					navi.goForward(squareSize);
					Thread.sleep(3000);
					odo.setPosition(new double [] {initX - squareSize, initY, 180}, new boolean [] {true, false, true});
					Thread.sleep(3000);
					
					navi.turnTo(90, true);
					Thread.sleep(3000);
					navi.goForward(2*squareSize);
					Thread.sleep(3000);
					odo.setPosition(new double [] {initX - squareSize, initY + 2*squareSize, 90}, new boolean [] {true, true, true});
					
					Sound.beep();
					
					
					//navi.goBackward(5);
					//navi.turnImm(85);
					//navi.goForward(15);
					
					//navi.turnImm(-85);
					
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
