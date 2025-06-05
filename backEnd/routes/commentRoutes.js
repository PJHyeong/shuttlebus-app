const express = require('express');
const Comment = require('../models/Comment');
const router = express.Router();

// 댓글 추가 라우터
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


// 댓글 삭제 라우터
router.delete('/comments/:id', async (req, res) => {
  const commentId = req.params.id;
  const { userId, userRole } = req.body; // userRole: 'admin'이면 관리자

  try {
    const comment = await Comment.findById(commentId);
    if (!comment) {
      return res.status(404).json({ message: '댓글을 찾을 수 없습니다.' });
    }

    // 작성자 본인 또는 관리자만 삭제 가능
    if (comment.userId !== userId && userRole !== 'admin') {
      return res.status(403).json({ message: '삭제 권한이 없습니다.' });
    }

    await Comment.findByIdAndDelete(commentId);
    return res.status(200).json({ message: '댓글이 삭제되었습니다.' });
  } catch (error) {
    console.error('댓글 삭제 실패:', error);
    return res.status(500).json({ message: '댓글 삭제 실패', error });
  }
});

// 댓글 목록 조회 라우터
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

router.patch('/comments/:id', async (req, res) => {
  const commentId = req.params.id;
  const { userId, userRole, content } = req.body; // userRole: 'admin'이면 관리자

  try {
    const comment = await Comment.findById(commentId);
    if (!comment) {
      return res.status(404).json({ message: '댓글을 찾을 수 없습니다.' });
    }

    // 작성자 본인 또는 관리자만 수정 가능
    if (comment.userId !== userId && userRole !== 'admin') {
      return res.status(403).json({ message: '수정 권한이 없습니다.' });
    }

    comment.content = content;
    await comment.save();

    return res.status(200).json({
      id:        comment._id,
      userId:    comment.userId,
      content:   comment.content,
      createdAt: comment.createdAt
    });
  } catch (error) {
    console.error('댓글 수정 실패:', error);
    return res.status(500).json({ message: '댓글 수정 실패', error });
  }
});


module.exports = router;