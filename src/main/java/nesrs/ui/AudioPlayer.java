package nesrs.ui;

import java.io.IOException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

import nesrs.apu.AudioOutListener;

public class AudioPlayer implements AudioOutListener, AutoCloseable {
   public static final int BUFFER_SIZE = 2048 * 2;

   public static final int SAMPLE_RATE = 44100;
   public static final int BIT_DEPTH = 16;

   private static final AudioFormat AUDIO_FORMAT = new AudioFormat(
         SAMPLE_RATE,
         16,
         1, // Mono
         true, // Signed
         true);

   private static int[] SQUARE_OUT_TABLE;
   private static int[] TND_OUT_TABLE;

   // Init lookup tables for Mixer.
   static {
      SQUARE_OUT_TABLE = new int[31];
      SQUARE_OUT_TABLE[0] = 0;
      for (int i = 1; i < 31; i++) {
         SQUARE_OUT_TABLE[i] = (int)((95.52 / (8128.0 / i + 100)) * Short.MAX_VALUE);
      }

      TND_OUT_TABLE = new int[203];
      TND_OUT_TABLE[0] = 0;
      for (int i = 1; i < 203; i++) {
         TND_OUT_TABLE[i] = (int)((163.67 / (24329.0 / i + 100)) * Short.MAX_VALUE);
      }
   }

   private byte[] _audioBuffer = new byte[735*2];
   private int _audioBufferIndex = 0;

   private byte[] _audioSamples = new byte[735*2];

   private int _currentCycleInSample = 0;

   private SourceDataLine _sdl;

   public AudioPlayer() throws LineUnavailableException {
      _sdl = AudioSystem.getSourceDataLine(AUDIO_FORMAT);

      _sdl.open(AUDIO_FORMAT, BUFFER_SIZE);
      _sdl.start();
   }

   @Override
   public void handleAudio(int rec1Dac, int rec2Dac, int triDac, int randomDac, int dmcDac) {
//      rec1Dac = 0;
//      rec2Dac = 0;
//      triDac = 0;
      randomDac = 0;
//      dmcDac = 0;

      _currentCycleInSample++;

      // Downsample to 44.1KHz hence pick 1 sample every 40 NES cpu cycles.
      if (_currentCycleInSample != 40) {
         return;
      }

      // Mix
      int routAudio = SQUARE_OUT_TABLE[rec1Dac + rec2Dac];
      int coutAudio = TND_OUT_TABLE[3 * triDac + 2 * randomDac + dmcDac];
      int sample = routAudio + coutAudio;

      if (_audioBufferIndex == _audioBuffer.length) {
         _audioBufferIndex = 0;
         _audioSamples = _audioBuffer;
      }

      _audioBuffer[_audioBufferIndex] = (byte) ((sample >> 8) & 0x00FF);
      _audioBuffer[_audioBufferIndex + 1] = (byte) (sample & 0x00FF);
      _audioBufferIndex += 2;

      _currentCycleInSample = 0;
   }

   public void handleSamples(byte[] audioSamples) {
      _audioSamples = audioSamples;
   }

   @Override
   public void render() {
      _sdl.write(_audioSamples, 0, _audioSamples.length);
   }

   @Override
   public void close() throws IOException {
      _sdl.stop();
      _sdl.close();
   }
}