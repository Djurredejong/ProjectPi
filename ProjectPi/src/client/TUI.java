package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class TUI {

	private BufferedReader in;

	private Client client;

	public TUI(Client client) {
		this.client = client;
		in = new BufferedReader(new InputStreamReader(System.in));
	}

	public void start() throws IOException {
		printMenu();
		String input;
		while (true) {
			System.out.println();
			System.out.println("Ready for the next command.");
			input = in.readLine();
			handleInput(input);
		}
	}

	public void handleInput(String input) throws IOException {
		String[] split = input.split("\\s");

		String cmd = split[0];

		String fileName = null;

		if (split.length > 2) {
			System.out.println("Sorry, spaces in file names are not allowed here.");
			return;
		}

		if (split.length == 2) {
			fileName = split[1];
			if (fileName.length() > client.getMaxFileNameLength()) {
				System.out.println("Sorry, the name of the file can be at most " + client.getMaxFileNameLength()
						+ " characters long.");
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
					System.out.println(fileName + " will now be downloaded.");
					client.download(fileName);
				}
				break;
			case ('u'):
				if (fileName == null) {
					System.out.println("If you want to upload a file, please provide the name of that file as well.");
				} else if (fileName.contentEquals("listFilesTemp.txt")) {
					System.out.println("Sorry, the name of the file cannot be listFilesTemp.txt.");
				} else {
					System.out.println(fileName + " will now be uploaded.");
					client.upload(fileName);
				}
				break;
			case ('r'):
				if (fileName == null) {
					System.out.println("If you want to remove a file, please provide the name of that file as well.");
				} else {
					System.out.println(fileName + " will now be removed (provided the file exists).");
					client.remove(fileName);
				}
				break;
			case ('l'):
				System.out.println("These are the files currently on the Raspberry Pi:");
				client.listFiles();
				break;
			case ('m'):
				printMenu();
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

	public void printMenu() {
		System.out.println("Type <d <filename>> for download,");
		System.out.println("     <u <filename>> for upload,");
		System.out.println("     <r <filename>> for removal,");
		System.out.println("     <l> for a list of files,");
		System.out.println("     <m> for this menu,");
		System.out.println(" or  <q> to quit,");
		System.out.println("Press the Return key after typing your command.");
	}

	/**
	 * Client needs to pass the user input to Transfer when downloading a file, in
	 * order for the user to be able to pause/resume the download, and for
	 * requesting statistics
	 */
	public BufferedReader getIn() {
		return in;
	}
}
