const express = require('express');
const Notice = require('../models/Notice');
const router = express.Router();
const { authenticationToken, requireAdmin } = require('../middlewares/authMiddleware'); // 인증 미들웨어



//모든 사용자 가능
router.get('/', async (req, res) => {   // 공지사항 목록 조회
    const notices = await Notice.find().sort({ createdAt: -1 }); // 최신 공지사항이 위에 오도록 정렬
    res.send(notices);
});



//관리자만 가능
router.post('/', authenticationToken, requireAdmin, async (req, res) => {  // 공지사항 등록
    const notice = new Notice(req.body);
    await notice.save(); // 공지사항 저장
    res.send({message: '공지사항이 등록되었습니다.'});
});

router.delete('/:id', authenticationToken, requireAdmin, async (req, res) => {  // 공지사항 삭제
    await Notice.findByIdAndDelete(req.params.id); // 공지사항 ID로 삭제
    res.send({message: '공지사항이 삭제되었습니다.'});
});


module.exports = router;


//특정 공지사항 조회 기능 추가 고민