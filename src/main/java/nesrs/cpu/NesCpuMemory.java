package nesrs.cpu;

import nesrs.apu.ApuPin;
import nesrs.cartridge.CartridgePin;
import nesrs.controller.Controller;
import nesrs.ppu.PpuPin;

public class NesCpuMemory implements CpuMemory {
   // CPU addressable memory (64Kb)

   // $C000 - $FFFF   16384   bytes   PRG-ROM (Cartridge ROM) upper bank - executable code
   // $8000 - $BFFF   16384   bytes   PRG-ROM (Cartridge ROM) lower bank - executable code
   // $6000 - $7FFF    8192   bytes   SRAM/PRG-RAM (Cartridge RAM)
   // $4020 - $5FFF    8160   bytes   Expansion ROM - Used with Nintendo's MMC5 to expand the capabilities of VRAM
   // $4000 - $401F      32   bytes   Input/Output registers (APU, DMA, External devices, etc.)
   // $2008 - $3FFF    8184   bytes   Mirror of $2000 - $2007 (1023 times)
   // $2000 - $2007       8   bytes   Input/Output registers (PPU)
   // $1800 - $1FFF    2048   bytes   Mirror of $0000-$07FF
   // $1000 - $17FF    2048   bytes   Mirror of $0000-$07FF
   // $0800 - $0FFF    2048   bytes   Mirror of $0000-$07FF
   // $0000 - $07FF    2048   bytes   RAM

   private final int[] _ram = new int[0x800]; // CPU ram (2Kb)

   private CartridgePin _cartridge;
   private PpuPin _ppu;
   private ApuPin _apu;

   private Controller _controller1;
   private Controller _controller2;

   public void setCartridge(CartridgePin cartridge) {
      _cartridge = cartridge;
   }

   public void setPpu(PpuPin ppu) {
      _ppu = ppu;
   }

   public void setApu(ApuPin apu) {
      _apu = apu;
   }

   public void setController1(Controller controller1) {
      _controller1 = controller1;
   }

   public void setController2(Controller controller2) {
      _controller2 = controller2;
   }

   @Override
   public int readCpuMemory(int address) {
      int page = address & 0xF000;
      
      if (page == 0x0000 || page == 0x1000) {
         // RAM
         return _ram[address & 0x07FF];

      } else if (page == 0x2000 || page == 0x3000) {
         // PPU
         if (_ppu != null) {
            return _ppu.readRegister(address & 0x0007);
         }

      } else if (page == 0x4000) {
         // I/O Registers or Expansion ROM

         if (address == 0x4015) {
            // APU
            if (_apu != null) {
               return _apu.readRegister(address);
            }

         } else if (address == 0x4016) {
            // Controller 1
            if (_controller1 != null) {
               return _controller1.read();
            }

         } else if (address == 0x4017) {
            // Controller 2
            if (_controller2 != null) {
               return _controller2.read();
            }

         } else if (address >= 0x4020) {
            // Expansion ROM/Cartridge
            if (_cartridge != null) {
               return _cartridge.readPrgMemory(address);
            }
         }
         
      } else {
         // Cartridge
         
         if (_cartridge != null) {
            return _cartridge.readPrgMemory(address);
         }
      }
      
      return 0;
   }

   @Override
   public int writeCpuMemory(int address, int value) {
      int page = (address & 0xF000);

      if (page == 0x0000 || page == 0x1000) {
         // RAM
         _ram[address & 0x07FF] = value;

      } else if (page == 0x2000 || page == 0x3000) {
         // PPU
         
         if (_ppu != null) {
            _ppu.writeRegister(address & 0x0007, value);
         }

      } else if (page == 0x4000) {
         // I/O Registers or Expansion ROM

         if (address <= 0x4013 || address == 0x4015 || address == 0x4017) {
            // APU
            if (_apu != null) {
               _apu.writeRegister(address, value);
            }

         } else if (address == 0x4014) {
            // DMA
            if (_ppu != null) {
               int memAddress = value << 8;
               for (int i = 0; i <= 0xFF; i++) {
                  int memValue = readCpuMemory(memAddress);
                  // Writes to 0x2004 which is mapped to ppu's spr ram register
                  _ppu.writeRegister(PpuPin.REG_SPR_RAM_IO, memValue);
                  memAddress++;
               }
            }

            //513 cycles 1 for read
            //1 write, final is read
            return 513;

         } else if (address == 0x4016) {
            // Controller 1
            if (_controller1 != null) {
               _controller1.write(value & 0x01);
            }

            // Controller 2
            if (_controller2 != null) {
               _controller2.write(value & 0x01);
            }

         } else if (address >= 0x4020) {
            // Expansion ROM
            if (_cartridge != null) {
               _cartridge.writePrgMemory(address, value);
            }
         }
         
      } else {
         // Cartridge
         if (_cartridge != null) {
            _cartridge.writePrgMemory(address, value);
         }
      }
      
      return 0;
   }
}
