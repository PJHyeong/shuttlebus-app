const mongoose = require('mongoose');

const reservationSchema = new mongoose.Schema({ // 예약 스키마
    userID: { type: mongoose.Schema.Types.ObjectId, ref: 'User' },
    stopName: String, 
    arrivalTime: Date
});

module.exports = mongoose.model('Reservation', reservationSchema);