package b;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;


public class LEDDevice {
    
    // create pin object
    final GpioPinDigitalOutput pin;
    
    // keep track of whether LED is on
    private boolean currentlyOn;
	
	/**
	 * Initialize the pin for the LED
	 */
	public LEDDevice(GpioController controller, int pinNumber) {
		if (pinNumber == 6) {
			this.pin = controller.provisionDigitalOutputPin(RaspiPin.GPIO_06, PinState.LOW);
		} else if (pinNumber == 13) {
			this.pin = controller.provisionDigitalOutputPin(RaspiPin.GPIO_13, PinState.LOW);
		} else {
			this.pin = controller.provisionDigitalOutputPin(RaspiPin.GPIO_25, PinState.LOW);
		}
		
		pin.setShutdownOptions(true, PinState.LOW);
		currentlyOn = false;
	}
	
	/**
	 * Turn the LED on
	 */
	public void turnOn() {
		pin.high();
		currentlyOn = true;
	}
	
	/**
	 * Turn the LED off
	 */
	public void turnOff() {
		pin.low();
		currentlyOn = false;
	}
	
	/**
	 * Return whether the LED is on
	 */
	public boolean isCurrentlyOn() {
		return currentlyOn;
	}
}
