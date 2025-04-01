from pymongo import MongoClient
from gps import gps, WATCH_ENABLE, WATCH_NEWSTYLE
import time

# MongoDB Atlas 연결 설정 <<주의!!! <> 는 지우고 id pw url 입력해야함. 
client = MongoClient('mongodb+srv://<ID>:<PW>@<URL>')
db = client['gps']  # 데이터베이스 이름
collection = db['gps']  # 컬렉션 이름

# GPS 설정
gpsd = gps(mode=WATCH_ENABLE | WATCH_NEWSTYLE)

print("GPS 데이터 수집 및 MongoDB 전송 시작...")

try:
    while True:
        # GPS 데이터 읽기
        report = gpsd.next()
        if report['class'] == 'TPV':
            latitude = getattr(report, 'lat', None)
            longitude = getattr(report, 'lon', None)
            timestamp = getattr(report, 'time', None)

            # 유효한 데이터만 처리
            if latitude is not None and longitude is not None and timestamp is not None:
                # MongoDB에 저장할 문서 생성
                doc = {
                    "latitude": latitude,
                    "longitude": longitude,
                    "timestamp": timestamp
                }

                # MongoDB에 문서 삽입
                collection.insert_one(doc)
                print(f"데이터 저장: {doc}")

                time.sleep(10)

except KeyboardInterrupt:
    print("GPS 데이터 수집 및 전송 종료")

except Exception as e:
    print(f"오류 발생: {e}")
