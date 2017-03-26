#include <stdlib.h>
#include <CurieBLE.h>

BLEPeripheral          blePeripheral;  // the board

BLEService             mq2Service("41EF6040-E249-4035-81EE-8999024D88ED");      //  gas sensor - NOTE THIS IS A RANDOM UUID - MORE RESEARCH NEEDED
BLEIntCharacteristic   mq2DataCharacteristic("41EF6041-E249-4035-81EE-8999024D88ED",   BLERead | BLENotify);  // INCREAMENT UUID BY 1...
BLEIntCharacteristic   mq2ConfigCharacteristic("41EF6042-E249-4035-81EE-8999024D88ED", BLERead | BLEWrite);  // INCREAMENT UUID BY 1...

BLEService             tmpService("41EF6050-E249-4035-81EE-8999024D88ED");      //  temp sensor - NOTE THIS IS A RANDOM UUID - MORE RESEARCH NEEDED
BLEIntCharacteristic   tmpDataCharacteristic("41EF6051-E249-4035-81EE-8999024D88ED",   BLERead | BLENotify);  // INCREAMENT UUID BY 1...
BLEIntCharacteristic   tmpConfigCharacteristic("41EF6052-E249-4035-81EE-8999024D88ED", BLERead | BLEWrite);  // INCREAMENT UUID BY 1...

/* mq2Config: 
*  0 = sensor is offf
*  1 = turn the sensor on for data collection
*  2 = set the sensor to loop
 */

/* mq2Data:
*  I am tottally stumped here - I can't seem to pass FLOAT variables up to android...
*/


// MQ2 SENSOR
const int  MQ2DATAPIN = A5;
const int  MQ2HEATERPIN = 12;
const long MQ2WARMUP = 180;
const long MQ2COOLDN = 360000;
      int  mq2SensorValue = 0;
      int  mq2SensorValueOld = 0;

// TEMPERATURE SENSOR
const int  TMPDATAPIN = A0;
      int  tmpSensorValue = 0;
      int  tmpSensorValueOld = 0;
      
// --------------------------------------------------------------------------------- SETUP
void setup() {
  Serial.begin(9600);
  delay(9000);  // time to connect everything for the testing
  Serial.println("Begin Setup...");

  pinMode(MQ2HEATERPIN, OUTPUT);

    // advertise the board on bluetooth
  blePeripheral.setLocalName("SENTIRE");
  
  // add service and charactersitic - MQ2
  blePeripheral.setAdvertisedServiceUuid(mq2Service.uuid());
  blePeripheral.addAttribute(mq2Service);
  blePeripheral.addAttribute(mq2DataCharacteristic);
  blePeripheral.addAttribute(mq2ConfigCharacteristic);  

  // add service and charactersitic - TEMPERATURE
  blePeripheral.setAdvertisedServiceUuid(tmpService.uuid());
  blePeripheral.addAttribute(tmpService);
  blePeripheral.addAttribute(tmpDataCharacteristic);
  blePeripheral.addAttribute(tmpConfigCharacteristic);

  // set event handlers and start BLE
  blePeripheral.setEventHandler(BLEConnected,blePeripheralConnectHandler);
  blePeripheral.setEventHandler(BLEDisconnected,blePeripheralDisconnectHandler);
  mq2ConfigCharacteristic.setEventHandler(BLEWritten, mq2ConfigCharWritten);
  tmpConfigCharacteristic.setEventHandler(BLEWritten, tmpConfigCharWritten);

  // init values
  mq2DataCharacteristic.setValue(0);
  mq2ConfigCharacteristic.setValue(0);  // start with sensor OFF
  tmpDataCharacteristic.setValue(0);
  tmpConfigCharacteristic.setValue(2);  // start with sensor ON

  // start ble
  blePeripheral.begin();
  Serial.println("End Setup...");
}

// --------------------------------------------------------------------------------- LOOP
void loop() {
  blePeripheral.poll();

  //bit odd way to handle the displaying of temperature data:
  runTMPSensor();
  delay(1000);
  
}

// --------------------------------------------------------------------------------- CENTRAL CONNECT
void blePeripheralConnectHandler(BLECentral& central) {
  // central connected event handler
  Serial.print("Connected event, central: ");
  Serial.println(central.address());
}

// --------------------------------------------------------------------------------- CENTRAL DISCONNECT
void blePeripheralDisconnectHandler(BLECentral& central) {
  // central disconnected event handler
  Serial.print("Disconnected event, central: ");
  Serial.println(central.address());
}

// --------------------------------------------------------------------------------- MQ2 DATA UPDATE
void mq2ConfigCharWritten(BLECentral& central, BLECharacteristic& characteristic) {
  if (mq2ConfigCharacteristic.value() == 0) {
    stopMQ2Sensor();
  } else if (mq2ConfigCharacteristic.value() == 1) {
    startMQ2Sensor();
    mq2ConfigCharacteristic.setValue(0);
  } else if (mq2ConfigCharacteristic.value() == 2) {
    // tbd - set up for looping    
  }
}

// -------------------------------------------------------------------------------- MQ2 START
void startMQ2Sensor(){
  int scratch = 0;
  unsigned long counter = millis();
  int temporary= 0;
  
  Serial.println("MQ2 SENSOR START");
  while(millis() < counter + MQ2WARMUP) {
    scratch = (int)((counter + MQ2WARMUP - millis())/1000);
    temporary = analogRead(MQ2DATAPIN);
    Serial.print("Time: ");
    Serial.print(millis());
    Serial.print(", mq2 value: ");
    Serial.println(temporary);
    delay(10000);
  }

  mq2SensorValue = analogRead(MQ2DATAPIN);
  if (mq2SensorValue != mq2SensorValueOld){
    mq2SensorValueOld = mq2SensorValue;
    mq2DataCharacteristic.setValue(mq2SensorValue); 
  }

  Serial.print("mq2: ..."); 
  Serial.println(mq2DataCharacteristic.value());  
  
  stopMQ2Sensor();
}


// -------------------------------------------------------------------------------- MQ2 STOP
void stopMQ2Sensor(){
  Serial.println("MQ2 SENSOR STOP");
  digitalWrite(MQ2HEATERPIN, LOW);
}

// --------------------------------------------------------------------------------- TMP DATA UPDATE
void tmpConfigCharWritten(BLECentral& central, BLECharacteristic& characteristic) {
  if (tmpConfigCharacteristic.value() == 0) {
    stopTMPSensor();
  } else if (tmpConfigCharacteristic.value() == 1) {
    startTMPSensor();
    //tmpConfigCharacteristic.setValue(0);
  } else if (tmpConfigCharacteristic.value() == 2) {
    // tbd - set up for looping    
  }
}

// -------------------------------------------------------------------------------- TMP STOP
void stopTMPSensor(){
  Serial.println("TMP SENSOR STOP");
  
}

// -------------------------------------------------------------------------------- TMP STOP
void startTMPSensor(){
  Serial.println("TMP SENSOR START");
  
}

void runTMPSensor() {
  if (tmpConfigCharacteristic.value() > 0) {
      tmpSensorValue = analogRead(TMPDATAPIN);
      Serial.print("temperature sensor value: ");
      Serial.print(tmpSensorValue);
    
      float voltage = (tmpSensorValue / 1024.0) * 5.0;
      Serial.print(", Volts: ");
      Serial.print(voltage);
      Serial.print(", degrees F: ");
      float temperature = (voltage - .5) * 100;
      Serial.print(temperature);
      Serial.print(", dc: ");

      if (tmpSensorValue != tmpSensorValueOld){
        tmpSensorValueOld = tmpSensorValue;
        tmpDataCharacteristic.setValue(tmpSensorValue); 
      } 
      Serial.println(tmpDataCharacteristic.value()); 
      
  }   
}

