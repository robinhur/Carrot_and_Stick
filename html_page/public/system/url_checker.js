console.log("open url_checker");

$.urlParam = function(name) {
    var results = new RegExp('[\?&]' + name + '=([^&#]*)').exec(window.location.href);
    if (results==null){
       return null;
    }
    else{		
       return results[1] || 0;
    }
}
var destination = $.urlParam('destination');
if (isNull(destination))
	destination = "login.html";
console.log("url_checker : " + destination);

function get_destination() {
	return destination;
}

function set_destination(new_dest) {
	destination = new_dest;
	console.log("url_checker : set_destination : " + destination);
}

function get_parameter(name) {
	return $.urlParam(name);
}

function isNull(obj) {
	return (typeof obj != "undefined" && obj != null && obj != "") ? false : true;
}