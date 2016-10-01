package ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import checkers.Chessboard;
import checkers.Pawn;
import checkers.Sequence;
import utils.ImageUtils;

/**
 * @author Simone Papandrea
 * @version 1.0
 * @since 2015-10-06
 */
public class Board extends JPanel {

	private static final long serialVersionUID = 1L;
	private static final Color WHITE_PAWN_COLOR = new Color(145, 113, 89);
	private static final Color BLACK_PAWN_COLOR = new Color(195, 0, 0);
	private static final Color WHITE_CELL_COLOR = new Color(221, 200, 195);
	private static final Color BLACK_CELL_COLOR = new Color(40, 37, 44);
	private static final Color GREEN_CELL_COLOR = new Color(34, 177, 76);
	private static final Color RED_CELL_COLOR = new Color(255, 127, 39);
	private Chessboard mChessboard;
	private Sequence mSequence;
	private final BufferedImage mKingIcon;
	private final JProgressBar mProgressBar;

	public Board() throws IOException {

		super(new BorderLayout());

		mProgressBar = new JProgressBar();
		mProgressBar.setIndeterminate(true);
		showLoadingBar(false);
		add(mProgressBar, BorderLayout.SOUTH);
		mChessboard = new Chessboard();
		setBackground(Color.DARK_GRAY);
		mKingIcon = ImageIO.read(new File("img/king.png"));
	}

	@Override
	public void paintComponent(Graphics g) {

		super.paintComponent(g);

		int size, cellSize, pawnSize;
		int w, h, x, y;
		int r = -1, c = -1, x0 = 0, y0 = 0, j0;
		final int padding = 5;
		final int margin = 5;
		boolean white;
		Color color;
		Pawn pawn;
		Image kingIcon;

		w = this.getWidth();
		h = this.getHeight();

		if (w > h) {

			size = h - margin * 2;
			x0 = (w - h) / 2;

		} else {

			size = w - margin * 2;
			y0 = (h - w) / 2;
		}

		cellSize = size / Chessboard.HEIGHT;
		pawnSize = cellSize - padding * 2;
		kingIcon = ImageUtils.resizeImage(mKingIcon, pawnSize - padding * 2);

		if (mSequence != null) {

			pawn = mSequence.getPawn();
			r = pawn.getI();
			c = pawn.getJ();
		}

		for (int i = 0; i < Chessboard.HEIGHT; i++) {

			for (int j = 0; j < Chessboard.HEIGHT; j++) {

				white = (i % 2 == 0 && j % 2 != 0) || (i % 2 == 1 && j % 2 == 0);
				x = j * cellSize + margin + x0;
				y = i * cellSize + margin + y0;
				j0 = j / 2;

				if (white)
					color = WHITE_CELL_COLOR;

				else {

					if (i == r && j0 == c)
						color = RED_CELL_COLOR;

					else if (mSequence != null && mSequence.contains(i, j0))
						color = GREEN_CELL_COLOR;

					else
						color = BLACK_CELL_COLOR;
				}

				g.setColor(color);
				g.fillRect(x, y, cellSize, cellSize);

				if (!white) {

					pawn = mChessboard.get(i, j0);

					if (pawn != null) {

						g.setColor(pawn.isWhite() ? WHITE_PAWN_COLOR : BLACK_PAWN_COLOR);
						g.fillOval(x + padding, y + padding, pawnSize, pawnSize);

						if (pawn.isKing())
							g.drawImage(kingIcon, x + padding * 2, y + padding + 2, null);
					}
				}
			}
		}
	}

	public void refresh(Chessboard chessboard, Sequence sequence) {

		this.mChessboard = chessboard;
		this.mSequence = sequence;
		this.repaint();
	}

	public void showLoadingBar(boolean visible) {

		mProgressBar.setVisible(visible);
	}

	public void reset() {

		showLoadingBar(false);
		refresh(new Chessboard(), null);
	}
}