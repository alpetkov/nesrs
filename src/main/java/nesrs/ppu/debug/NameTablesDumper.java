package nesrs.ppu.debug;

import nesrs.ppu.PpuMemory;
import nesrs.ppu.registers.CtrlRegister;
import nesrs.ppu.renderers.Palette;

public class NameTablesDumper {

   private final CtrlRegister _ctrlReg;
   private final PpuMemory _memory;

   public NameTablesDumper(CtrlRegister ctrlReg, PpuMemory memory) {
      _ctrlReg = ctrlReg;
      _memory = memory;
   }

   public int[][][] getNameTableData() {
      int[][] nameTableData1 = getNameTableData(0x2000);
      int[][] nameTableData2 = getNameTableData(0x2400);
      int[][] nameTableData3 = getNameTableData(0x2800);
      int[][] nameTableData4 = getNameTableData(0x2C00);

      return new int[][][] { nameTableData1, nameTableData2, nameTableData3, nameTableData4 };
   }

   private int[][] getNameTableData(int nameTableAddress) {
      int[][] nameTableData = new int[240][256];

      for (int y = 0; y < 240; y++) {
         for (int x = 0; x < 256; x++) {

            int tileX = x / 8;
            int tileY = y / 8;

            int tileAddress = nameTableAddress + 32 * tileY + tileX;
            int tileIndex = _memory.readMemory(tileAddress);

            int attributeTableX = tileX / 4;
            int attributeTableY = tileY / 4;
            int attributeAddress = nameTableAddress + 960 + 8 * attributeTableY + attributeTableX;

            int attributeByte = _memory.readMemory(attributeAddress);

            int attributeFineX = tileX % 4;
            int attributeFineY = tileY % 4;

            int attributePaletteData = 0x0;
            if (attributeFineX < 2) {
               if (attributeFineY < 2) {
                  // Square 0 (top left)
                  attributePaletteData = attributeByte & 0x03;
               } else {
                  // Square 2 (bottom left)
                  attributePaletteData = (attributeByte & 0x30) >> 4;
               }
            } else {
               if (attributeFineY < 2) {
                  // Square 1 (top right)
                  attributePaletteData = (attributeByte & 0x0C) >> 2;
               } else {
                  // Square 3 (bottom right)
                  attributePaletteData = (attributeByte & 0xC0) >> 6;
               }
            }

            int backgroundPatternTableAddress = getBackgroundPatternTableAddress();

            int fineY = y % 8;
            int tileDataLowAddress = backgroundPatternTableAddress + tileIndex * 16 + fineY;
            int tileDataLow = _memory.readMemory(tileDataLowAddress);
            int tileDataHigh = _memory.readMemory(tileDataLowAddress + 8);

            int fineX = x % 8;
            int bitPosition = 1 << (7 - fineX);

            int tilePaletteDataLowBit = (tileDataLow & bitPosition) != 0 ? 1 : 0;
            int tilePaletteDataHighBit = (tileDataHigh & bitPosition) != 0 ? 1 : 0;
            int paletteIndex =
                  ((attributePaletteData << 2) | (tilePaletteDataHighBit << 1) | tilePaletteDataLowBit) & 0xF;

            if (paletteIndex == 0x04 || paletteIndex == 0x08 || paletteIndex == 0x0C) {
               paletteIndex = 0x00;
            }

            int paletteAddress = 0x3F00 | (paletteIndex & 0x1F);
            int colorIndex = _memory.readMemory(paletteAddress);
            int rgb = Palette.RGB[colorIndex];

            nameTableData[y][x] = rgb;
         }
      }

      return nameTableData;
   }

   public int[][][] getPatternTables() {
      int[][] patternTable1 = getPatternTable(0x0000);
      int[][] patternTable2 = getPatternTable(0x1000);

      return new int[][][] { patternTable1, patternTable2 };
   }

   private int[][] getPatternTable(int patternTableAddress) {
      int[][] tiles = new int[8 * 16][8 * 16];
      for (int tileIndex = 0; tileIndex < 256; tileIndex++) {

         for (int fineY = 0; fineY < 8; fineY++) {

            int tileDataLowAddress = patternTableAddress + tileIndex * 16 + fineY;
            int tileDataLow = _memory.readMemory(tileDataLowAddress);
            int tileDataHigh = _memory.readMemory(tileDataLowAddress + 8);

            for (int fineX = 0; fineX < 8; fineX++) {
               int bitPosition = 1 << (7 - fineX);

               int tilePaletteDataLowBit = (tileDataLow & bitPosition) != 0 ? 1 : 0;
               int tilePaletteDataHighBit = (tileDataHigh & bitPosition) != 0 ? 1 : 0;
               int paletteIndex =
                     ((tilePaletteDataHighBit << 1) | tilePaletteDataLowBit) & 0xF;

               if (paletteIndex == 0x04 || paletteIndex == 0x08 || paletteIndex == 0x0C) {
                  paletteIndex = 0x00;
               }

               int paletteAddress = 0x3F00 | (paletteIndex & 0x1F);
               int colorIndex = _memory.readMemory(paletteAddress);
               int rgb = Palette.RGB[colorIndex & 0x3F];

               tiles[(tileIndex / 16) * 8 + fineY][(tileIndex % 16) * 8 + fineX] = rgb;
            }
         }
      }

      return tiles;
   }

   private int getBackgroundPatternTableAddress() {
      return ((_ctrlReg.value & CtrlRegister.BACKGROUND_PATTERN_TABLE_ADDR) != 0) ? 0x1000 : 0x0000;
   }
}
