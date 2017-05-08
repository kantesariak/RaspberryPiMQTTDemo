package base;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.google.gson.Gson;

public abstract class Message {

	public String dateString;

	public Message() {
		DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
		dateString = dateFormat.format(new Date());
	}

	public String toJSON() {
		return (new Gson()).toJson(this);
	}

}
