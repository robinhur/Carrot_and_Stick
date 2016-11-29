
var http = new Http();
http.setHttp();

function game_end() {
	alert("게임 끝!!!");
}

function load_array() {
	
	var items;
	
	$.get("./app/model/content_typing/" + get_parameter("content") + ".cas", function(data) {
		items = data.split('\n');
	});
	
	console.log(items.length);
	return items;
}

function Http(){
	this.arrStrs = load_array();
	/*this.arrStrs = new Array(
"계절이 지나가는 하늘에는",
"가을로 가득 차 있습니다.",
"",
"나는 아무 걱정도 없이",
"가을 속의 별들을 다 헬 듯합니다.",
"",
"가슴속에 하나 둘 새겨지는 별을",
"이제 다 못 헤는 것은",
"쉬이 아침이 오는 까닭이요,",
"내일 밤이 남은 까닭이요,",
"아직 나의 청춘이 다하지 않은 까닭입니다.");*/
	this.exString="";
	this.inputString="";
	this.speedCur=0;
	this.speedMax=0;
	
	this.accuracyTotal=0; //정확도
	this.accuracyCur=0; //정확도

	this.lengthTotal=0; //전체 글자수
	this.lengthTotalTrue=0; //전체 맞은 글자수
	this.lengthCurTrue=0; //현재 맞은 글자수
	
	this.timerInt;
	this.timerStopped=true;
	this.timerSec=0;
	this.idx=0; //this.arrStrs.length)

	this.setHttp=function(){
		/*** 문장 보여주기 ***/
		this.exString = this.arrStrs[this.idx];
		
		waitString1 = " - ";
		waitString2 = " - ";
		if (this.idx+1 < this.arrStrs.length) 
			waitString1 = this.arrStrs[this.idx+1];
		else if (this.idx+1 == this.arrStrs.length)
			waitString1 = "--- 끝 ---";
		
		if (this.idx+2 < this.arrStrs.length) 
			waitString2 = this.arrStrs[this.idx+2];
		else if (this.idx+2 == this.arrStrs.length)
			waitString2 = "--- 끝 ---";
		
		/*** 문장/입력 객체 가져오기***/
		var objInputString = this.obj("httpInputString");
		var objExString = this.obj("exString");
		var objWaitString1 = this.obj("exString2");
		var objWaitString2 = this.obj("exString3");
		
		/*** 타이머 초기화 ***/
		this.timerStopped=true;
		this.timerInt=window.clearInterval(http.timerInt);
		this.timerSec=0;

		/*** 문장 초기화 ***/
		objExString.innerHTML=this.exString;
		objWaitString1.innerHTML=waitString1;
		objWaitString2.innerHTML=waitString2;
		
		objInputString.value="";
		objInputString.focus();
	}
	this.keyUp=function(){
		var objInputString = this.obj("httpInputString");
		
		this.chkMiss();

		/*** 다른 문장으로 넘김 ***/
		if(this.exString.length<=objInputString.value.length){
			
			/*** 정확도 계산/출력 ***/
			this.lengthTotal += this.exString.length;
			this.lengthTotalTrue += this.lengthCurTrue;
			
			this.accuracyCur = Math.floor(this.lengthCurTrue/this.exString.length*100);
			this.accuracyTotal = Math.floor(this.lengthTotalTrue/this.lengthTotal*100);
			this.obj("prnAccuracyCur").innerHTML=this.accuracyCur;
			this.obj("prnAccuracyTotal").innerHTML=this.accuracyTotal;
			this.obj("barAccuracyCur").style.width=this.accuracyCur+"%";
			this.obj("barAccuracyTotal").style.width=this.accuracyTotal+"%";
			
			/*** 속도 계산/출력 ***/
			this.speedCur = Math.floor(this.lengthCurTrue / this.timerSec * 6000);
			if(this.speedMax<this.speedCur)this.speedMax = this.speedCur;
			this.obj("prnSpeedCur").innerHTML = this.speedCur;
			this.obj("prnSpeedMax").innerHTML = this.speedMax;
			this.obj("barSpeedCur").style.width=this.speedCur/10+"%";
			this.obj("barSpeedMax").style.width=this.speedMax/10+"%";

			this.idx++;
			if (this.idx == this.arrStrs.length)
				game_end();
			else
				this.setHttp();
			return false;
		}
		return true;
	}
	this.obj=function(id){
		return document.getElementById(id);
	}
	this.chkMiss=function(){
		var result="";
		this.lengthCurTrue=0;

		var objInputString = this.obj("httpInputString");
		this.inputString = objInputString.value;

		for(var i=0;i<this.exString.length;i++){
			if(this.exString.substring(i,i+1)!=this.inputString.substring(i,i+1) && i<this.inputString.length)
				result+="<font color=red>"+this.exString.substring(i,i+1)+"</font>";
			else{
				result+=this.exString.substring(i,i+1);
				this.lengthCurTrue++;
			}
		}
		var objExString = this.obj("exString");
		objExString.innerHTML=result;
	}
	this.chkTime=function(){
		if(this.timerStopped){
			this.timerStopped=false;
			this.timerSec=0;
			this.timerInt=window.setInterval("http.addSec()",10);
		}
	}
	this.addSec=function(){
		this.timerSec++;
	}
}