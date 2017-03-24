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
BLEFloatCharacteristic mq2DataCharacteristic("41EF6041-E249-4035-81EE-8999024D88ED", BLERead);  // INCREAMENT UUID BY 1...
BLEIntCharacteristic   mq2ConfigCharacteristic("41EF6042-E249-4035-81EE-8999024D88ED", BLERead | BLEWrite);  // INCREAMENT UUID BY 1...

// SENSORS
const int   gasPin1 = A5;
const int   heaterPin1 = 12;
const float gasBaseline = 0.00;
int   sensorValue;
  
const int ledPin = 13;
long warmup = 180000;
long downtime = 360000;


// --------------------------------------------------------- setup
void setup() {
  Serial.begin(9600);
  delay(9000);  // time to get connected and prep tests....
  Serial.print("Setup...");
  
  pinMode(heaterPin1, OUTPUT);

  // advertise the MQ2 on bluetooth
  blePeripheral.setLocalName("MQ2");
  blePeripheral.setAdvertisedServiceUuid(mq2Service.uuid());

  // add service and charactersitic
  blePeripheral.addAttribute(mq2Service);
  blePeripheral.addAttribute(mq2DataCharacteristic);
  blePeripheral.addAttribute(mq2ConfigCharacteristic);
  mq2DataCharacteristic.setValue(0);
  mq2ConfigCharacteristic.setValue(0);
  
  // start BLE
  blePeripheral.begin();
}


// --------------------------------------------------------- loop
void loop() {
  int scratch=0; 
  unsigned long counter = millis();
    
  // setup connection to central devices for control
  bleCentralConnect();

  // heat the sensor up
  Serial.println("Sensor on warmup...");
  digitalWrite(heaterPin1, HIGH);
  while(millis() < (counter + warmup))
  {
    scratch = (int)((counter + warmup - millis())/1000);
    Serial.print(scratch);
    Serial.print(",");
    delay(10000);
  }
  
  // get the sensor value  
  Serial.println("Unit off...");
  digitalWrite(heaterPin1, LOW);
  sensorValue = analogRead(gasPin1);
  recordData(sensorValue);  

  // cool down...  
  Serial.println("Sensor on cooldown...");
  while(millis() < (counter + downtime)) 
  {
    // cool down...
  }

}


// --------------------------------------------------------- functions
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
  
  mq2DataCharacteristic.setValue(sensorGas);    
}


void bleCentralConnect() {
  // listen for BLE peripherals to connect
  BLECentral central = blePeripheral.central();

  // if a central is connected:
  if (central) {
    Serial.print("Connected to central: ");  
    Serial.println(central.address());

    // logic for checking on data being read here    
    // logic for taking commands/messages here
    if (mq2ConfigCharacteristic.value()) { // any value other than 0
      Serial.println("Signal sent from android central...");
    } else {
      Serial.println("Signal not yet sent from android...");
    }

  }  
}

