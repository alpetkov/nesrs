package nesrs.cartridge.mappers;

import nesrs.cartridge.CartridgeMemory;
import nesrs.cartridge.NameTableMirroring.NameTableMirroringType;

public class Mapper004 extends BaseMapper {

   private int _bankSelect;
   // Bank registers
   private int[] _r = new int[8];

   private int _irqLatch;
   private boolean _irqReload = false;
   private int _irqCounter;
   private boolean _irqEnabled = true;

   private int _ppuAddress;

   public Mapper004(CartridgeMemory cartridgeMemory) {
      super(cartridgeMemory);

      // Fixed last bank
      for (int i = 24; i < 32; i++) {
         _prgRomMap[i] = _cartridgeMemory.prgRom.length + i - 32;
      }
   }

   @Override
   public void writePrgMemory(int cpuAddress, int value) {
      if (0x8000 <= cpuAddress && cpuAddress <= 0xFFFF) {

         if (0x8000 <= cpuAddress && cpuAddress <= 0x9FFF) {
            if ((cpuAddress & 0x1) == 0) {
               // bank select
               _bankSelect = value;

            } else {
               // bank data
               int bankRegisterIndex = _bankSelect & 0x07;
               if (bankRegisterIndex == 6 || bankRegisterIndex == 7) {
                  _r[bankRegisterIndex] = value & 0x3F;

               } else if (bankRegisterIndex == 0 || bankRegisterIndex == 1) {
                  _r[bankRegisterIndex] = value & 0xFE;

               } else {
                  _r[bankRegisterIndex] = value;
               }
            }

            updateBanks();

         } else if (0xA000 <= cpuAddress && cpuAddress <= 0xBFFF) {
            if ((cpuAddress & 0x0001) == 0) {
               // mirroring
               if ((value & 0x01) == 0) {
                  _cartridgeMemory.ntMirroringType = NameTableMirroringType.VERTICAL;
               } else {
                  _cartridgeMemory.ntMirroringType = NameTableMirroringType.HORIZONTAL;
               }
            } else {
               // TODO write protection
            }

         } else if (0xC000 <= cpuAddress && cpuAddress <= 0xDFFF) {
            if ((cpuAddress & 0x0001) == 0) {
               _irqLatch = value;
            } else {
               _irqCounter = 0;
            }
            _irqReload = true;

         } else if (0xE000 <= cpuAddress && cpuAddress <= 0xFFFF) {
            if ((cpuAddress & 0x0001) == 0) {
               _irqEnabled = false;
            } else {
               _irqEnabled = true;
            }
         }

       } else {
         super.writePrgMemory(cpuAddress, value);
      }
   }

   @Override
   public void writeChrMemory(int ppuAddress, int value) {
      super.writeChrMemory(ppuAddress, value);

      if ((_ppuAddress & 0x1000) == 0 && (ppuAddress & 0x1000) != 0) {
         updateIrqCounter();
      }
      _ppuAddress = ppuAddress;
   }

   @Override
   public int readChrMemory(int ppuAddress) {
      int value = super.readChrMemory(ppuAddress);

      if ((_ppuAddress & 0x1000) == 0 && (ppuAddress & 0x1000) != 0) {
         updateIrqCounter();
      }
      _ppuAddress = ppuAddress;

      return value;
   }

   private void updateBanks() {
      // PRG
      if ((_bankSelect & 0x40) == 0) {
         // 8KB
         int bankNumber = _r[6];
         int bankNumberIndex = bankNumber * 8;
         for (int i = 0; i < 8; i++) {
            _prgRomMap[i] = (bankNumberIndex + i) & (_cartridgeMemory.prgRom.length - 1);
         }

         // 8KB
         bankNumber = _r[7];
         bankNumberIndex = bankNumber * 8;
         for (int i = 8; i < 16; i++) {
            _prgRomMap[i] = (bankNumberIndex + i - 8) & (_cartridgeMemory.prgRom.length - 1);
         }

         // 8KB + 8KB
         // Fixed last two banks
         for (int i = 16; i < 32; i++) {
            _prgRomMap[i] = _cartridgeMemory.prgRom.length + i - 32;
         }

      } else {
         // 8KB
         // Fixed first bank
         for (int i = 0; i < 8; i++) {
            _prgRomMap[i] = _cartridgeMemory.prgRom.length + i - 16;
         }

         // 8KB
         int bankNumber = _r[7];
         int bankNumberIndex = bankNumber * 8;
         for (int i = 8; i < 16; i++) {
            _prgRomMap[i] = (bankNumberIndex + i - 8) & (_cartridgeMemory.prgRom.length - 1);
         }

         // 8KB
         bankNumber = _r[6];
         bankNumberIndex = bankNumber * 8;
         for (int i = 16; i < 24; i++) {
            _prgRomMap[i] = (bankNumberIndex + i) & (_cartridgeMemory.prgRom.length - 1);
         }

         // 8KB
         // Fixed last bank
         for (int i = 24; i < 32; i++) {
            _prgRomMap[i] = _cartridgeMemory.prgRom.length + i - 32;
         }
      }

      for (int i = 0; i < 32; i++) {
         _prgRomMap[i] = _prgRomMap[i] & (_cartridgeMemory.prgRom.length - 1);
      }

      // CHR
      if ((_bankSelect & 0x80) == 0) {
         // 2KB
         _chrMemMap[0] = _r[0];
         _chrMemMap[1] = _r[0] + 1;
         // 2KB
         _chrMemMap[2] = _r[1];
         _chrMemMap[3] = _r[1] + 1;
         // 1KB + 1KB + 1KB + 1KB
         _chrMemMap[4] = _r[2];
         _chrMemMap[5] = _r[3];
         _chrMemMap[6] = _r[4];
         _chrMemMap[7] = _r[5];
      } else {
         // 1KB + 1KB + 1KB + 1KB
         _chrMemMap[0] = _r[2];
         _chrMemMap[1] = _r[3];
         _chrMemMap[2] = _r[4];
         _chrMemMap[3] = _r[5];
         // 2KB
         _chrMemMap[4] = _r[0];
         _chrMemMap[5] = _r[0] + 1;
         // 2KB
         _chrMemMap[6] = _r[1];
         _chrMemMap[7] = _r[1] + 1;
      }

      for (int i = 0; i < 8; i++) {
         _chrMemMap[i] = _chrMemMap[i] & (_cartridgeMemory.chrMem.length - 1);
      }
   }

   private void updateIrqCounter() {
      if (_irqReload) {
         _irqReload = false;
         _irqCounter = _irqLatch;
      }

      if (_irqCounter == 0) {
//         if (_irqLatch == 0) {
//            return;
//         }
         if (_irqEnabled) {
            if (_irqListener != null) {
               _irqListener.handleIrq();
            }
         }
         _irqCounter = _irqLatch;
      } else {
         _irqCounter--;
      }
   }
}

