package base;

import com.google.gson.Gson;

public class LightSensorMessage extends Message {
	
	public double normalizedLDRValue;
	
	public LightSensorMessage(double normalizedLDRValue) {
		this.normalizedLDRValue = normalizedLDRValue;
	}
	
	public static LightSensorMessage fromJson(String json) {
		return (new Gson()).fromJson(json, LightSensorMessage.class);
	}
	
}
