package eval;

import checkers.Chessboard;

/**
 * @author Simone Papandrea
 * @version 1.0
 * @since 2015-10-06
 */
public class EvaluatorFirst extends EvaluatorOriginal {

	protected final static int DEFENSE_WEIGHT = 2;

	@Override
	protected int evalWhite(int i, int j) {

		int score = super.evalWhite(i, j);

		score += support(i, j, true);

		return score;
	}

	@Override
	protected int evalBlack(int i, int j) {

		int score = super.evalBlack(i, j);

		score += support(i, j, true);

		return score;
	}

	private int support(int i, int j, boolean white) {

		int score = 0;
		int value = 0;
		int i0 = white ? i + 1 : i - 1;
		int i1 = white ? 8 : -1;

		if (!Chessboard.exists(i, j) || ((Chessboard.exists(i0, j - 1) && mChessboard.isWhite(i0, j - 1) != white)
				&& (Chessboard.exists(i0, j + 1) && mChessboard.isWhite(i0, j + 1) != white)))

			if (i == i1)
				return 0;
			else
				return -1;
		else {

			value = support(i0, j - 1, white);

			if (value != -1)
				score += value + DEFENSE_WEIGHT;

			value = support(i0, j + 1, white);

			if (value != -1)
				score += value + DEFENSE_WEIGHT;
		}

		return score;
	}
}