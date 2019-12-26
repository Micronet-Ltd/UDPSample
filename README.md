# UDP_Sample_App_V2
|Product|Smart Hub/Smart Tab/Tab8|
|-------|------------------------|
|Project|UDP Sample Application|
|Project Name|UDP Sample Application|
|Aim/Objective|To document an Android sample application that how the UDP transmit feature works on Smart Hub, Smart Tab, and Tab8|
|Current Application Version| v1.0.0|
|Document Revision Number|01|
|Document REvision Date| 19 December 2019|

## Document History
|Document Revision|Written By|Date|Comments|
|-----------------|----------|----|--------|
|01|John Ho|19 December 2019|Draft|

## Preface
### Document Purpose
The purpose of this document is to explain the UDP transmit feature to our customer. ATS system is one of the main service we provide to our customer and the system is working on UDP communication protocol. Therefor, having 
an application to demonstrate the logic of how UDP transmit is going to work will benefit our customer to better understanding the ATS system, also for future custom modification on the system.

## Introduction
### Background Information

This application demonstrates how to preform UDP transmit on Smart Hub, Smart Tab and Tab 8. User will be able to configure the device to Client or Server, and choose between 2 Parties or 3 Parties communication.

|Develop Environment|Android Studio v3.5.3|
|-------------------|---------------------|
|Tested Model|Tab8: MsTab8/ SmartHub: TREQr_5/ SmartTab: --|
|Tested OS|Tab8: msm8953_64_c801-userdebug 9 PKQ1/ SmartHub: TREQr_5_0.1.27.0_20190926.1451/ SmartTab: --|
|Require Platform Keys and Properties|micronet-tab8-platform.keystore/ obc5_props.keystore/ obc5keys.properties/ tab8platformkey.properties|



### Application Feature
Below are the application features of the UDP Sample App

### UDP Transmit
#### Client
Device will perform as Client, User needs to enter designated Server IP Address, Port Number and Text Message for sending UDP transmit.
Client will also be able to receive response from Server or other Client

**Device will perform as Client. User needs to enter the designated Server IP address and Port number, then press Lock Config button on the UI

#### Server
Device will perform as Server, User needs to enter opening port for receiving UDP transmit

#### 2 Parties Communication
This is a Client to Server Communication. One device will be setup as Client, another device will be setup as Server.
Client will send UDP transmit to Server and wait for response.
Server will receive UDP transmit and send response to Client.

#### 3 Parties Communication 
This is a Client to Client Communication. Client One will send UDP transmit to Server, and Server will pass the packet to Client Two on the other end.

#### UDP Cycle
When UDP Cycle is on, Client will continuously sending UDP transmit to Server/ Client, user will also be able to set a time gap for each sending during cycle(From 1 second to 1 minute) 

### Keep Alive Mechanism
Keep Alive Mechanism is an approach to evaluate and maintain Internet connection of the device once the system starts UDP transmit process.

Example: Server will open another listening port behind the system, it keeps receiving heartbeat UDP packet (1 byte)from client, and send an ack back to the sender.
If server did not receive any heartbeat packet from client, it will automatically increases the Error Count by 1.
When the number of Error Count reaches the configurable limit (Default set to 10), Server will activate "Internet Re-Connection".

Note: To prevent unwanted Internet Reconnection happens, any successful heartbeat communication cycle occurs during the error counting will reset the number of Error Count to 0.

#### Internet Reconnection
When the number of Error Count reaches the limit, system will automatically send an command to the device and force it to re-establish Internet connection by turning OFF and ON the network.
The number of Error Count will be reset to 0 after the process.

Note: Changing the Network state of the device requires System Permission. 

### UDP Transmit Log Display
When UDP Transmit processing, each sending and receiving record will be logged and displayed on the Log tabs in real-time.
Each log contains information of:

|Date|Time|Type|IP|Port|Text Message|
|----|----|----|--|----|------------|

### Write Log Into File
When system starts running on device, it creates new folder called "UDP_Log" in the device storage for saving UDP transmit log.
Each time the device perform an UDP transmit, it will also create a new csv file to store the transmit record.
The csv file which contains transmit record will be named as Year_Month_DateTHours_Minutes_Second.csv

Example: csv file named 2017_07_23T16_37_34.csv means the file was created on the date 07/23/2017 at 4pm.


### Read Device Hardware and Software Information 
System will extract device information
These information included the following:

|OS Version|Current Device version|
|----------|----------------------|
|Android Build Version|Current Android OS version|
|Device Model|Device's production model|
|Device Name|Device's produce name|
|Serial|Serial number of the device|
|App Version|Current version of the application|
|Hardware Library Version|Not Available|
|Vehicle Bus Library Version|Not Available|

### Read Device Connectivity Information
System will extract device's Network Connectivity information and update them on UI in real-time.
These information included the following:

|Name|Purpose|
|----|-------|
|Device IP|Detect device's current IP address|
|Sim State|Detect sim card stats of the device |
|Single Strength|Detect the current single strength of the network connectivity|
|Internet State|Detect device Internet capability|
|Phone Number|Extract phone number of the sim card(Only when sim card inserted)|
|IMEI|Extract device IMEI number|


### Future Feature
Set up a 3rd-party server (Amazon Cloud, Google Firebase and such) for user to preform UDP transmit and UDP transmit log storage.

## User Interface
### Graphical User Interface
The graphical user interface is tab with a standard Android header.

-----

### SERVER CONFIG Tab
#### Vertical Layout
![With no user input](./images/ServiceConfig.PNG "With no user input")
#### Landscape Layout Part 1
![With no user input](./images/ServiceConfigLand1.PNG "With no user input")
#### Landscape Layout Part 2
![With no user input](./images/ServiceConfigLand2.PNG "With no user input")

Note: After user entered required information, and press Lock Config button. The radio buttons and edit text filed will be disable for editing, this is to prevent any changes during the UDP transmit process.

-----

### TRANSMIT ACTION Tab
#### Vertical Layout
![With no user input](./images/TransmitAction.PNG "With no user input")
#### Landscape Layout
![With no user input](./images/TransmitLand.PNG "With no user input")

-----

### LOG Tab
#### Vertical Layout
![With no user input](./images/LogNoDate.PNG "With no user input")
#### Landscape Layout
![With no user input](./images/LogNoDataLand.PNG "With no user input")

-----

### DEVICE INFO Tab
#### Vertical Layout
![With no user input](./images/DeviceInfo.PNG "With no user input")
#### Landscape Layout
![With no user input](./images/DeviceInfoLand.PNG "With no user input")

-----
### Application Setup

#### Preparation
Before you start, make sure the device that you'd like to setup as a Server is running on the static sim card and having a static IP.

For the Clients, they can be running on any kind of cellula network or connected to the WIFI.

#### Performing 2 Parties Communication
##### Client
At the SERVICE CONFIG Tab, select 2 parties for the Communication Type and Client for the Device Type. Enter the target server IP and designated port number, than press LOCK CONFIG.
Once user press LOCK CONFIG, the UI elements will be locked and become unchangeable. User can press RESET CONFIG to unlock the UI elements.
Switch to the TRABSMIT ACTION Tab, user will enter message, than press SEND or start a UDP cycle.
The TX will display out-going UDP transmit, and RX will display the incoming UDP transmit.
LOG Tab will produce the UDP transmit log record in real-time.

##### Server
At the SERVICE CONFIG Tab, select 2 parties for the Communication Type and Server for the Device Type. Enter the designated port number(Same as the Client), than press LOCK CONFIG,
Once user press LOCK CONFIG, the UI elements will be locked and become unchangeable. User can press RESET CONFIG to unlock the UI elements.
After LOCK CONFIG is pressed, the server will start listening for UDP transmit

#### Performing 3 Parties Communication
##### Client 1
At the SERVICE CONFIG Tab, select 3 parties for the Communication Type and Client for the Device Type. Enter the target server IP and designated port number, than press LOCK CONFIG.
Once user press LOCK CONFIG, the UI elements will be locked and become unchangeable. User can press RESET CONFIG to unlock the UI elements.
Switch to the TRABSMIT ACTION Tab, user will enter message, than press SEND or start a UDP cycle.
The TX will display out-going UDP transmit, and RX will display the incoming UDP transmit.
LOG Tab will produce the UDP transmit log record in real-time.

##### Client 2
At the SERVICE CONFIG Tab, select 3 parties for the Communication Type and Client for the Device Type. Insert the target server IP and designated port number, than press LOCK CONFIG.
Once user press LOCK CONFIG, the UI elements will be locked and become unchangeable. User can press RESET CONFIG to unlock the UI elements.
Switch to the TRABSMIT ACTION Tab, user will enter message, than press SEND or start a UDP cycle.
The TX will display out-going UDP transmit, and RX will display the incoming UDP transmit.
LOG Tab will produce the UDP transmit log record in real-time.

(** Note: For Client 2, please set the port number to be the Client 1 port plus 1.
Example: If Client 1 port number is set to 7501, Client 2 port number need to be set as 7502)

##### Server
At the SERVICE CONFIG Tab, select 3 parties for the Communication Type and Server for the Device Type. Enter the designated port number(Same as the Client 1), than press LOCK CONFIG,
Once user press LOCK CONFIG, the UI elements will be locked and become unchangeable. User can press RESET CONFIG to unlock the UI elements.
After LOCK CONFIG is pressed, the server will start listening for UDP transmit


### UDP Transmit Logic
#### DatagramSocket
The system use DatagramSocket for sending and receiving UDP packet, also it needs to setup a DatagramPacke structurer for reading the packet context.
Some of the basic logic such as:

Create a new DatagramSocket: DatagramSocket datagramSocket = new DatagramSocket(Port Number);
Create a new DatagramPacket: DatagramPack datagramPacket = new DatagramPacket(Packet, Packet Length, IP, Port);

Note: Base on different needs, the setup for DatagramSocket and DatagramPack could be differ, please see down below link for detailed reference.

Example of DatagramSocket on Server side:

```java
  try{
      DatagramSocket datagramsocket = new DatagramSocket(portNumner);
      DatagramPacket datagramPacket = new DatagramPacket(receiveData, receiveData.length)
      datagramSocket.receive(datagramPacket);
  }catch(Exception e){
  
  }

```

DatagramSocket: https://developer.android.com/reference/java/net/DatagramSocket

DatagramPacket: https://developer.android.com/reference/java/net/DatagramPacket

The benefit of using DatagramSocket and DatagramPacket in UDP transmit is to make the target address traceable. Since, unlike TCP, UDP communication protocol will not guarantee the packet delivery, 
there will be no solid communication channel created if two end points are performing UDP transmit. This problem is quite obvious especially when devices are using dynamic network connection, such as mobile network.

DatagramSocket and DatagramPacket will be able to mark down the network information of the device and those info will be packed and send with the UDP packet. 
User can decode the received UDP packet, extract those information and locate the sender network address.

Note: Thanks to the limitation of IPv4 and complexity of NAT, the network information you received from a UDP packet could be expired pretty soon.

#### UDP Hole Punching
The system is using the logic of UDP Hole Punching to perform the UDP transmit between Clients and Server. The basic idea of UDP Hole Punching is to setup a middle point (The Server)
which will listening to both end points (The Clients) and handling the UDP packet exchange.

You can find detailed information here:  https://en.wikipedia.org/wiki/UDP_hole_punching





