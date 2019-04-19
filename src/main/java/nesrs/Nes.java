package nesrs;

import java.io.ByteArrayInputStream;

import nesrs.apu.Apu;
import nesrs.apu.AudioOutListener;
import nesrs.cartridge.Cartridge;
import nesrs.cartridge.INesRomCartridgeReader;
import nesrs.controller.Controller;
import nesrs.cpu.Cpu;
import nesrs.cpu.NesCpuMemory;
import nesrs.ppu.Ppu;
import nesrs.ppu.VideoOutListener;

public class Nes implements Runnable {

   private enum State {
      STARTED,
      PAUSED,
      STOPPED
   }

   private State _state = State.STOPPED;
   private final Object _stateLock = new Object();

   private final Cpu _cpu;
   private final Apu _apu;
   private final Ppu _ppu;

   private Thread _nesThread;

   public Nes(byte[] rom, VideoOutListener videoOut, AudioOutListener audioOut,
         Controller controller1) {

      // Assemble cpu
      NesCpuMemory cpuMemory = new NesCpuMemory();
      _cpu = new Cpu(cpuMemory);

      // Assemble cartridge
      Cartridge cartridge = new INesRomCartridgeReader(new ByteArrayInputStream(rom)).readCartridge();
      cartridge.setIrqListener(() -> _cpu.irq());

      // Assemble ppu
      _ppu = new Ppu(cartridge);
      _ppu.setVblListener(() -> _cpu.nmi());
      _ppu.setVideoOutListener(videoOut);

      // Assemble apu
      _apu = new Apu(cpuMemory);
      _apu.setIrqListener(() -> _cpu.irq());
      _apu.setAudioOutListener(audioOut);

      // Memory-mapped devices
      cpuMemory.setCartridge(cartridge);
      cpuMemory.setPpu(_ppu);
      cpuMemory.setApu(_apu);
      cpuMemory.setController1(controller1);

   }

   public void start() {
      synchronized (_stateLock) {
         if (_state != State.STOPPED) {
            return;
         }

         _cpu.init();
         _apu.init();
         _ppu.init();

         _state = State.STARTED;

         _nesThread = new Thread(this);
         _nesThread.setDaemon(true);
         _nesThread.start();
      }
   }

   public void reset() {
      if (_state == State.STARTED) {
         _cpu.reset();
         _apu.reset();
         _ppu.reset();
      }
   }

   public void stop() {
      synchronized (_stateLock) {
         _state = State.STOPPED;
      }
   }

   @Override
   public void run() {
//      long _lastFrameEnd = 0;
//      int ppuCyclesF = 0;

      while (_state == State.STARTED) {
         // CPU
         int cpuCycles = _cpu.executeOp();

         // APU
         _apu.executeCycles(cpuCycles);

         // PPU
         int ppuCycles = (cpuCycles * 3); // TODO cycles between ppu and cpu depends on nes type
         _ppu.executeCycles(ppuCycles);

//         ppuCyclesF += ppuCycles;
//         if (ppuCyclesF >= 89342) {
//            long currentFrameEnd = System.currentTimeMillis();
//            if (_lastFrameEnd != 0) {
//               long currentFrameDuration = currentFrameEnd - _lastFrameEnd;
//               int fps = (int) (1000f / currentFrameDuration);
//               System.out.println("fps: " + fps);
//            }
//            _lastFrameEnd = currentFrameEnd;
//            ppuCyclesF = 0;
//         }
      }
   }
}
