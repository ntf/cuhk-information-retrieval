package engg5106.ir;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.StringTokenizer;

/**
 * 
 * @author NTF
 *@deprecated
 */
public class SimpleFileReader {

	private StringTokenizer st;
	private BufferedReader in;

	public SimpleFileReader(BufferedReader in) {
		this.in = in;
		eat("");
	}

	protected void eat(String s) {
		st = new StringTokenizer(s);
	}

	public String next() throws IOException {
		while (!st.hasMoreTokens()) {
			String line = in.readLine();
			if (line == null) {
				return null;
			}
			eat(line);
		}
		return st.nextToken();
	}

	public int nextInt() throws IOException {
		return Integer.parseInt(next());
	}

	public long nextLong() throws IOException {
		return Long.parseLong(next());
	}

	public double nextDouble() throws IOException {
		return Double.parseDouble(next());
	}
}
