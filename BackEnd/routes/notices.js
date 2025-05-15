const express = require('express');
const Notice = require('../models/Notice');
const router = express.Router();
const adminMiddleware = require('../middlewares/adminMiddleware'); // 인증 미들웨어


// 공지사항 목록 조회 (모두 가능)
router.get("/", async (req, res) => {
    try {
        const notices = await Notice.find();
        res.json(notices);
    } catch (error) {
        res.status(500).json({ message: "공지사항을 불러오는데 실패했습니다." });
    }
});




// 공지사항 추가 (관리자만 가능)
router.post("/", adminMiddleware, async (req, res) => {
    try {
        const { title, content } = req.body;
        const notice = new Notice({ title, content });
        await notice.save();
        res.status(201).json(notice);
    } catch (error) {
        res.status(500).json({ message: "공지사항 추가 실패" });
    }
});

// 공지사항 삭제 (관리자만 가능)
router.delete("/:id", adminMiddleware, async (req, res) => {
    const { id } = req.params;

    try {
        const Notice = await Notice.findByIdAndDelete(id);
        if (!Notice) {
            return res.status(404).json({ message: "공지사항이 존재하지 않습니다." });
        }
        res.json({ message: "공지사항이 삭제되었습니다." });
    } catch (error) {
        res.status(500).json({ message: "공지사항 삭제 실패" });
    }
});


//특정 공지사항 조회 기능 추가 고민



module.exports = router;