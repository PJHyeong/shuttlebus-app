const admin = require('../config/firebase');

async function sendPushNotification(fcmToken, title, body) { // 푸시 알림 전송 함수
    const message = {
        notification: { title, body },
        token: fcmToken
    };

    try {
        await admin.messaging().send(message); 
    } catch (error) {
        console.error('푸시 알림 전송 실패:', error);
    }
}

module.exports = { sendPushNotification };