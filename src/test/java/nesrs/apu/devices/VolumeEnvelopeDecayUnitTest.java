package nesrs.apu.devices;

import nesrs.apu.devices.VolumeEnvelopeDecayUnit;

import org.junit.Assert;
import org.junit.Test;

public class VolumeEnvelopeDecayUnitTest {

   @Test
   public void testPeriod() {
      VolumeEnvelopeDecayUnit envelope = new VolumeEnvelopeDecayUnit();

      envelope.write(false, false, 0xF);
      envelope.setDirty();

      envelope.clock();

      for (int i = 0xF; i>= 1; i--) {
         // Period is 16, so for the first 15 clocks it should return the same volume as before.
         for (int j = 1; j <= 15; j++) {
            envelope.clock();
            int volume = envelope.getVolume();
            Assert.assertEquals(i, volume);
         }

         // On clock 16 it should reduce the envelop value
         envelope.clock();
         int volume = envelope.getVolume();
         Assert.assertEquals(i - 1, volume);
      }

      // Assert volume remains 0 if looping is not on.
      for (int j = 1; j <= 32; j++) {
         envelope.clock();
         int volume = envelope.getVolume();
         Assert.assertEquals(0, volume);
      }
   }

   @Test
   public void testLooping() {
      VolumeEnvelopeDecayUnit envelope = new VolumeEnvelopeDecayUnit();

      envelope.write(true, false, 0xF);
      envelope.setDirty();
      envelope.clock();

      for (int i = 0xF; i>= 1; i--) {
         // Period is 16, so for the first 15 clocks it should return the same volume as before.
         for (int j = 1; j <= 15; j++) {
            envelope.clock();
            int volume = envelope.getVolume();
            Assert.assertEquals(i, volume);
         }

         // On clock 16 it should reduce the envelop value
         envelope.clock();
         int volume = envelope.getVolume();
         Assert.assertEquals(i - 1, volume);
      }

      for (int j = 1; j <= 15; j++) {
         envelope.clock();
         int volume = envelope.getVolume();
         Assert.assertEquals(0, volume);
      }

      // Assert on clock 16, sequence is restarted.
      envelope.clock();
      int volume = envelope.getVolume();
      Assert.assertEquals(0xF, volume);
   }
}
