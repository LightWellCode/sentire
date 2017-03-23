/* Name:    uno single sensor mq2
 * Purpose: 
 *          runs the mq2 sensor for gas detection
 *          
 * Dependencies:
 *          uno r3
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
  delay(warmup);
  Serial.print("Setup...");

  pinMode(heaterPin1, OUTPUT);
}


// --------------------------------------------------------- loop
void loop() {
  int scratch=0;

  unsigned long counter = millis();
  digitalWrite(heaterPin1, HIGH);
  Serial.print("Unit active...");
  
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
  
  while(millis() < (counter + downtime)) 
  {
    // cool down...
  }

// --------------------------------------------------------- functions
      
}
