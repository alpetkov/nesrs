package nesrs.cartridge.mappers;

import nesrs.cartridge.CartridgeMemory;

public class Mapper066 extends BaseMapper {

   public Mapper066(CartridgeMemory cartridgeMemory) {
      super(cartridgeMemory);
   }

   @Override
   public void writePrgMemory(int cpuAddress, int value) {
      if (0x8000 <= cpuAddress && cpuAddress <= 0xFFFF) {
         int chrBankNumber = (value & 0x03);
         // 8KB
         int chrBankNumberIndex = chrBankNumber * 8;
         for (int i = 0; i < 8; i++) {
            _chrMemMap[i] = (chrBankNumberIndex + i) & (_cartridgeMemory.chrMem.length - 1);
         }

         int prgBankNumber = ((value & 0x30) >> 4);
         // 32KB
         int prgBankNumberIndex = prgBankNumber * 32;
         for (int i = 0; i < 32; i++) {
            _prgRomMap[i] = (prgBankNumberIndex + i) & (_cartridgeMemory.prgRom.length - 1);
         }

       } else {
         super.writePrgMemory(cpuAddress, value);
      }
   }
}
