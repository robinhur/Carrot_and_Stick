console.log("html_loader.js");

loadCSS = function(location) {
	var cssLink = $("<link rel='stylesheet' type='text/css' href='"+location+"'>");
	$("head").append(cssLink); 
};

function loadSCRIPT(location) {
	$.getScript("./app/controller/" + location.substring(0,location.length-5) + ".js")
	.done(function(script, textStatus) {
	  //console.log("script load done");
	})
	.fail(function(jqxhr, settings, exception) {
	  console.log("script load failed!!!");
	//$( "div.log" ).text( "Triggered ajaxError handler." );
	});  
}

function loadHTML(location, target) {
	$(target).load(location);
}

function loadALL(destination, target) {
	
	console.log("loadALL = " + destination);
	
	//일단 HTML + SCRIPT만
	//CSS???
	
	$.when(
		loadCSS("./asset/css/"+destination.substring(0,destination.length-5) + ".css")
	).done(function() {
		//console.log("css found!!!");
		$.when(
			loadHTML("./app/view/"+destination, target)
		).done(function() {
			//console.log("html done!!!");
			loadSCRIPT(destination);
		});
	});  
	
}

$("body").load( "./layout/" + layout_file, function() {
	
	if (layout_file == "layout_0.html"){
		$('body').load("./app/view/"+destination);
		return;
	}
	
	$.when(
		loadCSS("./asset/css/common.css"),
    	loadALL("top_header.html","#top_header"),
    	loadHTML("./menu/"+menu_file,"#left_menu"),
    	loadHTML("./app/view/footer.html","#footer"),
		$.Deferred(function( deferred ){
			$( deferred.resolve );
		})
	).done(function(){

		loadALL(destination, "#main_content");
		console.log("done");

	});
	
});