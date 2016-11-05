console.log("open html_loader");

/* destination html 읽어오기 */
var destination = get_destination();
var rawFile;

if (window.XMLHttpRequest) {
	// code for modern browsers
	rawFile = new XMLHttpRequest();
 } else {
	// code for old IE browsers
	rawFile = new ActiveXObject("Microsoft.XMLHTTP");
}

console.log("load : " + "./"+destination.substring(0,destination.length-5));

rawFile.open("GET", "./"+destination.substring(0,destination.length-5), false);
rawFile.onreadystatechange = function ()
{
	if(rawFile.readyState === 4)
	{
		if(rawFile.status === 200 || rawFile.status == 0)
		{
			var allText = rawFile.responseText;
			console.log(allText);
			document.body.innerHTML = allText;
		}
	}
}
rawFile.send(null);	