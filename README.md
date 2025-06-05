## 🚌 명지대학교 셔틀버스 앱 제작(백엔드 담당)

### 📌 구현 목표! (MongoDB & Node.js)

* ✅서버 실행 (Express 서버 실행 후 MongoDB에 연결)
* ✅인증기능 (회원가입 & 로그인) => 외부 API로 변경 고민 중
* ✅관리자 역할 추가
* ✅공지사항 CRUD 
* ✅공지사항 댓글
* ✅데이터베이스(MongoDB) 설계
* ✅ngrok로 다른 네트워크에서도 앱 작동하게(PORT:5000)     
*      
* ✅셔틀버스 정류장 기능 (정류장 선택 후 알림 받을 수 있도록)
* ✅실시간 버스 위치 데이터를 받아 지도 상에 표시

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
        │   ├── db.js                 # 데이터베이스 연결 설정           
        │── models/    
        │   ├── User.js               # 유저 모델           
        │   ├── Reservation.js        # 알림 모델           
        │   ├── Buslocation.js            # 셔들버스 모델
        │   ├── Notice.js       # 공지사항 모델                
        │   ├── Comment.js       # 공지사항 댓글 모델         
        │── routes/       
        │   ├── authRoutes.js         # 로그인 및 회원가입 라우트        
        │   ├── buslocationRoutes.js      # 셔틀버스 위치 관련 라우트                    
        │   ├── reservations.js       # 알림 관련 라우트            
        │   ├── notices.js            # 공지사항 관련 라우트
        │   ├── commentRoutes.js            # 공지사항 댓글 관련 라우트       
        │── middlewares/       
        │   ├── adminMiddleware.js     # 관리자 인증 미들웨어           
        │── server.js                 # 메인 서버 파일       
        │── .env                      # 환경 변수 설정           
```
