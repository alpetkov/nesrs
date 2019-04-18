package nesrs.apu;

public interface AudioOutListener {
   void handleSamples(byte[] audioSamples);
}
