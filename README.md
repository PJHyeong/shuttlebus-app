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
        

본인 Client ID 복사 후 실행...!!!    
<div align=center>
<table><thead>
  <tr>
    <th colspan="2"><i>Tech Stacks</th>
  </tr></thead>
<tbody>
  <tr align = center>
    <td colspan="2">Frontend</td>
  </tr>
  <tr align = center> 
    <td><img src = "https://img.shields.io/badge/android%20studio-346ac1?style=for-the-badge&logo=android%20studio&logoColor=white"></td>
    <td><img src = "https://img.shields.io/badge/kotlin-%237F52FF.svg?style=for-the-badge&logo=kotlin&logoColor=white"></td>
  </tr>
  <tr align = center>
    <td colspan="2">Backend</td>
  </tr>
  <tr align = center>
    <td><img src = "https://img.shields.io/badge/node.js-6DA55F?style=for-the-badge&logo=node.js&logoColor=white"></td>
    <td><img src = "https://img.shields.io/badge/MongoDB-%234ea94b.svg?style=for-the-badge&logo=mongodb&logoColor=white"></td>
  </tr>
  <tr align = center>
    <td colspan="2">Hardware</td>
  </tr>
  <tr align = center>
    <td><img src = "https://img.shields.io/badge/-Raspberry_Pi-C51A4A?style=for-the-badge&logo=Raspberry-Pi"></td>
    <td><img src = "https://img.shields.io/badge/python-3670A0?style=for-the-badge&logo=python&logoColor=ffdd54"></td>
  </tr>
</tbody>
</table>
</div>


