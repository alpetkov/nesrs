package nesrs.apu.devices;

import nesrs.apu.devices.Timer;

import org.junit.Assert;
import org.junit.Test;

public class TimerTest {

   @Test
   public void testTimer() {
      Timer timer = new Timer(0);

      for (int i = 0; i < 10; i++) {
         boolean shouldOutputClock = timer.clock();
         Assert.assertFalse(shouldOutputClock);
      }

      timer.setPeriod(4);
      for (int i = 0; i < 3; i++) {
         boolean shouldOutputClock = timer.clock();
         Assert.assertFalse(shouldOutputClock);
      }
      boolean shouldOutputClock = timer.clock();
      Assert.assertTrue(shouldOutputClock);
   }
}
