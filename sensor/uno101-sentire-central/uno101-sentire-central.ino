/*
 * Sketch: uno101-sentire-central
 * 
 * Description:
 *  This runs a SENTIRE CENTRAL device.  Responsbilities are to:
 *  1. connect to peripheral BLE devices, loop through services/characteritics, register for notifications on those that are set to NOTIFY
 *  2. store received data to SD card
 *  3. responds to requests to retieve data from the SD card
 *  4. post successful retrieval of data, empty the SD card and restart the logging
 * 
 * Notes:
 * v0.1:
 *  1. Will only connect to sensors that have local names containing "SENTIRE"
 *  2. Will only register for notifications for charactersitcs that contain the word "DATA" ???? (UUID only???) Need to confirm this or move the config controls into descriptors
 * 
 */

#include <CurieBLE.h>
#include <SPI.h>
#include <SD.h>

// hardcoding ... yuck...
BLECharacteristic bTemperature;
int               bTemperatureOld = 0;
BLECharacteristic bMQ2;
int               bMQ2Old = 0;
BLEDevice         bPerpherial1;

/*
* SD card attached to SPI bus as follows:
** MOSI - pin 11
** MISO - pin 12
** CLK - pin 13
** CS - pin 4 (for MKRZero SD: SDCARD_SS_PIN)
*/

// SD CARD
const int chipSelect = 4;
      Sd2Card   card;
      SdVolume  volume;
      SdFile    root;
      File      dataFile;

      
// ------------------------------------------------------------------------ SETUP
void setup() {
  Serial.begin(9600);
  delay(9000); // give time to get the serial monitor turned on...
  Serial.println("Begin Setup");

  initSDCard();
  
  BLE.begin();
  BLE.setEventHandler(BLEDiscovered, bleDiscoverPeripheralHandler);
  
  BLE.scan(true);
  
  Serial.println("End Setup");
}

// ------------------------------------------------------------------------ onNotified
void onNotified(BLECharacteristic characteristic) {
  Serial.println("notified triggered...");
}


// ------------------------------------------------------------------------ LOOP
void loop() {
  
  BLE.poll();
  delay(10000); // just to keep things calm
  if (bTemperature.valueLength() > 0 && bTemperatureOld != bTemperature.intValue()) {
    bTemperatureOld = bTemperature.intValue();
    makeMsg(bTemperature.uuid(), bTemperatureOld);  
  }
  if (bMQ2.valueLength() > 0 && bMQ2Old != bMQ2.intValue()) {
    bMQ2Old = bMQ2.intValue();
    makeMsg(bMQ2.uuid(), bMQ2.intValue());  
  }
}


// --------------------------------------------------------------------------------- MAKE MSG
void makeMsg(String u, int v) {
  String msg;
  unsigned long counter = millis();

  msg = "Millis: ";
  msg += counter;
  msg += "; ";
  msg += "Device: ";
  msg += bPerpherial1.address();
  msg += "; ";
  msg += "Sensor: ";
  msg += u;
  msg += "; ";
  msg += "Value: ";
  msg += v;
  writeDataLog(msg);
}

// --------------------------------------------------------------------------------- INIT SD CARD
void initSDCard() {
  Serial.print("\nInitializing SD Card...");
    
  // we'll use the initialization code from the utility libraries
  // since we're just testing if the card is working!
  // see if the card is present and can be initialized:
  if (!SD.begin(chipSelect)) {
    Serial.println("Card failed, or not present");
    // don't do anything more:
    return;
  }
  Serial.println("card initialized.");  
}

// --------------------------------------------------------------------------------- WRITE DATALOG
void writeDataLog(String msg) {
  dataFile = SD.open("datalog.txt", FILE_WRITE);
  // if the file is available, write to it:
  if (dataFile) {
    dataFile.println(msg);
    dataFile.close();
    // print to the serial port too:
    Serial.println(msg);
  }
  // if the file isn't open, pop up an error:
  else {
    Serial.println("error opening datalog.txt");
  }  
}


// ------------------------------------------------------------------------- BLE DISCOVER PERIPHERAL HANDLER
void bleDiscoverPeripheralHandler(BLEDevice peripheral) {
  String pAddress;
  String pLocalName;
  String pDeviceName;
  String pRSSI;
  BLEService pService;
  BLECharacteristic pChar;
  
   if (peripheral.hasLocalName() && peripheral.localName().startsWith("SENTIRE", 0)) {
      if (peripheral.connect()) {
        Serial.println("Connected");
      } else {
        Serial.println("Failed to connect!");
        return;
      }     
       
      pAddress    = peripheral.address();
      pDeviceName = peripheral.deviceName();
      pLocalName  = peripheral.localName();
      pRSSI       = peripheral.rssi();
      
      if (peripheral.hasAdvertisedServiceUuid()) {
        // loop through the services
        bPerpherial1 = peripheral;
        Serial.println(pAddress + ", " + pDeviceName + ", " + pLocalName + ", " + pRSSI);
        peripheral.discoverAttributes();
        for (int i = 0; i < peripheral.serviceCount(); i++) {
          pService = peripheral.service(i);
          Serial.print("Service ");
          Serial.println(pService.uuid());
          //loop through the characteristics
          for (int i = 0; i < pService.characteristicCount(); i++) {
            pChar = pService.characteristic(i);
            exploreCharacteristic(pChar);   
          }  // end loop chars             
        }    // end loop services
      }      // end if services present
   }         // end if localName == SENTIRE
}


// ---------------------------------------------------------------------------  EXPLORE CHARACTERISTIC
void exploreCharacteristic(BLECharacteristic characteristic) {
  String u;
  u = characteristic.uuid();
   // print the UUID and properies of the characteristic
  Serial.print("\tCharacteristic ");
  Serial.print(u);
  Serial.print(", properties 0x");
  Serial.print(characteristic.properties());
  
  // check if the characteristic is readable
  if (characteristic.canRead()) {
    // read the characteristic value
    characteristic.read();

    if (characteristic.valueLength() > 0)
    {
      // print out the value of the characteristic
      Serial.print(", value 0x");
      printData(characteristic.value(), characteristic.valueLength());      
    }
  }
  
  // loop the descriptors of the characteristic and explore each
  for (int i = 0; i < characteristic.descriptorCount(); i++) {
    BLEDescriptor descriptor = characteristic.descriptor(i);

    exploreDescriptor(descriptor);
  }

  // subscribe for notifications
  if (characteristic.canSubscribe()) {
    if (characteristic.subscribe()) {
      Serial.println(" ... Subcribed...");
      // hardcoding ...yuck...
      if (u.equalsIgnoreCase("41EF6051-E249-4035-81EE-8999024D88ED")) {
        bTemperature = characteristic;        
      } else if (u.equalsIgnoreCase("41EF6041-E249-4035-81EE-8999024D88ED")) {
        bMQ2 = characteristic;
      } else {
        Serial.println("new uuid to register...");
      }
    }
  }
}


// ---------------------------------------------------------------------------  EXPLORE DESCRIPTOR
void exploreDescriptor(BLEDescriptor descriptor) {
  // print the UUID of the descriptor
  Serial.print("\t\tDescriptor ");
  Serial.print(descriptor.uuid());

  // read the descriptor value
  descriptor.read();

  // print out the value of the descriptor
  Serial.print(", value 0x");
  printData(descriptor.value(), descriptor.valueLength());

  Serial.println();
}

void printData(const unsigned char data[], int length) {
  for (int i = 0; i < length; i++) {
    unsigned char b = data[i];

    if (b < 16) {
      Serial.print("0");
    }

    Serial.print(b, HEX);
  }
}




