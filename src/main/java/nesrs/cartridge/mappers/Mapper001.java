package nesrs.cartridge.mappers;

import nesrs.cartridge.CartridgeMemory;
import nesrs.cartridge.NameTableMirroring.NameTableMirroringType;

public class Mapper001 extends BaseMapper {

   private int _shiftRegister;
   private int _writeCount = 0;

   private int _controlRegister = 0x0C;
   private int _chrBank0Register;
   private int _chrBank1Register;
   private int _prgBankRegister;

   public Mapper001(CartridgeMemory cartridgeMemory) {
      super(cartridgeMemory);

      updateBanks();
   }

   @Override
   public void writePrgMemory(int cpuAddress, int value) {
      if (0x8000 <= cpuAddress && cpuAddress <= 0xFFFF) {

         if ((value & 0x80) != 0) {
            _controlRegister |= 0x0C;

            _shiftRegister = 0x00;
            _writeCount = 0;

         } else {
            _shiftRegister |= ((value & 0x01) << _writeCount);
            _writeCount++;

            if (_writeCount == 5) {
                if (cpuAddress <= 0x9FFF) {
                   // Control
                   _controlRegister = _shiftRegister;

                   int nt = _controlRegister & 0x03;

                   switch (nt) {
                   case 0: _cartridgeMemory.ntMirroringType = NameTableMirroringType.ONE_SCREEN_A; break;
                   case 1: _cartridgeMemory.ntMirroringType = NameTableMirroringType.ONE_SCREEN_B; break;
                   case 2: _cartridgeMemory.ntMirroringType = NameTableMirroringType.VERTICAL; break;
                   case 3: _cartridgeMemory.ntMirroringType = NameTableMirroringType.HORIZONTAL; break;
                   }

                } else if (0xA000 <= cpuAddress && cpuAddress <= 0xBFFF) {
                   // Chr Bank 0
                   _chrBank0Register = _shiftRegister & 0x1F;

                } else if (0xC000 <= cpuAddress && cpuAddress <= 0xDFFF) {
                   // Chr Bank 1
                   _chrBank1Register = _shiftRegister & 0x1F;

                } else if (0xE000 <= cpuAddress && cpuAddress <= 0xFFFF) {
                   // Prg Bank
                   _prgBankRegister = _shiftRegister & 0x0F;
                }

                updateBanks();

               _shiftRegister = 0x00;
               _writeCount = 0;
            }
         }

      } else {
         super.writePrgMemory(cpuAddress, value);
      }
   }

   private void updateBanks() {
      int bankNumber;
      int bankNumberIndex;

      // PRG ROM
      int prgRomBankMode = (_controlRegister & 0x0C) >> 2;

      if (prgRomBankMode == 0 || prgRomBankMode == 1) {
         bankNumber = ((_prgBankRegister & 0x0F) >> 1);
         bankNumberIndex = bankNumber * 32;

         // PRG 32KB
         for (int i = 0; i < 32; i++) {
            _prgRomMap[i] = (bankNumberIndex + i) & (_cartridgeMemory.prgRom.length - 1);
         }

      } else if (prgRomBankMode == 2) {
         // PRG 16KB
         // Fix first 16KB bank
         bankNumber = 0x00;
         bankNumberIndex = bankNumber;
         for (int i = 0; i < 16; i++) {
            _prgRomMap[i] = (bankNumberIndex + i) & (_cartridgeMemory.prgRom.length - 1);
         }

         // Switch second 16KB bank
         bankNumber = (_prgBankRegister & 0x0F);
         bankNumberIndex = bankNumber * 16;
         for (int i = 16; i < 32; i++) {
            _prgRomMap[i] = (bankNumberIndex + (i - 16)) & (_cartridgeMemory.prgRom.length - 1);
         }

      } else {
         // PRG 16KB
         // Switch first 16KB bank
         bankNumber = (_prgBankRegister & 0x0F);
         bankNumberIndex = bankNumber * 16;
         for (int i = 0; i < 16; i++) {
            _prgRomMap[i] = (bankNumberIndex + i) & (_cartridgeMemory.prgRom.length - 1);
         }

         // Fix second 16KB bank
         bankNumber = 0x0F;
         bankNumberIndex = bankNumber * 16;
         for (int i = 16; i < 32; i++) {
            _prgRomMap[i] = (bankNumberIndex + (i - 16)) & (_cartridgeMemory.prgRom.length - 1);
         }
      }

      // CHR MEM
      int chrRomBankMode = (_controlRegister & 0x10) >> 4;

      if (chrRomBankMode == 0) {
         // CHR 8KB
         bankNumber = (_chrBank0Register >> 1);
         bankNumberIndex = bankNumber * 8;
         for (int i = 0; i < 8; i++) {
            _chrMemMap[i] = (bankNumberIndex + i) & (_cartridgeMemory.chrMem.length - 1);
         }

      } else {
         // CHR 4KB
         bankNumber = _chrBank0Register;
         bankNumberIndex = bankNumber * 4;
         for (int i = 0 ; i < 4; i++) {
            _chrMemMap[i] = (bankNumberIndex + i) & (_cartridgeMemory.chrMem.length - 1);
         }

         bankNumber = _chrBank1Register;
         bankNumberIndex = bankNumber * 4;
         for (int i = 0; i < 4; i++) {
            _chrMemMap[i + 4] = (bankNumberIndex + i) & (_cartridgeMemory.chrMem.length - 1);
         }
      }
   }
}
