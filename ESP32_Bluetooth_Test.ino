#include "BluetoothSerial.h"
#include <ESP32Servo.h>

BluetoothSerial SerialBT;
Servo myservo;//create servo object to control a servo

int servoPin = 18;

int val;
int val_ipt;
void setup() {
  //BTの処理
  Serial.begin(115200);
  SerialBT.begin("ESP32test");

  //Servo用の処理
  ESP32PWM::allocateTimer(0);
  ESP32PWM::allocateTimer(1);
  ESP32PWM::allocateTimer(2);
  ESP32PWM::allocateTimer(3);
  myservo.setPeriodHertz(50);
  myservo.attach(servoPin,500,2400);
  val =90;
}

void loop() {
  //Bluetooth経由でのデータ取得
  if (SerialBT.available()){
    String receiveData = SerialBT.readStringUntil(';');
    val_ipt = receiveData.toInt();
    if(val_ipt<0|180<val_ipt){
      SerialBT.print("invalid input\n");
    }
    else{
      val = val_ipt;
      SerialBT.print("set servo"+receiveData);
    }
 }
 myservo.write(val);
 delay(30);
}
