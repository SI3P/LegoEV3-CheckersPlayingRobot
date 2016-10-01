package eval;

import checkers.Chessboard;
import checkers.Pawn;

/**
 * @author Simone Papandrea
 * @version 1.0
 * @since 2015-10-06
 */
public abstract class Evaluator {

	protected final static int CELL_WEIGHT = 100;
	protected final static int KING_WEIGHT = 200;
	protected final static int WEIGHT = 10;
	protected final static int BORDER = 10;
	protected Chessboard mChessboard;

	public final int eval(Chessboard chessboard) {

		int value = 0;
		Pawn pawn;

		mChessboard = chessboard;

		for (int i = 0; i < Chessboard.HEIGHT; i++) {

			for (int j = 0; j < Chessboard.WIDTH; j++) {

				pawn = chessboard.get(i, j);

				if (pawn != null) {

					if (pawn.isWhite()) {

						if (pawn.isKing())
							value += evalWhiteKing(i, j);
						else
							value += evalWhite(i, j);

					} else {

						if (pawn.isKing())
							value -= evalBlackKing(i, j);
						else
							value -= evalBlack(i, j);
					}
				}
			}
		}

		value += (int) (Math.random() * WEIGHT);

		return value;
	}

	protected abstract int evalWhite(int i, int j);
	protected abstract int evalWhiteKing(int i, int j);
	protected abstract int evalBlack(int i, int j);
	protected abstract int evalBlackKing(int i, int j);
}