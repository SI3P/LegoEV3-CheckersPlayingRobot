package checkers;

import java.io.Serializable;

/**
 * @author Simone Papandrea
 * @version 1.0
 * @since 2015-10-06
 */
public class Pawn implements Cloneable, Serializable {

	private static final long serialVersionUID = -5998912331243110842L;
	private final boolean mWhite;
	private int mI, mJ;
	private boolean mKing;

	public Pawn(int i, int j, boolean white) {

		this(i, j, white, false);
	}

	public Pawn(int i, int j, boolean color, boolean king) {

		mI = i;
		mJ = j;
		mWhite = color;
		mKing = king;
	}

	public void set(int i, int j) {

		mJ = j;
		mI = i;
	}

	public void king() {

		mKing = true;
	}

	public int getJ() {

		return mJ;
	}

	public int getI() {

		return mI;
	}

	public boolean isKing() {

		return mKing;
	}

	public boolean isWhite() {

		return mWhite;
	}

	public boolean isBlack() {

		return !mWhite;
	}

	@Override
	public Object clone() {

		return new Pawn(mI, mJ, mWhite, mKing);

	}

	@Override
	public boolean equals(Object obj) {

		if (obj != null && obj instanceof Pawn) {

			Pawn pawn = (Pawn) obj;

			return this.mWhite == pawn.mWhite && this.mKing == pawn.mKing && this.mI == pawn.mI && this.mJ == pawn.mJ;
		}

		return false;
	}

	@Override
	public String toString() {

		return mWhite ? (mKing ? "W" : "w") : (mKing ? "B" : "b");
	}
}
