package nesrs.ui;

import java.io.IOException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

import nesrs.apu.AudioOutListener;
import nesrs.apu.devices.Mixer;

public class AudioPlayer implements AudioOutListener, AutoCloseable {
   public static final int BUFFER_SIZE = 2048 * 2;

   public static final int SAMPLE_RATE = 44100;
   public static final int BIT_DEPTH = 16;
   public static final boolean SIGNED = true;

   private static final AudioFormat PCM = new AudioFormat(
         SAMPLE_RATE,
         BIT_DEPTH,
         1, // Mono
         SIGNED, // Signed
         true);

   private int _currentCycleInSample = 0;

   private final Mixer mixer;

   private final byte[] _audioBuffer = new byte[735*2]; // (44100 / 60(fps))  * 2(bytes)
   private int _audioBufferIndex = 0;
   private byte[] _audioSamples = new byte[735*2]; // (44100 / 60(fps))  * 2(bytes)
   private SourceDataLine _sdl;

   public AudioPlayer() throws LineUnavailableException {
      mixer = new Mixer(Short.MAX_VALUE * 2 + 1, SIGNED);

      _sdl = AudioSystem.getSourceDataLine(PCM);

      _sdl.open(PCM, BUFFER_SIZE);
      _sdl.start();
   }

   @Override
   public void handleAudio(int rec1Dac, int rec2Dac, int triDac, int randomDac, int dmcDac) {
      _currentCycleInSample++;

      // Downsample to 44.1KHz hence pick 1 sample every 40 NES cpu cycles.
      if (_currentCycleInSample != 40) {
         return;
      }

      // Mix
      short sample = (short)mixer.mix(rec1Dac, rec2Dac, triDac, randomDac, dmcDac);

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