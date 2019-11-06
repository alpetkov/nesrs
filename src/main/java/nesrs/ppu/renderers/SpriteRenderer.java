package nesrs.ppu.renderers;

import nesrs.ppu.PpuMemory;
import nesrs.ppu.SpriteMemory;
import nesrs.ppu.registers.CtrlRegister;
import nesrs.ppu.registers.MaskRegister;
import nesrs.ppu.registers.StatusRegister;
import nesrs.util.BitUtil;

public class SpriteRenderer {

   private static class SpriteRenderPipeline {
      int _tileDataLow = 0x0; // 8 bits (8 pixels - 1 tile row pixels)
      int _tileDataHigh = 0x0; // 8 bits (8 pixels - 1 tile row pixels)
      int _attributePaletteData = 0x0; // 2 bits (for 8 pixels)
      boolean _isHighPriority = false;
      int _xPosition = 0x0; // 8 bits // X position on the screen
      boolean _isSpriteZero = false;
   }

   //
   // Sprite memory bytes
   //

   //   Attributes byte
   //  76543210
   //  ||||||||
   //  ||||||++- Palette (4 to 7) of sprite
   //  |||+++--- Unimplemented, reads back as 0
   //  ||+------ Priority (0: in front of background; 1: behind background)
   //  |+------- Flip sprite horizontally
   //  +-------- Flip sprite vertically
   public static final int SPR_ATTR_REVERT_VERTICALLY = 0x80; // bit 7
   public static final int SPR_ATTR_REVERT_HORIZONTALLY = 0x40; // bit 6
   public static final int SPR_ATTR_PRIORITY = 0x20; // bit 5
   public static final int SPR_ATTR_PALETTE = 0x3; // bits 0 & 1

   // In
   private final CtrlRegister _ctrlReg;
   private final MaskRegister _maskReg;
   private final StatusRegister _statusReg;

   private final PpuMemory _memory;
   private final SpriteMemory _sprMemory;

   private int[] _sprTempMemory = new int[0x20]; // Sprite temporary Memory (32b) (8 sprites)

   // Work

   // Sprite render pipeline
   private SpriteRenderPipeline[] _spriteRenderPipelineMemory = new SpriteRenderPipeline[8];

   private boolean _isSpriteZeroInRange = false;

   public SpriteRenderer(
         CtrlRegister ctrlReg,
         MaskRegister maskReg,
         StatusRegister statusReg,
         PpuMemory memory,
         SpriteMemory sprMemory) {

      _ctrlReg = ctrlReg;
      _maskReg = maskReg;
      _statusReg = statusReg;

      _memory = memory;
      _sprMemory = sprMemory;
   }

   //
   // Sprite render
   //

   public void reset() {
      SpriteRenderPipeline noSpriteRenderData = new SpriteRenderPipeline();
      noSpriteRenderData._tileDataLow = 0x0;
      noSpriteRenderData._tileDataHigh = 0x0; // Transparent
      noSpriteRenderData._attributePaletteData = 0x0; // Irrelevant palette select index
      noSpriteRenderData._isHighPriority = false; // < background
      noSpriteRenderData._xPosition = 0x0; // Irrelevant
      noSpriteRenderData._isSpriteZero = false;

      for (int  i = 0; i < _spriteRenderPipelineMemory.length; i++) {
         _spriteRenderPipelineMemory[i] = noSpriteRenderData;
      }
   }

   public void clearSpriteZeroInRangeFlag() {
      // Clear sprite zero flag
      _isSpriteZeroInRange = false;
   }

   public void executeScanlineSpriteCycle(
         int currentCycle,
         int currentScanline,
         int[] scanlineOffscreenBuffer) {

      // Render sprite pixel for current scanline
      if (0 <= currentCycle && currentCycle <= 255) {
         if (currentScanline > ScanlineHelper.FIRST_RENDER_SCANLINE) { // No sprites on first scanline
            if (currentCycle > 7 || !isSpriteClippingEnabled()) {
               renderSpritePixel(currentCycle, scanlineOffscreenBuffer);
            }
         }
      }

      // Evaluate/Fetch sprites for next scanline

      if (0 <= currentCycle && currentCycle <= 63) {
         // Init
         if (currentCycle == 0) {
            for (int i = 0 ; i < 32; i++) {
               _sprTempMemory[i] = 0xFF;
            }
         }

      } else if (64 <= currentCycle && currentCycle <= 255) {
         // Sprite evaluation for next scanline
         if (currentCycle == 64) {
            evaluateSprites(currentScanline);
         }

      } else if (256 <= currentCycle && currentCycle <= 319) {
         // Fetch sprite data for next scanline
//         if (currentCycle == 256) {
//            fetchSpriteTileData(currentScanline);
//         }
         if (currentCycle == 260) {
            fetchSpriteTileData(currentScanline);
         }
      }
   }

   private boolean isSpriteClippingEnabled() {
      return (_maskReg.value & MaskRegister.SPRITE_CLIPPING) == 0;
   }

   private void renderSpritePixel(int currentCycle, int[] scanlineOffscreenBuffer) {
      for (int i = 0; i < _spriteRenderPipelineMemory.length; i++) {
         // Go through all sprites evaluated for the scanline
         SpriteRenderPipeline spriteRenderPipeline = _spriteRenderPipelineMemory[i];

         int fineX = currentCycle - spriteRenderPipeline._xPosition;
         if (0 <= fineX && fineX <= 7) {
            int bitPosition = 1 << (7 - fineX);

            int tilePaletteDataLowBit = ((spriteRenderPipeline._tileDataLow & bitPosition) != 0) ? 1 : 0;
            int tilePaletteDataHighBit = ((spriteRenderPipeline._tileDataHigh & bitPosition) != 0) ? 1 : 0;

            if (tilePaletteDataLowBit != 0 || tilePaletteDataHighBit != 0) {
               // First non transparent sprite. We stop here!

               int bgPixel = scanlineOffscreenBuffer[currentCycle];

               // Determine pixels to draw
               if ((bgPixel & 0x3) == 0 ||
                     spriteRenderPipeline._isHighPriority ||
                     !_maskReg.isBackgroundVisibilityEnabled()) {

                  // BG transparent or SPRITE is high priority -> draw sprite
                  int paletteIndex = (((spriteRenderPipeline._attributePaletteData << 2) | (tilePaletteDataHighBit << 1) | tilePaletteDataLowBit)) & 0xF;

                  scanlineOffscreenBuffer[currentCycle] = 0x10 | paletteIndex;
               }

               // Sprite zero hit test
               if (_maskReg.isBackgroundVisibilityEnabled() &&
                     _maskReg.isSpriteVisibilityEnabled() &&
                     currentCycle <= 254 &&
                     (bgPixel & 0x3) != 0 &&
                     spriteRenderPipeline._isSpriteZero) {

                  // Sprite zero hit
                  _statusReg.value |= StatusRegister.SPRITE_ZERO_OCCURRENCE;
               }

               break;
            }
         }
      }
   }

   private void evaluateSprites(int currentScanline) {
      // Iterate over all 64 sprites and find the first 8 that are suitable for the next scanline.
      int spriteIndexForNextScanline = 0;
      for (int i = 0; i < 64; i++) {
         int spriteMemoryIndex = i << 2; // i*4

         int yPosition = _sprMemory.readMemory(spriteMemoryIndex);

         if (isSpriteInRangeForNextScanline(yPosition, currentScanline)) {

            if (spriteIndexForNextScanline < 8) {
               // 8 sprites are only visible for scanline.
               int tileIndex = _sprMemory.readMemory(spriteMemoryIndex + 1);
               int attributes = _sprMemory.readMemory(spriteMemoryIndex + 2);
               int xPosition = _sprMemory.readMemory(spriteMemoryIndex + 3);

               int spriteTempMemoryIndex = spriteIndexForNextScanline << 2; // * 4

               _sprTempMemory[spriteTempMemoryIndex] = yPosition;
               _sprTempMemory[spriteTempMemoryIndex + 1] = tileIndex;
               _sprTempMemory[spriteTempMemoryIndex + 2] = attributes;
               _sprTempMemory[spriteTempMemoryIndex + 3] = xPosition;

               spriteIndexForNextScanline++;

               if (i == 0) {
                  // Sprite #0 is in range
                  _isSpriteZeroInRange = true;
               }
            } else {
               // TODO FIXME Make this cycle perfect
               // More than 8 sprites suitable for next scanline.
               // Stop the evaluation and set the overflow flag.
               _statusReg.value |= StatusRegister.SCANLINE_SPRITE_COUNT;
               break;
            }
         }
      }
   }

   private boolean isSpriteInRangeForNextScanline(int sprYPosition, int currentScanline) {
      boolean isSpriteInRange = false;

      int spriteFineY =
            (currentScanline + 1) -
            ScanlineHelper.FIRST_RENDER_SCANLINE -
            (sprYPosition + 1);

      if (0 <= spriteFineY && spriteFineY <= 7) {
         isSpriteInRange = true;
      } else {
         if (is16PixelsSprite()) {
            // 8x16 sprite
            if (0 <= spriteFineY && spriteFineY <= 15) {
               isSpriteInRange = true;
            }
         }
      }

      return isSpriteInRange;
   }

   private void fetchSpriteTileData(int currentScanline) {
      for (int spriteIndex = 0; spriteIndex < 8; spriteIndex++) {

         int spriteAddress = spriteIndex << 2; // * 4
         int yPosition = _sprTempMemory[spriteAddress];
         int tileIndex = _sprTempMemory[spriteAddress + 1];
         int attributes = _sprTempMemory[spriteAddress + 2];
         int xPosition = _sprTempMemory[spriteAddress + 3];


         int fineY =
               currentScanline -
               ScanlineHelper.FIRST_RENDER_SCANLINE -
               yPosition;
         int spritePatternTableAddress;

         if (!is16PixelsSprite()) {
            if ((attributes & SPR_ATTR_REVERT_VERTICALLY) != 0) {
               fineY = 7 - fineY;
            }
            spritePatternTableAddress = ((_ctrlReg.value & CtrlRegister.SPRITE_PATTERN_TABLE_ADDR) != 0) ? 0x1000 : 0x0000;

         } else {
            if ((attributes & SPR_ATTR_REVERT_VERTICALLY) != 0) {
               fineY = 15 - fineY;
            }
            spritePatternTableAddress = ((tileIndex & 0x1) != 0) ? 0x1000 : 0x0000;
            tileIndex &= 0xFE; // clear bit 0
            if (fineY > 7) {
               // Pick second tile
               tileIndex++;
               fineY -= 8;
            }
         }

         SpriteRenderPipeline spriteRenderData = new SpriteRenderPipeline();

         if (yPosition == 0xFF &&
               (tileIndex == 0xFE || tileIndex == 0xFF) &&
               attributes == 0xFF &&
               xPosition == 0xFF) {

            // Although there is no sprite, we need to do dummy fetch so that the address line
            // is available (for Mapper04 for example).
            int tileDataLowAddress = spritePatternTableAddress + tileIndex * 16 + 0;
            _memory.read(tileDataLowAddress);
            //_memory.readMemory(tileDataLowAddress + 8);

            // No sprite
            spriteRenderData._tileDataLow = 0x0;
            spriteRenderData._tileDataHigh = 0x0; // Transparent
            spriteRenderData._attributePaletteData = 0x0; // Irrelevant palette select index
            spriteRenderData._isHighPriority = false; // < background
            spriteRenderData._xPosition = 0x0; // Irrelevant
            spriteRenderData._isSpriteZero = false;

         } else {

            int tileDataLowAddress = spritePatternTableAddress + tileIndex * 16 + fineY;
            spriteRenderData._tileDataLow = _memory.read(tileDataLowAddress);
            spriteRenderData._tileDataHigh = _memory.read(tileDataLowAddress + 8);
            if ((attributes & SPR_ATTR_REVERT_HORIZONTALLY) != 0) {
               spriteRenderData._tileDataLow = BitUtil.reverseByte(spriteRenderData._tileDataLow);
               spriteRenderData._tileDataHigh = BitUtil.reverseByte(spriteRenderData._tileDataHigh);
            }
            spriteRenderData._attributePaletteData = attributes & SPR_ATTR_PALETTE;
            spriteRenderData._isHighPriority = (attributes & SPR_ATTR_PRIORITY) == 0;
            spriteRenderData._xPosition = xPosition;
            spriteRenderData._isSpriteZero = (_isSpriteZeroInRange && spriteIndex == 0);
         }

         _spriteRenderPipelineMemory[spriteIndex] = spriteRenderData;
      }
   }

   private boolean is16PixelsSprite() {
      return (_ctrlReg.value & CtrlRegister.SPRITE_SIZE) != 0;
   }
}
