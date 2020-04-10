package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class TUI {
	private static final int maxFileNameLength = 100;

	private BufferedReader in;

	private Client client;

	public TUI(Client client) {
		this.client = client;
		in = new BufferedReader(new InputStreamReader(System.in));
	}

	public void start() throws IOException {
		String input;
		while (true) {
			System.out.println(
					"Type <d <filename>> for download, <u <filename>> for upload, <r <filename>> for removal,");
			System.out.println("<l> for a list of files, <s> for statistics, or <q> to quit.");
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
			if (fileName.length() > 100) {
				System.out.println(
						"Sorry, the name of the file can be at most " + maxFileNameLength + " characters long.");
				return;
			}
		}

		if (cmd.length() == 1) {
			char command = cmd.charAt(0);

			switch (command) {
			case ('d'):
				if (fileName == null) {
					System.out.println("If you want to download a file, please provide the name of that file as well.");
				} else {
					client.download(fileName);
				}
				break;
			case ('u'):
				if (fileName == null) {
					System.out.println("If you want to upload a file, please provide the name of that file as well.");
				} else {
					client.upload(fileName);
				}
				break;
			case ('r'):
				if (fileName == null) {
					System.out.println("If you want to remove a file, please provide the name of that file as well.");
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
				System.out.println("Please provide one of the valid command characters.");
				break;
			}

		} else {
			System.out.println("Please provide only one command character, followed by a space or nothing.");
		}
	}
}
