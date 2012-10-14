package life;

public interface BufferProvider {
	void addBufferChangeListener(BufferChangeListener listener);
	void removeBufferChangeListener(BufferChangeListener listener);
}
