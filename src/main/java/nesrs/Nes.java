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

   private final VideoOutListener _videoOut;
   private final AudioOutListener _audioOut;
   private final Controller _controller1;

   static final double nanoToMs = 1000000.0;
   
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

      _videoOut = videoOut;
      _audioOut = audioOut;
      _controller1 = controller1;
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

   public void run2() {
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
   
   @Override
   public void run() {
      long lastRunEnd = 0;
      long frameCpuCycles = 0;

      while (_state == State.STARTED) {
         updateInput();
         frameCpuCycles = runFrames(lastRunEnd, frameCpuCycles);
         renderGraphics();
         playAudio();
         lastRunEnd = System.currentTimeMillis();
      }
   }
   
   public void updateInput() {
      if (_controller1 != null) {
         _controller1.captureState();
      }
   }
   
   public long runFrames(long lastRunEnd, long frameCpuCycles) {
      long now = System.currentTimeMillis();
      // Each update is 16ms -> 60fps
      int framesToRun = 1;
      if (lastRunEnd != 0) {
         framesToRun = (int) ((now - lastRunEnd) / 16);
         if (framesToRun == 0) {
            framesToRun = 1;
         }
      }

      while (framesToRun-- > 0) {
         frameCpuCycles = updateFrame(frameCpuCycles);
      }
      
      return frameCpuCycles;
   }
   
   public long updateFrame(long frameCpuCycles) {
      long cpuTime = 0;
      long ppuTime = 0;
      long apuTime = 0;

      while (frameCpuCycles < 29781) {      
         long start = System.nanoTime();
         // CPU
         int cpuCycles = _cpu.executeOp();
         long end = System.nanoTime();
         cpuTime += (end - start);
         
         start = System.nanoTime();
         // APU
         _apu.executeCycles(cpuCycles);
         end = System.nanoTime();
         apuTime += (end - start);
         
         start = System.nanoTime();
         // PPU
         int ppuCycles = cpuCycles + cpuCycles + cpuCycles; // TODO cycles between ppu and cpu depends on nes type
         _ppu.executeCycles(ppuCycles);
         end = System.nanoTime();
         ppuTime += (end - start);
         
         frameCpuCycles += cpuCycles;
      }
      
      frameCpuCycles -= 29781;

//      System.out.println(
//            "CPU: " + (cpuTime / nanoToMs) +
//            ", APU: " + (apuTime / nanoToMs) +
//            ", PPU: " + (ppuTime / nanoToMs));
//            ", PPU FT: " + (_ppu.frameTime / nanoToMs) +
//            ", Render BG: " + (_ppu.renderTime / nanoToMs) +
//            ", Render SP: " + (_ppu.renderSpriteTime / nanoToMs));
//            ", Video: " + (_ppu.videoTime / nanoToMs));
//            ", Vblank: " + (_ppu.vblankTime / nanoToMs) +
//            ", Dummy: " + (_ppu.dummyTime / nanoToMs) + 
//            ", Regular: " + (_ppu.regularTime / nanoToMs) +
//            ", RegularX: " + (_ppu.regularXTime / nanoToMs) +
//            ", HandleFrame: " + (_ppu.handleFrameTime / nanoToMs));
      return frameCpuCycles;

   }
   
   public void renderGraphics() {
//      long start = System.nanoTime();
      _videoOut.render();
//      long end = System.nanoTime();
//      System.out.println("Render: " + ((end - start) / nanoToMs));
   }
   
   public void playAudio() {
//      double nanoToMs = 1000000.0;
//      long start = System.nanoTime();
//      _audioOut.render();
//      long end = System.nanoTime();
//      System.out.println("Sound: " + ((end - start) / nanoToMs));
   }
}