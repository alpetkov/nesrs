package nesrs.apu.devices;

import nesrs.apu.devices.Divider;

import org.junit.Assert;
import org.junit.Test;

import junit.framework.TestCase;

public class DividerTest extends TestCase {

   @Test
   public void testDividerPeriod() {
      Divider divider = new Divider(2);
      boolean shouldOutputClock;

      shouldOutputClock = divider.clock();
      Assert.assertFalse(shouldOutputClock);

      shouldOutputClock = divider.clock();
      Assert.assertTrue(shouldOutputClock);
   }

   @Test
   public void testDividerNoPeriod() {
      Divider divider = new Divider(0);

      Assert.assertEquals(0, divider.getPeriod());

      boolean shouldOutputClock;

      shouldOutputClock = divider.clock();
      Assert.assertFalse(shouldOutputClock);

      shouldOutputClock = divider.clock();
      Assert.assertFalse(shouldOutputClock);

      shouldOutputClock = divider.clock();
      Assert.assertFalse(shouldOutputClock);
   }

   @Test
   public void testDividerSetPeriod() {
      Divider divider = new Divider(3);

      Assert.assertEquals(3, divider.getPeriod());

      for (int i = 0; i < 2; i++) {
         boolean shouldOutputClock = divider.clock();
         Assert.assertFalse(shouldOutputClock);
      }

      divider.setPeriod(4);
      Assert.assertEquals(4, divider.getPeriod());

      boolean shouldOutputClock = divider.clock();
      Assert.assertTrue(shouldOutputClock);
   }

   @Test
   public void testDividerReset() {
      Divider divider = new Divider(0);

      for (int i = 1; i <= 10; i++) {
         boolean shouldOutputClock = divider.clock();
         Assert.assertFalse(shouldOutputClock);
      }

      divider.setPeriod(4);
      for (int i = 1; i <= 4; i++) {
         boolean shouldOutputClock = divider.clock();
         Assert.assertFalse(shouldOutputClock);
      }

      divider.reset();
      for (int i = 1; i <= 3; i++) {
         boolean shouldOutputClock = divider.clock();
         Assert.assertFalse(shouldOutputClock);
      }

      boolean shouldOutputClock = divider.clock();
      Assert.assertTrue(shouldOutputClock);
   }
}
