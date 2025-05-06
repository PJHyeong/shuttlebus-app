const mongoose = require('mongoose');

const busLocationSchema = new mongoose.Schema({
    latitude:  { type: Number, required: true },
    longitude: { type: Number, required: true },
    timestamp: { type: Date,   default: Date.now, index: true }
  }, {
    collection: 'gps'   // ← Atlas에 실제 데이터가 저장된 컬렉션 이름
  });

module.exports = mongoose.model('BusLocation', busLocationSchema);
