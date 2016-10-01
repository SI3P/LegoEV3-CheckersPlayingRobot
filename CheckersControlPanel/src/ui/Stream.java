package ui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;
import utils.ImageUtils;

/**
 * @author Simone Papandrea
 * @version 1.0
 * @since 2015-10-06
 */
public class Stream extends JPanel {

	private static final long serialVersionUID = 1L;
	private BufferedImage mImage;

	public Stream() {

		this.setBackground(Color.BLACK);
	}

	@Override
	protected void paintComponent(Graphics g) {

		super.paintComponent(g);

		int h = this.getHeight();
		int w = this.getWidth();

		if (mImage != null) {

			int size = Math.min(h, w);
			Image image = ImageUtils.resizeImage(mImage, size);

			int x = (w - size) / 2;
			int y = (h - size) / 2;
			g.drawImage(image, x, y, null);

		} else {

			g.setColor(Color.BLACK);
			g.fillRect(0, 0, w, h);
		}
	}

	public void refresh(BufferedImage image) {

		mImage = image;
		this.repaint();
	}

	public void reset() {

		refresh(null);
	}
}