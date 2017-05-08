package base;

import com.google.gson.Gson;

public class LightStatusMessage extends Message {
	
	public String status;
	
	public LightStatusMessage(String status) {
		this.status = status;
	}
	
	public static LightStatusMessage fromJson(String json) {
		return (new Gson()).fromJson(json, LightStatusMessage.class);
	}
	
}
