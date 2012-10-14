package life;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Board implements BufferProvider {
	private Map<BufferChangeListener, BufferChangeListener> listeners;
	private boolean[][] current;
	private boolean[][] next;
	private int width;
	private int height;

	public Board(int width, int height) {
		this.width = width;
		this.height = height;

		current = new boolean[width][height];
		next = new boolean[width][height];

		listeners = new ConcurrentHashMap<BufferChangeListener, BufferChangeListener>();

	}

	public void addBufferChangeListener(BufferChangeListener listener) {
		listeners.put(listener, listener);
	}

	public void removeBufferChangeListener(BufferChangeListener listener) {
		listeners.remove(listener);
	}

	long count = 0;
	long start = System.currentTimeMillis();

	public void flush() {
		++count;
		long time = System.currentTimeMillis();
		long delta = time - start;
		if (delta >= 1000L) {
			double gps = 1000.0 * (double) count / (double) delta;
			System.out.println(gps + " gps");
			start = time;
			count = 0;
		}
		boolean[][] buffer = next;
		current = next;
		next = new boolean[width][height];

		for (BufferChangeListener listener : listeners.keySet()) {
			listener.bufferChanged(buffer);
		}
	}

	public void update(int x, int y, int width, int height) {
		int cellX;
		int cellY;
		int neighborXStart;
		int neighborXEnd;
		int neighborYStart;
		int neighborYEnd;

		int neighborX;
		int neighborY;

		int neighborCount;

		for (cellX = x; cellX < x + width; ++cellX) {
			for (cellY = y; cellY < y + height; ++cellY) {
				neighborXStart = cellX - 1;
				neighborXEnd = cellX + 1;

				neighborYStart = cellY - 1;
				neighborYEnd = cellY + 1;

				if (neighborXStart < 0) {
					neighborXStart = 0;
				}
				if (neighborXEnd >= this.width) {
					neighborXEnd = this.width - 1;
				}

				if (neighborYStart < 0) {
					neighborYStart = 0;
				}
				if (neighborYEnd >= this.height) {
					neighborYEnd = this.height - 1;
				}

				neighborCount = 0;
				for (neighborX = neighborXStart; neighborX <= neighborXEnd; ++neighborX) {
					for (neighborY = neighborYStart; neighborY <= neighborYEnd; ++neighborY) {
						if (neighborX != cellX || neighborY != cellY) {
							if (current[neighborX][neighborY]) {
								++neighborCount;
							}
						}
					}
				}

				if (current[cellX][cellY]) {
					if (neighborCount >= 2 && neighborCount < 4) {
						next[cellX][cellY] = true;
					} else {
						next[cellX][cellY] = false;
					}
				} else if (neighborCount == 3) {
					next[cellX][cellY] = true;
				} else {
					next[cellX][cellY] = false;
				}
			}
		}
	}

	public void addVerticalLine() {
		int x = width / 2;
		for (int y = 0; y < height; ++y) {
			current[x][y] = true;
		}
	}

	public void addHorizontalLine() {
		int y = height / 2;

		for (int x = 0; x < width; ++x) {
			current[x][y] = true;
		}
	}
}
