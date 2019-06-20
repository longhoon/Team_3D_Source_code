
volatile int rate[10];                    // IBI 변수에 대한 배열
volatile unsigned long sampleCounter = 0;          // 펄스의 시간적 간격 설정
volatile unsigned long lastBeatTime = 0;           // IBI 확인
volatile int P =512;                      // 펄스의 최대값 
volatile int T = 512;                     // 펄스 주기 
volatile int thresh = 525;                // 심박 센서 값의 쓰레드값 설정
volatile int amp = 100;                   // 펄스의 진폭 설정
volatile boolean firstBeat = true;      
volatile boolean secondBeat = false;     


void interruptSetup(){     //interruptSetup 함수 구현, Timer2를 매 2ms마다 초기화한다.
  TCCR2A = 0x02;    
  TCCR2B = 0x06;    
  OCR2A = 0X7C;      
  TIMSK2 = 0x02;     
  sei();                
} 


// 타이머 2 인터럽트 서비스 루틴 설정
// 타이머 2는 매 2 밀리 초마다 측정값을 가져온다.
ISR(TIMER2_COMPA_vect){                         // Timer2가 124로 계산할 때 트리거 됨
  cli();                                      // 앞선 작업 도중 인터럽트를 비활성화한다.
  Signal = analogRead(pulsePin);              // 펄스 센서 값을 로드한다 
  sampleCounter += 2;                         // 변수를 사용하여 mS로 시간을 계산한다.
  int N = sampleCounter - lastBeatTime;       // 잡음을 피하기 위해 마지막 비트 이후의 시간을 모니터한다.
    
// 맥파의 최고점과 최저점을 찾는다.
  if(Signal < thresh && N > (IBI/5)*3){       // 마지막 IBI의 3/5를 대기하여 노이즈를 피함
    if (Signal < T){                   
      T = Signal;                         // 심박 측정 파형의 가장 낮은 지점을 파악 
    }
  }

  if(Signal > thresh && Signal > P){          // 노이즈를 피하기 위한 thresh 조건문
    P = Signal;                             // peak값을 p로 설정
  }                                        // 펄스 파형의 가장 높은 지점 파악

  //  아래부터 심박을 확인
  // 신호는 펄스가 발생할 때마다 값이 올라가도록 설정
  if (N > 250){                                   // 고주파 잡음을 피함
    if ( (Signal > thresh) && (Pulse == false) && (N > (IBI/5)*3) ){        
      Pulse = true;                               // 펄스가 존재한다면 펄스 플래그 설정
      //digitalWrite(blinkPin,HIGH);                // 테스트형 13 LED 전원 On
      IBI = sampleCounter - lastBeatTime;   
      
      // measure time between beats in mS
      lastBeatTime = sampleCounter;               // 맥박 시간 파악

      if(secondBeat){                        // 2번째 비트의 경우, secondBeat가 TRUE값일 경우의 조건문
        secondBeat = false;                  // secondBeat 플래그 지우기
        for(int i=0; i<=9; i++){             // 작동 시 realisitic BPM을 얻기 위해 누적 합계 값 시드
          rate[i] = IBI;                      
        }
      }

      if(firstBeat){                         // firstBeat == TRUE 인 경우 처음으로 비트를 찾은 경우로 인지
        firstBeat = false;                   // 첫 flag 삭제
        secondBeat = true;                   // 두 번째 비트 flag 설정
        sei();                               // 인터럽트 재허용
        return;                              // IBI 값은 신뢰할 수 없으므로 삭제
      }   


      // 지난 10개의 IBI값의 누적 합계 유지
      word runningTotal = 0;                  // runningTotal 변수 삭제   

      for(int i=0; i<=8; i++){                // rate 배열의 데이터를 이동
        rate[i] = rate[i+1];                  // 가장 오래된 IBI값을 드롭
        runningTotal += rate[i];              // 가장 오래된 9개의 IBI 값을 더한다
      }

      rate[9] = IBI;                          // rate 배열에 최신 IBI를 추가한다.
      runningTotal += rate[9];                // runningTotal에 최신 IBI를 추가
      runningTotal /= 10;                     // 지난 10 개의 IBI 값 평균
      BPM = 60000/runningTotal;               // 1분 안에 들어가는 비트의 양을 통해 BPM 값 추출
      QS = true;                              // 정량화 된 셀프 플래그 설정
      // QS 플래그가 ISR 내부에서 삭제되지 않음
    }                       
  }

  if (Signal < thresh && Pulse == true){   // 값이 감소할 떄, 비트 종료
    //digitalWrite(blinkPin,LOW);            // 테스트용 LED 13핀 
    Pulse = false;                         // 펄스 플래그 재설정
    amp = P - T;                           // 심박 파형의 진폭 계산
    thresh = amp/2 + T;                    // thresh를 진폭의 50%로 설정
    P = thresh;                            // 다음 번 시도 시 재설정
    T = thresh;
  }

  if (N > 2500){                           // 비트 없이 2.5초가 지났을 시의 조건문
    thresh = 512;                          // thresh 기본값 설정
    P = 512;                               // P 값 초기화
    T = 512;                               // T 값 초기화
    lastBeatTime = sampleCounter;          // lateBeatTime을 최신으로 Load        
    firstBeat = true;                      // noise를 피하기 위한 설정
    secondBeat = false;                    // 심장 박동 되 찾을 때의 작동
  }

  sei();                                   // 종료 시 인터럽트 활성화
}// isr 종료
