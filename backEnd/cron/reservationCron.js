const cron = require('node-cron');
const Reservation = require('../models/Reservation');
const Notice = require('../models/Notice');
const {sendNotification} = require('./services/notificationService');


cron.schedule('* * * * *', async () => {
    const now = new Date(); 
    const reservations = await Reservation.find({
        arrivalTime: {$lte: new Date(now.getTime() + 60000)} // 1분 전 예약
    });

    for (const res of reservations) {
        const user = await User.findById(res.userID);   // 예약한 유저 정보 가져오기
        if (user && user.fcmToken) {
            await sendNotification(user.fcmToken, '셔틀 알림', `${res.stopName}에 곧 도착합니다.`);
        }
        await Reservation.findByIdAndDelete(res._id); // 한 번 알림 후 삭제
    } 
});