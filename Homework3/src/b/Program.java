package b;

import java.util.Scanner;

import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;

import base.LightStatusMessage;
import base.MqttProgram;
import base.StatusMessage;

public class Program extends MqttProgram {

	// create controller
	final static GpioController controller = GpioFactory.getInstance();

	public static void main(String[] args) throws InterruptedException, MqttException {
		// create led devices
		LEDDevice ledLightStatus = new LEDDevice(controller, 6);
		LEDDevice ledPiA = new LEDDevice(controller, 13);
		LEDDevice ledPiC = new LEDDevice(controller, 25);

		// connect to broker
		String brokerAddr = args[0];
		String clientId = "PI_B";
		MemoryPersistence persistence = new MemoryPersistence();
		MqttClient client = new MqttClient(brokerAddr, clientId, persistence);

		MqttConnectOptions connOpts = new MqttConnectOptions();
		connOpts.setCleanSession(true);

		try {
			client.connect(connOpts);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Error connecting to broker!");
			return;
		}

		// create the listeners for the objects
		IMqttMessageListener statusAListener = new IMqttMessageListener() {
			@Override
			public void messageArrived(String topic, MqttMessage message) throws Exception {
				StatusMessage statusMessage = StatusMessage.fromJson(new String(message.getPayload()));
				if (statusMessage.isOnline) {
					System.out.println("Turning on LED for Pi A");
					ledPiA.turnOn();
				} else {
					System.out.println("Turning off LED for Pi A");
					ledPiA.turnOff();
				}
			}
		};
		IMqttMessageListener statusCListener = new IMqttMessageListener() {
			@Override
			public void messageArrived(String topic, MqttMessage message) throws Exception {
				StatusMessage statusMessage = StatusMessage.fromJson(new String(message.getPayload()));
				if (statusMessage.isOnline) {
					System.out.println("Turning on LED for Pi C");
					ledPiC.turnOn();
				} else {
					System.out.println("Turning off LED for Pi C");
					ledPiC.turnOff();
				}
			}
		};
		IMqttMessageListener lightStatusListener = new IMqttMessageListener() {
			@Override
			public void messageArrived(String topic, MqttMessage message) throws Exception {
				LightStatusMessage lightStatusMessage = LightStatusMessage.fromJson(new String(message.getPayload()));
				if (lightStatusMessage.status.equals("TurnOn")) {
					System.out.println("Turning on the light LED");
					ledLightStatus.turnOn();
				} else {
					System.out.println("Turning off the light LED");
					ledLightStatus.turnOff();
				}
			}
		};

		// subscribe to the topics
		try {
			client.subscribe(new String[] { "Status/RaspberryPiA", "Status/RaspberryPiC", "lightStatus" },
					new IMqttMessageListener[] { statusAListener, statusCListener, lightStatusListener });
		} catch (MqttException e) {
			System.err.println("Error subscribing to topics!");
			throw e;
		}

		// allow the user to enter the exit command when they want to exit the program
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
				scanner.close();
				break;
			}
		}

		// disconnect devices
		controller.shutdown();
		client.close();
	}

}
