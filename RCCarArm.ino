#include <Servo.h>
Servo myservo0;
Servo myservo1;
Servo myservo2;
Servo myservo3;
char command;
int angle0 = 90;
int angle1 = 90;
int angle2 = 90;
int angle3 = 90;
/*define logic control output pin*/
int in1=12;
int in2=8;
int in3=7;
int in4=4;
/*define channel enable output pins*/
int ENA=10;
int ENB=11;

void setup() {
  Serial.begin(9600);
  //myservo.attach(3);// attach servo on pin 3 to servo object
  myservo0.attach(3);
  myservo1.attach(5);
  myservo2.attach(6);
  myservo3.attach(9);  
  myservo0.write(angle0);
  myservo1.write(angle1);
  myservo2.write(angle2);
  myservo3.write(angle3);
  pinMode(in1,OUTPUT);
  pinMode(in2,OUTPUT);
  pinMode(in3,OUTPUT);
  pinMode(in4,OUTPUT);
  pinMode(ENA,OUTPUT);
  pinMode(ENB,OUTPUT);  
}

void loop() {
  command = Serial.read();
  if(command == 'C')
  {
    turnC();
    delay(10);
  }
    
  if(command == 'c')
  {
    turnCC();
    delay(10);
  }

  if(command == 'R')
  {
    retract();
    delay(10);
  }

  if(command == 'E')
  {
    extend();
    delay(10);
  }
  if(command == 'D')
  {
    down();
    delay(10);
  }
  if(command == 'U')
  {
    up();
    delay(10);
  }
  if(command == 'r')
  {
    releases();
    delay(10);
  }
  if(command == 'G')
  {
    grab();
    delay(10);
  }
  if(command == 'w')
  {
    _mForward();
    delay(10);
  }
  if(command == 's')
  {
    _mBack();
    delay(10);
  }
  if(command == 'a')
  {
    _mLeft();
    delay(10);
  }
  if(command == 'd')
  {
    _mRight();
    delay(10);
  }
  if(command == '0')
  {
    reset();
    delay(10);
  }
}

//When servo is looking down
void turnC(){
  if(angle0 < 180){
    angle0 = angle0 - 5;
    myservo0.write(angle0);
  }
}

void turnCC(){
  if(angle0 > 0){
    angle0 = angle0 + 5;
    myservo0.write(angle0);
  }
}

void retract(){
  if(angle1 < 180){
    angle1 = angle1 - 5;
    myservo1.write(angle1);
  }
}

void extend(){
  if(angle1 > 0){
    angle1 = angle1 + 5;
    myservo1.write(angle1);
  }
}

void down(){
  if(angle2 < 180){
    angle2 = angle2 - 5;
    myservo2.write(angle2);
  }
}

void up(){
  if(angle2 > 0){
    angle2 = angle2 + 5;
    myservo2.write(angle2);
  }
}

void releases(){
  if(angle3 < 180){
    angle3 = angle3 + 5;
    myservo3.write(angle3);
  }
}

void grab(){
  if(angle3 > 0){
    angle3 = angle3 - 5;
    myservo3.write(angle3);
  }
}

void reset(){
  angle0 = 90;
  angle1 = 90;
  angle2 = 90;
  angle3 = 90;
  myservo0.write(angle0);
  myservo1.write(angle1);
  myservo2.write(angle2);
  myservo3.write(angle3);
  _mStop();
}

void _mForward()
{ 
  digitalWrite(ENA,HIGH);
  digitalWrite(ENB,HIGH);
  digitalWrite(in1,LOW);//digital output
  digitalWrite(in2,HIGH);
  digitalWrite(in3,LOW);
  digitalWrite(in4,HIGH);
  Serial.println("Forward");
  
}
/*define back function*/
void _mBack()
{
  digitalWrite(ENA,HIGH);
  digitalWrite(ENB,HIGH);
  digitalWrite(in1,HIGH);
  digitalWrite(in2,LOW);
  digitalWrite(in3,HIGH);
  digitalWrite(in4,LOW);
  Serial.println("Back");
  
}
/*define left function*/
void _mLeft()
{
  digitalWrite(ENA,HIGH);
  digitalWrite(ENB,HIGH);
  digitalWrite(in1,LOW);
  digitalWrite(in2,HIGH);
  digitalWrite(in3,HIGH);
  digitalWrite(in4,LOW);
  Serial.println("Left");
  
}
/*define right function*/
void _mRight()
{
  digitalWrite(ENA,HIGH);
  digitalWrite(ENB,HIGH);
  digitalWrite(in1,HIGH);
  digitalWrite(in2,LOW);
  digitalWrite(in3,LOW);
  digitalWrite(in4,HIGH);
  Serial.println("Right");
  
}

void _mStop()
{
  digitalWrite(ENA,LOW);
  digitalWrite(ENB,LOW);
  Serial.println("Stop!");
}
