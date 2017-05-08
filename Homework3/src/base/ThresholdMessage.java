package base;

import com.google.gson.Gson;

public class ThresholdMessage extends Message {

	public double normalizedPotentiometerValue;
	
	public ThresholdMessage(double normalizedPotentiometerValue) {
		this.normalizedPotentiometerValue = normalizedPotentiometerValue;
	}
	
	public static ThresholdMessage fromJson(String json) {
		return (new Gson()).fromJson(json, ThresholdMessage.class);
	}
	
}
