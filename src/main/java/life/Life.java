package life;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicBoolean;

public class Life implements ShutdownListener {
	private BufferDisplay display;
	private Board board;
	private Set<LifeCalculator> calculators;
	private AtomicBoolean running;

	public static void main(String[] args) throws IOException {
		Life life = new Life(args.length == 0);

		if (args.length > 0) {
			File inputFile = new File(args[0]);
			LifeReader lifeReader = LifeReader.getLifeReader(inputFile,
					life.board);
			lifeReader.load();
			life.board.displayCurrent();
		}

		long interval = -1L;
		if (args.length > 1) {
			interval = Long.parseLong(args[1]);
		}

		while (life.isRunning()) {
			life.nextGeneration();

			if (interval > 0L) {
				try {
					Thread.sleep(interval);
				} catch (Exception e) {

				}
			}
		}
	}

	public Life(boolean drawLine) {
		running = new AtomicBoolean(true);

		display = new BufferDisplay("Game of Life", Configuration.getCellSize());
		display.addShutdownListener(this);
		display.start();

		int width = display.getWidth();
		int height = display.getHeight();

		board = new Board(width, height);
		// board.addHorizontalLine();
		// board.addVerticalLine();
		
		if (drawLine) {
			board.addLine(LineType.VERTICAL);
			board.addLine(LineType.HORIZONTAL);
		}

		// board.addLine(LineType.NEGATIVE_DIAGIONAL);
		// board.addLine(LineType.POSITIVE_DIAGIONAL);

		board.addBufferChangeListener(display);

		int numberCalculators = Configuration.getNumberThreads();
		calculators = new HashSet<LifeCalculator>(numberCalculators);

		int xInterval = width / numberCalculators;

		int lastX = (width / numberCalculators + width % numberCalculators);

		for (int i = 0; i < numberCalculators - 1; ++i) {
			int x = xInterval * i;

			calculators.add(new LifeCalculator(board, x, 0, xInterval, height));
		}
		calculators.add(new LifeCalculator(board, xInterval
				* (numberCalculators - 1), 0, lastX, height));
	}

	public void nextGeneration() {
		Set<FutureTask<Boolean>> running = new HashSet<FutureTask<Boolean>>(
				calculators.size());

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
