package nesrs.apu.devices;

import nesrs.apu.devices.Sequencer;

import org.junit.Assert;
import org.junit.Test;

public class SequencerTest {

   @Test
   public void testSequencer_NoSequence() {
      Sequencer sequencer = new Sequencer(null);

      Integer seq = sequencer.clock();
      Assert.assertEquals((Integer)(-1), seq);
   }

   @Test
   public void testSequencer_Sequence() {
      Sequencer sequencer = new Sequencer(new int[] {1, 2, 3});

      for (int i = 0; i < 10; i++) {
         Integer seq = sequencer.clock();

         Assert.assertEquals((Integer)((i % 3) + 1), seq);
      }
   }

   @Test
   public void testSequencer_SetSequence() {
      Sequencer sequencer = new Sequencer(new int[] {1, 2, 3});

      sequencer.clock();
      sequencer.clock();

      sequencer.setSequence(new int[] {4, 5, 6});

      Integer seq = sequencer.clock();

      Assert.assertEquals((Integer)6, seq);
   }

   @Test
   public void testSequencer_Reset() {
      Sequencer sequencer = new Sequencer(new int[] {1, 2, 3});

      sequencer.clock();
      sequencer.clock();

      sequencer.setSequence(new int[] {4, 5, 6});
      sequencer.reset();

      Integer seq = sequencer.clock();

      Assert.assertEquals((Integer)4, seq);
   }
}
