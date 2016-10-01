package utils;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import org.opencv.core.Mat;

/**
 * @author Simone Papandrea
 * @version 1.0
 * @since 2015-10-06
 */
public class ImageUtils {

	public static BufferedImage mat2BufferedImage(Mat m) {

		BufferedImage img = null;

		if (m != null) {

			int bufferSize;
			byte[] b;
			int type;
			final byte[] targetPixels;

			if (m.channels() > 1)
				type = BufferedImage.TYPE_3BYTE_BGR;
			else
				type = BufferedImage.TYPE_BYTE_GRAY;

			bufferSize = m.channels() * m.cols() * m.rows();
			b = new byte[bufferSize];
			m.get(0, 0, b);
			img = new BufferedImage(m.cols(), m.rows(), type);
			targetPixels = ((DataBufferByte) img.getRaster().getDataBuffer()).getData();
			System.arraycopy(b, 0, targetPixels, 0, b.length);
		}

		return img;
	}

	public static BufferedImage resizeImage(BufferedImage originalImage, int size) {

		BufferedImage resizedImage;
		Graphics2D g;

		if (size <= 0)
			size = 1;

		resizedImage = new BufferedImage(size, size,
				originalImage.getType() == 0 ? BufferedImage.TYPE_INT_ARGB : originalImage.getType());
		g = resizedImage.createGraphics();
		g.drawImage(originalImage, 0, 0, size, size, null);
		g.dispose();

		return resizedImage;
	}
}