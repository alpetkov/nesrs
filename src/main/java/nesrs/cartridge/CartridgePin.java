package nesrs.cartridge;

import nesrs.cpu.IrqListener;

public interface CartridgePin {
   //
   // From outside
   //

   int readPrgMemory(int cpuAddress);
   void writePrgMemory(int cpuAddress, int value);

   int readChrMemory(int ppuAddress);
   void writeChrMemory(int ppuAddress, int value);

   int readNameTable(int ppuAddress, int[][] ppuNtRAM);
   void writeNameTable(int ppuAddress, int value, int[][] ppuNtRAM);

   //
   // From inside
   //

   // /INT (kind of)
   void setIrqListener(IrqListener irqListener);
}
