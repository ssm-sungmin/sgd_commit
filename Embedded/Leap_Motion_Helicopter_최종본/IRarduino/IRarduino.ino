#include <TimerOne.h>
#define MODULATED 1 
 
const int IR_PIN = 3;
const unsigned long DURATION = 180000;
const int HEADER_DURATION = 2000;
const int HIGH_DURATION = 380;
const int ZERO_LOW_DURATION = 220;
const int ONE_LOW_DURATION = 600;
const byte ROTATION_STATIONARY = 63;
//const byte ROTATION_STATIONARY = 60;
const byte CAL_BYTE = 52; 
 
int Throttle, LeftRight, FwdBack;


 
void sendHeader()
{
  #ifndef MODULATED
    digitalWrite(IR_PIN, HIGH);
  #else
    TCCR2A |= _BV(COM2B1);
  #endif
   
  delayMicroseconds(HEADER_DURATION);
   
  #ifndef MODULATED
    digitalWrite(IR_PIN, LOW);
  #else
    TCCR2A &= ~_BV(COM2B1);
  #endif
   
  delayMicroseconds(HEADER_DURATION);
   
  #ifndef MODULATED
    digitalWrite(IR_PIN, HIGH);
  #else
    TCCR2A |= _BV(COM2B1);
  #endif
   
  delayMicroseconds(HIGH_DURATION);
   
  #ifndef MODULATED
    digitalWrite(IR_PIN, LOW);
  #else
    TCCR2A &= ~_BV(COM2B1);
  #endif
}
 
void sendZero()
{
  delayMicroseconds(ZERO_LOW_DURATION);
 
  #ifndef MODULATED
    digitalWrite(IR_PIN, HIGH);
  #else  
    TCCR2A |= _BV(COM2B1);
  #endif
   
  delayMicroseconds(HIGH_DURATION);
   
  #ifndef MODULATED
    digitalWrite(IR_PIN, LOW);
  #else
    TCCR2A &= ~_BV(COM2B1);
  #endif
}
 
void sendOne()
{
  delayMicroseconds(ONE_LOW_DURATION);
   
  #ifndef MODULATED
    digitalWrite(IR_PIN, HIGH);
  #else
    TCCR2A |= _BV(COM2B1);
  #endif
   
  delayMicroseconds(HIGH_DURATION);
   
  #ifndef MODULATED
    digitalWrite(IR_PIN, LOW);  
  #else
    TCCR2A &= ~_BV(COM2B1);
  #endif
}

 // 프로토콜 방식만 알고 넘어가면 된다.
void sendCommand(int throttle, int leftRight, int forwardBackward)
{
  byte b;
 
  sendHeader();
   
  for (int i = 7; i >=0; i--)
  { //63이 default 값이고, 0~62는 우회전, 64~127은 좌회전
    b = ((ROTATION_STATIONARY + leftRight) & (1 << i)) >> i;    
    if (b > 0) sendOne(); else sendZero();
  }
   
  for (int i = 7; i >=0; i--)
  { //63이 default 값이고, 0~62는 전진, 64~127은 후진
    b = ((63 + forwardBackward) & (1 << i)) >> i;    
    if (b > 0) sendOne(); else sendZero();
  } 
   
  for (int i = 7; i >=0; i--)
  { //throttle 범위는 0~127
    b = (throttle & (1 << i)) >> i;    
    if (b > 0) sendOne(); else sendZero();
  }
   
  for (int i = 7; i >=0; i--)
  {
    b = (CAL_BYTE & (1 << i)) >> i;
    if (b > 0) sendOne(); else sendZero();
  } 
}
 
void setup()
{
  pinMode(IR_PIN, OUTPUT);
  digitalWrite(IR_PIN, LOW);
 
  //setup interrupt interval: 180ms  
  Timer1.initialize(DURATION);
  Timer1.attachInterrupt(timerISR);
   
  //setup PWM: f=38Khz PWM=0.5  
  byte v = 8000 / 38;
  TCCR2A = _BV(WGM20);
  TCCR2B = _BV(WGM22) | _BV(CS20); 
  OCR2A = v;
  OCR2B = v / 2;
  Serial.begin(57600);
}
 
void loop()
{
  
}
 
void timerISR()
{   
  /*********************************
            Arduino Value
        
    Throttle convert to 0 to 127
    LeftRight convert to -63 to 63
    FwdBack convert to -63 to 63
  **********************************/

  /*********************************
          Procssing Value
        
        yaw 127 ~ 107 ~  87
        pitch 177 ~ 216 ~ 255
        throttle 0 ~ 85
  **********************************/
  
  //leap motion
  // read it and store it in val
  int val = Serial.read();
  
  while (Serial.available() > 0){
    val = Serial.read();

    if(val >= 0 && val <= 85){
      //Throttle = map(val, 0, 85, 0, 127);
      
      if(val <=40)
        Throttle = map(val, 0, 85, 0, 40);
        else
      Throttle = map(val, 0, 85, 0, 120);
    }
    if(val >= 86 && val <= 170 ){
      //LeftRight = map(val, 87, 127, -60, 60);
      
      if(val >= 96 && val <= 117)
        LeftRight = map(val, 87, 127, -27, 27);
       else
        LeftRight = map(val, 87, 127, -53, 53);
    }
    if(val >= 171 && val <= 255){
       //FwdBack = map(val, 177, 255, -60, 60);

       if(val >= 197 && val <= 236)
        FwdBack = map(val, 177, 255, -27, 27);
       else
        FwdBack = map(val, 177, 255, -53, 53);
    }  
  }
  
  //FwdBack = 0;
  //Throttle = 5;
  //LeftRight = 0;

  sendCommand(Throttle, LeftRight, FwdBack);
  
}
