package nesrs.apu.channels;

import nesrs.apu.devices.LengthCounter;
import nesrs.apu.devices.LinearCounter;
import nesrs.apu.devices.Sequencer;
import nesrs.apu.devices.Timer;

/**
 * Plays triangle waveforms.
 */
public class TriangleWaveChannel {

   private int _timerRawPeriod;
   private Timer _timer;
   private LinearCounter _linearCounter;
   private LengthCounter _lengthCounter;
   private Sequencer _sequencer = new Sequencer(
         new int[] {15, 14, 13, 12, 11, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1, 0,
                    0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15});

   public TriangleWaveChannel() {
      _timer = new Timer(0);
      _linearCounter = new LinearCounter();
      _lengthCounter = new LengthCounter();
   }

   public void writeControlRegister(int value) {
      _linearCounter.setControlFlag((value & 0x80) != 0);
      _linearCounter.setReloadValue(value & 0x7F);

      _lengthCounter.setHalted((value & 0x80) != 0);
   }

   public void writeFineTuneRegister(int value) {
      _timerRawPeriod = (_timerRawPeriod & 0x0700) | value;
      _timer.setPeriod(_timerRawPeriod + 1);
   }

   public void writeCoarseTuneRegister(int value) {
      _lengthCounter.setCount((value & 0xF8) >> 3);

      _linearCounter.setHalted();

      _timerRawPeriod = ((value << 8) & 0x0700) | (_timerRawPeriod & 0x00FF);
      _timer.setPeriod(_timerRawPeriod + 1);
   }

   public void setLengthCounterEnabled(boolean isEnabled) {
      _lengthCounter.setEnabled(isEnabled);
   }

   public int getLengthCounterCount() {
      return _lengthCounter.getCount();
   }

   public int getLinearCounterCount() {
      return _linearCounter.getCount();
   }

   public int getDac() {
      if (_timerRawPeriod == 0x0000 || _timerRawPeriod == 0x0100) {
         return 7;
      }

      return _sequencer.getCurrentValue();
   }

   public void clockTimer() {
      boolean shouldOutputClock = _timer.clock();
      if (shouldOutputClock) {
         if (_linearCounter.getCount() > 0 && _lengthCounter.getCount() > 0) {
            _sequencer.clock();
         }
      }
   }

   public void clockLengthCounter() {
      _lengthCounter.clock();
   }

   public void clockLinearCounter() {
      _linearCounter.clock();
   }
}