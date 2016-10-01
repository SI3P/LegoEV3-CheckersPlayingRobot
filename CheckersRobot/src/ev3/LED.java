package ev3;

import lejos.hardware.ev3.LocalEV3;
import lejos.utility.Delay;

/**
 * @author Simone Papandrea
 * @version 1.0
 * @since 2015-10-06
 */
public class LED {

	private final static int LIGHT_DELAY = 3000;

	public static void lightGreen() {

		led(1);
	}

	public static void lightRed() {

		led(2);
	}

	public static void lightOrange() {

		led(3);
	}

	public static void lightBlinkingGreen() {

		led(4);
	}

	public static void lightBlinkingRed() {

		led(5);
	}

	public static void lightBlinkingOrange() {

		led(6);
	}

	private static void led(int pattern) {

		lejos.hardware.LED led = LocalEV3.get().getLED();
		led.setPattern(pattern);
		Delay.msDelay(LIGHT_DELAY);
		led.setPattern(0);
	}
}