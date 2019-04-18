package nesrs.ppu.debug;

import nesrs.ppu.PpuMemory;
import nesrs.ppu.renderers.Palette;

public class PatternTablesDumper {

   private final PpuMemory _memory;

   public PatternTablesDumper(PpuMemory memory) {
      _memory = memory;
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
}