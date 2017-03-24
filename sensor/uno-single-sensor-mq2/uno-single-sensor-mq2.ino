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
BLEPeripheral blePeripheral;  // the board
BLEService mq2Service("41EF6040-E249-4035-81EE-8999024D88ED");      //  gas sensor - NOTE THIS IS A RANDOM UUID - MORE RESEARCH NEEDED
BLEIntCharacteristic mq2DataCharacteristic("41EF6041-E249-4035-81EE-8999024D88ED", BLERead);  // INCREAMENT UUID BY 1...
BLEIntCharacteristic mq2ConfigCharacteristic("41EF6042-E249-4035-81EE-8999024D88ED", BLERead | BLEWrite);  // INCREAMENT UUID BY 1...

// SENSORS
const int gasPin1 = A5;
const int heaterPin1 = 12;
int gasVal1 = 0;

const int ledPin = 13;
long warmup = 180000;
long downtime = 360000;

// EEPROM
int addr = 0;
int record = 0;
int reclen = 5;

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
  digitalWrite(heaterPin1, HIGH);
  Serial.print("Unit active...");
  
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
  
  while(millis() < (counter + warmup))
  {
    scratch = (int)((counter + warmup - millis())/1000);
    Serial.print(scratch);
    Serial.print(",");
    delay(10000);
  }

  gasVal1 = analogRead(gasPin1);

  digitalWrite(heaterPin1, LOW);
  Serial.println("Unit off...");
  Serial.print(millis());
  Serial.print(",");
  Serial.print("Gas1: ");
  Serial.println(gasVal1);
  mq2DataCharacteristic.setValue(gasVal1);
  
  
  while(millis() < (counter + downtime)) 
  {
    // cool down...
  }

// --------------------------------------------------------- functions
      
}
