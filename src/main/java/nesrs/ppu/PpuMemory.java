package nesrs.ppu;

import nesrs.cartridge.Cartridge;

public class PpuMemory {
   // PPU addressable memory (16Kb)

   // $4000 - $FFFF  49152   bytes   Mirror of $0000 - $3FFF
   // $3F20 - $3FFF    224   bytes   Mirror of Background + Sprite Palettes ($3F00-$3F1F)
   // $3F10 - $3F1F     16   bytes   Sprite Palette
   // $3F00 - $3F0F     16   bytes   Background Palette
   // $3000 - $3EFF   3840   bytes   Mirror of $2000 - $2EFF
   // $2FC0 - $2FFF     64   bytes   Attribute Table 3
   // $2C00 - $2FBF    960   bytes   Name Table 3 (32x30 tiles)
   // $2BC0 - $2BFF     64   bytes   Attribute Table 2
   // $2800 - $2BBF    960   bytes   Name Table 2 (32x30 tiles)
   // $27C0 - $27FF     64   bytes   Attribute Table 1
   // $2400 - $27BF    960   bytes   Name Table 1 (32x30 tiles)
   // $23C0 - $23FF     64   bytes   Attribute Table 0
   // $2000 - $23BF    960   bytes   Name Table 0 (32x30 tiles)
   // $1000 - $1FFF   4096   bytes   Pattern Table 1 (256x2x8, may be VROM)
   // $0000 - $0FFF   4096   bytes   Pattern Table 0 (256x2x8, may be VROM)

   // Name table A (960b) + Attribute table A (64b) = 1024b
   // Name table B (960b) + Attribute table B (64b) = 1024b
   private int[][] _ntVRAM = new int[2][1024]; // Name table VRAM (A + B)(2Kb) (aka CIRAM)

   int[] _paletteRAM = new int[32]; // Background (16b) + Sprite (16b) Palette RAM.

   private Cartridge _cartridge;

   public PpuMemory(Cartridge cartridge) {
      _cartridge = cartridge;
   }

   public final int[] readTile(int address) {
      int decodedAddress = address & 0x3FFF; // Size Mirroring 

      int tileDataLow = _cartridge.readChrMemory(decodedAddress);
      int tileDataHigh = _cartridge.readChrMemory(decodedAddress + 8);
      
      return new int[] { tileDataLow, tileDataHigh };
   }
   
   public final int read(int address) {
      int decodedAddress = decodeAddress(address);

      int bucket = (decodedAddress & 0x3000) >> 12;
      switch (bucket) {
      case 0:
         // CHR ROM/RAM
      case 1:
         // CHR ROM/RAM
         return _cartridge.readChrMemory(decodedAddress);
      case 2:
         // Name table
         return _cartridge.readNameTable(decodedAddress, _ntVRAM);
      case 3:
         // Palette
         return _paletteRAM[decodedAddress & 0x1F];
      }

      return 0;
   }

   public final void write(int address, int value) {
      int decodedAddress = decodeAddress(address);

      int bucket = (decodedAddress & 0x3000) >> 12;
      switch (bucket) {
      case 0:
         // CHR ROM/RAM
      case 1:
         // CHR ROM/RAM
         _cartridge.writeChrMemory(decodedAddress, value);
         break;
      case 2:
         // Name table
         _cartridge.writeNameTable(decodedAddress, value, _ntVRAM);
         break;
      case 3:
         // Palette
         _paletteRAM[decodedAddress & 0x1F] = value;
      }
   }

   public final void initNtRam() {
      for (int i = 0; i < _ntVRAM[0].length; i++) {
         _ntVRAM[0][i] = 0x00;
         _ntVRAM[1][i] = 0x00;
      }
   }

   /*package*/ static int decodeAddress(int address) {
      // Size Mirroring
      address = address & 0x3FFF;

      if ((address & 0x3F00) == 0x3F00) {
         // Mirror of Background + Sprite Palettes ($3F00-$3F1F)
         address = address & 0x3F1F;
         
         // Background + Sprite Palettes Mirroring
         // Addresses $3F10/$3F14/$3F18/$3F1C are mirrors of $3F00/$3F04/$3F08/$3F0C.
         switch (address) {
            case 0x3F10: address = 0x3F00; break;
            case 0x3F14: address = 0x3F04; break;
            case 0x3F18: address = 0x3F08; break;
            case 0x3F1C: address = 0x3F0C; break;
         }

      } else if ((address & 0x3000) == 0x3000) {
         // Mirror of Name Tables (0x2000 - 0x2EFF)
         address = 0x2000 | (address & 0x0FFF);
      }

      return address;
   }
}

