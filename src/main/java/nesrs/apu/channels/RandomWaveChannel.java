package nesrs.apu.channels;

import nesrs.apu.devices.LengthCounter;
import nesrs.apu.devices.Timer;
import nesrs.apu.devices.VolumeEnvelopeDecayUnit;

/**
 * Aka Noise Channel. Plays waveforms.
 */
public class RandomWaveChannel {

   private static final int[] PERIOD_LOOKUP = new int[] {
      /*0x0*/ 0x004,
      /*0x1*/ 0x008,
      /*0x2*/ 0x010,
      /*0x3*/ 0x020,
      /*0x4*/ 0x040,
      /*0x5*/ 0x060,
      /*0x6*/ 0x080,
      /*0x7*/ 0x0A0,
      /*0x8*/ 0x0CA,
      /*0x9*/ 0x0FE,
      /*0xA*/ 0x17C,
      /*0xB*/ 0x1FC,
      /*0xC*/ 0x2FA,
      /*0xD*/ 0x3F8,
      /*0xE*/ 0x7F2,
      /*0xF*/ 0xFE4
   };

   private VolumeEnvelopeDecayUnit _volumeEnvelopeDecayUnit;
   private Timer _timer;
   private LengthCounter _lengthCounter;
   private int _randomGeneratorMode;
   private int _randomShiftRegister; //15-bit right shift register with feedback

   public RandomWaveChannel() {
      _volumeEnvelopeDecayUnit = new VolumeEnvelopeDecayUnit();
      _timer = new Timer(0);
      _lengthCounter = new LengthCounter();
      _randomShiftRegister = 0x0001;
   }

   public void writeControlRegister(int value) {
      _lengthCounter.setHalted((value & 0x20) != 0);

      boolean envelopLoopingEnabled = (value & 0x20) != 0;
      boolean envelopDisabled = (value & 0x10) != 0;
      int envelopVolume = (value & 0x0F);
      _volumeEnvelopeDecayUnit.write(envelopLoopingEnabled, envelopDisabled, envelopVolume);
   }

   public void writeFineTuneRegister(int value) {
      _randomGeneratorMode = (value & 0x80) != 0 ? 1 : 0;

      int periodIndex = (value & 0x0F);
      _timer.setPeriod(PERIOD_LOOKUP[periodIndex]);
   }

   public void writeCoarseTuneRegister(int value) {
      _lengthCounter.setCount((value & 0xF8) >> 3);

      _volumeEnvelopeDecayUnit.setDirty();
   }

   public void setLengthCounterEnabled(boolean isEnabled) {
      _lengthCounter.setEnabled(isEnabled);
   }

   public int getLengthCounterCount() {
      return _lengthCounter.getCount();
   }

   public int getDac() {
      if ((_randomShiftRegister & 0x0001) != 0) {
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

         int bit0 = (_randomShiftRegister & 0x0001);
         int tapBit = 0;
         if (_randomGeneratorMode == 0) {
            // tap bit is bit 1
            tapBit = ((_randomShiftRegister & 0x0002) >> 1);

         } else {
            // tap bit is bit 6
            tapBit = ((_randomShiftRegister & 0x0040) >> 6);
         }

         // xor bit 0 (output) and tap bit to produce new input
         int newBit14 = bit0 ^ tapBit;

         // right shift the LFSR (and thus drain the output).
         _randomShiftRegister >>= 1;

         // Update with new input bit (bit14)
         _randomShiftRegister &= 0x3FFF;
         _randomShiftRegister |= (newBit14 << 14);
      }
   }

   public void clockLengthCounter() {
      _lengthCounter.clock();
   }

   public void clockEnvelope() {
      _volumeEnvelopeDecayUnit.clock();
   }
}