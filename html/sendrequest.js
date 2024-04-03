function sendRequest(method, path, content, callback, ecallback) {
	if (method == null) {
		method = "PUT";
	}
	if (path == null) {
		path = "/";
	}
	if (content == null) {
		content = "";
	}
	if (callback == null) {
		callback = function() {};
	}
	if (ecallback == null) {
		ecallback = function() {console.log("Request failed.")};
	}
        let xhr = new XMLHttpRequest();
        xhr.onreadystatechange = function () {
                if (xhr.readyState === XMLHttpRequest.DONE) { // Check if request is complete
                    if (xhr.status === 200) { // Check if request was successful
			callback(xhr);
                    } else {
			ecallback(xhr);
                    }
                }
            };
        xhr.open(method, path, true); // Open a GET request to a URL
        xhr.send(content); // Send the request
}


function logout() {
	let callback = function(xhr) {
		window.location.href = "/login";
	}
	sendRequest("PUT", "/logout", "", callback);
}


function clearToken() {
    document.cookie = "token=blah; ; path=/login; expires=Thu, 01 Jan 1970 00:00:00 GMT";
}

function setToken(token) {
	clearToken();
	let path = "/";
	let expiration = "expires=Thu, 18 Dec 2033 12:00:00 UTC";
	let cookie = "token=" + token + "; path=" + path + "; expiration=" + expiration;
	document.cookie = cookie;
}
