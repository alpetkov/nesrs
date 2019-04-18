package nesrs.ppu;

public interface VideoOutListener {
	void handleFrame(int[] scanlineVideo);
//
//	default void handleNameTables(int[][][] nameTablesVideo) {
//
//	}
}
