package base;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public abstract class MqttProgram {

	protected MqttClient client;

	protected void sendMessage(Message messageInfo, String topic, boolean retained) {
		MqttMessage message = new MqttMessage(messageInfo.toJSON().getBytes());
		message.setRetained(retained);
		message.setQos(2);
		try {
			client.publish(topic, message);
		} catch (MqttException e) {
			e.printStackTrace();
			System.out.println("Error publishing message to " + topic + " topic!");
		}
	}

}
