
#include <SoftwareSerial.h> //SoftwareSerial.h 라이브러리 호출

//bluetooth hc-06모델 사용을 위한 라이브러리 설정
SoftwareSerial bluetooth(12, 13);
int pulsePin = 0;                 // 심박센서 포트 설정
int blinkPin = 13;                // blink led
int fadePin = 5;                  // blink
int fadeRate = 0;                 // LED 테스트용 포트 설정

// interrupt service routine 설정
volatile int BPM;                   // 2ms 마다 아날로그 신호를 통해 BPM 변수 측정값 업데이트
volatile int Signal;                // 원래의 아날로그 신호 값
volatile int IBI = 600;             // 심박 측정 간의 간격을 설정
volatile boolean Pulse = false;     // "True" when User's live heartbeat is detected. "False" when not a "live beat". 
volatile boolean QS = false;        // becomes true when Arduoino finds a beat.


static boolean serialVisual = true;   // 아두이노 시리얼 모니터를 통한 테스터 화면 표시


void setup(){
  pinMode(8, OUTPUT);    //HC-06모듈
  digitalWrite(8,HIGH);
  Serial.println("Ready");  //센서 측정을 위한 준비가 완료되었음을 알림
  //pinMode(blinkPin,OUTPUT);         // 심박에 대한 blink pin 설정
  pinMode(fadePin,OUTPUT);          // 심박에 대한 fade pin 설정
  Serial.begin(9600); //시리얼 모니터의 결과 board rate 설정
  bluetooth.begin(9600);// 블루투스 전송의 결과 board rate 설정
  interruptSetup();     
  Serial.println("Ready");//   
}


// main문 구현
void loop(){
  BPM=BPM*0.6;
  
    serialOutput() ;    //시리얼 모니터 상의 출력 결과   
    
  if (QS == true){     // 심박 센서를 통한 심박이 측정되는 조건문
                 
        fadeRate = 255;         // LED Fade 테스트용으로 설정
                                
        serialOutputWhenBeatHappens();   // 심박 측정 결과 값을 시리얼 모니터 상에 출력함   
        QS = false;                    
  }
  int sound_value = analogRead(A1);  
            
     // BPM과 DB의 조건에 따른 특정 문자열을 안드로이드 애플리케이션에 전송        
  if (BPM >= 120){
    if(sound_value>=100){
    bluetooth.print(" BPM: ");
    bluetooth.print(BPM);
    bluetooth.print("   DB: ");
    bluetooth.print(sound_value);
    bluetooth.println("    [Dangerous!!!]");
    
    }
    else
    { 
     bluetooth.print(" BPM: ");
    bluetooth.print(BPM);
    bluetooth.print("   DB: ");
    bluetooth.print(sound_value);
    bluetooth.println("    [caution]");
    }
  }
  else {
   
    bluetooth.print(" BPM: ");
    bluetooth.print(BPM);
    bluetooth.print("   DB: ");
    bluetooth.print(sound_value);
    bluetooth.println("    [safe]");
   } 
 
     
  ledFadeToBeat();                      // 테스트용 LED fade
  delay(2000);                             //  위 loop문의 delay값 별도 설정
}





void ledFadeToBeat(){
    fadeRate -= 15;                         //  테스트용 LED fade값 설정
    fadeRate = constrain(fadeRate,0,255);   //  테스트용 LED fade값 설정
    analogWrite(fadePin,fadeRate);          // 테스트용 LED fade값 설정
  }
