package ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;

/**
 * @author Simone Papandrea
 * @version 1.0
 * @since 2015-10-06
 */
public class Log extends JPanel {

	private static final long serialVersionUID = 1L;
	private final JTextArea mTextArea;
	private final SimpleDateFormat mSDF = new SimpleDateFormat("HH:mm:ss");
	private final static String newline = "\n";

	public Log() {

		setLayout(new BorderLayout());
		mTextArea = new JTextArea();
		JScrollPane scrollPane = new JScrollPane(mTextArea);
		mTextArea.setEditable(false);
		setBorder(new EmptyBorder(5, 10, 5, 10));
		mTextArea.setFont(new Font("Arial", Font.BOLD, 16));
		add(scrollPane);
	}

	@Override
	public Dimension getMinimumSize() {

		return new Dimension(this.getWidth(), 100);
	}

	public void print(String message) {

		mTextArea.setText(mSDF.format(Calendar.getInstance().getTime()) + " : " + message + newline + mTextArea.getText());
	}

	public void reset() {

		mTextArea.setText("");
	}
}