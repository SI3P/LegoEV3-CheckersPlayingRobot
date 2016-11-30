package detection;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
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

		// Imgcodecs.imwrite("chessboard_pawns_"+(white?"white":"red") +
		// ".jpg",chessboardHalf);
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
		// Imgcodecs.imwrite("chessboard_negative"+(white?"white":"red")
		// +".jpg", chessboardPawns);

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
		List<Point> points;
		Mat chessboard, chessboardMatGrey;

		chessboard = new Mat();
		Core.flip(mat.t(), chessboard, 0);
		// Imgcodecs.imwrite("chessboard_captured.jpg", chessboard);
		chessboardMatGrey = new Mat();
		Imgproc.cvtColor(chessboard, chessboardMatGrey, Imgproc.COLOR_BGRA2GRAY);
		Imgproc.GaussianBlur(chessboardMatGrey, chessboardMatGrey, new Size(11, 11), 0);
		outerBox = new Mat(chessboard.size(), CvType.CV_8UC1);
		Imgproc.adaptiveThreshold(chessboardMatGrey, outerBox, 255, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C,
				Imgproc.THRESH_BINARY_INV, 5, 2);
		chessboardMatGrey.release();
		points = findContour(outerBox);
		Collections.sort(points, new ClockWiseComparator(points));
		outerBox = correctPerspective(chessboard, points);
		//Imgcodecs.imwrite("outerBox .jpg", outerBox);
		chessboard.release();

		mBoard = ImageUtils.mat2BufferedImage(outerBox);

		return outerBox;
	}

	private List<Point> findContour(Mat src) {

		List<Point> points = null;
		MatOfPoint contour;
		List<MatOfPoint> contours;
		double area, maxim = 0;
		MatOfPoint2f approx, curve;

		contours = new ArrayList<>();
		Imgproc.findContours(src, contours, new Mat(), Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);

		if (contours.size() > 0) {

			for (int contourIdx = 0; contourIdx < contours.size(); contourIdx++) {

				contour = contours.get(contourIdx);
				area = Imgproc.contourArea(contour);
				approx = new MatOfPoint2f();
				curve = new MatOfPoint2f(contour.toArray());
				Imgproc.approxPolyDP(curve, approx, Imgproc.arcLength(curve, true) * 0.02, true);

				if (approx.total() == 4 && maxim < area) {

					maxim = area;
					points = approx.toList();
				}
			}
		}

		return points;
	}

	private Mat correctPerspective(Mat mat, List<Point> ptsSrc) {

		Mat undistorted;
		Mat src, dst;
		Point ptTopLeft, ptTopRight, ptBottomRight, ptBottomLeft;
		double side, maxArea, area;
		List<Point> ptsDst;
		
		ptTopLeft = ptsSrc.get(0);
		ptBottomLeft = ptsSrc.get(1);
		ptBottomRight = ptsSrc.get(2);
		ptTopRight = ptsSrc.get(3);

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
		ptsDst = new ArrayList<Point>();
		ptsDst.add(new Point(0, 0));
		ptsDst.add(new Point(0, side - 1));
		ptsDst.add(new Point(side - 1, side - 1));
		ptsDst.add(new Point(side - 1, 0));
		src = Converters.vector_Point2f_to_Mat(ptsSrc);
		dst = Converters.vector_Point2f_to_Mat(ptsDst);
		Imgproc.warpPerspective(mat, undistorted, Imgproc.getPerspectiveTransform(src, dst), new Size(side, side));
		src.release();
		dst.release();

		return undistorted;
	}

	public BufferedImage getImageBoard() {

		return this.mBoard;
	}
}