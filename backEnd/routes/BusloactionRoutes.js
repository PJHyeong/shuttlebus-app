const express     = require('express');
const router      = express.Router();
const BusLocation = require('../models/Buslocation');  // ① 모델 임포트

// ② 최신 위치 조회 엔드포인트
router.get('/latest', async (req, res) => {
    try {
      const latest = await BusLocation
        .findOne()
        .sort({ timestamp: -1 })
        .lean();
  
      if (!latest) {
        return res.status(404).json({ error: 'No location data yet.' });
      }
  
      // 클라이언트가 기대하는 형태로 필드명 매핑
      return res.json({
        lat: latest.latitude,
        lng: latest.longitude
      });
    } catch (err) {
      console.error('Error fetching latest bus location:', err);
      return res.status(500).json({ error: 'Server error' });
    }
  });
  
  // ③ 라우터 내보내기
  module.exports = router;