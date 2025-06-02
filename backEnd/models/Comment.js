const mongoose = require('mongoose');

const CommentSchema = new mongoose.Schema({
    announcementId: { type: mongoose.Schema.Types.ObjectId, ref: 'Announcement', required: true }, 
    userId: { type: mongoose.Schema.Types.ObjectId, ref: 'User', required: true }, 
    content: { type: String, required: true },
    createdAt: { type: Date, default: Date.now }
});

module.exports = mongoose.model('Comment', CommentSchema);