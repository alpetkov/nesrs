package nesrs.cartridge.mappers;

import nesrs.cartridge.CartridgeMemory;

public class Mapper002 extends BaseMapper {

   public Mapper002(CartridgeMemory cartridgeMemory) {
      super(cartridgeMemory);

      // Fixed to the last.
      for (int i = 0; i < 16; i++) {
         _prgRomMap[31 - i] = cartridgeMemory.prgRom.length - 1 - i;
      }
   }

   @Override
   public void writePrgMemory(int cpuAddress, int value) {
      if (0x8000 <= cpuAddress && cpuAddress <= 0xFFFF) {
         // 16KB
         int bankNumber = (value & 0x0F);
         int bankNumberIndex = bankNumber * 16;
         for (int i = 0; i < 16; i++) {
            _prgRomMap[i] = bankNumberIndex & (_cartridgeMemory.prgRom.length - 1);
         }

       } else {
         super.writePrgMemory(cpuAddress, value);
      }
   }
}
