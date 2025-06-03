// 파일 경로: routes/commentRoutes.js

const express = require('express');
const Comment = require('../models/Comment');
const router = express.Router();


router.post('/comments', async (req, res) => {
  const { announcementId, userId, content } = req.body;

  try {
    // userId는 이제 String 타입(닉네임)이므로 그대로 사용
    const comment = new Comment({ announcementId, userId, content });
    await comment.save();

    // 저장된 comment 객체를 가공하여 응답
    return res.status(201).json({
      id:        comment._id,
      userId:    comment.userId,
      content:   comment.content,
      createdAt: comment.createdAt
    });
  } catch (error) {
    console.error('댓글 추가 실패:', error);
    return res.status(500).json({ message: '댓글 추가 실패', error });
  }
});



router.get('/comments', async (req, res) => {
  const { announcementId } = req.query;

  try {
    const comments = await Comment.find({ announcementId }).sort({ createdAt: 1 });
    const result = comments.map(c => ({
      id:        c._id,
      userId:    c.userId,
      content:   c.content,
      createdAt: c.createdAt
    }));
    return res.status(200).json(result);
  } catch (err) {
    console.error('댓글 조회 실패:', err);
    return res.status(500).json({ message: '댓글 조회 실패', error: err });
  }
});


module.exports = router;
