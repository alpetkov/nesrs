package nesrs.apu.devices;

import nesrs.apu.devices.LengthCounter;

import org.junit.Assert;
import org.junit.Test;

public class LengthCounterTest {

   @Test
   public void testLengthCounter() {
      LengthCounter lc = new LengthCounter();

      lc.setCount(0x03); // 2

      int currentCount;

      currentCount = lc.getCount();
      Assert.assertEquals(2, currentCount);

      lc.clock();
      currentCount = lc.getCount();
      Assert.assertEquals(1, currentCount);

      lc.clock();
      currentCount = lc.getCount();
      Assert.assertEquals(0, currentCount);

      lc.clock();
      currentCount = lc.getCount();
      Assert.assertEquals(0, currentCount);
   }

   @Test
   public void testLengthCounter_Halted() {
      LengthCounter lc = new LengthCounter();

      lc.setCount(0x03); // 2
      lc.setHalted(true);

      for (int i = 0; i < 10; i++) {
         lc.clock();
         int currentCount = lc.getCount();
         Assert.assertEquals(2, currentCount);
      }

      lc.setHalted(false);

      int currentCount;

      lc.clock();
      currentCount = lc.getCount();
      Assert.assertEquals(1, currentCount);

      lc.clock();
      currentCount = lc.getCount();
      Assert.assertEquals(0, currentCount);

      lc.clock();
      currentCount = lc.getCount();
      Assert.assertEquals(0, currentCount);
   }

   @Test
   public void testLengthCounter_Enabled() {
      LengthCounter lc = new LengthCounter();

      lc.setCount(0x03); // 2
      int currentCount = lc.getCount();
      Assert.assertEquals(2, currentCount);

      lc.setEnabled(false);

      for (int i = 0; i < 10; i++) {
         lc.clock();
         currentCount = lc.getCount();
         Assert.assertEquals(0, currentCount);
      }
   }
}
