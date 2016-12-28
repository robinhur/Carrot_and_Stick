console.log("open top_header");

div2Resize();
window.addEventListener('resize', div2Resize);

function div2Resize() {
	var div2 = document.getElementById('left_menu');
	div2.style.height = window.innerHeight - 150 + 'px';
	var div2 = document.getElementById('main_content');
	div2.style.height = window.innerHeight - 150 + 'px';
}