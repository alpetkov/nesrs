package nesrs.apu.devices;

import nesrs.apu.devices.LinearCounter;

import org.junit.Assert;
import org.junit.Test;

public class LinearCounterTest {

   @Test
   public void testLinearCounter_NotHalted() {
      LinearCounter lc = new LinearCounter();

      for (int i = 0; i < 100; i++) {
         lc.clock();
         Assert.assertEquals(0, lc.getCount());
      }

      lc.setControlFlag(true);
      lc.setReloadValue(0);
      for (int i = 0; i < 100; i++) {
         lc.clock();
         Assert.assertEquals(0, lc.getCount());
      }

      lc.setControlFlag(true);
      lc.setReloadValue(0x70);
      for (int i = 0; i < 100; i++) {
         lc.clock();
         Assert.assertEquals(0, lc.getCount());
      }

      lc.setControlFlag(true);
      lc.setReloadValue(0x7F);
      for (int i = 0; i < 100; i++) {
         lc.clock();
         Assert.assertEquals(0, lc.getCount());
      }
   }

   @Test
   public void testLinearCounter_Halted_ControlFlagIsSet() {
      LinearCounter lc = new LinearCounter();

      lc.setControlFlag(true);
      lc.setReloadValue(0x04);
      for (int i = 0; i < 100; i++) {
         lc.clock();
         Assert.assertEquals(0, lc.getCount());
      }

      lc.setHalted();
      for (int i = 4; i >= 0; i--) {
         lc.clock();
         Assert.assertEquals(4, lc.getCount());
      }

      lc.setControlFlag(false);
      lc.setHalted();
      for (int i = 4; i >= 0; i--) {
         lc.clock();
         Assert.assertEquals(i, lc.getCount());
      }
   }

   @Test
   public void testLinearCounter_Halted_ControlFlagIsClear() {
      LinearCounter lc = new LinearCounter();

      lc.setControlFlag(false);
      lc.setReloadValue(0x04);
      lc.setHalted();
      for (int i = 4; i >= 0; i--) {
         lc.clock();
         Assert.assertEquals(i, lc.getCount());
      }
   }
}
