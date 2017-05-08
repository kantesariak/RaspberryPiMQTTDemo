package base;

import com.google.gson.Gson;

public class StatusMessage extends Message {

	public boolean isOnline;
	
	public StatusMessage(boolean isOnline) {
		this.isOnline = isOnline;
	}
	
	public static StatusMessage fromJson(String json) {
		return (new Gson()).fromJson(json, StatusMessage.class);
	}
	
}
