package nesrs.cartridge;

import nesrs.cartridge.mappers.BaseMapper;
import nesrs.cartridge.mappers.Mapper;
import nesrs.cartridge.mappers.Mapper000;
import nesrs.cartridge.mappers.Mapper001;
import nesrs.cartridge.mappers.Mapper002;
import nesrs.cartridge.mappers.Mapper004;
import nesrs.cartridge.mappers.Mapper066;
import nesrs.cpu.IrqListener;

public class Cartridge implements CartridgePin {
   private final Mapper _mapper;

   public Cartridge(CartridgeMemory cartridgeMemory, int mapperNumber) {
      _mapper = buildMapper(mapperNumber, cartridgeMemory);
   }

   @Override
   public void setIrqListener(IrqListener irqListener) {
      _mapper.setIrqListener(irqListener);
   }

   @Override
   public int readPrgMemory(int cpuAddress) {
      return _mapper.readPrgMemory(cpuAddress);
   }

   @Override
   public void writePrgMemory(int cpuAddress, int value) {
      _mapper.writePrgMemory(cpuAddress, value);
   }

   @Override
   public int readChrMemory(int ppuAddress) {
      return _mapper.readChrMemory(ppuAddress);
   }

   @Override
   public void writeChrMemory(int ppuAddress, int value) {
      _mapper.writeChrMemory(ppuAddress, value);
   }

   @Override
   public int readNameTable(int ppuAddress, int[][] ppuNTRAM) {
      return _mapper.readNameTable(ppuAddress, ppuNTRAM);
   }

   @Override
   public void writeNameTable(int ppuAddress, int value, int[][] ppuNTRAM) {
      _mapper.writeNameTable(ppuAddress, value, ppuNTRAM);
   }

   public CartridgeMemory getCartridgeMemory() {
      return ((BaseMapper)_mapper).getCartridgeMemory();
   }

   private static Mapper buildMapper(int mapperNumber, CartridgeMemory cartridgeMemory) {
      if (mapperNumber == 0) {
         return new Mapper000(cartridgeMemory);

      } else if (mapperNumber == 1) {
         return new Mapper001(cartridgeMemory);

      } else if (mapperNumber == 2) {
         return new Mapper002(cartridgeMemory);

      } else if (mapperNumber == 4) {
         return new Mapper004(cartridgeMemory);


      } else if (mapperNumber == 66) {
         return new Mapper066(cartridgeMemory);
      }

      return null;
   }
}
