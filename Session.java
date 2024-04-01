




public class Session {
	private long login_time;
	private int token;

	public Session(int token) {
		this.login_time = System.currentTimeMillis();
		this.token = token;
	}

	public long timeElapsed() {
		long cur_time = System.currentTimeMillis();
		return cur_time - login_time;
	}

}
