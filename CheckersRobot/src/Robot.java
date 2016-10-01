import checkers.Pawn;
import checkers.Move;
import checkers.Sequence;

/**
 * @author Simone Papandrea
 * @version 1.0
 * @since 2015-10-06
 */
public class Robot {

	private final Controller mController;

	public Robot(double size, int cells) {

		mController = new Controller(size, cells);
	}

	public void move(Sequence sequence) {

		Pawn pawn;
		int i, j;

		pawn = (Pawn) sequence.getPawn().clone();

		for (Move move : sequence.getMoves()) {

			grab(pawn.getI(), pawn.getJ());

			if (move.isCapture()) {

				i = move.getI0();
				j = move.getJ0();
				move(i, j);
				grab(move.getI(), move.getJ());
				pawn.set(i, j);
			} else
				move(move.getI(), move.getJ());

			mController.reset();
		}
	}

	private void move(int i, int j) {

		go_to(i, j);
		mController.openClaw();
		mController.liftElevator();
	}

	private void grab(int i, int j) {

		mController.liftElevator();
		go_to(i, j);
		mController.closeClaw();
		mController.liftElevator();
	}

	private void go_to(int i, int j) {

		j *= 2;
		if (i % 2 != 0)
			j++;

		mController.gotoXY(7 - i, j + 2);
		mController.lowerElevator();
	}

	void test(int i, int j) {

		mController.liftElevator();
		go_to(i, j);
		mController.liftElevator();
		mController.reset();
		mController.flt();
	}
}