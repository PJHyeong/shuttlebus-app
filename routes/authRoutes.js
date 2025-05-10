const express = require('express');
const User = require('../models/User');
const jwt = require('jsonwebtoken');
const bcrypt = require('bcryptjs');
const router = express.Router();

//회원가입 
router.post("/register", async (req, res) => {
  try {
      const { studentid, password, name } = req.body;
      const existingUser = await User.findOne({ studentid });

      if (existingUser) return res.status(400).json({ message: "이미 가입된 이메일입니다." });

      const user = new User({ studentid, password, name });
      await user.save();
      
      res.status(201).json({ message: "회원가입 성공" });
  } catch (error) {
      res.status(500).json({ message: "회원가입 실패" });
  }
});
//로그인
router.post("/login", async (req, res) => {
  try {
      const { studentid, password } = req.body;
      const user = await User.findOne({ studentid });

      if (!user || !(await user.comparePassword(password))) {   
          return res.status(400).json({ message: "이메일 또는 비밀번호가 잘못되었습니다." });
      }

      // JWT 토큰 생성
      const token = jwt.sign({ id: user._id }, process.env.JWT_SECRET, { expiresIn: "7d" }); // 7일 동안 유효한 토큰 생성(유효기간 설정할 지 말지)

      res.json({ token, user: { id: user._id, name: user.name, studentid: user.studentid } });
  } catch (error) {
      res.status(500).json({ message: "로그인 실패" });
  }
});

module.exports = router;