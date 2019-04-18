package nesrs.apu.devices;

import org.junit.Assert;
import org.junit.Test;

import nesrs.apu.devices.FrequencySweepUnit.ClockStatus;

public class FrequencySweepUnitTest {

   @Test
   public void testClock() {
      FrequencySweepUnit sweepUnit = new FrequencySweepUnit(false);

      ClockStatus clockStatus;

      int period = 3;
      int shiftCount = 2;
      sweepUnit.write(true, period, false, shiftCount);

      int channelPeriod = 0x00F0; // 11 bits
      for (int i = 1; i <= period + 1; i++) {
         sweepUnit.clock(channelPeriod);
         clockStatus = sweepUnit.getStatus();
         Assert.assertFalse(clockStatus.shouldChangeChannelTimerRawPeriod);
      }

      sweepUnit.clock(channelPeriod);
      clockStatus = sweepUnit.getStatus();
      Assert.assertTrue(clockStatus.shouldChangeChannelTimerRawPeriod);
      Assert.assertEquals(channelPeriod + (channelPeriod >> shiftCount), clockStatus.newTimerRawPeriod);
   }
}
