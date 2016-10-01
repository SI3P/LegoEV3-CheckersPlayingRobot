import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;
import ai.Engine;
import ai.Engine.Level;
import ai.Engine.Strategy;
import checkers.Chessboard;
import checkers.Sequence;
import checkers.Status;
import connection.Command;
import ui.Log;
import ui.Board;
import ui.Stream;
import detection.BoardDetector;

/**
 * @author Simone Papandrea
 * @version 1.0
 * @since 2015-10-06
 */
public class CheckersControlPanel extends JFrame {

	private static final long serialVersionUID = 1L;
	private static final String IP = "10.0.1.1";
	private static final int PORT = 55555;
	private final VideoCapture mVideo;
	private Socket mSocket;
	private ObjectOutputStream mOutStream;
	private Board mBoard;
	private Stream mStream;
	private Log mLog;
	private Button mBtnConnect;
	private Chessboard mChessboard;
	private GameThread mGame;

	public CheckersControlPanel() throws IOException {

		super("Checkers Control Panel");

		this.addWindowListener(new WindowAdapter() {

			public void windowClosing(WindowEvent e) {

				disconnect();
				System.exit(0);
			}
		});

		mVideo = new VideoCapture();
		buildGUI();
		setIconImage(new ImageIcon("img/king.png").getImage());
		setVisible(true);
		setExtendedState(JFrame.MAXIMIZED_BOTH);
	}

	private void buildGUI() throws IOException {

		JPanel panel = new JPanel(new BorderLayout());
		ControlListener cl = new ControlListener();

		mBoard = new Board();
		mStream = new Stream();
		mLog = new Log();
		mBtnConnect = new Button("Connect");
		mBtnConnect.setFont(new Font("Arial", Font.BOLD, 16));
		mBtnConnect.addActionListener(cl);

		JSplitPane splitPaneH = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		splitPaneH.setLeftComponent(mStream);
		splitPaneH.setRightComponent(mBoard);
		splitPaneH.setResizeWeight(0.6);

		JPanel bottomPanel = new JPanel();
		bottomPanel.setLayout(new BorderLayout());
		bottomPanel.add(mLog, BorderLayout.CENTER);
		bottomPanel.add(mBtnConnect, BorderLayout.EAST);

		JSplitPane splitPaneV = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		splitPaneV.setLeftComponent(splitPaneH);
		splitPaneV.setRightComponent(bottomPanel);
		splitPaneV.setResizeWeight(0.8);
		panel.add(splitPaneV, BorderLayout.CENTER);

		this.add(panel);
	}

	private void connect() throws UnknownHostException, IOException {

		DataInputStream dI;
		Level level = Level.EASY;
		Strategy strategy = Strategy.FIRST;

		mSocket = new Socket(IP, PORT);
		mOutStream = new ObjectOutputStream(mSocket.getOutputStream());
		mBtnConnect.setLabel("Disconnect");

		dI = new DataInputStream(mSocket.getInputStream());
		level = Level.values()[dI.readInt()];
		strategy = Strategy.values()[dI.readInt()];

		mLog.print("Level: " + level.name() + "	Strategy: " + strategy.name());
		start(level, strategy);
	}

	private void disconnect() {

		try {

			sendCommand(new Command(Status.DISCONNECT));
			mOutStream.close();
			mSocket.close();

		} catch (Exception e) {

			mLog.print("Failure Error closing connection with EV3.");
			System.out.println(e);
		}

		stop();
		mBtnConnect.setLabel("Connect");
		mLog.print("Disconnected.");
	}

	private void start(Level level, Strategy strategy) {

		mGame = new GameThread(level, strategy);
		mGame.start();
	}

	private void stop() {

		if (mGame != null) {

			try {
				mGame.terminate();
				mGame.join();
			} catch (Exception e) {
			}

			mGame = null;
		}
	}

	private void resetGUI() {

		mChessboard = null;
		mLog.reset();
		mStream.reset();
		mBoard.reset();
	}

	private boolean sendCommand(Command command) {

		try {

			mOutStream.writeObject(command);
			mOutStream.flush();

		} catch (IOException e) {
			return false;
		}

		return true;
	}

	private void printStatus(Status status) {

		switch (status) {

		case WIN:
			mLog.print("Player loses");
			break;

		case LOSE:
			mLog.print("Player wins");
			break;

		case DRAW:
			mLog.print("Draw!");
			break;

		default:
			break;

		}
	}

	private class ControlListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent arg0) {

			String command = arg0.getActionCommand();

			switch (command) {

			case "Connect":

				try {

					resetGUI();
					connect();

				} catch (Exception e) {
					mLog.print("Failure Error establishing connection with EV3:" + e.getMessage());
				}

				break;

			case "Disconnect":

				disconnect();
				break;
			}
		}
	}

	public class GameThread extends Thread {

		private volatile boolean running = true;
		private final Level level;
		private final Strategy strategy;

		public GameThread(Level level, Strategy strategy) {

			this.level = level;
			this.strategy = strategy;

		}

		@Override
		public void run() {

			BoardDetector detector = new BoardDetector();
			Chessboard chessboard = null;
			Mat mat = new Mat();
			boolean repeat = false;

			mVideo.open(0);

			do {

				try {

					mVideo.read(mat);

					chessboard = detector.calibrate(mat);

				} catch (Exception ex) {

					repeat = true;

					mLog.print(ex.getMessage());

					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {

					}
				}

			} while (repeat && running);

			if (mat != null)
				mat.release();

			if (running)
				game(detector, chessboard, level, strategy);

			mVideo.release();
		}

		private void game(BoardDetector detection, Chessboard chessboard, Level level, Strategy strategy) {

			Status status = Status.GAME;
			Command command = null;
			Sequence sequence;
			Mat mat = new Mat();
			Chessboard temp;
			Engine engine;
			boolean player = false;
			int wait = 0;

			engine = new Engine(chessboard, level, strategy);

			while (running && status == Status.GAME) {

				command = null;

				if (mVideo.read(mat)) {

					try {

						chessboard = detection.detect(mat);
						mLog.print("Board detected.");

						if (!chessboard.equals(mChessboard)) {

							wait = 0;
							mLog.print("Checking...");
							sequence = null;
							temp = engine.checkMove(mChessboard, chessboard);
							player = engine.getPlayer();

							if (temp != null) {

								mStream.refresh(detection.getImageBoard());
								chessboard = temp;

								if (player) {

									mLog.print("Elaborating...");
									mBoard.showLoadingBar(true);
									status = engine.move(chessboard);
									mBoard.showLoadingBar(false);

									if (status == Status.GAME) {

										sequence = engine.getSequence();
										mLog.print("EV3:" + sequence.toString());
										command = new Command(sequence);
									}
								}

								mChessboard = chessboard;
								mBoard.refresh(chessboard, sequence);

							} else {

								mLog.print((player ? "EV3" : "Player") + " : illegal move.");
								command = new Command(Status.ILLEGAL);
							}

						} else {

							if (player && ++wait == 3) {
								command = new Command(Status.GAME);
								wait = 0;
							}
						}

						mLog.print(player ? "Waiting for EV3" : "Waiting for player");

					} catch (Exception e) {

						mLog.print(e.getMessage());
					}
				}

				if (command != null)
					sendCommand(command);

				if (mat != null)
					mat.release();

				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {

				}
			}

			printStatus(status);
			sendCommand(new Command(status));
		}

		private void terminate() {

			running = false;
		}
	}

	public static void main(String[] args) throws IOException {

		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

		new CheckersControlPanel();
	}
}
