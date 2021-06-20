package nesrs.apu;

public interface AudioOutListener {
   void handleAudio(int rec1Dac, int rec2Dac, int triDac, int randomDac, int dmcDac);

   default void render() {}
}
