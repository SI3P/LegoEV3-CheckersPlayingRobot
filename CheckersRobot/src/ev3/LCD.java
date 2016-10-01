package ev3;

import java.util.ArrayList;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.lcd.Font;
import lejos.hardware.lcd.TextLCD;

/**
* @author  Simone Papandrea
* @version 1.0
* @since   2015-10-06
*/
public class LCD {
	
	private final static int BUFFER_SIZE=10;
	private static ArrayList<String> BUFFER=new ArrayList<String>(BUFFER_SIZE);

	public static void print(String message,boolean clear) {

		if(clear)		
			BUFFER.clear();
		
		BUFFER.add(0,message);			
				
		draw();
	}	
	
	public static void print(String[] messages,boolean clear) {
		
		int length;
		
		if(clear)			
			BUFFER.clear();
				
		length=messages.length-1;
		
		for(int i=length;i>=0;i--)
			BUFFER.add(0,messages[i]);
		
		draw();
	}
	
	private static void draw(){
		
		TextLCD tLCD;
		int size;
		
		tLCD= LocalEV3.get().getTextLCD(Font.getSmallFont());
		tLCD.clear();		
		size=BUFFER.size();
	
		while(size>BUFFER_SIZE)
			BUFFER.remove(--size);
		
		for(int i=0;i<size;i++)			
			tLCD.drawString(BUFFER.get(i), 0, i);
		
		tLCD.refresh();			
	}	
	
	/*public static void draw(Board board) {

	GraphicsLCD tLCD;
	Pawn pawn;
	int height, width, size, padding;
	String label;

	tLCD = LocalEV3.get().getGraphicsLCD();
	tLCD.clear();

	height = tLCD.getHeight() / 8;
	width = tLCD.getWidth() / 8;
	size = Math.min(height, width);
	padding = size / 5;

	for (int i = 0; i < Board.HEIGHT; i++) {

		int w, b;

		for (int j = 0; j < Board.HEIGHT; j += 2) {

			if (i % 2 == 0) {
				w = j + 1;
				b = j;

			} else {

				w = j;
				b = j + 1;
			}

			tLCD.setColor(0, 0, 0);
			tLCD.fillRect(size * b, size * i, size, size);
			tLCD.setColor(255, 255, 255);
			tLCD.fillRect(size * w, size * i, size, size);

			pawn = board.get(i, j / 2);

			if (pawn != null) {

				if (pawn.isWhite()) {
					
					if (pawn.isKing())
						label = "W";
					else
						label = "w";
				} 
				else {

					if (pawn.isKing())
						label = "B";
					else
						label = "b";
				}
				
				tLCD.setColor(255, 255, 255);
				tLCD.drawString(label, size * b + padding, size * i, 0);
			}
		}
	}
}*/	
}