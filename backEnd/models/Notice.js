const mongoose = require('mongoose');

const noticeSchema = new mongoose.Schema({
    title: String,
    content: String,
    createdAt: { type: Date, default: Date.now}
    },{
  toJSON: {
    transform(doc, ret) {
      const d = new Date(ret.createdAt);
      const pad = n => String(n).padStart(2, '0');
      const year  = d.getFullYear();
      const month = pad(d.getMonth() + 1);
      const day   = pad(d.getDate());
      const hour  = pad(d.getHours());
      const min   = pad(d.getMinutes());
      ret.createdAt = `${year}-${month}-${day} ${hour}:${min}`;  // e.g. "2025-05-23 12:58"
      return ret;
    }
  }
});

    module.exports = mongoose.model('Notice', noticeSchema); 