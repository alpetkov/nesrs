package nesrs.ppu.renderers;

import nesrs.ppu.PpuMemory;
import nesrs.ppu.registers.CtrlRegister;
import nesrs.ppu.registers.MaskRegister;
import nesrs.ppu.registers.VramAddressScrollRegister;
import nesrs.util.BitUtil;

public class BackgroundRenderer {

   // 0x0 - 0x2000, 0x1 - 0x2400, 0x2 - 0x2800, 0x3 - 0x2C00
   private static final int[] NAMETABLE_IDX_TO_NAMETABLE_ADDRESS =
         new int[] { 0x2000, 0x2400, 0x2800, 0x2C00 };

   // 0x0 | 0x1
   // 0x2 | 0x3
   private static final int[] NAMETABLE_IDX_TO_HORIZONTAL_NAMETABLE_IDX =
         new int[] { 0x1, 0x0, 0x3, 0x2 };
   private static final int[] NAMETABLE_IDX_TO_VERTICAL_NAMETABLE_IDX =
         new int[] { 0x2, 0x3, 0x0, 0x1 };

   private static class BackgroundTileLatch {
      int _tileIndex = 0x0; // 8 bits
      int _tileDataLow = 0x0; // 8 bits
      int _tileDataHigh = 0x0; // 8 bits
      int _attributePaletteData = 0x0; // 2 bits
   }

   private static class BackgroundRenderPipeline {
      int _tileDataLow = 0x0; // 16 bits (16 pixels - 2 tiles' row pixels)
      int _tileDataHigh = 0x0; // 16 bits (16 pixels - 2 tiles' row pixels)
      int _attributePalleteDataLow = 0x0;
      int _attributePalleteDataHigh = 0x0;
   }

   // In
   private final CtrlRegister _ctrlReg;
   private final MaskRegister _maskReg;
   private final VramAddressScrollRegister _vramAddressScrollReg;

   private final PpuMemory _memory;

   // Work
   private BackgroundTileLatch _bgTileLatch = new BackgroundTileLatch();
   private BackgroundRenderPipeline _bgRenderPipeline = new BackgroundRenderPipeline();

   public BackgroundRenderer(
         CtrlRegister ctrlReg,
         MaskRegister maskReg,
         VramAddressScrollRegister vramAddressScrollReg,
         PpuMemory memory) {

      _ctrlReg = ctrlReg;
      _maskReg = maskReg;
      _vramAddressScrollReg = vramAddressScrollReg;

      _memory = memory;
   }

   public void executeScanlineBackgroundCycle(
         int currentCycle,
         int[] scanlineOffscreenBuffer,
         boolean shouldRender) {

      // Memory fetch = 2 cc
      //
      // Memory fetch phase 1 through 128
      // 1. Name table byte // 2 cc
      // 2. Attribute table byte // 2 cc
      // 3. Pattern table bitmap #0 // 2 cc
      // 4. Pattern table bitmap #1 // 2 cc
      // This process is repeated 32 times. Each 8 cc.
      // All valid on-screen pixel data arrives at the PPU's video output during this time
      //
      // Memory fetch phase 129 through 160
      // 1. Garbage name table byte // 2 cc
      // 2. Garbage name table byte // 2 cc
      // 3. Pattern table bitmap #0 // 2 cc
      // 4. Pattern table bitmap #1 // 2 cc
      // This process is repeated 8 times. Each 8 cc.
      //
      // Memory fetch phase 161 through 168
      // 1. Name table byte // 2 cc
      // 2. Attribute table byte // 2 cc
      // 3. Pattern table bitmap #0 (for next scanline) // 2 cc
      // 4. Pattern table bitmap #1 (for next scanline) // 2 cc
      // This process is repeated 2 times. Each 8 cc.
      //
      // Memory fetch phase 169 through 170
      // 1. Name table byte // 2 cc
      // 2. Name table byte // 2 cc

      if (0 <= currentCycle && currentCycle <= 255) {
         // Memory fetch phase 1 through 128
         int fetchTilePhaseCycle = currentCycle & 0x07;

         if (shouldRender) {
            // Handle render pipeline feed at the beginning of the phase
            if (fetchTilePhaseCycle == 0) {
               loadBackgroundRenderPipeline();
            }
            renderTileData(
                  currentCycle,
                  scanlineOffscreenBuffer,
                  true/*drawPixel*/);
         }

         // Fetch tile (8 cc - do it at once as opposed to 4 x 2cc)
         if (fetchTilePhaseCycle == 7) {
            fetchTileData();
         }

//         if (currentCycle == 251) {
//            incrementBackgroundFineY();
//         }
      } else if (256 <= currentCycle && currentCycle <= 319) {
         if (currentCycle == 256) {
            incrementBackgroundFineY();
         }

         // Memory fetch phase 129 through 160
         // Do nothing

      } else if (320 <= currentCycle && currentCycle <= 335) {
         // Memory fetch phase 161 through 168
         int fetchTilePhaseCycle = currentCycle & 0x07;

         // Handle render pipeline feed at the beginning of the phase
         if (fetchTilePhaseCycle == 0) {
            loadBackgroundRenderPipeline();
         }
         renderTileData(
               currentCycle,
               scanlineOffscreenBuffer,
               false/*drawPixel*/);

         // Fetch tile (8 cc - do it at once as opposed to 4 x 2cc)
         if (fetchTilePhaseCycle == 7) {
            fetchTileData();
         }

      } else {
         // Memory fetch phase 169 through 170
         // Do nothing
      }
   }

   private void loadBackgroundRenderPipeline() {
      // Load pipeline from latch.

      // Load in MSB
      _bgRenderPipeline._tileDataLow =
            (BitUtil.reverseByte(_bgTileLatch._tileDataLow) << 8) |
            (_bgRenderPipeline._tileDataLow & 0x00FF);

      // Load in MSB
      _bgRenderPipeline._tileDataHigh =
            (BitUtil.reverseByte(_bgTileLatch._tileDataHigh) << 8) |
            (_bgRenderPipeline._tileDataHigh & 0x00FF);

      // Load in MSB
      int attributePalleteDataLow = 0x00;
      if ((_bgTileLatch._attributePaletteData & 0x01) != 0) {
         attributePalleteDataLow = 0xFF; // replicate 8 times
      }
      _bgRenderPipeline._attributePalleteDataLow =
         attributePalleteDataLow << 8 | (_bgRenderPipeline._attributePalleteDataLow & 0x00FF);

      // Load in MSB
      int attributePalleteDataHigh = 0x00;
      if ((_bgTileLatch._attributePaletteData & 0x02) != 0) {
         attributePalleteDataHigh = 0xFF; // replicate 8 times
      }
      _bgRenderPipeline._attributePalleteDataHigh =
         attributePalleteDataHigh << 8 | (_bgRenderPipeline._attributePalleteDataHigh & 0xFF);
   }

   private void renderTileData(
         int currentCycle,
         int[] scanlineOffscreenBuffer,
         boolean drawPixel) {

      // Handle background pixel rendering. Pixel is rendered every cycle for total of 256 pixels
      if (drawPixel) {
         if (currentCycle > 7 || !isBackgroundClippingEnabled()) {
            renderBackgroundPixel(currentCycle, scanlineOffscreenBuffer);
         }
      }

      // Shift pipeline
      _bgRenderPipeline._tileDataLow >>= 1;
      _bgRenderPipeline._tileDataHigh >>= 1;
      _bgRenderPipeline._attributePalleteDataLow >>= 1;
      _bgRenderPipeline._attributePalleteDataHigh >>= 1;
   }

   private void renderBackgroundPixel(int currentCycle, int[] scanlineOffscreenBuffer) {
      // Determine palette data
      int fineX = _vramAddressScrollReg.getBackgroundFineX();
      int bitPosition = 1 << fineX;

      int tilePaletteDataLowBit = (_bgRenderPipeline._tileDataLow & bitPosition) != 0 ? 1 : 0;
      int tilePaletteDataHighBit = (_bgRenderPipeline._tileDataHigh & bitPosition) != 0 ? 2 : 0;
      int attributePaletteDataLowBit = (_bgRenderPipeline._attributePalleteDataLow & bitPosition) != 0 ? 4 : 0;
      int attributePaletteDataHighBit = (_bgRenderPipeline._attributePalleteDataHigh & bitPosition) != 0 ? 8 : 0;

      int paletteIndex =
            attributePaletteDataHighBit | attributePaletteDataLowBit |
            tilePaletteDataHighBit | tilePaletteDataLowBit;

      // Palette mirroring
      if (paletteIndex == 0x04 || paletteIndex == 0x08 || paletteIndex == 0x0C) {
         paletteIndex = 0x00;
      }

      scanlineOffscreenBuffer[currentCycle] = paletteIndex;
   }

   /*package*/ void fetchTileData() {
      int nameTableIndex = 0;
      int nameTableAddress = 0;
      int tileX = 0;
      int tileY = 0;

      //
      // Name table read
      //
      nameTableIndex = _vramAddressScrollReg.getNameTableIndex();
      nameTableAddress = NAMETABLE_IDX_TO_NAMETABLE_ADDRESS[nameTableIndex];
      tileX = _vramAddressScrollReg.getBackgroundTileX();
      tileY = _vramAddressScrollReg.getBackgroundTileY();

      //int tileAddress = nameTableAddress + 32 * tileY + tileX;
      int tileAddress = nameTableAddress + (tileY<<5) + tileX;
      _bgTileLatch._tileIndex = _memory.read(tileAddress);

      //
      // Attribute table read
      //
      int attributeTableX = tileX >> 2; // tileX / 4;
      int attributeTableY = tileY >> 2; // tileY / 4;
      // 32x30 (960) tiles in nametable. The last 64 (actually 60) bytes are for attribute data.
      // Each attribute byte is for 32x32 pixels (4x4 tiles).
      //int attributeAddress = nameTableAddress + 960 + 8 * attributeTableY + attributeTableX;
      int attributeAddress = nameTableAddress + 960 + (attributeTableY << 3) + attributeTableX;
      int attributeByte = _memory.read(attributeAddress);

      int attributeFineX = tileX & 0x3; // tileX % 4
      int attributeFineY = tileY & 0x3; // tileY % 4
      if (attributeFineY < 2) {
         if (attributeFineX < 2) {
            // Square 0 (top left)
            _bgTileLatch._attributePaletteData = attributeByte & 0x03;
         } else {
            // Square 1 (top right)
            _bgTileLatch._attributePaletteData = (attributeByte & 0x0C) >> 2;
         }
      } else {
         if (attributeFineX < 2) {
            // Square 2 (bottom left)
            _bgTileLatch._attributePaletteData = (attributeByte & 0x30) >> 4;
         } else {
            // Square 3 (bottom right)
            _bgTileLatch._attributePaletteData = (attributeByte & 0xC0) >> 6;
         }
      }

      // tileX is calculated and stored. Increment for next fetch.
      incrementBackgroundTileX();

      //
      // Pattern table bitmap #0 read
      //
      int fineY = 0;
      int backgroundPatternTableAddress = 0;
      int tileDataLowAddress = 0;

      fineY = _vramAddressScrollReg.getBackgroundFineY();
      backgroundPatternTableAddress = getBackgroundPatternTableAddress();
      //tileDataLowAddress = backgroundPatternTableAddress + _bgTileLatch._tileIndex * 16 + fineY;
      tileDataLowAddress = backgroundPatternTableAddress + (_bgTileLatch._tileIndex << 4) + fineY;
      _bgTileLatch._tileDataLow = _memory.read(tileDataLowAddress);

      //
      // Pattern table bitmap #1 read
      //
      int tileDataHighAddress = tileDataLowAddress + 8;
      _bgTileLatch._tileDataHigh = _memory.read(tileDataHighAddress);
   }

   private void incrementBackgroundTileX() {
      int tileX = _vramAddressScrollReg.getBackgroundTileX();
      int nameTableIndex = _vramAddressScrollReg.getNameTableIndex();

      tileX++;
      if (tileX > 31) {
         tileX = 0;
         nameTableIndex = NAMETABLE_IDX_TO_HORIZONTAL_NAMETABLE_IDX[nameTableIndex];
      }

      _vramAddressScrollReg.setBackgroundTileX(tileX);
      _vramAddressScrollReg.setNameTableIndex(nameTableIndex);
   }

   private void incrementBackgroundFineY() {
      int fineY = _vramAddressScrollReg.getBackgroundFineY();
      int nameTableIndex = _vramAddressScrollReg.getNameTableIndex();
      int tileY = _vramAddressScrollReg.getBackgroundTileY();

      fineY++;
      if (fineY > 7) {
         fineY = 0;
         tileY++;
         if (tileY == 31) {
            tileY = 0;
         } else if (tileY == 30) {
            tileY = 0;
            nameTableIndex = NAMETABLE_IDX_TO_VERTICAL_NAMETABLE_IDX[nameTableIndex];
         }
      }

      _vramAddressScrollReg.setBackgroundFineY(fineY);
      _vramAddressScrollReg.setBackgroundTileY(tileY);
      _vramAddressScrollReg.setNameTableIndex(nameTableIndex);
   }

   private boolean isBackgroundClippingEnabled() {
      return (_maskReg.value & MaskRegister.BACKGROUND_CLIPPING) == 0;
   }

   private int getBackgroundPatternTableAddress() {
      return ((_ctrlReg.value & CtrlRegister.BACKGROUND_PATTERN_TABLE_ADDR) != 0) ? 0x1000 : 0x0000;
   }
}
