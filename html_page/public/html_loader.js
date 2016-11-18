console.log("open html_loader");

function load_failed() {
	alert("비정상 접근입니다");
	location.href=window.location.href.split('?')[0];
}



/* destination html 읽어오기 */
var destination;
var rawFile;

if (window.XMLHttpRequest) {
	// code for modern browsers
	rawFile = new XMLHttpRequest();
 } else {
	// code for old IE browsers
	rawFile = new ActiveXObject("Microsoft.XMLHTTP");
}

if (destination.length < 5)
	load_failed();

console.log("load : " + "./"+destination.substring(0,destination.length-5));

rawFile.open("GET", "./"+destination.substring(0,destination.length-5), true);
rawFile.onprogress = function(e) {
	console.log("onProgress : 불러 오는 중" );
}
rawFile.onerror = function() {
	alert("rawFile.onerror");
}
rawFile.onreadystatechange = function ()
{
	if(rawFile.readyState === 4)
	{
		if(rawFile.status === 200)
		{
			var allText = rawFile.responseText;
			console.log("allText.length : " + allText.length);
			document.body.innerHTML = allText;
		} else if (rawFile.status == 404)
		{	
			load_failed();
		}
	}
}
rawFile.send(null);	
