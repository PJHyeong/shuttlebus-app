const express = require('express');
const Comment = require('../models/Comment'); 
const router = express.Router();

// 댓글 추가 API
router.post('/comments', async (req, res) => {
    const { announcementId, userId, content } = req.body;

    try {
        const comment = new Comment({ announcementId, userId, content });
        await comment.save();
        res.status(201).json(comment);
    } catch (error) {
        res.status(500).json({ message: '댓글 추가 실패', error });
    }
});


module.exports = router;