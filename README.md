## 🚌 명지대학교 셔틀버스 앱 제작(백엔드 담당)

### 📌 구현 목표! (MongoDB & Node.js)

* ✅서버 실행 (Express 서버 실행 후 MongoDB에 연결)
* ⚠️인증기능 (회원가입 & 로그인) => 외부 API로 변경 고민 중
* 실시간 버스 위치 데이터를 받아 지도 상에 표시
* 셔틀버스 정류장 기능 (정류장 선택 후 알림 받을 수 있도록)
* 공지사항 관리


### ⚙️ 개발 환경
- **백엔드**       
    - VSCode - v1.99.0     
    - Node.js - v22.14.0              
    - MongoDB - v6.15.0        
    - mongoose - v8.13.1     
    - express - v5.1.0     


    
```
         shuttle-bus-backend/    
        │── config/    
        │   ├── db.js             # ✅데이터베이스 연결 설정 - 25/04/01    
        │── models/    
        │   ├── User.js           # ✅유저 모델 - 25/04/02    
        │   ├── Notification.js   # 알림 모델    
        │   ├── BusStop.js        # 정류장 모델    
        │   ├── Announcement.js   # 공지사항 모델       
        │── routes/       
        │   ├── authRoutes.js     # ⚠️로그인 및 회원가입 라우트 - 25/04/02(외부 API로 변경 고민 중)          
        │   ├── busStopRoutes.js  # 셔틀버스 정류장 관련 라우트       
        │   ├── notificationRoutes.js # 알림 관련 라우트       
        │   ├── announcementRoutes.js # 공지사항 관련 라우트       
        │── middleware/       
        │   ├── authMiddleware.js # JWT 인증 미들웨어       
        │── server.js             # ✅메인 서버 파일 - 25/04/01       
        │── .env                  # ✅환경 변수 설정 -  25/04/01     
```
