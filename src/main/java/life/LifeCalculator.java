package life;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

public class LifeCalculator {
	private static Executor executor = Executors.newCachedThreadPool();
	private int x;
	private int y;
	private int width;
	private int height;
	private Board board;
	
	public LifeCalculator(Board board, int x, int y, int width, int height) {
		this.board = board;
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}

	public FutureTask<Boolean> go() {
		FutureTask<Boolean> future = new FutureTask<Boolean>(new Task(), true);
		executor.execute(future);
		return future;
	}

	class Task implements Runnable {
		public void run() {
			board.update(x, y, width, height);	
		}
		
	}
}
