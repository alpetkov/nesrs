package nesrs.ui;

import java.io.IOException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

import nesrs.apu.Apu;
import nesrs.apu.AudioOutListener;

public class AudioPlayer implements AudioOutListener, AutoCloseable {

   private static final AudioFormat AUDIO_FORMAT = new AudioFormat(
         Apu.SAMPLE_RATE,
         Apu.BIT_DEPTH,
         Apu.NUM_CHANNELS,
         true, /*AudioFormat.Encoding.PCM_SIGNED*/
         Apu.BIG_ENDIAN);

   private byte[] _audioSamples = new byte[735*2];
   
   private SourceDataLine _sdl;

   public AudioPlayer() throws LineUnavailableException {
      _sdl = AudioSystem.getSourceDataLine(AUDIO_FORMAT);

      _sdl.open(AUDIO_FORMAT, Apu.BUFFER_SIZE);
      _sdl.start();
   }

   @Override
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