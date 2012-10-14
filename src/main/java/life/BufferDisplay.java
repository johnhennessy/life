package life;

import java.awt.BorderLayout;
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
	private int size;
	private JFrame frame;
	private JPanel panel;
	private int screenWidth;
	private int screenHeight;
	private int virtualWidth;
	private int virtualHeight;
	
	private BlockingQueue<boolean[][]> bufferQueue = new LinkedBlockingQueue<boolean[][]>(
			1000);
	private Map<ShutdownListener, ShutdownListener> shutdownListeners = new ConcurrentHashMap<ShutdownListener, ShutdownListener>();

	private boolean[][] pixelBuffer;

	public BufferDisplay(String name, int size) {
		this.size = size;
		frame = new JFrame(name);
		frame.setUndecorated(true);
		frame.setVisible(true);
		frame.createBufferStrategy(2);

		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		virtualWidth = Configuration.getWidth();
		virtualHeight = Configuration.getHeight();
		
		screenWidth = virtualWidth * size;
		screenHeight = virtualHeight * size;

		frame.setSize(screenWidth, screenHeight);
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
			while (!bufferQueue.offer(pixelBuffer, 10L, TimeUnit.MILLISECONDS));
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
				int virtualWidth = getVirtualWidth();
				int virtualHeight = getVirtualHeight();

				if (virtualWidth > 0 && virtualHeight > 0) {
					for (int i = 0; i < virtualWidth; ++i) {
						for (int j = 0; j < virtualHeight; ++j) {
							if (pixelBuffer[i][j]) {
								graphics.fillRect(i * size, j * size, size, size);
							}
						}
					}
				}
			}

			bf.show();

			Toolkit.getDefaultToolkit().sync();
		}
	}
	
	private int getVirtualWidth() {
		if (pixelBuffer == null) {
			return 0;
		} else if (virtualWidth < pixelBuffer.length) {
			return virtualWidth;
		} else {
			return pixelBuffer.length;
		}
	}
	
	private int getVirtualHeight() {
		if (pixelBuffer == null || pixelBuffer.length == 0) {
			return 0;
		} else if (virtualHeight < pixelBuffer[0].length) {
			return virtualHeight;
		} else {
			return pixelBuffer[0].length;
		}
	}

	private int getScreenWidth() {
		return getVirtualWidth() * size;
	}

	private int getScreenHeight() {
		return getVirtualHeight() * size;
	}

	private void clear(Graphics2D graphics) {
		graphics.setColor(Configuration.BACKGROUND_COLOR);
		graphics.fillRect(0, 0, screenWidth, screenHeight);
		graphics.setColor(Configuration.getForegroundColor());
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
		return virtualWidth;
	}

	public int getHeight() {
		return virtualHeight;
	}
}