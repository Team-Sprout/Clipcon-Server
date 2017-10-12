# Global Clipboard Server

### Global Clipboard
Global Clipboard는 다수의 사용자가 클립보드를 이용하여 데이터를 간편하게 주고 받을 수 있는 Server-Client
구조의 데이터 전송 플랫폼입니다. 사용자는 클립보드를 이용한 복사, 붙여넣기 인터페이스와 이에 접근 가능한 단축키 (Ctrl C, Ctrl V)를 이용하여 다른 번거로운 과정 없이 간편하게 데이터를 주고 받을 수 있습니다. 따라서 다른 사용자가 복사한 데이터를 자신이 붙여넣는 것 처럼 사용할 수 있습니다. 클립보드를 이용하기 때문에 클립보드에 복사할 수 있는 모든 종류의 데이터(Text, Capture Image, File)를 전송할 수 있습니다.

***
### ClipCon
ClipCon은 Global Clipboard를 사용할 수 있는 클라이언트 어플리케이션의 이름입니다. 현재 본 프로젝트에서는 윈도우와 안드로이드에서 구동되는 어플리케이션을 개발하였습니다. ClipCon에 대한 자세한 내용은 다음을 참고하여 주시기 바랍니다.
* 윈도우: [Windows ClipCon GitHub](https://github.com/team-sprout/clipcon-client)
* 안드로이드: [Android ClipCon GitHub](https://github.com/team-sprout/clipcon-AndroidClient)
***
### 유용성
Global Clipboard는 다수의 다바이스 사이에서 데이터를 주고 받는 상황이라면 어디서든 활용가능하지만, 신뢰있는 사용자간에 전송할 데이터가 간헐적으로 발생할 때 가장 효과적으로 사용할수 있습니다.
* 1인 2PC 이상 사용자의 PC간 데이터 교환
  * 예) 작업용 데스크탑과 개인용 노트북 간 텍스트 또는 파일 복사/붙여넣기 등
* 다수의 사용자와 협업 시, 데이터 전송 및 교환
  * 예1) 팀원과 함께 발표 자료를 만들 때 적절한 캡처 이미지 복사/붙여넣기
  * 예2) 교수자가 학생들에게 즉석에서 필요한 수업자료를 클립보드를 이용하여 배포 등
***
### 구조
