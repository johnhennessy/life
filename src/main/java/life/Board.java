package life;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Board implements BufferProvider, LifeDish {
	private Map<BufferChangeListener, BufferChangeListener> listeners;
	private boolean[][] current;
	private boolean[][] next;
	private int width;
	private int height;
	private LifeRules rules;

	public Board(int width, int height) {
		this.width = width;
		this.height = height;

		current = new boolean[width][height];
		next = new boolean[width][height];

		listeners = new ConcurrentHashMap<BufferChangeListener, BufferChangeListener>();
		rules = new LifeRules();
	}

	public void addBufferChangeListener(BufferChangeListener listener) {
		listeners.put(listener, listener);
	}

	public void removeBufferChangeListener(BufferChangeListener listener) {
		listeners.remove(listener);
	}

	long count = 0;
	long start = System.currentTimeMillis();

	public void displayCurrent() {
		boolean[][] buffer = new boolean[width][height];
		for (int x = 0;x < width;++x) {
			for (int y = 0;y < height;++y) {
				buffer[x][y] = current[x][y];
			}
		}
		
		for (BufferChangeListener listener : listeners.keySet()) {
			listener.bufferChanged(buffer);
		}
	}
	
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
					next[cellX][cellY] = rules.isSurvivor(neighborCount);
				} else {
					next[cellX][cellY] = rules.isBirth(neighborCount);
				}
			}
		}
	}
	
	public void addLine(LineType lineType) {
		int x = 0;
		int y = 0;
		
		if (lineType == LineType.HORIZONTAL) {
			y = height / 2;
			for (x = 0;x < width;++x) {
				current[x][y] = true;
			}
		} else if (lineType == LineType.VERTICAL) {
			x = width / 2;
			for (y = 0;y < height;++y) {
				current[x][y] = true;
			}
		} else if (lineType == LineType.NEGATIVE_DIAGIONAL || lineType == LineType.POSITIVE_DIAGIONAL) {
			int centerX = width / 2;
			int centerY = height / 2;
			
			int minX = centerX - centerY;
			if (minX < 0) {
				minX = 0;
			}
			int minY = centerY - centerX;
			if (minY < 0) {
				minY = 0;
			}
			
			int maxX = (2 * centerX) - minX;
			int maxY = (2 * centerY) - minY;
			
			int xStep = 1;
			int yStep = 1;
			
			if (lineType == LineType.NEGATIVE_DIAGIONAL) {
				x = minX;
				y = minY;
			} else {
				x = minX;
				y = maxY - 1;
				yStep = -1;
			}
			
			while (x >= minX && x < maxX && y >= minY && y < maxY) {
				current[x][y] = true;
				
				x += xStep;
				y += yStep;
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

	public void bloom(int x, int y) {
		current[x][y] = true;
	}

	public void kill(int x, int y) {
		current[x][y] = false;
	}

	public boolean isAlive(int x, int y) {
		return current[x][y];
	}

	public void setRules(LifeRules rules) {
		this.rules = rules;
	}

	public CoordinateSystem getCoordinateSystem() {
		return CoordinateSystem.ABSOLUTE;
	}

	public int getAbsoluteWidth() {
		return width;
	}

	public int getAbsoluteHeight() {
		return height;
	}
}
