package life;

import java.awt.Dimension;
import java.awt.Toolkit;

public class Configuration {
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
	
	public static int getNumberThreads() {
		return (int)(Runtime.getRuntime().availableProcessors() * 1.5);
	}
	
	public static int getWidth() {
//		return maxWidth / 2 + 1;
		return 1000;
	}
	
	public static int getHeight() {
//		return maxHeight / 2 + 1;
		return 600;
	}
}
