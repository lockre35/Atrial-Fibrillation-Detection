/*

Copyright (c) 2012-2014 RedBearLab

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

*/

/*
 *    ECG ADC Device
 *	
 *	  Read from pin A5
 *	  Send data in 20 byte arrays
 *    Convert 0 to 1023 input to 0 255 input
 *	  Add a 5ms delay between each recording
 *    
 */

//"SPI.h/Nordic_nRF8001.h/RBL_nRF8001.h" are needed in every new project
#include <SPI.h>
#include <Nordic_nRF8001.h>
#include <RBL_nRF8001.h>

void setup()
{
  //  
  // For BLE Shield or Blend:
  //   Default pins set to 9 and 8 for REQN and RDYN
  //   Set your REQN and RDYN here before ble_begin() if you need
  //
  // For Blend Micro:
  //   Default pins set to 6 and 7 for REQN and RDYN
  //   So, no need to set for Blend Micro.
  //
  
  // Set BLE name
  ble_set_name("ECG Slave");
  
  // Init. and start BLE library.
  ble_begin();
  
  // Enable serial debug
  Serial.begin(57600);
}


const int analogInPin = A5;
int sensorValue = 0;        // value output to the PWM (analog out)
int outputValue = 0;
int i;
byte bytes[20];

void loop()
{
  //While connected
  if ( ble_connected() )
  {
    //Loop through 20 times and record analog input
    for(i=0; i<19; i++) {
      sensorValue = analogRead(analogInPin);
      outputValue = map(sensorValue, 0, 1023, 0, 255);  
      bytes[i] = (byte) outputValue;
      delay(5);
    }
    
    //Read once more and use ble_write at the delay
	sensorValue = analogRead(analogInPin);
	outputValue = map(sensorValue, 0, 1023, 0, 255);  
	bytes[19] = (byte) outputValue;
   
    //Set up the data transfer and reset array to zeros
    ble_write_bytes(bytes, 20);
    memset(bytes,0,sizeof(bytes));
  }
	
  //Send the ble write event
  ble_do_events();
  
//  //Used for debugging
//  if ( ble_available() )
//  {
//    while ( ble_available() )
//    {
//      Serial.write(ble_read());
//    }
//    
//    Serial.println();
//  }
}