package checkers;

import java.util.List;

/**
 * @author Simone Papandrea
 * @version 1.0
 * @since 2015-10-06
 */
public class Chessboard implements Cloneable {

	public static final int HEIGHT = 8;
	public static final int WIDTH = HEIGHT / 2;
	public static final int N_PAWNS = 12;
	private final Pawn[][] mCells;

	public Chessboard() {

		mCells = new Pawn[HEIGHT][WIDTH];
	}

	public Chessboard(List<Pawn> pawns) throws BoardException {

		this();

		for (Pawn pawn : pawns) {

			int i = pawn.getI();
			int j = pawn.getJ();

			if ((i % 2 == 0 && j % 2 == 0) || i % 2 != 0 && j % 2 != 0) {

				j = (int) Math.floor(j / 2);
				pawn.set(i, j);
				mCells[i][j] = pawn;

			} else
				throw new BoardException(i, j);
		}
	}

	public void init() {

		for (int i = 0; i < 3; i++) {

			for (int j = 0; j < WIDTH; j++) {

				mCells[i][j] = new Pawn(i, j, false);
				mCells[HEIGHT - i - 1][j] = new Pawn(HEIGHT - i - 1, j, true);
			}
		}
	}

	public void free(int i, int j) {

		mCells[i][j] = null;
	}

	public boolean isFree(int i, int j) {

		return mCells[i][j] == null;
	}

	public boolean isWhite(int i, int j) {

		if (!isFree(i, j))
			return mCells[i][j].isWhite();
		else
			return false;
	}

	public boolean isBlack(int i, int j) {

		if (!isFree(i, j))
			return mCells[i][j].isBlack();
		else
			return false;
	}

	public boolean isKing(int i, int j) {

		if (!isFree(i, j))
			return mCells[i][j].isKing();
		else
			return false;
	}

	public boolean move(Pawn pawn, int i, int j) {

		if (canMove(pawn, i, j)) {

			free(pawn.getI(), pawn.getJ());
			set(pawn, i, j);
			return true;
		}

		return false;
	}

	public boolean canMove(Pawn pawn, int i1, int j1) {

		int i = pawn.getI();
		int j = pawn.getJ();
		int dI = i - i1;
		int dJ = j - j1;
		boolean even = i % 2 == 0;

		return exists(i1, j1) && isFree(i1, j1) && Math.abs(dI) == 1 && Math.abs(dJ) <= 1
				&& ((even && dJ >= 0) || (!even && dJ <= 0))
				&& (pawn.isKing() || ((pawn.isWhite() && dI == 1) || (pawn.isBlack() && dI == -1)));
	}

	public Pawn capture(Pawn pawn, int i1, int j1) {

		Pawn cap = null;
		int i, j;
		int[] coords;

		i = pawn.getI();
		j = pawn.getJ();

		coords = canCapture(pawn, i1, j1);

		if (coords != null) {

			cap = get(i1, j1);
			free(i1, j1);
			set(pawn, coords[0], coords[1]);
			free(i, j);
		}

		return cap;
	}

	public int[] canCapture(Pawn pawn, int i1, int j1) {

		if (exists(i1, j1)) {

			int i, j;
			int dI, dJ;
			boolean even;
			Pawn cap;

			cap = get(i1, j1);

			if (cap != null) {

				i = pawn.getI();
				j = pawn.getJ();
				dI = i - i1;
				dJ = j - j1;
				even = i % 2 == 0;

				if (pawn.isWhite() != cap.isWhite()

						&& Math.abs(dI) == 1 && Math.abs(dJ) <= 1

						&& ((even && dJ >= 0) || (!even && dJ <= 0))

						&& (pawn.isKing()

								|| (!cap.isKing() && ((pawn.isWhite() && dI == 1)

										|| (pawn.isBlack() && dI == -1)

								)))) {

					int i2, j2;
					i2 = i - dI * 2;
					j2 = j + (even ? (dJ == 0 ? 1 : -1) : (dJ == 0 ? -1 : 1));

					if (exists(i2, j2) && isFree(i2, j2))
						return new int[] { i2, j2 };
				}
			}
		}

		return null;
	}

	public static boolean exists(int i, int j) {

		return i > -1 && i < Chessboard.HEIGHT && j > -1 && j < Chessboard.WIDTH;
	}

	public Pawn get(int i, int j) {

		return mCells[i][j];
	}

	public void set(Pawn pawn, int i, int j) {

		pawn.set(i, j);

		if ((i == (HEIGHT - 1) && pawn.isBlack()) || (i == 0 && pawn.isWhite()))
			pawn.king();

		mCells[i][j] = pawn;
	}

	public boolean check() {

		int[] counts = this.count();

		return counts[0] == N_PAWNS && counts[1] == N_PAWNS;
	}

	public int[] count() {

		int countW = 0, countB = 0;

		for (int i = 0; i < HEIGHT; i++) {

			for (int j = 0; j < WIDTH; j++)

				if (this.isWhite(i, j))
					countW++;
				else if (this.isBlack(i, j))
					countB++;
		}

		return new int[] { countW, countB };
	}

	@Override
	public Object clone() {

		Chessboard board = new Chessboard();
		Pawn pawn;

		for (int i = 0; i < HEIGHT; i++) {

			for (int j = 0; j < WIDTH; j++) {

				pawn = mCells[i][j];

				if (pawn != null)
					board.mCells[i][j] = (Pawn) pawn.clone();
			}
		}

		return board;
	}

	@Override
	public boolean equals(Object obj) {

		boolean result = false;

		if (obj != null && obj instanceof Chessboard) {

			Chessboard board = (Chessboard) obj;
			result = true;

			for (int i = 0; i < HEIGHT; i++) {

				for (int j = 0; j < WIDTH; j++) {

					if ((isFree(i, j) != board.isFree(i, j)) || (isWhite(i, j) != board.isWhite(i, j))) {

						return false;
					}
				}
			}
		}

		return result;
	}

	public static class BoardException extends Exception {

		private static final long serialVersionUID = 1L;
		private int mX;
		private int mY;

		public BoardException() {

			this(-1, -1);
		}

		public BoardException(int x, int y) {

			mX = x;
			mY = y;
		}

		@Override
		public String getMessage() {

			if (mX == -1 || mY == -1)
				return "Invalid board.";
			else
				return "Can not create board. A pawn is on a white cell (" + (mX + 1) + "," + (mY + 1) + ").";
		}
	}
}