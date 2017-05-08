package a;

import java.time.Instant;
import java.util.Scanner;

import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import base.*;

public class Program extends MqttProgram implements Runnable {

	// arg: broker_address+port ex: 192.168.1.100:1883
	public static void main(String[] args) throws Exception {
		Program p = new Program(args[0]);

		Thread t = new Thread(p);
		t.start();

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
				p.isRunning = false;
				break;
			}
		}

		scanner.close();
		t.join();
	}

	private boolean isRunning;
	private String brokerAddr;

	private Program(String brokerAddr) {
		this.brokerAddr = brokerAddr;
		this.isRunning = true;
	}

	@Override
	public void run() {
		try {
			// create device
			AnalogInputDevice dev = new AnalogInputDevice();

			// connect to mqtt broker
			String broker = brokerAddr;
			String clientId = "PI_A";
			MemoryPersistence persistence = new MemoryPersistence();
			client = new MqttClient(broker, clientId, persistence);
			
			MqttConnectOptions connOpts = new MqttConnectOptions();
			connOpts.setKeepAliveInterval(15);
			connOpts.setCleanSession(true);
			// set our last will message as offline in case of disconnect
			connOpts.setWill("Status/RaspberryPiA", (new StatusMessage(false)).toJSON().getBytes(), 2, true);
			
			// try to connect
			try {
				client.connect(connOpts);
			} catch (Exception e) {
				System.out.println("Error connecting to broker!");
				throw e;
			}

			// send online status message
			sendMessage(new StatusMessage(true), "Status/RaspberryPiA", true);

			// subscribe to our topics to get the last messages sent for the old values
			LightSensorMessageListener lightListener = new LightSensorMessageListener();
			ThresholdMessageListener thresholdListener = new ThresholdMessageListener();
			try {
				client.subscribe(new String[] { "lightSensor", "threshold" },
						new IMqttMessageListener[] { lightListener, thresholdListener });
			} catch (MqttException e) {
				System.out.println("Error subscribing to topics!");
				throw e;
			}

			double lastLDRVal = -1;
			double lastThresholdVal = -1;

			// wait a second while for old values, else just keep going
			long startWaitTimeMS = Instant.now().toEpochMilli();
			while (Instant.now().toEpochMilli() - startWaitTimeMS < 1000) {
				if (lightListener.getLastMessage() != null && thresholdListener.getLastMessage() != null) {
					break;
				}
			}
			if (lightListener.getLastMessage() != null) {
				lastLDRVal = lightListener.getLastMessage().normalizedLDRValue;
			} else {
				lastLDRVal = 0;
			}
			if (thresholdListener.getLastMessage() != null) {
				lastThresholdVal = thresholdListener.getLastMessage().normalizedPotentiometerValue;
			} else {
				lastThresholdVal = 0;
			}

			// check values and publish messages
			while (isRunning) {
				// check light sensor
				double newLDRVal = dev.sampleLDRNormalized();
				if (Math.abs(newLDRVal - lastLDRVal) > 0.07) {
					// there was a significant difference, lets publish a message
					sendMessage(new LightSensorMessage(newLDRVal), "lightSensor", true);
					// store new value
					lastLDRVal = newLDRVal;
				}

				// check potentiometer/threshold
				double newThresholdVal = dev.samplePotentiometerNormalized();
				if (Math.abs(newThresholdVal - lastThresholdVal) > 0.07) {
					// there was a significant difference, lets publish a message
					sendMessage(new ThresholdMessage(newThresholdVal), "threshold", true);
					// store new value
					lastThresholdVal = newThresholdVal;
				}

				Thread.sleep(100);
			}

			// send status message
			sendMessage(new StatusMessage(false), "Status/RaspberryPiA", true);

			// disconnect
			client.disconnect();
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
	}

	public class LightSensorMessageListener implements IMqttMessageListener {

		private LightSensorMessage lastMessage;

		public LightSensorMessage getLastMessage() {
			return lastMessage;
		}
		
		@Override
		public void messageArrived(String topic, MqttMessage message) throws Exception {
			// debug
			System.out.println("MESSAGE RECIEVED - (" + topic + ") " + new String(message.getPayload()));

			lastMessage = LightSensorMessage.fromJson(new String(message.getPayload()));
		}
	}
	
	public class ThresholdMessageListener implements IMqttMessageListener {

		private ThresholdMessage lastMessage;

		public ThresholdMessage getLastMessage() {
			return lastMessage;
		}

		@Override
		public void messageArrived(String topic, MqttMessage message) throws Exception {
			// debug
			System.out.println("MESSAGE RECIEVED - (" + topic + ") " + new String(message.getPayload()));

			lastMessage = ThresholdMessage.fromJson(new String(message.getPayload()));
		}

	}

}
