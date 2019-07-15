package nesrs.apu;

public interface AudioOutListener {
   void handleSamples(byte[] audioSamples);
   
   default void render() {}
}
