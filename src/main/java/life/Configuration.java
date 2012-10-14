package life;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;

public class Configuration {
	public static Color BACKGROUND_COLOR = Color.BLACK;
	public static Color FOREGROUND_COLOR = Color.GREEN;
	
	private static int maxHeight;
	private static int maxWidth;
	
	static {
		// Get the default toolkit
		Toolkit toolkit = Toolkit.getDefaultToolkit();

		// Get the current screen size
		Dimension screenSize = toolkit.getScreenSize();
		maxWidth = screenSize.width;
		maxHeight = screenSize.height;
	}
	
	private static int r = 255;
	private static int g = 255;
	private static int b = 255;
	private static int colorDirection = -1;
	
	public static int getNumberThreads() {
		return (int)(Runtime.getRuntime().availableProcessors() * 2);
	}
	
	public static int getCellSize() {
		return 2;
	}
	
	public static int getWidth() {
		return maxWidth / getCellSize();
//		return 1001;
//		return maxWidth;
//		return 400;
	}
	
	public static int getHeight() {
		return maxHeight / getCellSize();
//		return 601;
//		return maxHeight;
//		return 300;
	}
	
	public static Color getForegroundColor() {
//		r += colorDirection;
//		g += colorDirection;
//		b += colorDirection;
//		
//		if (r < 0) {
//			r = 1;
//			g = 1;
//			b = 1;
//			
//			colorDirection = 1;
//		} else if (r > 255) {
//			r = 254;
//			g = 254;
//			b = 254;
//			
//			colorDirection = -1;
//		}
//
////		System.out.println("rgb("+r+","+g+","+b+")");
//			
//		return new Color(r, g, b);
		
		return FOREGROUND_COLOR;
	}
}
