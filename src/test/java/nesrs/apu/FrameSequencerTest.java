package nesrs.apu;

import nesrs.apu.FrameSequencer;
import nesrs.apu.channels.RandomWaveChannel;
import nesrs.apu.channels.RectangleWaveChannel;
import nesrs.apu.channels.TriangleWaveChannel;

import org.junit.Assert;
import org.junit.Test;

public class FrameSequencerTest {

   @Test
   public void testClockSequncer_LengthCounterClock() {
      RectangleWaveChannel rwc1 = new RectangleWaveChannel();

      FrameSequencer fs = new FrameSequencer(
            rwc1,
            new RectangleWaveChannel(),
            new TriangleWaveChannel(),
            new RandomWaveChannel());

      // Set length counter
      rwc1.writeCoarseTuneRegister(0x18);

      Assert.assertEquals(2, rwc1.getLengthCounterCount());

      // Set mode 0
      fs.writeRegister(0);

      // First sequence - no length counter changes
      fs.clockSequencer();
      Assert.assertEquals(2, rwc1.getLengthCounterCount());

      // Second sequence - length counter decrement
      fs.clockSequencer();
      Assert.assertEquals(1, rwc1.getLengthCounterCount());

      // Third sequence - no length counter changes
      fs.clockSequencer();
      Assert.assertEquals(1, rwc1.getLengthCounterCount());

      // Fourth sequence - length counter decrement
      fs.clockSequencer();
      Assert.assertEquals(0, rwc1.getLengthCounterCount());

      // First sequence - no length counter changes
      fs.clockSequencer();
      Assert.assertEquals(0, rwc1.getLengthCounterCount());

      // Second sequence - length counter decrement, but length counter already 0, so no changes
      fs.clockSequencer();
      Assert.assertEquals(0, rwc1.getLengthCounterCount());
   }
}
