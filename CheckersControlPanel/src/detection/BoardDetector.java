package detection;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.utils.Converters;
import checkers.Chessboard;
import checkers.Pawn;
import checkers.Chessboard.BoardException;
import utils.ImageUtils;

/**
 * @author Simone Papandrea
 * @version 1.0
 * @since 2015-10-06
 */
public class BoardDetector {

	private static final int PAWN_AREA = 800;
	private Scalar mLR1, mUR1;
	private Scalar mLR2, mUR2;
	private BufferedImage mBoard;

	public Chessboard calibrate(Mat mat) throws BoardException {

		Mat chessboard;

		mLR1 = new Scalar(180, 225, 225);
		mLR2 = new Scalar(180, 225, 225);
		mUR1 = new Scalar(0, 0, 0);
		mUR2 = new Scalar(0, 0, 0);

		chessboard = analyze(mat);
		calibrate(chessboard, mLR1, mUR1, true);
		calibrate(chessboard, mLR2, mUR2, false);
		chessboard.release();

		return check(mat);
	}

	public Chessboard detect(Mat mat) throws BoardException {

		Mat chessboard;
		Mat chessboardHSV;
		List<Pawn> pawns;

		chessboard = analyze(mat);
		chessboardHSV = new Mat();
		pawns = new ArrayList<Pawn>();
		Imgproc.cvtColor(chessboard, chessboardHSV, Imgproc.COLOR_BGR2HSV);
		pawns.addAll(findPawns(chessboardHSV, true));
		pawns.addAll(findPawns(chessboardHSV, false));
		chessboardHSV.release();
		chessboard.release();

		return new Chessboard(pawns);
	}

	private Chessboard check(Mat mat) throws BoardException {

		Chessboard chessboard, temp;

		chessboard = this.detect(mat);
		temp = new Chessboard();
		temp.init();

		if (!chessboard.equals(temp))
			throw new BoardException();

		return chessboard;
	}

	private void calibrate(Mat chessboard, Scalar lowerHSV, Scalar upperHSV, boolean white) {

		Mat chessboardGray;
		Mat circles;
		Mat chessboardHalf;
		Mat chessboardHSV;
		double cx, cy, x, y, rad;
		double data[], hsv[], low, upper;
		int radius, threshold;
		int h, w;

		chessboardGray = new Mat();
		h = chessboard.height() / 2;
		w = chessboard.width();
		chessboardHalf = chessboard.submat(new Rect(0, white ? h : 0, w, h));
		chessboardHSV = new Mat();
		circles = new Mat();

		Imgproc.cvtColor(chessboardHalf, chessboardHSV, Imgproc.COLOR_BGR2HSV);
		Imgproc.medianBlur(chessboardHalf, chessboardHalf, 5);
		Imgproc.cvtColor(chessboardHalf, chessboardGray, Imgproc.COLOR_BGR2GRAY);
		Imgproc.HoughCircles(chessboardGray, circles, Imgproc.CV_HOUGH_GRADIENT, 2, chessboardGray.height() / 4, 50, 30,
				25, 30);

		chessboardGray.release();

		for (int i = 0; i < circles.cols(); i++) {

			data = circles.get(0, i);
			cx = Math.round(data[0]);
			cy = Math.round(data[1]);
			radius = (int) Math.round(data[2]) - 2;

			Imgproc.circle(chessboardHalf, new Point(cx, cy), radius, new Scalar(0, 255, 0));

			for (int r = 0; r < radius * 2 / 3; r += 5) {

				for (int z = 0; z < 360; z += 45) {

					rad = Math.toRadians(z);
					x = cx + Math.cos(rad) * r;
					y = cy + Math.sin(rad) * r;

					Imgproc.circle(chessboardHalf, new Point(x, y), 3, new Scalar(255, 0, 0));

					hsv = chessboardHSV.get((int) y, (int) x);

					if (hsv == null)
						continue;

					for (int j = 0; j < 3; j++) {

						threshold = j == 0 ? 0 : 5;
						low = Math.max(hsv[j] - threshold, 0);
						upper = Math.min(hsv[j] + threshold, j == 0 ? 180 : 225);

						if (lowerHSV.val[j] > low)
							lowerHSV.val[j] = low;

						if (upperHSV.val[j] < upper)
							upperHSV.val[j] = upper;
					}
				}
			}
		}

		// Imgcodecs.imwrite("chessboard_pawns_"+(white?"white":"red") + ".jpg",
		// chessboardHalf);

		chessboardHalf.release();
		chessboardHSV.release();
		circles.release();
	}

	private List<Pawn> findPawns(Mat chessboardMatHSV, boolean white) {

		List<Pawn> pawns;
		List<MatOfPoint> shapes;
		MatOfPoint2f cell2f;
		RotatedRect rect;
		Mat chessboardPawns;
		Scalar lower, upper;
		Point center;
		Size size;
		int i, j;
		double area;
		final double cellHeight, cellWidth;

		pawns = new ArrayList<Pawn>();
		shapes = new ArrayList<MatOfPoint>();
		chessboardPawns = new Mat();

		if (white) {

			lower = mLR1;
			upper = mUR1;

		} else {

			lower = mLR2;
			upper = mUR2;
		}

		Core.inRange(chessboardMatHSV, lower, upper, chessboardPawns);
		Imgproc.morphologyEx(chessboardPawns, chessboardPawns, Imgproc.MORPH_OPEN,
				Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(5, 5)));
		// Imgcodecs.imwrite("chessboard_negative"+(white?"white":"red") +
		// ".jpg", chessboardPawns);

		size = chessboardPawns.size();
		cellHeight = size.height / Chessboard.HEIGHT;
		cellWidth = size.width / Chessboard.HEIGHT;

		Imgproc.findContours(chessboardPawns, shapes, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE,
				new Point(0, 0));

		chessboardPawns.release();

		for (MatOfPoint shape : shapes) {

			area = Imgproc.contourArea(shape, false);

			if (area >= PAWN_AREA) {

				cell2f = new MatOfPoint2f();

				Imgproc.approxPolyDP(new MatOfPoint2f(shape.toArray()), cell2f, 2, true);
				rect = Imgproc.minAreaRect(cell2f);

				center = rect.center;
				i = (int) Math.floor(center.y / cellHeight);
				j = (int) Math.floor(center.x / cellWidth);
				pawns.add(new Pawn(i, j, white));
			}
		}

		return pawns;
	}

	private Mat analyze(Mat mat) {

		Mat outerBox;
		Mat chessboard = new Mat(), chessboardMatGrey = new Mat();
		Mat lines;
		final Mat kernel;
		final byte[] data = { 0, 1, 0, 1, 1, 1, 0, 1, 0 };
		double[][] exLines;
		Point[] points;

		Core.flip(mat.t(), chessboard, 0);
		// Imgcodecs.imwrite("chessboard_captured.jpg", chessboard);
		Imgproc.cvtColor(chessboard, chessboardMatGrey, Imgproc.COLOR_BGRA2GRAY);
		Imgproc.GaussianBlur(chessboardMatGrey, chessboardMatGrey, new Size(11, 11), 0);

		outerBox = new Mat(chessboard.size(), CvType.CV_8UC1);
		Imgproc.adaptiveThreshold(chessboardMatGrey, outerBox, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C,
				Imgproc.THRESH_BINARY, 5, 2);
		chessboardMatGrey.release();
		Core.bitwise_not(outerBox, outerBox);

		kernel = new Mat(3, 3, CvType.CV_8U);
		kernel.put(0, 0, data);
		Imgproc.dilate(outerBox, outerBox, kernel);

		outerBox = findMaxBlob(outerBox);
		Imgproc.erode(outerBox, outerBox, kernel);
		kernel.release();

		lines = findLines(outerBox);
		lines = mergeLines(chessboard.size(), lines);
		exLines = findExtremeLines(lines);

		lines.release();
		points = calculateIntersect(exLines, outerBox.size());
		outerBox = correctPerspective(chessboard, points);
		chessboard.release();

		mBoard = ImageUtils.mat2BufferedImage(outerBox);

		return outerBox;
	}

	private Mat findMaxBlob(Mat outerBox) {

		double[] pixels;
		double max = -1;
		int area;
		Point maxPt = null;
		Mat mask;

		mask = Mat.zeros(outerBox.rows() + 2, outerBox.cols() + 2, CvType.CV_8U);

		for (int y = 0; y < outerBox.size().height; y++) {

			for (int x = 0; x < outerBox.size().width; x++) {

				pixels = outerBox.get(y, x);

				if (pixels[0] >= 128) {

					area = Imgproc.floodFill(outerBox, mask, new Point(x, y), new Scalar(164, 164, 164));

					if (area > max) {

						maxPt = new Point(x, y);
						max = area;
					}
				}
			}
		}

		mask.release();
		mask = Mat.zeros(outerBox.rows() + 2, outerBox.cols() + 2, CvType.CV_8U);
		Imgproc.floodFill(outerBox, mask, maxPt, new Scalar(255, 255, 255));

		mask.release();
		mask = Mat.zeros(outerBox.rows() + 2, outerBox.cols() + 2, CvType.CV_8U);

		for (int y = 0; y < outerBox.size().height; y++) {

			for (int x = 0; x < outerBox.size().width; x++) {

				pixels = outerBox.get(y, x);

				if (pixels[0] == 164 && x != maxPt.x && y != maxPt.y)
					Imgproc.floodFill(outerBox, mask, new Point(x, y), new Scalar(0, 0, 0));
			}
		}

		mask.release();

		return outerBox;
	}

	private Mat findLines(Mat outerBox) {

		Mat lines = new Mat();

		Imgproc.HoughLines(outerBox, lines, 1, Math.PI / 180, 200);

		return lines;
	}

	private Mat mergeLines(Size size, Mat lines) {

		double[] data1, data2;
		double rho1, theta1, rho2, theta2;
		Point ptA1, ptB1, ptA2, ptB2;

		for (int x = 0; x < lines.rows(); x++) {

			data1 = lines.get(x, 0);
			rho1 = data1[0];
			theta1 = data1[1];

			if (rho1 == 0 && theta1 == -100)
				continue;

			ptA1 = new Point();
			ptB1 = new Point();

			if (theta1 > Math.PI * 45 / 180 && theta1 < Math.PI * 135 / 180) {

				ptA1.x = 0;
				ptA1.y = rho1 / Math.sin(theta1);
				ptB1.x = size.width;
				ptB1.y = -ptB1.x / Math.tan(theta1) + rho1 / Math.sin(theta1);
			} else {

				ptA1.y = 0;
				ptA1.x = rho1 / Math.cos(theta1);
				ptB1.y = size.height;
				ptB1.x = -ptB1.y / Math.tan(theta1) + rho1 / Math.cos(theta1);
			}

			for (int y = 0; y < lines.rows(); y++) {

				if (x != y) {

					data2 = lines.get(y, 0);
					rho2 = data2[0];
					theta2 = data2[1];

					if (Math.abs(rho2 - rho1) < 20 && Math.abs(theta2 - theta1) < Math.PI * 10 / 180) {

						ptA2 = new Point();
						ptB2 = new Point();

						if (theta2 > Math.PI * 45 / 180 && theta2 < Math.PI * 135 / 180) {

							ptA2.x = 0;
							ptA2.y = rho2 / Math.sin(theta2);
							ptB2.x = size.width;
							ptB2.y = -ptB2.x / Math.tan(theta2) + rho2 / Math.sin(theta2);
						} else {

							ptA2.y = 0;
							ptA2.x = rho2 / Math.cos(theta2);
							ptB2.y = size.height;
							ptB2.x = -ptB2.y / Math.tan(theta2) + rho2 / Math.cos(theta2);
						}

						if (((double) (ptA2.x - ptA1.x) * (ptA2.x - ptA1.x) + (ptA2.y - ptA1.y) * (ptA2.y - ptA1.y) < 64
								* 64)
								&& ((double) (ptB2.x - ptB1.x) * (ptB2.x - ptB1.x)
										+ (ptB2.y - ptB1.y) * (ptB2.y - ptB1.y) < 64 * 64)) {

							lines.put(x, 0, new double[] { (rho1 + rho2) / 2, (theta1 + theta2) / 2 });
							lines.put(y, 0, new double[] { 0, -100 });
						}
					}
				}
			}
		}

		return lines;
	}

	private double[][] findExtremeLines(Mat lines) {

		double[][] exLines;
		double[] topLine, bottomLine, leftLine, rightLine;
		double[] data;
		double lIntercept = 100000, rIntercept = 0, xIntercept;
		double rho, theta;

		topLine = new double[] { 1000, 1000 };
		bottomLine = new double[] { -1000, -1000 };
		leftLine = new double[] { 1000, 1000 };
		rightLine = new double[] { -1000, -1000 };

		for (int x = 0; x < lines.rows(); x++) {

			data = lines.get(x, 0);
			rho = data[0];
			theta = data[1];

			if (rho == 0 && theta == -100)
				continue;

			xIntercept = rho / Math.cos(theta);

			if (theta > Math.PI * 80 / 180 && theta < Math.PI * 100 / 180) {

				if (rho < topLine[0])
					topLine = data;

				if (rho > bottomLine[0])
					bottomLine = data;
			}

			else if (theta < Math.PI * 10 / 180 || theta > Math.PI * 170 / 180) {

				if (xIntercept > rIntercept) {

					rightLine = data;
					rIntercept = xIntercept;

				} else if (xIntercept <= lIntercept) {

					leftLine = data;
					lIntercept = xIntercept;
				}
			}
		}

		exLines = new double[4][];
		exLines[0] = topLine;
		exLines[1] = bottomLine;
		exLines[2] = leftLine;
		exLines[3] = rightLine;

		return exLines;
	}

	private Point[] calculateIntersect(double[][] exLines, Size size) {

		Point ptTopLeft, ptTopRight, ptBottomRight, ptBottomLeft;
		Point left1, left2, right1, right2, bottom1, bottom2, top1, top2;
		double topA, topB, topC;
		double bottomA, bottomB, bottomC;
		double leftA, leftB, leftC;
		double rightA, rightB, rightC;
		double topLeft, topRight, bottomRight, bottomLeft;
		double[] topLine, bottomLine, leftLine, rightLine;
		final double height = size.height;
		final double width = size.width;

		topLine = exLines[0];
		bottomLine = exLines[1];
		leftLine = exLines[2];
		rightLine = exLines[3];
		left1 = new Point();
		left2 = new Point();

		if (leftLine[1] != 0) {

			left1.x = 0;
			left1.y = leftLine[0] / Math.sin(leftLine[1]);
			left2.x = width;
			left2.y = -left2.x / Math.tan(leftLine[1]) + left1.y;

		} else {

			left1.y = 0;
			left1.x = leftLine[0] / Math.cos(leftLine[1]);
			left2.y = height;
			left2.x = left1.x - height * Math.tan(leftLine[1]);
		}

		leftA = left2.y - left1.y;
		leftB = left1.x - left2.x;
		leftC = leftA * left1.x + leftB * left1.y;
		right1 = new Point();
		right2 = new Point();

		if (rightLine[1] != 0) {

			right1.x = 0;
			right1.y = rightLine[0] / Math.sin(rightLine[1]);
			right2.x = width;
			right2.y = -right2.x / Math.tan(rightLine[1]) + right1.y;

		} else {

			right1.y = 0;
			right1.x = rightLine[0] / Math.cos(rightLine[1]);
			right2.y = height;
			right2.x = right1.x - height * Math.tan(rightLine[1]);
		}

		rightA = right2.y - right1.y;
		rightB = right1.x - right2.x;
		rightC = rightA * right1.x + rightB * right1.y;
		bottom1 = new Point();
		bottom2 = new Point();
		bottom1.x = 0;
		bottom1.y = bottomLine[0] / Math.sin(bottomLine[1]);
		bottom2.x = width;
		bottom2.y = -bottom2.x / Math.tan(bottomLine[1]) + bottom1.y;
		bottomA = bottom2.y - bottom1.y;
		bottomB = bottom1.x - bottom2.x;
		bottomC = bottomA * bottom1.x + bottomB * bottom1.y;
		top1 = new Point();
		top2 = new Point();
		top1.x = 0;
		top1.y = topLine[0] / Math.sin(topLine[1]);
		top2.x = width;
		top2.y = -top2.x / Math.tan(topLine[1]) + top1.y;
		topA = top2.y - top1.y;
		topB = top1.x - top2.x;
		topC = topA * top1.x + topB * top1.y;

		topLeft = leftA * topB - leftB * topA;
		ptTopLeft = new Point((topB * leftC - leftB * topC) / topLeft, (leftA * topC - topA * leftC) / topLeft);

		topRight = rightA * topB - rightB * topA;
		ptTopRight = new Point((topB * rightC - rightB * topC) / topRight, (rightA * topC - topA * rightC) / topRight);

		bottomRight = rightA * bottomB - rightB * bottomA;
		ptBottomRight = new Point((bottomB * rightC - rightB * bottomC) / bottomRight,
				(rightA * bottomC - bottomA * rightC) / bottomRight);

		bottomLeft = leftA * bottomB - leftB * bottomA;
		ptBottomLeft = new Point((bottomB * leftC - leftB * bottomC) / bottomLeft,
				(leftA * bottomC - bottomA * leftC) / bottomLeft);

		return new Point[] { ptTopLeft, ptTopRight, ptBottomRight, ptBottomLeft };
	}

	private Mat correctPerspective(Mat mat, Point[] ptsSrc) {

		Mat undistorted;
		Mat src, dst;
		Point ptTopLeft, ptTopRight, ptBottomRight, ptBottomLeft;
		double side, maxArea, area;

		ptTopLeft = ptsSrc[0];
		ptTopRight = ptsSrc[1];
		ptBottomRight = ptsSrc[2];
		ptBottomLeft = ptsSrc[3];

		maxArea = Math.pow((ptBottomLeft.x - ptBottomRight.x), 2) + Math.pow((ptBottomLeft.y - ptBottomRight.y), 2);
		area = Math.pow((ptTopRight.x - ptBottomRight.x), 2) + Math.pow((ptTopRight.y - ptBottomRight.y), 2);

		if (area > maxArea)
			maxArea = area;

		area = Math.pow((ptTopRight.x - ptTopLeft.x), 2) + Math.pow((ptTopRight.y - ptTopLeft.y), 2);

		if (area > maxArea)
			maxArea = area;

		area = Math.pow((ptBottomLeft.x - ptTopLeft.x), 2) + Math.pow((ptBottomLeft.y - ptTopLeft.y), 2);

		if (area > maxArea)
			maxArea = area;

		side = Math.sqrt((double) maxArea);
		undistorted = new Mat(new Size(side, side), CvType.CV_8UC1);

		List<Point> ptsDst = new ArrayList<Point>();
		ptsDst.add(new Point(0, 0));
		ptsDst.add(new Point(side - 1, 0));
		ptsDst.add(new Point(side - 1, side - 1));
		ptsDst.add(new Point(0, side - 1));

		src = Converters.vector_Point2f_to_Mat(Arrays.asList(ptsSrc));
		dst = Converters.vector_Point2f_to_Mat(ptsDst);

		Imgproc.warpPerspective(mat, undistorted, Imgproc.getPerspectiveTransform(src, dst), new Size(side, side));

		src.release();
		dst.release();

		// Imgcodecs.imwrite("chessboard_warped.jpg", undistorted);

		return undistorted;
	}

	public BufferedImage getImageBoard() {

		return this.mBoard;
	}
}