import checkers.Sequence;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import connection.Command;
import checkers.Status;
import lejos.hardware.Button;
import lejos.utility.TextMenu;
import ev3.LCD;
import ev3.LED;

/**
 * @author Simone Papandrea
 * @version 1.0
 * @since 2015-10-06
 */
public class CheckersRobot extends Thread {

	private static final double BOARD_CELL_SIZE = 3.1;
	private static final int BOARD_CELLS = 8;
	private static final int PORT = 55555;
	private final Socket mClient;
	private final DataOutputStream mOutStream;
	private static Robot ROBOT;

	public CheckersRobot(Socket socket, int level, int strategy) throws IOException {

		mClient = socket;
		mOutStream = new DataOutputStream(mClient.getOutputStream());
		mOutStream.writeInt(level);
		mOutStream.writeInt(strategy);
		mOutStream.flush();
	}

	public static void main(String[] args) throws IOException {

		final TextMenu menu;
		int function;

		ROBOT = new Robot(BOARD_CELL_SIZE, BOARD_CELLS);
		menu = new TextMenu(new String[] { "Game", "Test" }, 1, "Select function:");

		while (true) {

			LCD.print(new String[] { "Press any button to start", "Press ESC to exit" }, true);

			if (Button.waitForAnyPress() == Button.ESCAPE.getId())
				break;

			function = menu.select();

			if (function == 0)
				game();
			else
				test();
		}
	}

	private static void game() throws IOException {

		final TextMenu menu;
		final int level, strategy;
		ServerSocket server;
		CheckersRobot thread;

		LCD.print("Play Game", true);

		menu = new TextMenu(new String[] { "Hard", "Medium", "Easy" }, 1, "Select level:");
		level = menu.select();
		menu.setItems(new String[] { "Pawns", "Original", "First" });
		menu.setTitle("Select the strategy:");
		strategy = menu.select();
		server = new ServerSocket(PORT);

		LCD.print("Awaiting player...", true);

		thread = new CheckersRobot(server.accept(), level, strategy);
		thread.start();

		try {
			thread.join();
		} catch (InterruptedException e) {

		}

		server.close();
	}

	@Override
	public void run() {

		Sequence sequence = null;
		ObjectInputStream inputStream = null;
		Command command;
		Status status;
		boolean game = true;

		LCD.print("Player connect", false);
		LED.lightBlinkingOrange();

		try {

			inputStream = new ObjectInputStream(mClient.getInputStream());

			do {

				command = (Command) inputStream.readObject();
				status = command.getStatus();
				game = checkStatus(status);

				if (game) {

					if (status == Status.GAME) {

						if (command.getSequence() != null)
							sequence = command.getSequence();

						LCD.print(sequence.toString(), false);
						ROBOT.move(sequence);
					}
				}

			} while (game);

		} catch (Exception e) {

		}

		if (inputStream != null)
			try {
				inputStream.close();
			} catch (IOException e) {
			}

		try {
			mOutStream.close();
		} catch (IOException e) {
		}

		try {
			mClient.close();
		} catch (IOException e) {
		}
	}

	public boolean checkStatus(Status status) {

		switch (status) {

		case WIN:

			LCD.print("Player lose", false);
			LED.lightGreen();
			return false;

		case LOSE:
			LCD.print("Player win", false);
			LED.lightRed();
			return false;

		case DRAW:

			LCD.print("Draw!", false);
			LED.lightOrange();
			return false;

		case ILLEGAL:

			LCD.print("Illegal move", false);
			LED.lightBlinkingRed();
			return true;

		case DISCONNECT:

			LCD.print("Player disconnected", false);
			LED.lightBlinkingRed();
			return false;

		default:

			LED.lightBlinkingGreen();
			return true;
		}
	}

	private static void test() {

		LCD.print("Test Function:", true);
		int x, y;

		for (int i = 0; i < 7; i++) {

			x = 7 - i;
			y = (i + 1) / 2;
			LCD.print("Moves to (" + x + "," + y + ")", false);
			ROBOT.test(x, y);

			LCD.print(new String[] { "Press any button to continue", "Press ESC to exit" }, false);

			if (Button.waitForAnyPress() == Button.ESCAPE.getId())
				break;
		}

		LCD.print("Test Function terminated", false);
	}
}