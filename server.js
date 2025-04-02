const express = require("express");
const cors = require("cors");
const connectDB = require("./config/db");
const authRoutes = require("./routes/authRoutes");
// const busStopRoutes = require("./routes/busStopRoutes");
// const announcementRoutes = require("./routes/announcementRoutes");

require("dotenv").config();

const app = express();
connectDB();

app.use(cors());
app.use(express.json());

app.use("/api/auth", authRoutes);
// app.use("/api/bus", busStopRoutes);
// app.use("/api/announcements", announcementRoutes);

const PORT = process.env.PORT || 5000;
app.listen(PORT, () => console.log(`🟢 Server started on port ${PORT}`));