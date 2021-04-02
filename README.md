## 안드로이드 앱 브릿지 정의

> [App to Web]
+ 뒤로가기 스크립트 호출
   backKey() 호출

> [Web to App]
+ 앱 종료 브릿지 함수
   closeApp()

+ 토스트팝업 브릿지 함수
   toastPopup(msg)
   - msg : 토스트팝업 호출시 노출될 메시지

+ 텍스트복사 브릿지 함수
   clipboardCopy(msg)
   - msg : 복사할 메시지
-------------------------------------
## 스크립트파일 작성 예시
```javascript
const bridge = window.welfare;
function closeApp()
{
	try{
		bridge.closeApp();
	}
	catch(e){
     		console.error(e);
   	}
}
function toastPopup(msg)
{
	try{
		bridge.toastPopup(msg);
	}
	catch(e){
     		console.error(e);
   	}
}
```
