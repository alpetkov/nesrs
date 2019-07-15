package nesrs.ppu;

public interface VideoOutListener {
   void handleFrame(int[] framePixels);
   
   default void render() {}
}
