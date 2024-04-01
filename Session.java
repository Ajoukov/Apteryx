public class Session {
	private long login_time;
	private int token;
	private String username;

	public Session(int token) {
		this.login_time = System.currentTimeMillis();
		this.token = token;
		this.username = "Anonymous";
	}
	public Session(int token, String username) {
		this.login_time = System.currentTimeMillis();
		this.token = token;
		this.username = username;
	}

	public long timeElapsed() {
		long cur_time = System.currentTimeMillis();
		return cur_time - login_time;
	}
	public String getUsername() {
		return username;
	}

}
