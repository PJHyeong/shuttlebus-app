const express = require("express");
const User = require("../models/User");
const jwt = require("jsonwebtoken");
const bcrypt = require("bcryptjs");

const router = express.Router();

// 회원가입
router.post("/register", async (req, res) => {
    try {
        let { studentid, password, name, role } = req.body; // role을 추가
        if(!role) role = "user"; // 기본 역할을 "user"로 설정

        const existingUser = await User.findOne({ studentid });
        if (existingUser) return res.status(400).json({ message: "이미 가입된 이메일입니다." });

        const user = new User({ studentid, password, name, role }); // role 저장
        await user.save();
        
        res.status(201).json({ message: "회원가입 성공!" });
    } catch (error) {
        console.error(error);
        res.status(500).json({ message: "회원가입 실패" });
    }
});

// 로그인
router.post("/login", async (req, res) => {
    try {
        const { studentid, password } = req.body;
        const user = await User.findOne({ studentid });

        if (!user || !(await user.comparePassword(password))) {
            return res.status(400).json({ message: "이메일 또는 비밀번호가 잘못되었습니다." });
        }

        // JWT 토큰 생성 및 사용자 역할 포함
        const token = jwt.sign({ id: user._id, role: user.role }, process.env.JWT_SECRET, { expiresIn: "7d" });

        res.json({ 
            token, 
            user: { 
                id: user._id, 
                name: user.name, 
                studentid: user.studentid, 
                role: user.role // 사용자 역할 포함
            } 
        });
    } catch (error) {
        res.status(500).json({ message: "로그인 실패" });
    }
});

module.exports = router;