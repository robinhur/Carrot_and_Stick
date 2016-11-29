console.log("open page_config");

var page_config = {
	//destination	//layout 					//menu
	"login.html":	{"layout":"layout_0.html",	"menu":"0.html"					}, 
	"main.html":	{"layout":"layout_4.html",	"menu":"menu_main.html"		}, 
	"typing.html":	{"layout":"layout_4.html",	"menu":"menu_main.html"		}, 
	"setting.html":	{"layout":"layout_4.html",	"menu":"menu_setting.html"	}
};

var layout_file=page_config[get_destination()]["layout"];
var menu_file=page_config[get_destination()]["menu"];

console.log("layout_file = " + layout_file);
console.log("menu_file = " + menu_file);
