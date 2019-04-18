package nesrs.apu;

import nesrs.apu.channels.RandomWaveChannel;
import nesrs.apu.channels.RectangleWaveChannel;
import nesrs.apu.channels.TriangleWaveChannel;
import nesrs.apu.devices.Divider;
import nesrs.apu.devices.Sequencer;

public class FrameSequencer {

   // 3bits per step (xxxxxfle)
   // Bit 2 - set interrupt flag
   // Bit 1 - clock length counters and sweep units
   // Bit 0 - clock envelops and triangle's linear counters.
   private static final int[] MODE0_SEQUENCE_STEPS = new int[] {0x1, 0x3, 0x1, 0x7};
   private static final int[] MODE1_SEQUENCE_STEPS = new int[] {0x3, 0x1, 0x3, 0x1, 0x0};
   // mode 0: 4-step  effective rate (approx)
   // ---------------------------------------
   //     - - - f      60 Hz
   //     - l - l     120 Hz
   //     e e e e     240 Hz
   //
   // mode 1: 5-step  effective rate (approx)
   // ---------------------------------------
   //     - - - - -   (interrupt flag never set)
   //     l - l - -    96 Hz
   //     e e e e -   192 Hz

   // 1.79MHz / DIVIDER_PERIOD = 240Hz
   private static final int DIVIDER_PERIOD = (14916/2); // TODO there is fraction here.

   private Divider _divider;
   private Sequencer _sequencer;

   private boolean _isIrqDisabled;
   private boolean _irqFlag;

   private RectangleWaveChannel _rectangleWaveChannel1;
   private RectangleWaveChannel _rectangleWaveChannel2;
   private TriangleWaveChannel _triangleWaveChannel;
   private RandomWaveChannel _randomWaveChannel;

   public FrameSequencer(
         RectangleWaveChannel rectangleWaveChannel1,
         RectangleWaveChannel rectangleWaveChannel2,
         TriangleWaveChannel triangleWaveChannel,
         RandomWaveChannel randomWaveChannel) {

      _rectangleWaveChannel1 = rectangleWaveChannel1;
      _rectangleWaveChannel2 = rectangleWaveChannel2;
      _triangleWaveChannel = triangleWaveChannel;
      _randomWaveChannel = randomWaveChannel;

      _divider = new Divider(DIVIDER_PERIOD);
      _sequencer = new Sequencer(MODE0_SEQUENCE_STEPS);
   }

   public void writeRegister(int value) {
      _divider.reset();
      _sequencer.reset();

      int mode = (value & 0x80) != 0 ? 1 : 0;
      _isIrqDisabled = (value & 0x40) != 0;
      if (_isIrqDisabled) {
         _irqFlag = false;
      }

      _sequencer.setSequence(mode == 0 ? MODE0_SEQUENCE_STEPS : MODE1_SEQUENCE_STEPS);

      if (mode == 1) {
         clockSequencer();
      }
   }

   public boolean getIrqStatus() {
      return _irqFlag;
   }

   public void resetIrqStatus() {
      _irqFlag = false;
   }

   public void clock() {
      boolean shouldClockSequencer = _divider.clock();
      if (shouldClockSequencer) {
         clockSequencer();
      }
   }

   void clockSequencer() {
      int step = _sequencer.clock();

      if ((step & 0x4) != 0) {
         _irqFlag = !_isIrqDisabled;
      }

      if ((step & 0x2) != 0) {
         // Clock length counters and sweep units.
         _rectangleWaveChannel1.clockLengthCounterAndSweepUnit();
         _rectangleWaveChannel2.clockLengthCounterAndSweepUnit();
         _triangleWaveChannel.clockLengthCounter();
         _randomWaveChannel.clockLengthCounter();
      }

      if ((step & 0x1) != 0) {
         // Clock envelops and triangle's linear counters.
         _rectangleWaveChannel1.clockEnvelope();
         _rectangleWaveChannel2.clockEnvelope();
         _triangleWaveChannel.clockLinearCounter();
         _randomWaveChannel.clockEnvelope();
      }
   }
}