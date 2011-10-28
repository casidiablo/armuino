ADK example project
===================

This is a simple Android application that demonstrate how to use Accessory Development Kit. It basically controls a Scara robot which is built with tree servos.

Arduino board must run this sketch:

    #include <Servo.h>
    #include <Usb.h>
    #include <AndroidAccessory.h>
    
    #define  FOREARM     11
    #define  ARM         12
    #define  HAND        13
    #define  MOVE_MSG    0x2
    
    AndroidAccessory acc("egoclean",
    		     "arm",
    		     "egoclean arm",
    		     "1.0",
    		     "http://www.android.com",
    		     "0000000012345678");
    Servo foreArm;
    Servo arm;
    Servo hand;
    
    void setup() {
      Serial.begin(115200);
      Serial.print("App started");
      
      foreArm.attach(FOREARM);
      foreArm.write(90);
      arm.attach(ARM);
      arm.write(90);
      hand.attach(HAND);
      hand.write(0);
      
      Serial.println("Servos attached");
      
      acc.powerOn();
    }
    
    void loop() {
      byte msg[3];
      if (acc.isConnected()) {
        int len = acc.read(msg, sizeof(msg), 1);
        if (len > 0) {
          if (msg[0] == MOVE_MSG) {
            int howMuch = msg[2];
            if (msg[1] == FOREARM){
              foreArm.write(howMuch);
            } else if (msg[1] == ARM){
              arm.write(howMuch);
            } else if (msg[1] == HAND){
              hand.write(howMuch);
            }
          }
        }
        msg[0] = -1;
      } else {
        foreArm.write(90);
        arm.write(90);
        hand.write(0);
      }
      delay(10);
    }

How to run Arduino sketch
=========================

In order to be able to run sketch above, you must:

- Have an Arduino Mega for Android or an Arduino UNO with a USB shield
- Follow [these steps][1] in order to install ADK libraries for Arduino

  [1]: http://developer.android.com/guide/topics/usb/adk.html
