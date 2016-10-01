package connection;

import java.io.Serializable;
import checkers.Sequence;
import checkers.Status;

/**
 * @author Simone Papandrea
 * @version 1.0
 * @since 2015-10-06
 */
public class Command implements Serializable {

	private static final long serialVersionUID = 111586028485525630L;
	private final Status mStatus;
	private final Sequence mSequence;

	public Command(Status status) {

		mSequence = null;
		mStatus = status;
	}

	public Command(Sequence sequence) {

		mSequence = sequence;
		mStatus = Status.GAME;
	}

	public Sequence getSequence() {

		return mSequence;
	}

	public Status getStatus() {

		return mStatus;
	}
}