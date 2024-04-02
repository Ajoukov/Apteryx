import java.lang.ProcessBuilder;
import java.io.*;

public class ApteryxProcessor {
	public String getResults() {
		ProcessBuilder pb = new ProcessBuilder();
		pb.command("python3", "scripts/test.py");

		String ret = "";
		try {
			Process p = pb.start();
			int code = p.waitFor();
			BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
			ret = in.readLine();
		} catch (InterruptedException e) {
			return "failure";
		} catch (IOException e) {
			return "failure";
		}
		return ret;
	}
}
