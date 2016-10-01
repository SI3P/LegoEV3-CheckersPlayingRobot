package eval;

/**
 * @author Simone Papandrea
 * @version 1.0
 * @since 2015-10-06
 */
public class EvaluatorOriginal extends EvaluatorPawns {

	@Override
	protected int evalWhiteKing(int i, int j) {

		int score = super.evalWhiteKing(i, j);

		if (i == 0 || i == 7)
			score -= BORDER;

		if (j == 0 || j == 7)
			score -= BORDER;

		return score;
	}

	@Override
	protected int evalBlackKing(int i, int j) {

		int score = super.evalBlackKing(i, j);

		if (i == 0 || i == 7)
			score -= BORDER;

		if (j == 0 || j == 7)
			score -= BORDER;

		return score;
	}
}