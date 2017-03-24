/* Name:    uno single sensor mq2
 * Purpose: 
 *          runs the mq2 sensor for gas detection
 *          turn on BLE services to publish data
 *          
 * Dependencies:
 *          uno 101 r3
 *          v9 battery
 *          mq-2 sensor
 *          1k ohm resitor
 *          TO220 5V regulator
 *          TO92 transistor
 * 
 * Pins:
 *        A5 - gas data pin
 *        12 - heater pin - note - sensor is powered externally with a v9 battery
 *        13 - led pin
 *        
 */

 
//#include <EEPROM.h>
#include <stdlib.h>
#include <CurieBLE.h>

// BLE
BLEPeripheral          blePeripheral;  // the board
BLEService             mq2Service("41EF6040-E249-4035-81EE-8999024D88ED");      //  gas sensor - NOTE THIS IS A RANDOM UUID - MORE RESEARCH NEEDED
BLEIntCharacteristic mq2DataCharacteristic("41EF6041-E249-4035-81EE-8999024D88ED", BLERead);  // INCREAMENT UUID BY 1...
BLEIntCharacteristic   mq2ConfigCharacteristic("41EF6042-E249-4035-81EE-8999024D88ED", BLERead | BLEWrite);  // INCREAMENT UUID BY 1...

/* mq2Config: 
*  0 = sensor is offf
*  1 = turn the sensor on for data collection
*  2 = set the sensor to loop
 */

/* mq2Data:
*  I am tottally stumped here - I can't seem to pass FLOAT variables up to android...
*/


// SENSORS
const int   gasPin1 = A5;
const int   heaterPin1 = 12;
const float gasBaseline = 0.00;
//long warmup = 180000;
//long downtime = 360000;
long warmup = 1800;
long downtime = 3600;
int  sensorValue;
  
const int ledPin = 13;


// --------------------------------------------------------- setup
void setup() {
  Serial.begin(9600);
  delay(9000);  // time to get connected and prep tests....
  Serial.print("Setup...");
  
  pinMode(heaterPin1, OUTPUT);

  // advertise the MQ2 on bluetooth
  blePeripheral.setLocalName("SENTIRE");
  blePeripheral.setAdvertisedServiceUuid(mq2Service.uuid());

  // add service and charactersitic
  blePeripheral.addAttribute(mq2Service);
  blePeripheral.addAttribute(mq2DataCharacteristic);
  blePeripheral.addAttribute(mq2ConfigCharacteristic);
  mq2DataCharacteristic.setValue(0.0);
  mq2ConfigCharacteristic.setValue(0);
  
  // start BLE
  blePeripheral.begin();
}


// --------------------------------------------------------- loop
void loop() {
  // setup connection to central devices for control
  bleCentralConnect();

  // sensors need to be run sequentially
  // run first sensor
  // runMQ2Sensor();

}


// --------------------------------------------------------- functions
void runMQ2Sensor() {
  int scratch=0; 
  unsigned long counter = millis();
  
  // heat the sensor up
  Serial.println("runMQ2Sensor - Sensor on warmup...");
  //digitalWrite(heaterPin1, HIGH);
  while(millis() < (counter + warmup))
  {
    scratch = (int)((counter + warmup - millis())/1000);
    delay(10000);
  }
  
  // get the sensor value  
  offMQ2Sensor();
  sensorValue = analogRead(gasPin1);
  recordData(sensorValue);  

  // cool down...  
  Serial.println("runMQ2Sensor - Sensor on cooldown...");
  while(millis() < (counter + downtime)) 
  {
    // cool down...
  }

}

void offMQ2Sensor() {
  //Serial.println("Unit off...");
  digitalWrite(heaterPin1, LOW);  
}


void recordData(int v) {
  float sensorVolt;
  float sensorGas;
  //float ratio;
  
  sensorVolt  = (float) v/1024*5.0;
  sensorGas   = (5.0 - sensorVolt)/sensorVolt;
  //ratio       = sensorGas / gasBaseline;

  Serial.print("Time: ");
  Serial.print(millis());
  Serial.print("; ");
  Serial.print("Voltage: ");
  Serial.print(sensorVolt);
  Serial.print("; ");
  Serial.print("Gas: ");
  Serial.print(sensorGas);
  Serial.print("; ");
  //Serial.print("Gas Ratio to Baseline: ");
  //Serial.print(ratio);
  //Serial.println("; ");
  
  mq2DataCharacteristic.setValue(v); 
  Serial.print("mq2: ..."); 
  Serial.println(mq2DataCharacteristic.value());  
}


void bleCentralConnect() {
  // listen for BLE peripherals to connect
  BLECentral central = blePeripheral.central();

  // if a central is connected:
  if (central) {
//    Serial.print("Connected to central: ");  
//    Serial.println(central.address());

    // logic for checking on data being read here    
    // logic for taking commands/messages here
    if (mq2ConfigCharacteristic.value() == 0) { // any value other than 0
      // off - nothing to do...
      offMQ2Sensor();
    } else if (mq2ConfigCharacteristic.value() == 1) {
      // turn on for a single test
      runMQ2Sensor();
      mq2ConfigCharacteristic.setValue(0);
    } else if (mq2ConfigCharacteristic.value() == 2) {
      // turn on for looping...
    }

  }  
}


