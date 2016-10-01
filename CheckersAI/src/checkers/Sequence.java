package checkers;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Simone Papandrea
 * @version 1.0
 * @since 2015-10-06
 */
public class Sequence implements Comparable<Sequence>, Cloneable, Serializable {

	private static final long serialVersionUID = 2374858955650912218L;
	private final Pawn mPawn;
	private final List<Move> mMoves;

	public Sequence(Pawn pawn) {

		mPawn = pawn;
		mMoves = new ArrayList<Move>();
	}

	public void add(int i, int j) {

		mMoves.add(new Move(i, j));
	}

	public void add(int i, int j, int i0, int j0, boolean capture, boolean king) {

		mMoves.add(new Move(i, j, i0, j0, capture, king));
	}

	public void addAll(List<Move> moves) {

		mMoves.addAll(0, moves);
	}

	public boolean isEmpty() {

		return mMoves.isEmpty();
	}

	@Override
	public int compareTo(Sequence o) {

		return Integer.compare(o.eval(), eval());
	}

	public Pawn getPawn() {

		return mPawn;
	}

	public Move getFirstMove() {

		return mMoves.get(0);
	}

	public List<Move> getMoves() {

		return mMoves;
	}

	public boolean contains(int i, int j) {

		for (Move move : mMoves) {

			if ((move.getI() == i && move.getJ() == j) || (move.getI0() == i && move.getJ0() == j))
				return true;
		}

		return false;
	}

	private int eval() {

		Move move;
		int catches = 0, kcatches = 0, firstK = -1;
		int size;
		boolean king;

		size = mMoves.size();

		for (int i = 0; i < size; i++) {

			move = mMoves.get(i);

			if (move.isCapture()) {

				catches++;

				if (move.isKingCapture()) {

					kcatches++;

					if (firstK == -1)
						firstK = i;
				}
			}
		}

		king = mPawn.isKing();

		return catches * 1000 + ((king && catches > 0) ? 100 : 0) + (king ? kcatches * 10 : 0) - firstK;
	}

	@Override
	public boolean equals(Object obj) {

		if (obj != null && obj instanceof Sequence) {

			Sequence sequence = (Sequence) obj;

			return this.mPawn.equals(sequence.mPawn) && this.mMoves.equals(sequence.mMoves);
		}

		return false;
	}

	@Override
	public Object clone() {

		Sequence sequence = new Sequence(mPawn);

		sequence.mMoves.addAll(mMoves);

		return sequence;
	}

	@Override
	public String toString() {

		int i = mPawn.getI();
		int j = mPawn.getJ() * 2;

		if (i % 2 != 0)
			j++;

		String string = "Pawn " + mPawn.toString() + " in (" + i + "," + j + ") moves to";

		for (Move move : mMoves)
			string += " " + move.toString();

		return string;
	}
}