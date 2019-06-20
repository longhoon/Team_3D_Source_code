
void serialOutput(){   // 시리얼 모니터 상의 출력 결과를 위한 함수
 if (serialVisual == true){  
     arduinoSerialMonitorVisual('-', Signal);   // Serial Monitor Visualizer 함수 호출
 } else{
      sendDataToSerial('S', Signal);     // sendDataToSerial 함수 호출
 }        
}


//  BPM과 DB 계산 함수 구현
void serialOutputWhenBeatHappens(){  
  int sound_value = analogRead(A1);  //sound_value 변수 내에 A1 포트에 연결된 소리센서의 아날로그 신호값을 전달받는다.
  //BPM=BPM*0.6;
 if (serialVisual == true){  //  Serial monitor visuallizer 작동 여부 확인
  //BPM과 DB의 조건문에 따른 문자열 출력 
  if (BPM >120){
    if(sound_value>=100){
    Serial.print("BPM: ");
    Serial.print(BPM);
    Serial.print("     ");
    Serial.print("DB: ");
    Serial.print(sound_value);
    Serial.print("  [unsafe]");
    }
    else if(sound_value<100){
    Serial.print("BPM: ");
    Serial.print(BPM);
    Serial.print("     ");
    Serial.print("DB: ");
    Serial.print(sound_value);
    Serial.print("  [caution]");
    }
  }
  else {
    Serial.print("BPM: ");
    Serial.print(BPM);
    Serial.print("     ");
    Serial.print("DB: ");
    Serial.print(sound_value);
    Serial.print("  [safe]");
   } 
 }
 else{
        sendDataToSerial('B',BPM);   // 심박율을 'B' 접두사로 전송
        sendDataToSerial('Q',IBI);   // 시간 간격을 'Q' 점두사로 전송
 }   
}



//  Symbol과 data를 특정 app이나 모듈에 전송 (테스트용) 
void sendDataToSerial(char symbol, int data ){
    Serial.print(symbol);

    Serial.println(data);                
  }


//  Serial Monitor Visualizer 작동 구현
void arduinoSerialMonitorVisual(char symbol, int data ){    
  const int sensorMin = 0;      // 센서값 최소점을 설정
const int sensorMax = 1024;    // 센서값 최대값을 설정

  int sensorReading = data;
  // 센서 값을 12등분하여 mapping함
  int range = map(sensorReading, sensorMin, sensorMax, 0, 11);


  switch (range) {
  case 0:     
    Serial.println("");     /////아스키 코드로 펄스값에 따른 신호 표기
    break;
  case 1:   
    Serial.println("");
    break;
  case 2:    
    Serial.println("");
    break;
  case 3:    
    Serial.println("");
    break;
  case 4:   
    Serial.println("");
    break;
  case 5:   
    Serial.println("");
    break;
  case 6:   
    Serial.println("");
    break;
  case 7:   
    Serial.println("");
    break;
  case 8:  
    Serial.println("");
    break;
  case 9:    
    Serial.println("");
    break;
  case 10:   
    Serial.println("");
    break;
  case 11:   
    Serial.println("");
    break;
  
  } 
}
