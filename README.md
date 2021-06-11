## 앱 브릿지 정의

> [App to Web]
+ 뒤로가기 스크립트 호출
   + backKey() 호출

> [Web to App]
+ 앱 종료 브릿지 함수
   + closeApp()

+ 토스트팝업 브릿지 함수
   + toastPopup(msg)
     - msg : 토스트팝업 호출시 노출될 메시지

+ 텍스트복사 브릿지 함수
   + clipboardCopy(msg)
     - msg : 복사할 메시지
+ 유심 핸드폰 가져오기 브릿지 함수
   + getPhoneNumber(callbackFnName)
     - callbackFnName : 리턴으로 실행될 함수명 - 인자로 핸드폰번호 넘김
+ OCR 촬영 브릿지 함수
   + callOcrCamera(callbackFnName, token)
     - callbackFnName : 리턴으로 실행될 함수명 - 인자로 서버응답 json 넘김
     - token : 앱 -> 백엔드 통신시 헤더에 세팅할 토큰값
+ 톡톡로그인 정보 가져오기 브릿지 함수
   + getLoginInfo(callbackFnName)
     - callbackFnName : 리턴으로 실행될 함수명 - 인자로 로그인응답 json 넘김
---
## javascript 작성 예시
> ### Android
```javascript
const bridge = window.welfare;
function closeApp(){
	try{
		bridge.closeApp();
	}
	catch(e){
     		console.error(e);
   	}
}
function toastPopup(msg){
	try{
		bridge.toastPopup(msg);
	}
	catch(e){
     		console.error(e);
   	}
}
```
> ### IOS
```javascript
const bridge = window.webkit.messageHandlers;
function closeApp(){
	const params = {};
	try {
		bridge.closeApp.postMessage(params);
	}catch(e){
		console.error(e);
	}
}
function toastPopup(msg){
	const params = {'msg' : msg};
	try {
		bridge.toastPopup.postMessage(params);
	}catch(e){
		console.error(e);
	}
}
```
