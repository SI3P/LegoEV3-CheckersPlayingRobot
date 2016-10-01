import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.motor.EV3MediumRegulatedMotor;
import lejos.hardware.motor.UnregulatedMotor;
import lejos.hardware.port.MotorPort;
import lejos.hardware.port.Port;
import lejos.robotics.RegulatedMotor;

/**
 * @author Simone Papandrea
 * @version 1.0
 * @since 2015-10-06
 */
public class Controller {

	private static final int CLOSE_CLAW = 110;
	private static final int LIFT_ELEVATOR = -100;
	private static final int ELEVATOR_SPEED = 100;
	private static final double LARGE_WHEEL_SIZE = Math.PI * 5.6;
	private static final double SMALL_WHEEL_SIZE = Math.PI * 3.8;
	private static RegulatedMotor mClaw;
	private static RegulatedMotor mCarriage;
	private static RegulatedMotor mBelt;
	private static RegulatedMotor mElevator;
	private final double mCellSize;
	private final int mBalance;

	public Controller(double cellSize, int cellNumbers) {

		this.mCellSize = cellSize;
		this.mBalance = cellNumbers / 2 - 1;
		calibrate();
	}

	private void calibrate() {

		mCarriage = new EV3LargeRegulatedMotor(MotorPort.C);
		mCarriage.setSpeed(100);
		mCarriage.resetTachoCount();

		mBelt = new EV3LargeRegulatedMotor(MotorPort.A);
		mBelt.setSpeed(80);
		mBelt.resetTachoCount();

		mClaw = new EV3MediumRegulatedMotor(MotorPort.D);
		mClaw.setSpeed(50);
		mClaw.resetTachoCount();

		mElevator = new EV3LargeRegulatedMotor(MotorPort.B);
		mElevator.setSpeed(100);
		mElevator.resetTachoCount();
	}

	private void calibrate(Port port, int pwr, boolean reverse) {

		UnregulatedMotor motor;
		int count = -99999999;

		motor = new UnregulatedMotor(port);
		motor.setPower(pwr);

		if (reverse)
			motor.backward();
		else
			motor.forward();

		while (motor.getTachoCount() != count) {

			count = motor.getTachoCount();

			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
			}
		}

		motor.stop();
		motor.close();
	}

	public void openClaw() {

		mClaw.rotateTo(0);
	}

	public void closeClaw() {

		mClaw.rotateTo(CLOSE_CLAW);
	}

	private void resetCarriage() {

		mCarriage.rotateTo(0);
	}

	private void rewindBelt() {

		mBelt.rotateTo(0);
	}

	public void liftElevator() {

		mElevator.setSpeed(ELEVATOR_SPEED);
		mElevator.rotateTo(LIFT_ELEVATOR);
	}

	public void lowerElevator() {

		mElevator.setSpeed(ELEVATOR_SPEED / 2);
		mElevator.rotateTo(0);
	}

	private void moveBelt(int i) {

		int toAngle = (int) Math.round(i * mCellSize / SMALL_WHEEL_SIZE * 360.0);

		mBelt.rotateTo(toAngle);
	}

	private void moveCarriage(int j) {

		int toAngle = (int) Math.round(j * mCellSize / LARGE_WHEEL_SIZE * 360.0);

		mCarriage.rotateTo(toAngle);
	}

	public void reset() {

		moveBelt(mBalance);
		resetCarriage();
		rewindBelt();
		lowerElevator();
		openClaw();
	}

	public void gotoXY(int i, int j) {

		moveBelt(mBalance);
		moveCarriage(j);
		moveBelt(i);
	}

	public void flt() {

		mClaw.flt();
		mCarriage.flt();
		mBelt.flt();
		mElevator.flt();

	}
}