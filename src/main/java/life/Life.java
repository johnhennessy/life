package life;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicBoolean;

public class Life implements ShutdownListener {
	private BufferDisplay display;
	private Board board;
	private Set<LifeCalculator> calculators;
	private AtomicBoolean running;
	
	public static void main(String[] args) {
		Life life = new Life();

		while (life.isRunning()) {
			life.nextGeneration();
		}
	}
	
	public Life() {
		running = new AtomicBoolean(true);
		
		display = new BufferDisplay("Game of Life");
		display.addShutdownListener(this);
		display.start();
		
		int width = display.getWidth();
		int height = display.getHeight();

		board = new Board(width, height);
		board.addHorizontalLine();
		board.addVerticalLine();
		
		board.addBufferChangeListener(display);

		int numberCalculators = Configuration.getNumberThreads();
		calculators = new HashSet<LifeCalculator>(
				numberCalculators);

		int xInterval = width / numberCalculators;

		int lastX = (width / numberCalculators + width % numberCalculators);

		for (int i = 0; i < numberCalculators - 1; ++i) {
			int x = xInterval * i;

			calculators.add(new LifeCalculator(board, x, 0, xInterval,
					height));
		}
		calculators.add(new LifeCalculator(board,
				xInterval * (numberCalculators-1), 0,
				lastX, height));
	}
	
	public void nextGeneration() {
		Set<FutureTask<Boolean>> running = new HashSet<FutureTask<Boolean>>(calculators.size());

		for (LifeCalculator calculator : calculators) {
			running.add(calculator.go());
		}

		for (FutureTask<Boolean> future : running) {
			try {
				future.get();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		if (this.running.get()) {
			board.flush();
		}
	}

	public void shutdownRequested() {
		running.set(false);
	}
	
	public boolean isRunning() {
		return running.get();
	}
}
