Laptop 1 (Broker):
	Install Mosquitto and make sure it is running via the instructions in the design choice document

Laptop 2 (log):

 To execute the code:
	java -jar ProgramD.jar tcp://xxx.xxx.x.xxx:1883  (x represents ip address and the port we are using is port 1883)
 Example:
	java -jar ProgramD.jar tcp://192.168.1.104:1883

Pi A:
 To execute the code:
	java -jar ProgramA.jar tcp://xxx.xxx.x.xxx:1883  (x represents ip address and the port we are using is port 1883)
 Example:
	java -jar ProgramA.jar tcp://192.168.1.104:1883

Pi B:
 To execute the code:
	java -jar ProgramB.jar tcp://xxx.xxx.x.xxx:1883  (x represents ip address and the port we are using is port 1883)
 Example:
	java -jar ProgramB.jar tcp://192.168.1.104:1883

Pi C:
 To execute the code:
	java -jar ProgramC.jar tcp://xxx.xxx.x.xxx:1883  (x represents ip address and the port we are using is port 1883)
 Example:
	java -jar ProgramC.jar tcp://192.168.1.104:1883
