
import lejos.robotics.SampleProvider;

public class wallObstacle {
	private Odometer odo;
	private Navigation navi;
	private SampleProvider usSensor;
	private float[] usData;
	private double prevDist  = 0;
	
	
	public wallObstacle (Odometer odo, Navigation navi, SampleProvider usSensor, float[] usData) {
		this.odo = odo;
		this.navi = navi;
		this.usSensor = usSensor;
		this.usData = usData;
		
	}
	
	public void activate(){
		
		while(true){
			try {
				Thread.sleep(10);
				System.out.println(getFilteredData());
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
