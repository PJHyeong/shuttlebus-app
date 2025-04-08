const express = require('express');
const router = express.Router();
const Marker = require('../models/marker');  // MongoDB 모델

// 모든 마커를 가져오는 GET 요청
router.get('/', async (req, res) => {
    try {
        const markers = await Marker.find();  // MongoDB에서 마커 데이터를 가져옴
        res.json(markers);
    } catch (err) {
        res.status(500).json({ message: err.message });
    }
});

// 새로운 마커 추가하는 POST 요청
router.post('/', async (req, res) => {
    const { lat, lng } = req.body;

    const newMarker = new Marker({
        lat,
        lng
    });

    try {
        await newMarker.save();  // MongoDB에 새 마커 저장
        res.status(201).json(newMarker);  // 새 마커 반환
    } catch (err) {
        res.status(400).json({ message: err.message });
    }
});

module.exports = router;
