const express = require("express");
const Marker = require("../models/Marker");  // Marker 모델 불러오기
const router = express.Router();

// 모든 마커 데이터 가져오기
router.get("/", async (req, res) => {
    try {
        // 최신 마커 1개를 MongoDB에서 가져오기
        const markers = await Marker.find().sort({ timestamp: -1 }).limit(1).exec();
        res.json(markers);  // 마커 데이터 응답으로 보내기
    } catch (err) {
        res.status(500).json({ message: err.message });
    }
});

// 새로운 마커 추가하기
router.post("/", async (req, res) => {
    const { lat, lng } = req.body;  // 클라이언트에서 lat, lng 값을 받기

    const newMarker = new Marker({
        lat,
        lng
    });

    try {
        await newMarker.save();  // MongoDB에 새 마커 데이터 저장
        res.status(201).json(newMarker);  // 저장된 마커 데이터 응답으로 보내기
    } catch (err) {
        res.status(400).json({ message: err.message });
    }
});

module.exports = router;
