const express = require("express"); // Express.js 프레임워크
const cors = require("cors");   // CORS 미들웨어
const connectDB = require("./config/db");   // MongoDB 연결 설정
const authRoutes = require("./routes/authRoutes");  // 로그인 및 회원가입 관련 라우트
const reservations = require("./routes/reservations"); // 셔틀 예약 관련 라우트
const BuslocationRoutes = require('./routes/BusloactionRoutes'); // 버스 정류장 관련 라우트
const notices = require("./routes/notices");   // 공지사항 관련 라우트
const commentRoutes = require('./routes/commentRoutes'); // 공지사항 댓글 관련 라우트

require("dotenv").config(); 

const app = express();  // Express 애플리케이션 생성
connectDB();  // MongoDB 연결


// 미들웨어 설정
app.use(cors());
app.use(express.json());


// API 라우트 설정
app.use("/api/auth", authRoutes);
app.use("/api/notices", notices);
app.use("/api/reservations", reservations);
app.use('/api/bus', BuslocationRoutes);
app.use('/api', commentRoutes);



const PORT = process.env.PORT || 5000;  // 포트 설정(5000번)
app.listen(PORT, () => console.log(`🟢 Node.js 서버가 ${PORT} 포트에서 실행 중 입니다`));