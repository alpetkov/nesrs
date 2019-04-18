package nesrs.cartridge.mappers;

import nesrs.cartridge.CartridgeMemory;
import nesrs.cartridge.NameTableMirroring;
import nesrs.cartridge.NameTableMirroring.NameTableIndex;
import nesrs.cpu.IrqListener;

public abstract class BaseMapper implements Mapper {
   protected final CartridgeMemory _cartridgeMemory;

   // CPU's Prg address space is divided into 32 banks each pointing to 1KB of memory (this is
   // what the CPU can address).
   protected final int[] _prgRomMap = new int[32];
   protected final int[] _chrMemMap = new int[8];

   protected IrqListener _irqListener;

   public BaseMapper(CartridgeMemory cartridgeMemory) {
      _cartridgeMemory = cartridgeMemory;

      for (int i = 0; i < 32; i++) {
         _prgRomMap[i] = i & (cartridgeMemory.prgRom.length - 1);
      }

      for (int i = 0; i < 8; i++) {
         _chrMemMap[i] = i & (cartridgeMemory.chrMem.length - 1);
      }
   }

   @Override
   public void setIrqListener(IrqListener irqListener) {
      _irqListener = irqListener;
   }

   public CartridgeMemory getCartridgeMemory() {
      return _cartridgeMemory;
   }

   @Override
   public int readPrgMemory(int cpuAddress) {
      int page = (cpuAddress & 0xF000);

      if (page == 0x4000 || page == 0x5000) {
         // Expansion ROM
         return readExpansionRom(cpuAddress);

      } else if (page == 0x6000 || page == 0x7000) {
         // RAM
         return _cartridgeMemory.prgRam[cpuAddress & 0x1FFF]; // 8KB

      } else {
         // ROM
         return _cartridgeMemory.prgRom[_prgRomMap[(cpuAddress & 0x7FFF) >> 10]] // 32
                                       [(cpuAddress & 0x03FF)]; // 1KB
      }
   }

   @Override
   public void writePrgMemory(int cpuAddress, int value) {
      int page = (cpuAddress & 0xF000);

      if (page == 0x6000 || page == 0x7000) {
         // RAM
         _cartridgeMemory.prgRam[cpuAddress & 0x1FFF] = value;
      }
   }

   @Override
   public int readChrMemory(int ppuAddress) {
      if (0x0000 <= ppuAddress && ppuAddress <= 0x1FFF) {
         return _cartridgeMemory.chrMem[_chrMemMap[(ppuAddress & 0x1FFF) >> 10]] // 8
                                       [ppuAddress & 0x03FF]; // 1KB
      }

      return 0;
   }

   @Override
   public void writeChrMemory(int ppuAddress, int value) {
      if (0x0000 <= ppuAddress && ppuAddress <= 0x1FFF) {
         if (_cartridgeMemory.isChrMemRam) {
            _cartridgeMemory.chrMem[_chrMemMap[(ppuAddress & 0x1FFF) >> 10]] // 3 bits -> 0..7
                                   [ppuAddress & 0x03FF] = value;
         }
      }
   }

   @Override
   public int readNameTable(
         int ppuAddress,
         int[][] ppuNtRAM) {

      NameTableIndex nameTableIndex =
            NameTableMirroring.getNameTableIndex(
                  ppuAddress,
                  _cartridgeMemory.ntMirroringType);
      int nameTableOffset = NameTableMirroring.getNameTableOffset(ppuAddress);

      return readNameTable(nameTableIndex, nameTableOffset, ppuNtRAM);
   }

   @Override
   public void writeNameTable(int ppuAddress, int value, int[][] ppuNtRAM) {
      NameTableIndex nameTableIndex =
            NameTableMirroring.getNameTableIndex(
                  ppuAddress,
                  _cartridgeMemory.ntMirroringType);
      int nameTableOffset = NameTableMirroring.getNameTableOffset(ppuAddress);

      writeNameTable(nameTableIndex, nameTableOffset, value, ppuNtRAM);
   }

   protected int readExpansionRom(int cpuAddress) {
      return 0;
   }

   protected int readNameTable(
         NameTableIndex nameTableIndex,
         int nameTableOffset,
         int[][] ppuNtRAM) {

      switch (nameTableIndex) {
         case A: return ppuNtRAM[0][nameTableOffset];
         case B: return ppuNtRAM[1][nameTableOffset];
         case C: assert false; break; //TODO
         case D: assert false; break; //TODO
      }

      return 0;
   }

   protected void writeNameTable(
         NameTableIndex nameTableIndex,
         int nameTableOffset,
         int value,
         int[][] ppuNtRAM) {

      switch (nameTableIndex) {
         case A: ppuNtRAM[0][nameTableOffset] = value;
                 break;
         case B: ppuNtRAM[1][nameTableOffset] = value;
                 break;
         case C: assert false; break; //TODO
         case D: assert false; break; //TODO
      }
   }
}
