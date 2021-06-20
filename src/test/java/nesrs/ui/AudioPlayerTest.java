package nesrs.ui;

import static nesrs.ui.AudioPlayer.SAMPLE_RATE;

import org.junit.Test;

public class AudioPlayerTest {

   private static int intSample(double sample) {

      // clip if outside [-1, +1]
      if (Double.isNaN(sample)) throw new IllegalArgumentException("sample is NaN");
      if (sample < -1.0) sample = -1.0;
      if (sample > +1.0) sample = +1.0;

      // convert to bytes
      int s = (int) (Short.MAX_VALUE * sample);

      return s;
   }

   // create a note (sine wave) of the given frequency (Hz), for the given
   // duration (seconds) scaled to the given volume (amplitude)
   private static double[] note(double hz, double duration, double amplitude) {
       int n = (int) (SAMPLE_RATE * duration);
       double[] a = new double[n+1];
       for (int i = 0; i <= n; i++)
           a[i] = amplitude * Math.sin(2 * Math.PI * i * hz / SAMPLE_RATE);
       return a;
   }

   private byte[] notesToBytes(double[] notes) {
      byte[] result = new byte[notes.length * 2];
      
      for (int i = 0; i < notes.length; i++) {
         int sample = intSample(notes[i]);
    
         result[2 * i] = (byte)((sample >> 8) & 0x00FF);
         result[2 * i + 1] = (byte)(sample & 0x00FF);
      }
      
      return result;
   }
   
//   @Test
   public void testNotes() throws Exception {
      AudioPlayer audioPlayer = new AudioPlayer();

      // 440 Hz for 1 sec
      double freq = 440.0;
      double[] notes = note(freq, 1, 1);
      audioPlayer.handleSamples(notesToBytes(notes));

      // scale increments
      int[] steps = { 0, 2, 4, 5, 7, 9, 11, 12 };
      for (int i = 0; i < steps.length; i++) {
          double hz = 440.0 * Math.pow(2, steps[i] / 12.0);
          notes = note(hz, 1.0, 0.5);
          audioPlayer.handleSamples(notesToBytes(notes));
          audioPlayer.render();
      }

      // need to call this in non-interactive stuff so the program doesn't terminate
      // until all the sound leaves the speaker.
      audioPlayer.close();
   }
}
