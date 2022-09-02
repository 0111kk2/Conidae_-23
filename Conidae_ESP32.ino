/*void setup() {
  // put your setup code here, to run once:

}

void loop() {
String val_ipt = "90,80";
char charBuf[50];
char *value = NULL;
val_ipt.toCharArray(charBuf,50);
value = strtok(charBuf,",");
String rightValue = value;
int right= rightValue.toInt();
value = strtok(NULL,",");
String leftValue = value;
int left= leftValue.toInt();
Serial.print("right is:");
Serial.println(right);
Serial.print("left is:");
Serial.println(left);
}*/
//=========================================---
#include "BluetoothSerial.h"
#include <ESP32Servo.h>

BluetoothSerial SerialBT;
Servo rightservo;//create servo object to control a servo
Servo leftservo;
int rightPin = 18;
int leftPin = 5;
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
  rightservo.setPeriodHertz(50);
  leftservo.setPeriodHertz(50);
  rightservo.attach(rightPin,500,2400);
  leftservo.attach(leftPin,500,2400);
  
  val =90;
}

void loop() {
  //Bluetooth経由でのデータ取得
  if (SerialBT.available()){
    String val_ipt = SerialBT.readStringUntil(';');
    //文字列解析のためにchar型に変換
    char charBuf[50];
    char *value = NULL;
    val_ipt.toCharArray(charBuf,50);
    value = strtok(charBuf,",");
    String rightValue = value;
    int right= rightValue.toInt();
    value = strtok(NULL,",");
    String leftValue = value;
    int left= leftValue.toInt();

    if(right<0|180<right){
      SerialBT.print("invalid right input\n");
    }
    else if(left<0|180<left){
      SerialBT.print("invalid left input\n");
    }
    else {
      SerialBT.print("setR:");
      SerialBT.print(right);
      rightservo.write(right);
      SerialBT.print("L:");
      SerialBT.print(left);
      leftservo.write(left);
      SerialBT.print("\n");
    }
 }

 delay(30);
}
