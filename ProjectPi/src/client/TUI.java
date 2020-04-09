package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

public class TUI {

	private BufferedReader in;
	private PrintWriter out;

	private Client client;

	public TUI(Client client) {
		this.client = client;
		in = new BufferedReader(new InputStreamReader(System.in));
		out = new PrintWriter(System.out, true);
	}

	public void start() throws IOException {
		String input;
		while (true) {
			out.write("Type d filename for download, u filename for upload, r filename to remmove,");
			out.write(" l for a list of files, s for statistics, or q to quit.");
			input = in.readLine();
			handleInput(input);
		}
	}

	public void handleInput(String input) throws IOException {
		String[] split = input.split("\\s");

		String cmd = split[0];

		String fileName = null;

		if (split.length > 1) {
			fileName = split[1];
		}

		if (cmd.length() == 1) {
			char command = cmd.charAt(0);

			switch (command) {
			case ('d'):
				if (fileName == null) {
					out.write("If you want to download a file, please provide the name of that file as well.");
				} else {
					client.download(fileName);
				}
				break;
			case ('u'):
				if (fileName == null) {
					out.write("If you want to upload a file, please provide the name of that file as well.");
				} else {
					client.upload(fileName);
				}
				break;
			case ('r'):
				if (fileName == null) {
					out.write("If you want to remove a file, please provide the name of that file as well.");
				} else {
					client.remove(fileName);
				}
				break;
			case ('l'):
				client.listFiles();
				break;
			case ('s'):
				client.showStats();
				break;
			case ('q'):
				client.quit();
				break;
			default:
				out.write("Please provide one of the valid command characters.");
				break;
			}

		} else {
			out.write("Please provide only one command character, followed by a space or nothing.");
		}
	}
}
