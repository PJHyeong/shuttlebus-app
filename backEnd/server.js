const express = require("express");
const cors = require("cors");
const connectDB = require("./config/db");
const authRoutes = require("./routes/authRoutes");
// const busStopRoutes = require("./routes/busStopRoutes");
// const announcementRoutes = require("./routes/announcementRoutes");

//const http = require('http');
//const socketIo = require('socket.io');
// const markersRoutes = require("./routes/markersRoutes");

require("dotenv").config();

// HTTP 서버와 Socket.io 서버 통합
//const server = http.createServer(app);  // HTTP 서버로 express 앱을 감쌈
//const io = socketIo(server);  // Socket.io 서버 설정

const app = express();
connectDB();

app.use(cors());
app.use(express.json());

app.use("/api/auth", authRoutes);
// app.use("/api/bus", busStopRoutes);
// app.use("/api/announcements", announcementRoutes);
// app.use('/api/markers', markersRoutes); 

// Socket.io 이벤트 처리
io.on("connection", (socket) => {
    console.log("A user connected");

    // 실시간으로 클라이언트에 마커 업데이트 전송
    setInterval(async () => {
        // MongoDB에서 최신 마커 데이터를 가져와서 클라이언트로 전송
        const markers = await Marker.find().sort({ timestamp: -1 }).limit(1).exec();
        socket.emit("updateMarkers", markers);  // 클라이언트에게 실시간 마커 데이터 전송
    }, 1000);  // 1초마다 데이터를 전송

    socket.on("disconnect", () => {
        console.log("A user disconnected");
    });
});



const PORT = process.env.PORT || 5000;
app.listen(PORT, () => console.log(`🟢 Server started on port ${PORT}`));

// 기존의 express 서버를 HTTP 서버로 실행
//const PORT = process.env.PORT || 5000;
//server.listen(PORT, () => console.log(`🟢 Server started on port ${PORT}`));