package nesrs.apu.channels;

import nesrs.apu.channels.RectangleWaveChannel;

import org.junit.Assert;
import org.junit.Test;

public class RectangleWaveChannelTest {

   @Test
   public void testLengthCounter() {
      RectangleWaveChannel rwc1 = new RectangleWaveChannel();

      // Set length counter
      rwc1.writeCoarseTuneRegister(0x18);
      Assert.assertEquals(2, rwc1.getLengthCounterCount());

      rwc1.clockLengthCounterAndSweepUnit();
      Assert.assertEquals(1, rwc1.getLengthCounterCount());

      rwc1.clockLengthCounterAndSweepUnit();
      Assert.assertEquals(0, rwc1.getLengthCounterCount());

      rwc1.clockLengthCounterAndSweepUnit();
      Assert.assertEquals(0, rwc1.getLengthCounterCount());
   }

   @Test
   public void testChannelPeriod() {
      RectangleWaveChannel rwc1 = new RectangleWaveChannel();

      // Set channel period
      rwc1.writeFineTuneRegister(0xFE);
      rwc1.writeCoarseTuneRegister(0x07);
      Assert.assertEquals(0x07FE, rwc1.getChannelPeriod());
      Assert.assertEquals(0x0FFE, rwc1.getTimer().getPeriod());

      rwc1.writeFineTuneRegister(0x13);
      Assert.assertEquals(0x0713, rwc1.getChannelPeriod());
      Assert.assertEquals(0x0E28, rwc1.getTimer().getPeriod());

      rwc1.writeCoarseTuneRegister(0x01);
      Assert.assertEquals(0x0113, rwc1.getChannelPeriod());
      Assert.assertEquals(0x0228, rwc1.getTimer().getPeriod());
   }

   @Test
   public void testDutyCycle() {
      RectangleWaveChannel rwc1 = new RectangleWaveChannel();

      rwc1.writeControlRegister(0x80); // Type 2

      // Set channel period
      rwc1.writeFineTuneRegister(0x01);
      Assert.assertEquals(0x0004, rwc1.getTimer().getPeriod());

      // 0, 1, 1, 1, 1, 0, 0, 0
      for (int i = 0; i < 4; i++) {
         rwc1.clockTimer();
      }
      Assert.assertEquals(0, rwc1.getDutyCycleSequencer().getCurrentValue());

      for (int i = 0; i < 4; i++) {
         rwc1.clockTimer();
      }
      Assert.assertEquals(1, rwc1.getDutyCycleSequencer().getCurrentValue());

      for (int i = 0; i < 4; i++) {
         rwc1.clockTimer();
      }
      Assert.assertEquals(1, rwc1.getDutyCycleSequencer().getCurrentValue());

      for (int i = 0; i < 4; i++) {
         rwc1.clockTimer();
      }
      Assert.assertEquals(1, rwc1.getDutyCycleSequencer().getCurrentValue());

      for (int i = 0; i < 4; i++) {
         rwc1.clockTimer();
      }
      Assert.assertEquals(1, rwc1.getDutyCycleSequencer().getCurrentValue());

      for (int i = 0; i < 4; i++) {
         rwc1.clockTimer();
      }
      Assert.assertEquals(0, rwc1.getDutyCycleSequencer().getCurrentValue());

      for (int i = 0; i < 4; i++) {
         rwc1.clockTimer();
      }
      Assert.assertEquals(0, rwc1.getDutyCycleSequencer().getCurrentValue());

      for (int i = 0; i < 4; i++) {
         rwc1.clockTimer();
      }
      Assert.assertEquals(0, rwc1.getDutyCycleSequencer().getCurrentValue());
   }

   @Test
   public void testDac() {

   }
}
