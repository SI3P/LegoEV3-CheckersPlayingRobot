package eval;

import checkers.Chessboard;

/**
 * @author Simone Papandrea
 * @version 1.0
 * @since 2015-10-06
 */
public class EvaluatorPawns extends Evaluator {

	@Override
	protected int evalWhite(int i, int j) {

		return (int) (CELL_WEIGHT + (Math.pow(Chessboard.HEIGHT - i - 1, 2)));
	}

	@Override
	protected int evalWhiteKing(int i, int j) {

		return KING_WEIGHT;
	}

	@Override
	protected int evalBlack(int i, int j) {

		return (int) (CELL_WEIGHT + (Math.pow(i, 2)));
	}

	@Override
	protected int evalBlackKing(int i, int j) {

		return KING_WEIGHT;
	}
}