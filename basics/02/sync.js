// 순서대로 실행하기  (결과 비교: 02\results\sync.js)
// 비동기 처리(시간 걸리는 작업을 나중에 처리 - 동시 작업 X)
function displayA() {
  console.log('A');
}
function displayB(callback) { // callback 함수를 인자로 받음
  setTimeout(() => {
    console.log('B');  
    callback();
  }, 2000);
}
function displayC() {
  console.log('C');
}
displayA();
displayB(displayC);
