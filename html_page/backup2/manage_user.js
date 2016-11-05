
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
			var filename = location.href.substring(location.href.lastIndexOf('/')+1);
			if (filename!='main.html'){
				location.href='main.html';
				return;				
			} 
		  } else {
			// No user is signed in.
			console.log("onAuthStateChanged : no user");
		  }
		});

	  function getFirebase() {
		  return firebase.database();
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
			location.href='index.html';
		}, function(error) {
		// An error happened.
		});

	  }
  