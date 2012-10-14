package life;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferStrategy;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class BufferDisplay extends Thread implements BufferChangeListener,
		KeyListener {
	public static final Color BACKGROUND = Color.BLACK;
	public static final Color FOREGROUND = Color.GREEN;

	private JFrame frame;
	private JPanel panel;
	private int width;
	private int height;
	private BlockingQueue<boolean[][]> bufferQueue = new LinkedBlockingQueue<boolean[][]>(
			100);
	private Map<ShutdownListener, ShutdownListener> shutdownListeners = new ConcurrentHashMap<ShutdownListener, ShutdownListener>();

	private boolean[][] pixelBuffer;

	public BufferDisplay(String name) {
		frame = new JFrame(name);
		frame.setUndecorated(true);
		frame.setVisible(true);
		frame.createBufferStrategy(2);

		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		width = Configuration.getWidth();
		height = Configuration.getHeight();

		frame.setSize(width, height);
		panel = new JPanel();
		// panel = new JPanel();
		Container contentPane = frame.getContentPane();
		contentPane.setLayout(new BorderLayout());
		contentPane.add(panel, BorderLayout.CENTER);
		redraw();

		frame.addKeyListener(this);
	}

	public void bufferChanged(boolean[][] pixelBuffer) {
		try {
			while (!bufferQueue.offer(pixelBuffer, 100L, TimeUnit.MILLISECONDS));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void run() {
		redraw();
		try {
			for (;;) {
				pixelBuffer = bufferQueue.take();
				redraw();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void redraw() {
		if (frame.isVisible()) {
			BufferStrategy bf = frame.getBufferStrategy();
			Graphics2D graphics = (Graphics2D) bf.getDrawGraphics();
			clear(graphics);

			if (pixelBuffer != null) {
				int actualWidth = getActualWidth();
				int actualHeight = getActualHeight();

				if (actualWidth > 0 && actualHeight > 0) {
					for (int i = 0; i < actualWidth; ++i) {
						for (int j = 0; j < actualHeight; ++j) {
							if (pixelBuffer[i][j]) {
								graphics.drawRect(i, j, 1, 1);
							}
						}
					}
				}
			}

			bf.show();

			Toolkit.getDefaultToolkit().sync();
		}
	}

	private int getActualWidth() {
		if (pixelBuffer == null) {
			return 0;
		} else if (width < pixelBuffer.length) {
			return width;
		} else {
			return pixelBuffer.length;
		}
	}

	private int getActualHeight() {
		if (pixelBuffer == null || pixelBuffer.length == 0) {
			return 0;
		} else if (height < pixelBuffer[0].length) {
			return height;
		} else {
			return pixelBuffer[0].length;
		}
	}

	private void clear(Graphics2D graphics) {
		graphics.setColor(BACKGROUND);
		graphics.fillRect(0, 0, width, height);
		graphics.setColor(FOREGROUND);
	}

	public void addShutdownListener(ShutdownListener shutdownListener) {
		shutdownListeners.put(shutdownListener, shutdownListener);
	}

	public void removeShutdownListener(ShutdownListener shutdownListener) {
		shutdownListeners.remove(shutdownListener);
	}

	public void keyTyped(KeyEvent e) {
		if (e.getKeyChar() == 'c') {
			System.exit(0);
			System.out.println("Shutting down");
			for (ShutdownListener shutdownListener : shutdownListeners.keySet()) {
				shutdownListener.shutdownRequested();
			}
			frame.dispose();
		}
	}

	public void keyPressed(KeyEvent e) {

	}

	public void keyReleased(KeyEvent e) {

	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}
}