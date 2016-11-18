console.log("open url_checker");

var destination = "login.html";
if (location.href.lastIndexOf('?') != -1) {
	var param_destination = location.href.substring(location.href.lastIndexOf('?')+1, 	location.href.length);
	if (param_destination.lastIndexOf('=') != -1) {
		destination = param_destination.substring(param_destination.lastIndexOf('=')+1,param_destination.length);
	} else {
		//// param이 잘못됨....
	}
}

console.log("url_checker : " + destination);

function get_destination() {
	console.log("url_checker : get_destination : " + destination);
	return destination;
}

function set_destination(new_dest) {
	destination = new_dest;
	console.log("url_checker : set_destination : " + destination);
}