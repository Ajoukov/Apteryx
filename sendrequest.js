/*var originalOpen = XMLHttpRequest.prototype.open;
XMLHttpRequest.prototype.open = function(method, url) {
    var token = window.localStorage.token;
    if (token) {
        this.setRequestHeader('token', token);
    }
    originalOpen.apply(this, arguments);
};
*/

function clearToken() {
    document.cookie = "token=blah; ; path=/login; expires=Thu, 01 Jan 1970 00:00:00 GMT";
/*    var cookies = document.cookie.split(";");

    for (var i = 0; i < cookies.length; i++) {
        var cookie = cookies[i];
        var eqPos = cookie.indexOf("=");
        var name = eqPos > -1 ? cookie.substr(0, eqPos) : cookie;
        document.cookie = name + "=;expires=Thu, 01 Jan 1970 00:00:00 GMT;path=/;";
    }
*/
}

function setToken(token) {
	clearToken();
	let path = "/";
	let expiration = "expires=Thu, 18 Dec 2033 12:00:00 UTC";
	let cookie = "token=" + token + "; path=" + path + "; expiration=" + expiration;
//	console.log("TOKEN:" + token);
	document.cookie = cookie;
}


function sendRequest(url) {
	window.location.replace(url);
	return;
/*

	let token = window.localStorage.token;
	let headers = new Headers();
	headers.append("token", token);
	fetch(url + "?token=" + token, {
	method: 'GET',
	headers: headers
	})
	.then(response => {
		if (response.ok) {
			// If the request is successful, navigate to the new page
			window.location.href = url;
		} else {
			// Handle error if needed
			console.error('Failed to fetch page:', response.statusText);
		}
	}).catch(error => {
		// Handle network errors
		console.error('Network error:', error);
	});
*/
}
