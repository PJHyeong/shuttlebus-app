const jwt = require('jsonwebtoken');
const User = require('../models/User');



async function authenticationToken(req, res, next) {
    const token = req.headers['authorization']?.split(' ')[1]; // Bearer 토큰에서 실제 토큰 추출
    if (!token) return res.sendStatus(401); // 토큰이 없으면 인증 실패

    try {
        const decoded = jwt.verify(token, process.env.JWT_SECRET); // 토큰 검증
        const user= await User.findById(decoded.id); // 사용자 정보 조회
        if (!user) return res.sendStatus(403); // 사용자 정보가 없으면 인증 실패
        req.user = user;
        next();
    } catch {
        res.sendStatus(403); // 토큰 검증 실패
    }
}

function requireAdmin(req, res, next) {
    if(!req.user?.isAdmin) return res.status(403).json({ message: "관리자 권한이 필요합니다." });
    next();
}


module.exports = { authenticationToken, requireAdmin };