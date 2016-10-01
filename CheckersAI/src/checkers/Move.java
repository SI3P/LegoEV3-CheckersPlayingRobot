package checkers;

import java.io.Serializable;

/**
 * @author Simone Papandrea
 * @version 1.0
 * @since 2015-10-06
 */
public class Move implements Serializable {

	private static final long serialVersionUID = -1386945639629147092L;
	private final boolean mCapture;
	private final boolean mKing;
	private final int mI, mJ;
	private final int mI0, mJ0;

	public Move(int i, int j) {

		this(i, j, -1, -1, false, false);
	}

	public Move(int i, int j, int i0, int j0, boolean capture, boolean king) {

		this.mJ = j;
		this.mI = i;
		this.mI0 = i0;
		this.mJ0 = j0;
		this.mCapture = capture;
		this.mKing = capture && king;
	}

	public int getJ() {

		return mJ;
	}

	public int getI0() {

		return mI0;
	}

	public int getJ0() {

		return mJ0;
	}

	public int getI() {

		return mI;
	}

	public boolean isCapture() {

		return mCapture;
	}

	public boolean isKingCapture() {

		return mKing;
	}

	@Override
	public boolean equals(Object obj) {

		if (obj != null && obj instanceof Move) {

			Move move = (Move) obj;

			return this.mCapture == move.mCapture && this.mKing == move.mKing && this.mI == move.mI
					&& this.mJ == move.mJ;
		}

		return false;
	}

	@Override
	public String toString() {

		int i = mI;
		int j = mJ * 2;

		if (i % 2 != 0)
			j++;

		return "[" + i + "," + j + "]";
	}
}
