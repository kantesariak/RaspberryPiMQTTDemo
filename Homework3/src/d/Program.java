package d;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

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

	public static void main(String[] args) throws Exception {

		// connect to mqtt broker
		String broker = args[0];
		String clientId = "PI_D";
		MemoryPersistence persistence = new MemoryPersistence();
		MqttClient client = new MqttClient(broker, clientId, persistence);

		MqttConnectOptions connOpts = new MqttConnectOptions();
		connOpts.setCleanSession(true); // should be true?

		try {
			client.connect(connOpts);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Error connecting to broker!");
			return;
		}

		IMqttMessageListener lightSensorListener = new IMqttMessageListener() {
			@Override
			public void messageArrived(String topic, MqttMessage message) throws Exception {
				LightSensorMessage lightSensorMessage = LightSensorMessage.fromJson(new String(message.getPayload()));

				String logMsg = "[" + lightSensorMessage.dateString + "] - " + topic + " - normalizedLDRValue: "
						+ lightSensorMessage.normalizedLDRValue;

				System.out.println(logMsg);

				logMessage("All_log.txt", logMsg);
			}
		};
		IMqttMessageListener thresholdListener = new IMqttMessageListener() {
			@Override
			public void messageArrived(String topic, MqttMessage message) throws Exception {
				ThresholdMessage thresholdMessage = ThresholdMessage.fromJson(new String(message.getPayload()));

				String logMsg = "[" + thresholdMessage.dateString + "] - " + topic + " - normalizedPotentiometerValue: "
						+ thresholdMessage.normalizedPotentiometerValue;
				System.out.println(logMsg);

				logMessage("All_log.txt", logMsg);
			}
		};
		IMqttMessageListener lightStatusListener = new IMqttMessageListener() {
			@Override
			public void messageArrived(String topic, MqttMessage message) throws Exception {
				LightStatusMessage lightStatusMessage = LightStatusMessage.fromJson(new String(message.getPayload()));

				String logMsg = "[" + lightStatusMessage.dateString + "] - " + topic + " - status: "
						+ lightStatusMessage.status;
				System.out.println(logMsg);

				logMessage("All_log.txt", logMsg);
				logMessage("LED1_log.txt", logMsg);
			}

		};
		IMqttMessageListener statusListener = new IMqttMessageListener() {
			@Override
			public void messageArrived(String topic, MqttMessage message) throws Exception {
				StatusMessage statusMessage = StatusMessage.fromJson(new String(message.getPayload()));

				String logMsg = "[" + statusMessage.dateString + "] - " + topic + " - isOnline: "
						+ statusMessage.isOnline;

				System.out.println(logMsg);

				logMessage("All_log.txt", logMsg);
			}
		};

		try {
			client.subscribe(new String[] { "lightSensor", "threshold", "lightStatus", "Status/#" },
					new IMqttMessageListener[] { lightSensorListener, thresholdListener, lightStatusListener,
							statusListener });
		} catch (MqttException e) {
			e.printStackTrace();
			System.out.println("Error subscribing to topics!");
		}
	}

	private static void logMessage(String file, String message) {
		try (FileWriter fw = new FileWriter(file, true);
				BufferedWriter bw = new BufferedWriter(fw);
				PrintWriter out = new PrintWriter(bw)) {
			out.println(message);
		} catch (Exception e) {
			System.out.println("Error saving message: " + message + ", to " + file);
			e.printStackTrace();
		}
	}

}
