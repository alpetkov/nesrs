package nesrs.ppu;

public interface PpuPin {

   static final int REG_CTRL = 0; // PPU Control Register (W)
   static final int REG_MASK = 1; // PPU Mask Register (W)
   static final int REG_STATUS = 2; // PPU Status Register (R)

   static final int REG_SPR_RAM_ADDR = 3; // SPR-RAM Address Register (W)
   static final int REG_SPR_RAM_IO = 4; // SPR-RAM I/O Register (RW)

   static final int REG_SCROLL = 5; // Scroll Register (W2)
   static final int REG_VRAM_ADDR = 6; // VRAM Address Register (W2)
   static final int REG_VRAM_IO = 7; // VRAM I/O Register (RW)

   //
   // From outside/input
   //

   void init();
   // /RST
   void reset();

   // CLK (kind of)
   void executeCycles(int ppuCycles);

   // R/W, D0-D8, A0-A2, /CS
   int readRegister(int register);
   void writeRegister(int register, int value);

   //
   // From inside/output
   //

   // /INT (kind of)
   void setVblListener(VblListener vblListener);
   // VOUT (kind of)
   void setVideoOutListener(VideoOutListener videoOutListener);

   // /RD, /WR, AD0-AD7, A8-A13, ALE (kind of)
   int readMemory(int address);
   void writeMemory(int address, int value);
}