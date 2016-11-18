console.log("open html_loader");

 /**
  * function to load a given css file 
  */ 
 loadCSS = function(href) {
     var cssLink = $("<link rel='stylesheet' type='text/css' href='"+href+"'>");
     $("head").append(cssLink); 
 };

/**
 * function to load a given js file 
 */ 
 loadJS = function(src) {
     var jsLink = $("<script type='text/javascript' src='"+src+"'>");
     $("head").append(jsLink); 
 }; 
  
 // load the css file 
 //loadCSS("style.css");

 // load the js file 
 //loadJS("one.js");

function load_failed() {
	alert("비정상 접근입니다");
	location.href=window.location.href.split('?')[0];
}

/* destination의 html 읽어오기 */
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

console.log("file load : " + "./"+destination.substring(0,destination.length-5));

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
			load_stylesheet();
			load_javascript();
		} else if (rawFile.status == 404)
		{	
			load_failed();
		}
	}
}
rawFile.send(null);	

function load_stylesheet() {
	console.log("load stylesheet");
	
	var ul = document.getElementById("style");
	var items = ul.getElementsByTagName("li");
	for (var i = 0; i < items.length; ++i) {		
		console.log("script load : " + "./" + destination + "/" + items[i].innerHTML);
		
		loadCSS("./" + destination + "/" + items[i].innerHTML);
		//$.getStylesheet("./" + destination + "/" + items[i].innerHTML)
		//.done(function(script, textStatus) {
		//  console.log("style load done");
		//})
		//.fail(function(jqxhr, settings, exception) {
		//  console.log("style load failed!!!");
		//$( "div.log" ).text( "Triggered ajaxError handler." );
		//});  
	}
	
	document.getElementById("style").remove();
}

function load_javascript() {
	console.log("load javascript");
	
	var ul = document.getElementById("script");
	var items = ul.getElementsByTagName("li");
	for (var i = 0; i < items.length; ++i) {		
		console.log("script load : " + "./" + destination + "/" + items[i].innerHTML);
		
		$.getScript("./" + destination + "/" + items[i].innerHTML)
		.done(function(script, textStatus) {
		  //console.log("script load done");
		})
		.fail(function(jqxhr, settings, exception) {
		  console.log("script load failed!!!");
		//$( "div.log" ).text( "Triggered ajaxError handler." );
		});  
	}
	
	document.getElementById("script").remove();
}
