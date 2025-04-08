// Node.js 예제: Express와 MongoDB 드라이버 사용
const express = require('express');
const { MongoClient } = require('mongodb');

const app = express();
const port = 3000;

// MongoDB 연결
const url = "mongodb://<username>:<password>@<cluster-url>/<dbname>?retryWrites=true&w=majority";
const client = new MongoClient(url);

async function connectDB() {
  try {
    await client.connect();
    console.log("MongoDB에 연결됨");
  } catch (error) {
    console.error("DB 연결 오류:", error);
  }
}

app.get('/gps', async (req, res) => {
  try {
    const db = client.db("<dbname>");        // 데이터베이스 이름
    const collection = db.collection("<collection>");  // 컬렉션 이름
    const gpsData = await collection.find().toArray();
    res.json(gpsData);
  } catch (error) {
    console.error(error);
    res.status(500).send("서버에서 오류 발생");
  }
});

app.listen(port, () => {
  console.log(`서버가 포트 ${port}에서 동작중`);
  connectDB();
});
