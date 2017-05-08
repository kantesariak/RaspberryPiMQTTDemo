package c;

import java.util.Scanner;

import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import base.LightSensorMessage;
import base.LightStatusMessage;
import base.MqttProgram;
import base.StatusMessage;
import base.ThresholdMessage;

public class Program extends MqttProgram {

	// arg: broker_address+port ex: 192.168.1.100:1883
	public static void main(String[] args) throws Exception {
		Program p = new Program(args[0]);
		System.out.println("Starting up...");
		p.connect();

		Scanner scanner = new Scanner(System.in);
		while (true) {
			System.out.print("Type 'EXIT' to gracefully exit the program: ");
			String input = null;
			try {
				input = scanner.nextLine();
			} catch (Exception e) {
			}
			if (input == null || !input.equals("EXIT")) {
				System.out.println("Bad input!");
			} else {
				p.close();
				break;
			}
		}

		scanner.close();
	}

	private double lightSensorValue;
	private double thresholdValue;
	private boolean status; // true = on; false = off;

	private String brokerAddr;

	private Program(String brokerAddr) {
		this.brokerAddr = brokerAddr;
	}

	public void connect() {
		try {
			// connect to mqtt broker
			String broker = brokerAddr;
			String clientId = "PI_C";
			MemoryPersistence persistence = new MemoryPersistence();
			client = new MqttClient(broker, clientId, persistence);

			MqttConnectOptions connOpts = new MqttConnectOptions();
			connOpts.setKeepAliveInterval(15);
			connOpts.setCleanSession(true);
			connOpts.setWill("Status/RaspberryPiC", (new StatusMessage(false)).toJSON().getBytes(), 2, true);
			connOpts.setAutomaticReconnect(true);

			try {
				client.connect(connOpts);
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("Error connecting to broker!");
				return;
			}

			// send online message
			sendMessage(new StatusMessage(true), "Status/RaspberryPiC", true);

			// subscribe to our topics to get the last messages sent for the old
			// values
			IMqttMessageListener lightSensorListener = new IMqttMessageListener() {
				@Override
				public void messageArrived(String topic, MqttMessage message) throws Exception {
					LightSensorMessage lightSensorMessage = LightSensorMessage
							.fromJson(new String(message.getPayload()));
					lightSensorValue = lightSensorMessage.normalizedLDRValue;
					sendLightStatusMessage();
					System.out.println("Received light sensor message");
				}
			};
			IMqttMessageListener thresholdListener = new IMqttMessageListener() {
				@Override
				public void messageArrived(String topic, MqttMessage message) throws Exception {
					ThresholdMessage thresholdMessage = ThresholdMessage.fromJson(new String(message.getPayload()));
					thresholdValue = thresholdMessage.normalizedPotentiometerValue;
					sendLightStatusMessage();
					System.out.println("Received threshold message");
				}
			};

			// subscribe
			try {
				client.subscribe(new String[] { "lightSensor", "threshold" },
						new IMqttMessageListener[] { lightSensorListener, thresholdListener });
			} catch (MqttException e) {
				e.printStackTrace();
				System.out.println("Error subscribing to topics!");
			}
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
	}

	private boolean firstTime = true;
	private void sendLightStatusMessage() {
		if (firstTime) {
			// turn on if greater than or equal to 0
			if (lightSensorValue >= thresholdValue) {
				sendMessage(new LightStatusMessage("TurnOn"), "lightStatus", true);
				status = true;
			} else {
				sendMessage(new LightStatusMessage("TurnOff"), "lightStatus", true);
				status = false;
			}
			firstTime = false;
		} else {
			// turn on if greater than or equal to 0
			if (lightSensorValue >= thresholdValue) {
				// only publish if the previous status is false
				if (!status) {
					sendMessage(new LightStatusMessage("TurnOn"), "lightStatus", true);
					status = true;
				}
			} else {
				// only publish if the previous status is true
				if (status) {
					sendMessage(new LightStatusMessage("TurnOff"), "lightStatus", true);
					status = false;
				}
			}
		}
	}

	public void close() {
		try {
			// send status message
			sendMessage(new StatusMessage(false), "Status/RaspberryPiC", true);
			// disconnect
			client.disconnect();
		} catch (Exception e) {
		}
	}

}
