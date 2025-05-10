const express = require('express');
const router = express.Router();
const Reservation = require('../models/Reservation');


router.post('/', async (req, res) => {  // 예약 생성
    const { userID, stopName, arrivalTime } = req.body;
    const reservation = new Reservation({ userID, stopName, arrivalTime });
    await reservation.save();
    res.send({message: '예약이 완료되었습니다.'});
});

module.exports = router; 