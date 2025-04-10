const mongoose = require("mongoose");

// 마커 데이터 스키마 정의
const markerSchema = new mongoose.Schema({
    lat: { type: Number, required: true },  // 위도
    lng: { type: Number, required: true },  // 경도
    timestamp: { type: Date, default: Date.now }  // 데이터 생성 시간
});

// Marker 모델 정의
const Marker = mongoose.model("Marker", markerSchema);

module.exports = Marker;