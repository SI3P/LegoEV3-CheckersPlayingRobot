package ai;

import java.util.List;

import checkers.Chessboard;
import checkers.Sequence;
import checkers.Status;
import eval.Evaluator;
import eval.EvaluatorFirst;
import eval.EvaluatorOriginal;
import eval.EvaluatorPawns;

/**
 * @author Simone Papandrea
 * @version 1.0
 * @since 2015-10-06
 */
public class Engine {

	private static final int DRAW = 400;
	private int mDraw = 0;
	private final Evaluator mEvaluator;
	private final Level mLevel;
	protected List<Sequence> mSequences;
	private Sequence mSequence;
	private boolean mPlayer = false;

	public enum Level {

		HARD(10), MEDIUM(8), EASY(6);

		private int value;

		private Level(int value) {
			this.value = value;
		}
	}

	public enum Strategy {

		PAWNS, ORIGINAL, FIRST
	}

	public Engine(Chessboard chessboard, Level level, Strategy strategy) {

		mLevel = level;
		mEvaluator = getEvaluator(strategy);
		mSequences = Moves.generateSequences(chessboard, false);
	}

	private Evaluator getEvaluator(Strategy strategy) {

		Evaluator evaluator;

		switch (strategy) {

		case PAWNS:
			evaluator = new EvaluatorPawns();
			break;

		case ORIGINAL:
			evaluator = new EvaluatorOriginal();
			break;

		case FIRST:
			evaluator = new EvaluatorFirst();
			break;

		default:
			evaluator = new EvaluatorPawns();
		}

		return evaluator;
	}

	public Status move(Chessboard chessboard) {

		Status status = Status.GAME;
		Sequence sequence;
		Chessboard temp;

		if (mDraw != DRAW) {

			sequence = miniMax(chessboard, 0);

			if (sequence == null)
				status = Status.LOSE;

			else {

				if (sequence.getPawn().isKing())
					this.mDraw++;
				else
					this.mDraw = 0;

				temp = Moves.simulate(chessboard, sequence);
				mSequence = sequence;
				mSequences = Moves.generateSequences(temp, false);
			}

			if (mSequences == null || mSequences.size() == 0)
				status = Status.WIN;
		} else
			status = Status.DRAW;

		return status;
	}

	public Chessboard checkMove(Chessboard last, Chessboard detected) {

		Chessboard current = null, temp;

		if (last != null) {

			if (mPlayer) {

				temp = Moves.simulate(last, mSequence);

				if (detected.equals(temp)) {

					current = temp;
					mPlayer = !mPlayer;
				}

				else
					current = checkUserMove(temp, detected);
			}

			else if (mSequences != null) {

				current = checkUserMove(last, detected);

				if (current != null)
					mPlayer = !mPlayer;
			}

		} else
			current = detected;

		return current;
	}

	private Chessboard checkUserMove(Chessboard last, Chessboard detected) {

		Chessboard temp;

		for (Sequence sequence : mSequences) {

			temp = Moves.simulate(last, sequence);

			if (detected.equals(temp))
				return temp;
		}

		return null;
	}

	public Sequence getSequence() {

		return mSequence;
	}

	public boolean getPlayer() {

		return mPlayer;
	}

	public Sequence miniMax(Chessboard chessboard, int depth) {

		Sequence sequence = null;
		List<Sequence> sequences;
		Chessboard temp;
		int score, max, min, value;
		int size;

		min = Integer.MAX_VALUE;
		max = -min;
		value = max;
		sequences = Moves.generateSequences(chessboard, true);

		if (sequences != null) {

			size = sequences.size();

			if (size == 1)

				sequence = sequences.get(0);

			else {

				for (Sequence seq : sequences) {

					temp = Moves.simulate(chessboard, seq);
					score = eval(temp, depth + 1, min, max, false);

					if (score > value) {
						value = score;
						sequence = seq;
					}

					max = Math.max(max, value);
				}
			}
		}

		return sequence;
	}

	private int eval(Chessboard chessboard, int depth, int min, int max, boolean maximize) {

		Chessboard temp;
		List<Sequence> sequences;
		int score, value;

		sequences = Moves.generateSequences(chessboard, maximize);

		if (sequences == null || (depth >= mLevel.value && quiescent(sequences)))

			score = mEvaluator.eval(chessboard);

		else {

			score = maximize ? -Integer.MAX_VALUE : Integer.MAX_VALUE;

			for (Sequence seq : sequences) {

				temp = Moves.simulate(chessboard, seq);
				value = eval(temp, depth + 1, min, max, !maximize);

				if (maximize) {

					score = Math.max(score, value);

					if (score >= min)
						break;

					max = Math.max(max, score);

				} else {

					score = Math.min(score, value);

					if (score <= max)
						break;

					min = Math.min(min, score);
				}
			}
		}

		return score;
	}

	private boolean quiescent(List<Sequence> sequences) {

		return !sequences.get(0).getFirstMove().isCapture();
	}
}