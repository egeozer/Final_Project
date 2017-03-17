


import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.Button;
import lejos.hardware.Sound;
import lejos.hardware.lcd.TextLCD;

public class TestMotors {
	
	// Loading motor is connected to output B
	public static final EV3LargeRegulatedMotor loadingMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("B"));
	// Gear motor is connected to output C
	public static final EV3LargeRegulatedMotor winchMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("C"));
	
	// Instance the text display
	public static int buttonC;
	
	final static TextLCD t = LocalEV3.get().getTextLCD();
		
		public static void main(String[] args) 
		{
			
			winchMotor.setAcceleration(300);
			loadingMotor.setAcceleration(300);
			
			winchMotor.setSpeed(300);
			loadingMotor.setSpeed(400);
			
			Sound.setVolume(40);
			
			while(true)
			{
				
				// Get the position of the target from the user
				buttonC = getButtonChoise();
			
				if (buttonC == Button.ID_UP){
					
					// TODO: get in front of the dispenser
					
					// ready the loading arm to receive the ball
					loadingMotor.rotate(-150);
					
					// beep, and wait 5 seconds to receive the ball
					Sound.beep();
					try {
					    Thread.sleep(5000);
					} catch(InterruptedException ex) {
					    Thread.currentThread().interrupt();
					}
					
					// pull back the elastic to the desired position
					winchMotor.rotate(-630);
										
					// load the ball into the launcher and hold the elastic in position
					loadingMotor.rotate(150);
					
					// TODO: navigate to the firing line and turn to the firing position
					
					// unwind the winch to ensure the launcher can fire at full power
					winchMotor.rotate(630);
					
					// release the ball
					loadingMotor.rotate(50);
					
				}
									
				if (buttonC == Button.ID_LEFT)
					winchMotor.rotate(-450);
				
				if (buttonC == Button.ID_RIGHT)
					winchMotor.rotate(450);
				
				//if (buttonC == Button.ID_UP)
					//loadingMotor.rotate(-150);
					
				if (buttonC == Button.ID_DOWN)
					loadingMotor.rotate(90);
					
				if (buttonC == Button.ID_ESCAPE)
					return;
			}
		}
		
		public static int getButtonChoise()
		{	
			// holds the user's button choice
			int buttonChoice;
			
			// poll the button input until either the up, left or right button is pressed
			do {
				
				// clear the display
				t.clear();

				// ask the user whether the target is to the left, the right or in the center
				t.drawString("< Left | Right >", 0, 0);
				t.drawString("       |        ", 0, 1);
				t.drawString(" ______|_______ ", 0, 2);
				t.drawString("  ---  UP  ---  ", 0, 3);
				t.drawString("      DOWN      ", 0, 4);

				buttonChoice = Button.waitForAnyPress();
			} while (buttonChoice != Button.ID_LEFT
					&& buttonChoice != Button.ID_RIGHT && buttonChoice != Button.ID_UP && buttonChoice!= Button.ID_DOWN
					&& buttonChoice != Button.ID_ESCAPE);
			
			// return the button pressed by the user
			return buttonChoice;
		}
}