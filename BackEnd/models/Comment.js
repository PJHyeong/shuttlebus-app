const mongoose = require("mongoose");
const Schema = mongoose.Schema;

const CommentSchema = new Schema({
  announcementId: { type: mongoose.Types.ObjectId, ref: "Announcement", required: true },
  userId:         { type: String, required: true },   // ObjectId → String(닉네임) 으로 변경
  content:        { type: String, required: true },
  createdAt:      { type: Date,   default: Date.now }
});

module.exports = mongoose.model("Comment", CommentSchema);
