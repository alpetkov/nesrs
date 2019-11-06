package nesrs.apu.channels;

import nesrs.apu.devices.Timer;
import nesrs.cpu.CpuMemory;

/**
 * Channel that plays samples.
 */
public class DeltaModulationChannel {

   private static final int[] PERIOD_LOOKUP = new int[] {
      /*0x0*/ 0x1AC,
      /*0x1*/ 0x17C,
      /*0x2*/ 0x154,
      /*0x3*/ 0x140,
      /*0x4*/ 0x11E,
      /*0x5*/ 0x0FE,
      /*0x6*/ 0x0E2,
      /*0x7*/ 0x0D6,
      /*0x8*/ 0x0BE,
      /*0x9*/ 0x0A0,
      /*0xA*/ 0x08E,
      /*0xB*/ 0x080,
      /*0xC*/ 0x06A,
      /*0xD*/ 0x054,
      /*0xE*/ 0x048,
      /*0xF*/ 0x036
   };

   private int _outputUnitShiftRegister;
   private boolean _outputUnitSilenceFlag;
   private int _outputUnitCounter;

   private int _dmaReaderAddressCounter;
   private int _dmaReaderBytesRemainCounter;

   private int _sampleBufferValue; // 1 byte of sample
   private boolean _sampleBufferEmptyFlag;

   private boolean _irqFlag;

   private boolean _isIrqEnabled;
   private boolean _isLoopingOn;
   private Timer _timer;

   private int _sampleAddress;
   private int _sampleLength;

   private int _dacCounter;
   private int _dac;

   private CpuMemory _cpuMemory;

   public DeltaModulationChannel(CpuMemory cpuMemory) {
      _cpuMemory = cpuMemory;

      _timer = new Timer(0);

      _sampleBufferEmptyFlag = true;

      _dacCounter = 0;
      _dac = 0;
   }

   public void writeControlRegister(int value) {
      _isIrqEnabled = (value & 0x80) != 0;
      if (!_isIrqEnabled) {
         _irqFlag = false;
      }
      _isLoopingOn = (value & 0x40) != 0;
      _timer.setPeriod(PERIOD_LOOKUP[value & 0x0F]);
   }

   public void writeDacCounterRegister(int value) {
      _dacCounter = value;
      _dac = value;
   }

   public void writeSampleAddressRegister(int value) {
      _sampleAddress = value;
   }

   public void writeSampleLengthRegister(int value) {
      _sampleLength = value;
   }

   public void setSampleBytesRemainCounterEnabled(boolean isEnabled) {
      // Clear irqFlag
      _irqFlag = false;

      if (isEnabled) {
         if (_dmaReaderBytesRemainCounter == 0) {
            dmaReaderRestartSampleCounters();
            if (_sampleBufferEmptyFlag) {
               dmaReaderFetchSampleToSampleBuffer();
            }
         }
      } else {
         _dmaReaderBytesRemainCounter = 0;
      }
   }

   public int getSampleBytesRemainCounter() {
      return _dmaReaderBytesRemainCounter;
   }

   public boolean getIrqStatus() {
      return _irqFlag;
   }

   private void dmaReaderRestartSampleCounters() {
      _dmaReaderAddressCounter = (_sampleAddress << 6) + 0xC000;
      _dmaReaderBytesRemainCounter = (_sampleLength << 4) + 1;
   }

   private void dmaReaderFetchSampleToSampleBuffer() {
      if (_dmaReaderBytesRemainCounter > 0) {
         _sampleBufferValue = _cpuMemory.read(_dmaReaderAddressCounter);

         _sampleBufferEmptyFlag = false;

         _dmaReaderAddressCounter++;
         if (_dmaReaderAddressCounter >= 0xFFFF) {
            _dmaReaderAddressCounter = 0x8000;
         }

         _dmaReaderBytesRemainCounter--;
         if (_dmaReaderBytesRemainCounter == 0) {
            if (_isLoopingOn) {
               dmaReaderRestartSampleCounters();
            } else {
               if (_isIrqEnabled) {
                  _irqFlag = true;
               }
            }
         }
      }
   }

   public int getDac() {
      return _dac;
   }

   public void clockTimer() {
      boolean shouldOutputClock = _timer.clock();
      if (shouldOutputClock) {

         if (_sampleBufferEmptyFlag) {
            dmaReaderFetchSampleToSampleBuffer();
         }

         // Empty sample buffer to output unit at the beginning of new output cycle.
         if (_outputUnitCounter == 0) {
            _outputUnitCounter = 8;

            if (_sampleBufferEmptyFlag) {
               _outputUnitSilenceFlag = true;
               _outputUnitShiftRegister = 0x0;
            } else {
               _outputUnitSilenceFlag = false;
               _outputUnitShiftRegister = _sampleBufferValue;

               _sampleBufferEmptyFlag = true;
            }

//            if (_sampleBufferEmptyFlag) {
//               dmaReaderFetchSampleToSampleBuffer();
//            }
         }

         // Update DAC with output unit values.
         if (!_outputUnitSilenceFlag) {
            if ((_outputUnitShiftRegister & 0x01) != 0) {
               // Increment
               if (_dacCounter < 126) {
                  _dacCounter += 2;
               }
            } else {
               // Decrement
               if (_dacCounter > 1) {
                  _dacCounter -= 2;
               }
            }
         }
         _dac = (_dacCounter & 0x7E) | (_dac & 0x01);

         // Shift output unit.
         _outputUnitShiftRegister >>= 1;
         _outputUnitCounter -= 1;
      }
   }
}