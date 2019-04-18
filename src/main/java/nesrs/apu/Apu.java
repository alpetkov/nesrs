package nesrs.apu;

import nesrs.apu.channels.DeltaModulationChannel;
import nesrs.apu.channels.RandomWaveChannel;
import nesrs.apu.channels.RectangleWaveChannel;
import nesrs.apu.channels.TriangleWaveChannel;
import nesrs.cpu.CpuMemory;
import nesrs.cpu.IrqListener;

public class Apu implements ApuPin {

   public static final int SAMPLE_RATE = 44100; // 44.1KHz
   public static final int BIT_DEPTH = 16; // 16 bits per sample
   public static final int NUM_CHANNELS = 1; // Mono
   public static final boolean BIG_ENDIAN = true;
   public static final int BUFFER_SIZE = 4096 * 2;

   private static int[] SQUARE_OUT_TABLE;
   private static int[] TND_OUT_TABLE;

   // Init lookup tables.
   static {
      SQUARE_OUT_TABLE = new int[31];
      SQUARE_OUT_TABLE[0] = 0;
      for (int i = 1; i < 31; i++) {
         SQUARE_OUT_TABLE[i] = (int)((95.52 / (8128.0 / i + 100)) * Short.MAX_VALUE);
      }

      TND_OUT_TABLE = new int[203];
      TND_OUT_TABLE[0] = 0;
      for (int i = 1; i < 203; i++) {
         TND_OUT_TABLE[i] = (int)((163.67 / (24329.0 / i + 100)) * Short.MAX_VALUE);
      }
   }

	private final RectangleWaveChannel _rectangleWaveChannel1;
	private final RectangleWaveChannel _rectangleWaveChannel2;
	private final TriangleWaveChannel _triangleWaveChannel;
	private final RandomWaveChannel _randomWaveChannel;
	private final DeltaModulationChannel _deltaModulationChannel;

	private final FrameSequencer _frameSequencer;

   private AudioOutListener _audioOutListener;
	private IrqListener _irqListener;

	// Work
	private int _currentCycleInSample = 0;

	// Audio out
	private final byte[] _audioBuffer = new byte[1024*2];
	private int _audioBufferIndex = 0;

   public Apu(CpuMemory cpuMemory) {
      _rectangleWaveChannel1 = new RectangleWaveChannel(false);
      _rectangleWaveChannel2 = new RectangleWaveChannel(true);
      _triangleWaveChannel = new TriangleWaveChannel();
      _randomWaveChannel = new RandomWaveChannel();
      _deltaModulationChannel = new DeltaModulationChannel(cpuMemory);

      _frameSequencer = new FrameSequencer(
            _rectangleWaveChannel1,
            _rectangleWaveChannel2,
            _triangleWaveChannel,
            _randomWaveChannel);
   }

   @Override
   public void setAudioOutListener(AudioOutListener audioOutListener) {
      _audioOutListener = audioOutListener;
   }

   @Override
   public void setIrqListener(IrqListener irqListener) {
      _irqListener = irqListener;
   }

   @Override
   public void init() {
   }

   @Override
   public void reset() {
   }

   @Override
   public void executeCycles(int cycles) {
      for (int i = 0; i < cycles; i++) {

         // Clock channels
         _rectangleWaveChannel1.clockTimer();
         _rectangleWaveChannel2.clockTimer();
         _triangleWaveChannel.clockTimer();
         _randomWaveChannel.clockTimer();
         _deltaModulationChannel.clockTimer();

         _frameSequencer.clock();

         // Output IRQ
         if (_frameSequencer.getIrqStatus() || _deltaModulationChannel.getIrqStatus()) {
            if (_irqListener != null) {
               _irqListener.handleIrq();
            }
         }

         // Output sound samples if needed
         _currentCycleInSample++;
         // Downsample to 44.1KHz hence pick 1 sample every 40 NES cycles.
         if (_currentCycleInSample == 40) {
            int routAudio = SQUARE_OUT_TABLE[_rectangleWaveChannel1.getDac() +
                                             _rectangleWaveChannel2.getDac()];

            int coutAudio = TND_OUT_TABLE[(3 * _triangleWaveChannel.getDac() +
                                          2 * _randomWaveChannel.getDac() +
                                         _deltaModulationChannel.getDac()) % 203];

            int outAudio = coutAudio + routAudio;

            if (_audioBufferIndex == _audioBuffer.length) {
               _audioBufferIndex = 0;
               if (_audioOutListener != null) {
                  _audioOutListener.handleSamples(_audioBuffer);
               }
            }

            int sample = outAudio;
            _audioBuffer[_audioBufferIndex] = (byte)((sample >> 8) & 0x00FF);
            _audioBuffer[_audioBufferIndex + 1] = (byte)(sample & 0x00FF);
            _audioBufferIndex += 2;

            _currentCycleInSample = 0;
         }
      }
   }

	@Override
	public int readRegister(int register) {
	   if (register == 0x4015) {
	      int dmcIrqFlag = _deltaModulationChannel.getIrqStatus() ? 0x80 : 0;
	      int frameIrqFlag = _frameSequencer.getIrqStatus() ? 0x40 : 0;
	      int dmcStatusFlag = _deltaModulationChannel.getSampleBytesRemainCounter() > 0 ? 0x10 : 0;
	      int randomStatusFlag = _randomWaveChannel.getLengthCounterCount() > 0 ? 0x08 : 0;
	      int triangleStatusFlag = _triangleWaveChannel.getLengthCounterCount() > 0 ? 0x04 : 0;
	      int rectangle2StatusFLag = _rectangleWaveChannel2.getLengthCounterCount() > 0 ? 0x02 : 0;
	      int rectangle1StatusFlag = _rectangleWaveChannel1.getLengthCounterCount() > 0 ? 0x01 : 0;

	      _frameSequencer.resetIrqStatus();

	      return (dmcIrqFlag |
	            frameIrqFlag |
	            dmcStatusFlag |
	            randomStatusFlag |
	            triangleStatusFlag |
	            rectangle2StatusFLag |
	            rectangle1StatusFlag);
	   }

		return 0;
	}

	@Override
	public void writeRegister(int register, int value) {

	   switch (register) {
	      // Rectangle wave 1
	      case 0x4000: {
	         _rectangleWaveChannel1.writeControlRegister(value);
	         break;
	      }
         case 0x4001: {
            _rectangleWaveChannel1.writeSweepUnitRegister(value);
            break;
         }
         case 0x4002: {
            _rectangleWaveChannel1.writeFineTuneRegister(value);
            break;
         }
         case 0x4003: {
            _rectangleWaveChannel1.writeCoarseTuneRegister(value);
            break;
         }

         // Rectangle wave 2 (nearly identical to first)
         case 0x4004: {
            _rectangleWaveChannel2.writeControlRegister(value);
            break;
         }
         case 0x4005: {
            _rectangleWaveChannel2.writeSweepUnitRegister(value);
            break;
         }
         case 0x4006: {
            _rectangleWaveChannel2.writeFineTuneRegister(value);
            break;
         }
         case 0x4007: {
            _rectangleWaveChannel2.writeCoarseTuneRegister(value);
            break;
         }

         // Triangle
         case 0x4008: {
            _triangleWaveChannel.writeControlRegister(value);
            break;
         }
         case 0x4009 : {
            // Not used
            break;
         }
         case 0x400A : {
            _triangleWaveChannel.writeFineTuneRegister(value);
            break;
         }
         case 0x400B : {
            _triangleWaveChannel.writeCoarseTuneRegister(value);
            break;
         }

         // Noise
         case 0x400C : {
            _randomWaveChannel.writeControlRegister(value);
            break;
         }
         case 0x400D : {
            // Not used
            break;
         }
         case 0x400E : {
            _randomWaveChannel.writeFineTuneRegister(value);
            break;
         }
         case 0x400F : {
            _randomWaveChannel.writeCoarseTuneRegister(value);
            break;
         }

         // DMC
         case 0x4010: {
            _deltaModulationChannel.writeControlRegister(value);
            break;
         }
         case 0x4011: {
            _deltaModulationChannel.writeDacCounterRegister(value);
            break;
         }
         case 0x4012: {
            _deltaModulationChannel.writeSampleAddressRegister(value);
            break;
         }
         case 0x4013: {
            _deltaModulationChannel.writeSampleLengthRegister(value);
            break;
         }

         // Length counters
         case 0x4015: {
            _deltaModulationChannel.setSampleBytesRemainCounterEnabled((value & 0x10) != 0);
            _randomWaveChannel.setLengthCounterEnabled((value & 0x08) != 0);
            _triangleWaveChannel.setLengthCounterEnabled((value & 0x04) != 0);
            _rectangleWaveChannel2.setLengthCounterEnabled((value & 0x02) != 0);
            _rectangleWaveChannel1.setLengthCounterEnabled((value & 0x01) != 0);
            break;
         }

         // Frame Sequencer
         case 0x4017: {
            _frameSequencer.writeRegister(value);
            break;
         }

         default:
            break;
      }
	}
}
