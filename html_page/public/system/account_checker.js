console.log("open account_checker");

 // Initialize Firebase
var config = {
	apiKey: "AIzaSyCgQHDJ5lBrYybWdxFSube7QVC7sPohVzU",
	authDomain: "carotandstick-35d0e.firebaseapp.com",
	databaseURL: "https://carotandstick-35d0e.firebaseio.com",
	storageBucket: "carotandstick-35d0e.appspot.com",
	messagingSenderId: "93332277132"
};
firebase.initializeApp(config);
console.log("initializeApp() called");

firebase.auth().onAuthStateChanged(function(user) {
	if (user) {
		// User is signed in.
		console.log("onAuthStateChanged : signed in");
		firebase_user = user;
		if (get_destination() == "login.html")
			location.href="?destination=main.html";
		else
			get_user_info();
	} else {
		// No user is signed in.
		console.log("onAuthStateChanged : no user");
		if (get_destination() != "login.html"){
			console.log("onAuthStateChanged : no user : return to login");
			location.href=window.location.href.split('?')[0];
		}
	}
});

function add_credit() {

	console.log("add_credit() called");

	var add_value = parseInt(document.getElementById("credit_num").value);
	
	if (parseInt(document.getElementById("user_credit").innerText)+add_value < 0){
		document.getElementById("credit_num").value = "";
	} else {
		
		var now_time = Math.floor(Date.now()/1000);
		var delta = Math.abs(add_value);
		var updown, content;
		if ((add_value) > 0){
			updown = '+';
			content = 'Web 적립';
		}
		else{
			updown = '-';
			content = 'Web 차감';
		}
		
		console.log("log!! : " + now_time + " : " + updown + ":" + delta + ":" + content);
		
		firebase.database().ref('/logs/'+ firebase.auth().currentUser.uid).child(now_time).update({
			
			timestamp: now_time,
			updown: updown,
			delta: delta,
			content: content
			
		});
		
		var result = parseInt(document.getElementById("user_credit").innerText)+add_value;
		firebase.database().ref('/users/' + firebase.auth().currentUser.uid).update({
			credit: result
		});
		document.getElementById("credit_num").value = "";

	}

}

function get_user_info() {
	
	console.log("get_user_info() called");
	
	firebase.database().ref('/users/' + firebase.auth().currentUser.uid).once('value').then(function(snapshot) {
	  document.getElementById("user_email").innerText = snapshot.val().email;
	  document.getElementById("user_name").innerText = snapshot.val().name;
	  document.getElementById("user_credit").innerText = snapshot.val().credit;
	  // Windows APP 가동!!!
	  eval("window.external.getUserInfo(snapshot.val().email, snapshot.val().name, snapshot.val().credit);");
	  console.log("getUserInfo called");
	});
			
	var creditRef = firebase.database().ref('/users/' + firebase.auth().currentUser.uid);
	creditRef.on('child_changed', function(data) {
		if (data.key == 'credit')
			document.getElementById("user_credit").innerText = data.val();
	});
	
}

function login_user() {

	console.log("login_user() called");

	var email = document.getElementById("email").value;
	var password = document.getElementById("password").value;

	firebase.auth().signInWithEmailAndPassword(email, password).catch(function(error) {
		// Handle Errors here.
		var errorCode = error.code;
		var errorMessage = error.message;
		console.log("login_user : errorCode = " + errorCode);
		console.log("login_user : errorMessage = " + errorMessage);
			
		alert(errorMessage);		
	});

}

function logout_user() {
	console.log("logout_user() called");

	firebase.auth().signOut().then(function() {
	// Sign-out successful.
		location.href=window.location.href.split('?')[0];
	}, function(error) {
	// An error happened.
	});

}