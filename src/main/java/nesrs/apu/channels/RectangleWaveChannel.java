package nesrs.apu.channels;

import nesrs.apu.devices.FrequencySweepUnit;
import nesrs.apu.devices.FrequencySweepUnit.ClockStatus;
import nesrs.apu.devices.LengthCounter;
import nesrs.apu.devices.Sequencer;
import nesrs.apu.devices.Timer;
import nesrs.apu.devices.VolumeEnvelopeDecayUnit;

/**
 * Plays waveforms.
 */
public class RectangleWaveChannel {

   private static final int[][] DUTY_CYCLES = new int[][] {
//     0, 7, 6, 5, 4, 3, 2, 1
      {0, 1, 0, 0, 0, 0, 0, 0}, // 12.5%
      {0, 1, 1, 0, 0, 0, 0, 0}, // 25%
      {0, 1, 1, 1, 1, 0, 0, 0}, // 50%
      {1, 0, 0, 1, 1, 1, 1, 1}, // 75%
   };

   private VolumeEnvelopeDecayUnit _volumeEnvelopeDecayUnit;
   private FrequencySweepUnit _sweepUnit;
   private int _timerRawPeriod;
   private Timer _timer;
   private Sequencer _dutyCycleSequencer;
   private LengthCounter _lengthCounter;

   public RectangleWaveChannel() {
      this(false);
   }

   public RectangleWaveChannel(boolean shouldAdd1WhenDecreasing) {
      _volumeEnvelopeDecayUnit = new VolumeEnvelopeDecayUnit();
      _sweepUnit = new FrequencySweepUnit(shouldAdd1WhenDecreasing);
      _timerRawPeriod = 0;
      _timer = new Timer(0);
      _dutyCycleSequencer = new Sequencer(new int[0]);
      _lengthCounter = new LengthCounter();
   }

   public void writeControlRegister(int value) {
      int dutyCycleType = ((value & 0xC0) >> 6);
      _dutyCycleSequencer.setSequence(DUTY_CYCLES[dutyCycleType]);

      _lengthCounter.setHalted((value & 0x20) != 0);

      boolean envelopLoopingEnabled = (value & 0x20) != 0;
      boolean envelopDisabled = (value & 0x10) != 0;
      int envelopVolume = (value & 0x0F);
      _volumeEnvelopeDecayUnit.write(envelopLoopingEnabled, envelopDisabled, envelopVolume);
   }

   public void writeSweepUnitRegister(int value) {
      _sweepUnit.write(value);
   }

   public void writeFineTuneRegister(int value) {
      _timerRawPeriod = (_timerRawPeriod & 0x0700) | value;
      _timer.setPeriod(_timerRawPeriod + 1);
   }

   public void writeCoarseTuneRegister(int value) {
      _lengthCounter.setCount(value >> 3);

      _timerRawPeriod = ((value << 8) & 0x0700) | (_timerRawPeriod & 0x00FF);
      _timer.setPeriod(_timerRawPeriod + 1);

      _dutyCycleSequencer.reset();

      _volumeEnvelopeDecayUnit.setDirty();
   }

   public void setLengthCounterEnabled(boolean isEnabled) {
      _lengthCounter.setEnabled(isEnabled);
   }

   public int getLengthCounterCount() {
      return _lengthCounter.getCount();
   }

   public int getDac() {
      if (_sweepUnit.getStatus().shouldSilenceDac) {
         return 0;
      }

      if (_dutyCycleSequencer.getCurrentValue() == 0) {
         return 0;
      }

      if (_lengthCounter.getCount() == 0) {
         return 0;
      }

      return _volumeEnvelopeDecayUnit.getVolume();
   }

   public void clockTimer() {
      boolean shouldOutputClock = _timer.clock();
      if (shouldOutputClock) {
         _dutyCycleSequencer.clock();
      }
   }

   public void clockLengthCounterAndSweepUnit() {
      _lengthCounter.clock();

      _sweepUnit.clock(_timerRawPeriod);
      ClockStatus sweepClockStatus = _sweepUnit.getStatus();
      if (sweepClockStatus.shouldChangeChannelTimerRawPeriod) {
         _timerRawPeriod = sweepClockStatus.newTimerRawPeriod;
         _timer.setPeriod((_timerRawPeriod + 1) << 1);
      }
   }

   public void clockEnvelope() {
      _volumeEnvelopeDecayUnit.clock();
   }

   /*package*/ int getChannelPeriod() {
      return _timerRawPeriod;
   }

   /*package*/ Sequencer getDutyCycleSequencer() {
      return _dutyCycleSequencer;
   }

   /*package*/ Timer getTimer() {
      return _timer;
   }
}