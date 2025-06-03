const mongoose = require("mongoose");
const bcrypt = require("bcryptjs");



const userSchema = new mongoose.Schema({
    studentid: { type: String, required: true, unique: true },
    password: { type: String, required: true },
    name: { type: String, required: true },
    role: { type: String, enum: ['user', 'admin'], default: 'user' }, // 'user' 또는 'admin' 관리자 역할 추가
    createdAt: { type: Date, default: Date.now },
 
});


//bcrypt 라이브러리 사용
//비밀번호 암호화 - 회원가입 시 비밀번호를 해시화하여 저장
userSchema.pre("save", async function(next) {
  if (!this.isModified("password")) return next();
  this.password = await bcrypt.hash(this.password, 10);
  next();
});

//비밀번호 비교 - 로그인 시 입력된 비밀번호와 DB에 저장된 해시된 비밀번호를 비교
userSchema.methods.comparePassword = async function(enteredPassword) {
  return await bcrypt.compare(enteredPassword, this.password);
};

module.exports = mongoose.model("User", userSchema);
