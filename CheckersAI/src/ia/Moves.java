package ia;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import checkers.Chessboard;
import checkers.Move;
import checkers.Pawn;
import checkers.Sequence;

/**
 * @author Simone Papandrea
 * @version 1.0
 * @since 2015-10-06
 */
public class Moves {

	static List<Sequence> generateSequences(Chessboard chessboard, boolean player) {

		List<Sequence> temp, sequences = null;
		Pawn pawn;
		Sequence first;
		boolean white;

		temp = new ArrayList<Sequence>();

		for (int i = 0; i < Chessboard.HEIGHT; i++) {

			for (int j = 0; j < Chessboard.WIDTH; j++) {

				pawn = chessboard.get(i, j);

				if (pawn != null) {

					white = pawn.isWhite();

					if (player == white)
						temp.addAll(find(chessboard, pawn, new Sequence(pawn)));
				}
			}
		}

		if (temp.size() > 0) {

			Collections.sort(temp);
			sequences = new ArrayList<Sequence>();
			first = temp.get(0);

			for (Sequence sequence : temp) {

				if (sequence.compareTo(first) == 0)
					sequences.add(sequence);
				else
					break;
			}
		}

		return sequences;
	}

	private static List<Sequence> find(Chessboard chessboard, Pawn pawn, Sequence sequence) {

		List<Sequence> sequences;
		Sequence branch;
		Pawn cur, cap;
		Chessboard temp;
		int i, j, i1, j1;
		int moves, left, right;
		boolean white, king, even;
		int direction;

		i = pawn.getI();
		j = pawn.getJ();
		white = pawn.isWhite();
		king = pawn.isKing();
		direction = white ? -1 : 1;
		moves = king ? 2 : 1;
		even = i % 2 == 0;
		sequences = new ArrayList<Sequence>();

		while (moves-- > 0) {

			left = even ? -1 : 0;
			right = even ? 0 : 1;

			do {

				i1 = i + direction;
				j1 = j + left;
				temp = (Chessboard) chessboard.clone();
				branch = (Sequence) sequence.clone();
				cur = (Pawn) pawn.clone();
				cap = temp.capture(cur, i1, j1);

				if (cap != null) {

					branch.add(i1, j1, cur.getI(), cur.getJ(), true, cap.isKing());
					sequences.addAll(find(temp, cur, branch));
				}

				else if (branch.isEmpty() && temp.canMove(cur, i1, j1)) {

					branch.add(i1, j1);
					sequences.add(branch);

				} else if (!branch.isEmpty() && !sequences.contains(branch))
					sequences.add(branch);

			} while (left++ < right);

			direction = -direction;
		}

		return sequences;
	}

	static Chessboard simulate(Chessboard chessboard, Sequence sequence) {

		Chessboard clone = (Chessboard) chessboard.clone();

		if (!sequence.isEmpty()) {

			Pawn pawn = (Pawn) sequence.getPawn().clone();

			for (Move move : sequence.getMoves()) {

				if (move.isCapture())
					clone.capture(pawn, move.getI(), move.getJ());
				else
					clone.move(pawn, move.getI(), move.getJ());
			}
		}

		return clone;
	}
}