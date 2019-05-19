package hidra;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

/**
 * Helper class to execute terminal commands.
 * Adapted code from: https://crunchify.com/how-to-run-windowsmac-commands-in-java-and-return-the-text-result/
 */
public class Terminal {
	public static void main(String[] args) {
		System.out.println("Why are you running Terminal?");
	}
	
	public static void execute(String command) throws IOException {
		Runtime rt = Runtime.getRuntime();
		Terminal rte = new Terminal();
		printOutput errorReported, outputMessage;
		
		try {
			
			Process proc = rt.exec("sudo -S " + command);
			
			OutputStream stdin = proc.getOutputStream();
			String line = "user" + "\n";
			stdin.write(line.getBytes());
			stdin.flush();
			
			errorReported = rte.getStreamWrapper(proc.getErrorStream(), "ERROR");
			outputMessage = rte.getStreamWrapper(proc.getInputStream(), "OUTPUT");
			errorReported.start();
			outputMessage.start();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public printOutput getStreamWrapper(InputStream is, String type) {
		return new printOutput(is, type);
	}
 
	private class printOutput extends Thread {
		InputStream is = null;
 
		printOutput(InputStream is, String type) {
			this.is = is;
		}
 
		public void run() {
			String s = null;
			try {
				BufferedReader br = new BufferedReader(
						new InputStreamReader(is));
				while ((s = br.readLine()) != null) {
					System.out.println(s);
				}
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
		}
	}
}
