function sendRequest(url, token) {
	fetch(url + "?token=" + token, {
	method: 'GET',
	}).then(response => {
		if (response.ok) {
			// If the request is successful, navigate to the new page
			window.location.href = url + "?token=" + token;
		} else {
			// Handle error if needed
			console.error('Failed to fetch page:', response.statusText);
		}
	}).catch(error => {
		// Handle network errors
		console.error('Network error:', error);
	});
}
