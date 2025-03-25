## 셔틀 버스 앱 제작
클라이언트 ID 발급
네이버 지도 SDK를 사용하려면 다음과 같이 애플리케이션을 등록하고 클라이언트 ID를 발급받아야 합니다.

1. "네이버 클라우드 플랫폼"에 로그인한 후 "콘솔"에 들어갑니다.
2.Services에서 Application Services 하위의 Maps를 선택합니다.
3.Application 등록을 선택하고 API 선택 하위의 Dynamic Map을 체크합니다.
4.Android 앱 패키지 이름에 네이버 지도 SDK를 사용하고자 하는 앱의 패키지명을 추가하고 등록합니다.
5.등록한 애플리케이션의 인증 정보를 선택해 키 ID를 확인합니다.

인증 정보에서 Client ID 복사 후 AndroidManifest.xml 에서

            <meta-data
                        android:name="com.naver.maps.map.NCP_KEY_ID"
                        android:value="본인 Client ID 복사" />
        

본인 Client ID 복사 후 실행...!!
