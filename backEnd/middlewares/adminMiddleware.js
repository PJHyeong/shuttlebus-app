const jwt = require("jsonwebtoken");

const adminMiddleware = (req, res, next) => {
    const token = req.headers.authorization?.split(" ")[1]; // Bearer <token>
    if (!token) return res.status(403).json({ message: "권한이 없습니다." });

    try {
        const decoded = jwt.verify(token, process.env.JWT_SECRET);
        if (decoded.role !== 'admin') {
            return res.status(403).json({ message: "관리자 권한이 필요합니다." });
        }
        req.user = decoded; // 요청에 사용자 정보 추가
        next();
    } catch (error) {
        res.status(401).json({ message: "유효하지 않은 토큰입니다." });
    }
};

module.exports = adminMiddleware;