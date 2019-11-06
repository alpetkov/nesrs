package nesrs.ppu;

import java.util.Arrays;

import nesrs.cartridge.Cartridge;
import nesrs.ppu.registers.CtrlRegister;
import nesrs.ppu.registers.MaskRegister;
import nesrs.ppu.registers.SpriteRamAddressRegister;
import nesrs.ppu.registers.StatusRegister;
import nesrs.ppu.registers.VramAddressScrollRegister;
import nesrs.ppu.renderers.Palette;
import nesrs.ppu.renderers.ScanlineHelper;
import nesrs.util.BitUtil;

public class Ppu implements PpuPin {

   // Registers
   private final CtrlRegister _ctrlReg = new CtrlRegister();
   private final MaskRegister _maskReg = new MaskRegister();
   private final StatusRegister _statusReg = new StatusRegister();
   private final SpriteRamAddressRegister _sprRamAddressReg = new SpriteRamAddressRegister();
   final VramAddressScrollRegister _vramAddressScrollReg = new VramAddressScrollRegister();

   // Memory
   private final PpuMemory _memory; // VRAM
   private final int[] _spriteRam = new int[256]; // Sprite RAM (256b) (64 sprites)

   // Rendering
   private static final int NES_HEIGHT = 240;
   private static final int NES_WIDTH = 256;

   private final int[] _scanlineOffscreenBuffer = new int[NES_WIDTH]; // 256 pixels per scanline
   private final int[] _frameBuffer = new int[NES_HEIGHT * NES_WIDTH];

   // Background pipeline
   // Eval
   private final BackgroundTileLatch _bgTileLatch = new BackgroundTileLatch();
   // Fetched
   private final BackgroundRenderPipeline _bgRenderPipeline = new BackgroundRenderPipeline();

   // Sprite pipeline
   // Eval
   private final int[] _spriteTempMemory = new int[32]; // Sprite Temporary Memory (32b) (8 sprites)
   private boolean _isSpriteZeroInRange = false;
   // Fetched
   private final SpriteRenderPipeline _spriteRenderPipeline = new SpriteRenderPipeline();

   // PPU execution counters
   private int _currentCycle;
   private int _currentScanline;
   private int _currentScanlineCyclesCount;
   private boolean _isOddFrame;
   private boolean _canSetVblForFrame;

   // Signal listeners
   private VblListener _vblListener;
   private VideoOutListener _videoOutListener;

   public Ppu(Cartridge cartridge) {
      this(new PpuMemory(cartridge));
   }

   // For unit tests.
   Ppu(PpuMemory memory) {
      _memory = memory;
   }

   //
   // PPU
   //

   @Override
   public void setVblListener(VblListener vblListener) {
      _vblListener = vblListener;
   }

   @Override
   public void setVideoOutListener(VideoOutListener videoListener) {
      _videoOutListener = videoListener;
   }

   @Override
   public void init() {
      //
      // PPU power up state
      //

      //   Initial Register Values Register    At Power    After Reset
      //   PPUCTRL ($2000)    0x00 0000    0x00 0000
      //   PPUMASK ($2001)    0000 0xx0    0000 0xx0
      //   PPUSTATUS ($2002)    +0+x xxxx    U??x xxxx
      //   $2003    $00    unchanged
      //   $2005/$2006 latch    cleared    cleared
      //   PPUSCROLL ($2005)    $0000    $0000
      //   PPUADDR ($2006)    $0000    unchanged
      //   PPUDATA ($2007)    random    $00
      //   odd frame     ?     ?
      //   CHR RAM    pattern    unchanged
      //   NT RAM    mostly $FF    unchanged
      //   SPR RAM    pattern    pattern
      //
      //   ? = unknown, x = irrelevant, + = often set, U = unchanged

      _currentCycle = -1;
      _currentScanline = 0;
      _currentScanlineCyclesCount = ScanlineHelper.CYCLES_COUNT_IN_SCANLINE;
      _isOddFrame = true;
      _canSetVblForFrame = true;

      // Registers
      _ctrlReg.value = 0x00;
      _maskReg.value = 0x06;
      _statusReg.value = 0x00;
      _sprRamAddressReg.value = 0x00;
      _vramAddressScrollReg.init();

      _memory.initNtRam();
   }

   @Override
   public void reset() {
      _currentCycle = -1;
      _currentScanline = 0;
      _currentScanlineCyclesCount = ScanlineHelper.CYCLES_COUNT_IN_SCANLINE;
      _isOddFrame = true;
      _canSetVblForFrame = true;

      // Registers
      _ctrlReg.value = 0x00;
      _maskReg.value = 0x06;
      _statusReg.value &= 0x80;
      _vramAddressScrollReg.reset();
   }

   @Override
   public void executeCycles(int cycles) {
      for (int i = 0; i < cycles; i++) {
         _currentCycle++;

         //
         // Determine scanline & frame
         //

         if (_currentCycle == _currentScanlineCyclesCount) {
            // New scanline
            _currentCycle = 0;
            _currentScanline++;
            _currentScanlineCyclesCount = ScanlineHelper.CYCLES_COUNT_IN_SCANLINE;

            if (_currentScanline == ScanlineHelper.SCANLINES_COUNT_IN_FRAME) {
               // New frame
               _currentScanline = 0;
               _isOddFrame = !_isOddFrame;
               Arrays.fill(_frameBuffer, 0x00);
            }
         }

         //
         // Scanline rendering
         //

         switch (_currentScanline) {
         case 0: {
            // VBLANK START.
            if (_currentCycle == 0) {
               // Set VBlank flag
               if (_canSetVblForFrame) {
                  _statusReg.setInVblank(true);
               }

               // Clear spr ram address
               _sprRamAddressReg.value = 0;

            } else if (_currentCycle == 2) {
               if (_ctrlReg.isExecNmiOnVblEnabled()) {
                  _vblListener.handleVbl();
               }

               // Clear VBL lock
               _canSetVblForFrame = true;
            }

            break;
         }
         case 1: case 2: case 3: case 4: case 5: case 6: case 7: case 8: case 9:
         case 10: case 11: case 12: case 13: case 14: case 15: case 16: case 17: case 18: case 19: {
            // VBLANK REST.
            break;
         }
         case 20: {
            // DUMMY.
            if (_currentCycle == 0) {
               // Clear VBlank flag
               _statusReg.setInVblank(false);
               
               // Fix flags
               _statusReg.value &= ~StatusRegister.SCANLINE_SPRITE_COUNT;
               _statusReg.value &= ~StatusRegister.SPRITE_ZERO_OCCURRENCE;
               // Clear sprite zero flag
               _isSpriteZeroInRange = false;

               if (_isOddFrame && _maskReg.isBackgroundVisibilityEnabled()) {
                  _currentScanlineCyclesCount = ScanlineHelper.CYCLES_COUNT_IN_SCANLINE - 1;
               }
            }

            // Render
            if (_maskReg.isRenderingEnabled()) {
                executeRenderingCycle(true);
            }

            break;
         }
         case 21:case 22:case 23:case 24:case 25:case 26:case 27:case 28:case 29:case 30:
         case 31:case 32:case 33:case 34:case 35:case 36:case 37:case 38:case 39:case 40:
         case 41:case 42:case 43:case 44:case 45:case 46:case 47:case 48:case 49:case 50:
         case 51:case 52:case 53:case 54:case 55:case 56:case 57:case 58:case 59:case 60:
         case 61:case 62:case 63:case 64:case 65:case 66:case 67:case 68:case 69:case 70:
         case 71:case 72:case 73:case 74:case 75:case 76:case 77:case 78:case 79:case 80:
         case 81:case 82:case 83:case 84:case 85:case 86:case 87:case 88:case 89:case 90:
         case 91:case 92:case 93:case 94:case 95:case 96:case 97:case 98:case 99:case 100:
         case 101:case 102:case 103:case 104:case 105:case 106:case 107:case 108:case 109:case 110:
         case 111:case 112:case 113:case 114:case 115:case 116:case 117:case 118:case 119:case 120:
         case 121:case 122:case 123:case 124:case 125:case 126:case 127:case 128:case 129:case 130:
         case 131:case 132:case 133:case 134:case 135:case 136:case 137:case 138:case 139:case 140:
         case 141:case 142:case 143:case 144:case 145:case 146:case 147:case 148:case 149:case 150:
         case 151:case 152:case 153:case 154:case 155:case 156:case 157:case 158:case 159:case 160:
         case 161:case 162:case 163:case 164:case 165:case 166:case 167:case 168:case 169:case 170:
         case 171:case 172:case 173:case 174:case 175:case 176:case 177:case 178:case 179:case 180:
         case 181:case 182:case 183:case 184:case 185:case 186:case 187:case 188:case 189:case 190:
         case 191:case 192:case 193:case 194:case 195:case 196:case 197:case 198:case 199:case 200:
         case 201:case 202:case 203:case 204:case 205:case 206:case 207:case 208:case 209:case 210:
         case 211:case 212:case 213:case 214:case 215:case 216:case 217:case 218:case 219:case 220:
         case 221:case 222:case 223:case 224:case 225:case 226:case 227:case 228:case 229:case 230:
         case 231:case 232:case 233:case 234:case 235:case 236:case 237:case 238:case 239:case 240:
         case 241:case 242:case 243:case 244:case 245:case 246:case 247:case 248:case 249:case 250:
         case 251:case 252:case 253:case 254:case 255:case 256:case 257:case 258:case 259:case 260: {
            // REGULAR.

            if (_currentCycle == 0) {
               // Clear offscreen buffers
               Arrays.fill(_scanlineOffscreenBuffer, 0x00);
            }   
            
            // Render
            if (_maskReg.isRenderingEnabled()) {
               executeRenderingCycle(false);
            }

            // Send to video
            if (_currentCycle == _currentScanlineCyclesCount - 1) {
               int[] scanlineBuffer = getScanlineVideo();

               System.arraycopy(
                     scanlineBuffer,
                     0,
                     _frameBuffer,
                     (_currentScanline - 21) * NES_WIDTH,
                     NES_WIDTH);
               if (_currentScanline == 260) {
                  _videoOutListener.handleFrame(_frameBuffer);    
               }
            }
            break;
         }
         case 261:
            // WASTE.
            break;
         }
      }
   }

   @Override
   public int readRegister(int register) {
      int result;
      switch (register) {

      case PpuPin.REG_STATUS: {
         // $2002 R toggle = 0
         int status = _statusReg.value;

         _statusReg.setInVblank(false);
         _vramAddressScrollReg._toggle = false;

         // Reading one PPU cycle before VBL unsets it
         if (ScanlineHelper.WASTE_SCANLINE == _currentScanline &&
               _currentCycle == _currentScanlineCyclesCount - 1) {
            _canSetVblForFrame = false;
            _ctrlReg.setExecNmiOnVblEnabled(false);

         } else if (ScanlineHelper.VBLANK_START_SCANLINE == _currentScanline &&
               _currentCycle <= 1) {
            _ctrlReg.setExecNmiOnVblEnabled(false);
         }

         result = status;
         break;
      }

      case PpuPin.REG_SPR_RAM_IO: {
         result = _spriteRam[_sprRamAddressReg.value];
         break;
      }

      case PpuPin.REG_VRAM_IO: {
         int vramAddress = _vramAddressScrollReg._address & 0x3FFF;
         if (0 <= vramAddress && vramAddress < 0x3F00) {
            result = _vramAddressScrollReg._lastValue;
            _vramAddressScrollReg._lastValue = _memory.read(vramAddress);
         } else { // Background palette
            result = _memory.read(vramAddress);
            // $2000-$2fff are mirrored at $3000-$3fff
            _vramAddressScrollReg._lastValue = _memory.read(vramAddress - 0x1000);
         }

         _vramAddressScrollReg._address += _ctrlReg.getVramAddressInc();
         _vramAddressScrollReg._address &= 0x7FFF;

         break;
      }

      default: {
         // ???
         result = 0;
      }
      }

      return result;
   }

   @Override
   public void writeRegister(int register, int value) {
      switch (register) {

      case PpuPin.REG_CTRL: {
         // $2000 W %---- --NN
         // temp    %---- NN--   ---- ----
         // t:0000|1100|0000|0000=d:0000|0011
         _vramAddressScrollReg._tempAddress &= 0x73FF;
         _vramAddressScrollReg._tempAddress |= ((value << 10) & 0x0C00);

         if (_statusReg.isInVblank() && !_ctrlReg.isExecNmiOnVblEnabled() &&
               ((value & CtrlRegister.EXEC_NMI_ON_VBLANK) != 0)) {
            _vblListener.handleVbl();
         }

         _ctrlReg.value = value;
         break;
      }

      case PpuPin.REG_MASK: {
         _maskReg.value = value;
         break;
      }

      case PpuPin.REG_SPR_RAM_ADDR: {
         _sprRamAddressReg.value = value & 0xFF;
         break;
      }

      case PpuPin.REG_SPR_RAM_IO: {
         _spriteRam[_sprRamAddressReg.value] = value;

         _sprRamAddressReg.value += 1;
         _sprRamAddressReg.value &= 0xFF;
         break;
      }

      case PpuPin.REG_SCROLL: {
         // Total bits affected with $2005:
         // temp        %0yyy --YY   YYYX XXXX
         // fineX

         if (!_vramAddressScrollReg._toggle) {
            // First write

            // $2005     W %XXXX Xxxx (toggle is cleared)
            //
            // temp      %0--- ---- ---X XXXX
            // Fine X    %xxx
            // toggle    1

            // t:0000000000011111=d:11111000
            // x=d:00000111
            _vramAddressScrollReg._tempAddress &= 0x7FE0;
            _vramAddressScrollReg._tempAddress |= ((value >> 3) & 0x1F);
            _vramAddressScrollReg._bgFineX = (value & 0x07);
         } else {
            // Second write

            // $2005     W %YYYY Yyyy (toggle is set)
            // temp      %0yyy --YY YYY- ----
            // toggle    0

            // t:0000001111100000=d:11111000
            // t:0111000000000000=d:00000111
            _vramAddressScrollReg._tempAddress &= 0x0C1F;
            _vramAddressScrollReg._tempAddress |= ((value << 2) & 0x03E0);
            _vramAddressScrollReg._tempAddress |= ((value << 12) & 0x7000);
         }

         _vramAddressScrollReg._toggle = !_vramAddressScrollReg._toggle;
         break;
      }

      case PpuPin.REG_VRAM_ADDR: {

         // Total bits affected with $2006:
         // temp      %00yy NNYY YYYX XXXX
         // address   temp

         if (!_vramAddressScrollReg._toggle) {
            // Upper address byte (first write)

            // $2006     W %--yy NNYY (toggle is cleared)
            // temp      %00yy NNYY ---- ----

            // t:0011111100000000=d:00111111
            // t:1100000000000000=0
            _vramAddressScrollReg._tempAddress &= 0x00FF;
            _vramAddressScrollReg._tempAddress |= ((value << 8) & 0x3F00);
         } else {
            // Lower address byte (second write)

            // $2006     W %YYYX XXXX (toggle is set)
            // temp      %0--- ----   YYYX XXXX
            // address   temp

            // t:0000000011111111=d:11111111
            // v=t
            _vramAddressScrollReg._tempAddress &= 0x7F00;
            _vramAddressScrollReg._tempAddress |= (value & 0x00FF);
            _vramAddressScrollReg._address = _vramAddressScrollReg._tempAddress;
         }

         _vramAddressScrollReg._toggle = !_vramAddressScrollReg._toggle;
         break;
      }

      case PpuPin.REG_VRAM_IO: {
         if ((_statusReg.value & StatusRegister.VRAM_WRITE_FLAG) == 0) {
            _memory.write(_vramAddressScrollReg._address & 0x3FFF, value);
         }

         _vramAddressScrollReg._address += _ctrlReg.getVramAddressInc();
         _vramAddressScrollReg._address &= 0x7FFF;
         break;
      }

      default: {
         // ???
      }
      }
   }

   @Override
   public int readMemory(int address) {
      return _memory.read(address);
   }

   @Override
   public void writeMemory(int address, int value) {
      _memory.write(address, value);
   }

   private int[] getScanlineVideo() {
      int[] scanlineVideoBuffer = new int[_scanlineOffscreenBuffer.length];
      for (int i = 0; i < _scanlineOffscreenBuffer.length; i++) {
         int paletteOffset = _scanlineOffscreenBuffer[i];
         // decode
         switch (paletteOffset) {
         case 0x10: paletteOffset = 0x00; break;
         case 0x14: paletteOffset = 0x04; break;
         case 0x18: paletteOffset = 0x08; break;
         case 0x1C: paletteOffset = 0x0C; break;
         }
         int colorIndex = _memory._paletteRAM[paletteOffset];
         
         int rgb = Palette.RGB[colorIndex & 0x3F];
         scanlineVideoBuffer[i] = rgb;
      }

      return scanlineVideoBuffer;
   }

   // 0x0 - 0x2000, 0x1 - 0x2400, 0x2 - 0x2800, 0x3 - 0x2C00
   private static final int[] NAMETABLE_IDX_TO_NAMETABLE_ADDRESS =
         new int[] { 0x2000, 0x2400, 0x2800, 0x2C00 };
   
   private void executeRenderingCycle(boolean isDummyScanline) {
      switch (_currentCycle) {
      case 0:case 1:case 2:case 3:case 4:case 5:case 6:case 7:case 8:case 9:case 10:case 11:case 12:case 13:case 14:case 15:
      case 16:case 17:case 18:case 19:case 20:case 21:case 22:case 23:case 24:case 25:case 26:case 27:case 28:case 29:case 30:case 31:
      case 32:case 33:case 34:case 35:case 36:case 37:case 38:case 39:case 40:case 41:case 42:case 43:case 44:case 45:case 46:case 47:
      case 48:case 49:case 50:case 51:case 52:case 53:case 54:case 55:case 56:case 57:case 58:case 59:case 60:case 61:case 62:case 63: {
         // Memory fetch phase 1 through 128
         int fetchBgTilePhaseCycle = _currentCycle & 0x07;

         if (fetchBgTilePhaseCycle == 0) {
            // Handle render pipeline feed at the beginning of the phase
            _bgRenderPipeline.load(_bgTileLatch);
         }
         
         if (!isDummyScanline) {
            renderPixel();
         }

         if (fetchBgTilePhaseCycle == 7) {
            // Fetch data for next tile to the latch
            fetchBgTileData();
         }

         break;
      }
      case 64: {
         // Sprite evaluation for next scanline
         evaluateSpritesForNextScanline(_currentScanline);

         // Fallthrough/Continue to rendering.
      }
      case 65:case 66:case 67:case 68:case 69:case 70:case 71:case 72:case 73:case 74:case 75:case 76:case 77:case 78:case 79:
      case 80:case 81:case 82:case 83:case 84:case 85:case 86:case 87:case 88:case 89:case 90:case 91:case 92:case 93:case 94:case 95:
      case 96:case 97:case 98:case 99:case 100:case 101:case 102:case 103:case 104:case 105:case 106:case 107:case 108:case 109:case 110:case 111:
      case 112:case 113:case 114:case 115:case 116:case 117:case 118:case 119:case 120:case 121:case 122:case 123:case 124:case 125:case 126:case 127:
      case 128:case 129:case 130:case 131:case 132:case 133:case 134:case 135:case 136:case 137:case 138:case 139:case 140:case 141:case 142:case 143:
      case 144:case 145:case 146:case 147:case 148:case 149:case 150:case 151:case 152:case 153:case 154:case 155:case 156:case 157:case 158:case 159:
      case 160:case 161:case 162:case 163:case 164:case 165:case 166:case 167:case 168:case 169:case 170:case 171:case 172:case 173:case 174:case 175:
      case 176:case 177:case 178:case 179:case 180:case 181:case 182:case 183:case 184:case 185:case 186:case 187:case 188:case 189:case 190:case 191:
      case 192:case 193:case 194:case 195:case 196:case 197:case 198:case 199:case 200:case 201:case 202:case 203:case 204:case 205:case 206:case 207:
      case 208:case 209:case 210:case 211:case 212:case 213:case 214:case 215:case 216:case 217:case 218:case 219:case 220:case 221:case 222:case 223:
      case 224:case 225:case 226:case 227:case 228:case 229:case 230:case 231:case 232:case 233:case 234:case 235:case 236:case 237:case 238:case 239:
      case 240:case 241:case 242:case 243:case 244:case 245:case 246:case 247:case 248:case 249:case 250:case 251:case 252:case 253:case 254:case 255: {
         // Memory fetch phase 1 through 128
         int fetchBgTilePhaseCycle = _currentCycle & 0x07;
         
         if (fetchBgTilePhaseCycle == 0) {
            // Handle render pipeline feed at the beginning of the phase
            _bgRenderPipeline.load(_bgTileLatch);
         }

         if (!isDummyScanline) {
            renderPixel();
         }

         if (fetchBgTilePhaseCycle == 7) {
            // Fetch data for next tile to the latch
            fetchBgTileData();
         }
         break;
      }
      case 256: {
         // Memory fetch phase 129 through 160

         // v:0000010000011111=t:0000010000011111
         _vramAddressScrollReg._address &= 0xFBE0;
         _vramAddressScrollReg._address |= (_vramAddressScrollReg._tempAddress & 0x041F);

         _vramAddressScrollReg.incrementBackgroundFineY();

         // Fetch sprite data for next scanline
         fetchSpriteTileDataForNextScanline(_currentScanline);

         break;
      }
      case 257:case 258:case 259:case 260:case 261:case 262:case 263:case 264:case 265:case 266:case 267:case 268:case 269:case 270:case 271:
      case 272:case 273:case 274:case 275:case 276:case 277:case 278:case 279:case 280:case 281:case 282:case 283:case 284:case 285:case 286:case 287:
      case 288:case 289:case 290:case 291:case 292:case 293:case 294:case 295:case 296:case 297:case 298:case 299:case 300:case 301:case 302: {
         // Memory fetch phase 129 through 160
         break;
      }
      case 303: {
         // Memory fetch phase 129 through 160
         if (isDummyScanline) {
            // Frame start
            _vramAddressScrollReg._address = _vramAddressScrollReg._tempAddress;
         }
         break;
      }
      case 304:case 305:case 306:case 307:case 308:case 309:case 310:case 311:case 312:case 313:case 314:case 315:case 316:case 317:case 318:case 319: {
         // Memory fetch phase 129 through 160
         break;
      }
      case 320:case 321:case 322:case 323:case 324:case 325:case 326: {
         // Memory fetch phase 161 through 168
         break;
      }
      case 327: {
         // Memory fetch phase 161 through 168
         fetchNextScanlineBgTileData();
         break;
      }
      case 328:case 329:case 330:case 331:case 332:case 333:case 334: {
         // Memory fetch phase 161 through 168
         break;
      }
      case 335: {
         // Memory fetch phase 161 through 168
         fetchNextScanlineBgTileData();
         break;
      }
      case 336:case 337:case 338:case 339:case 340:case 341: {
         break;
      }
      }
   }
   
   private void renderPixel() {
      int pixel = 0x00;
      
      // BG pixel.
      if (_currentCycle > 7 || !_maskReg.isBackgroundClippingEnabled()) {
         int fineX = _vramAddressScrollReg.getBackgroundFineX();
         int bitPosition = 1 << fineX;

         pixel = _bgRenderPipeline.getPixel(bitPosition);
      }
      // Shift BG pipeline.
      _bgRenderPipeline.shift(1);
      
      // Sprite pixel.
      if (_currentScanline > ScanlineHelper.FIRST_RENDER_SCANLINE) { // No sprites on first scanline
      
         if (_currentCycle > 7 || !_maskReg.isSpriteClippingEnabled()) {
            SpriteRenderTileData spriteRenderTileData = _spriteRenderPipeline.data[_currentCycle];
            if (spriteRenderTileData != null) {
               int fineX = _currentCycle - spriteRenderTileData._xPosition;
               int bitPosition = 1 << (7 - fineX);
               int spritePixel = spriteRenderTileData.getPixel(bitPosition);

               boolean isBgPixelTransparent = (pixel & 0x3) == 0;

               // Determine pixels to draw
               if (isBgPixelTransparent || spriteRenderTileData._isHighPriority ||
                     !_maskReg.isBackgroundVisibilityEnabled()) {

                  // BG transparent or SPRITE is high priority -> draw sprite
                  pixel = spritePixel;
               }

               // Sprite zero hit test
               if (spriteRenderTileData._isSpriteZero && !isBgPixelTransparent && 
                     _maskReg.isBackgroundVisibilityEnabled() &&
                     _maskReg.isSpriteVisibilityEnabled()  && _currentCycle <= 254) {
                  // Sprite zero hit
                  _statusReg.value |= StatusRegister.SPRITE_ZERO_OCCURRENCE;
               }
            }
         }
      }
      
      _scanlineOffscreenBuffer[_currentCycle] = pixel;
   }

   private void fetchBgTileData() {
      int nameTableIndex = 0;
      int nameTableAddress = 0;
      int tileX = 0;
      int tileY = 0;

      //
      // Name table read
      //
      nameTableIndex = (_vramAddressScrollReg._address >> 10) & 0x3;
      nameTableAddress = NAMETABLE_IDX_TO_NAMETABLE_ADDRESS[nameTableIndex];
      tileX = _vramAddressScrollReg._address & 0x1F;
      tileY = (_vramAddressScrollReg._address >> 5) & 0x1F;

      //int tileAddress = nameTableAddress + 32 * tileY + tileX;
      int tileAddress = nameTableAddress + (tileY << 5) + tileX;
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
      int squareSelector = (attributeFineY & 2) | ((attributeFineX & 2) >> 1);
      _bgTileLatch._attributePaletteData = (attributeByte & ATTRIBUTE_PALETTE_DATA_SQUARE_BITS[squareSelector]) >> (squareSelector << 1);
      
      // tileX is calculated and stored. Increment for next fetch.
      _vramAddressScrollReg.incrementBackgroundTileX();

      //
      // Pattern table bitmap #0 read
      //
      int fineY = 0;
      int backgroundPatternTableAddress = 0;
      int tileDataLowAddress = 0;

      fineY = (_vramAddressScrollReg._address >> 12) & 0x7;
      backgroundPatternTableAddress = _ctrlReg.getBackgroundPatternTableAddress();
      tileDataLowAddress = backgroundPatternTableAddress + (_bgTileLatch._tileIndex << 4) + fineY;
      int[] res = _memory.readTile(tileDataLowAddress);
//      _bgTileLatch._tileDataLow = _memory.read(tileDataLowAddress);
      _bgTileLatch._tileDataLow = res[0];
      _bgTileLatch._tileDataHigh = res[1];

      //
      // Pattern table bitmap #1 read
      //
//      int tileDataHighAddress = tileDataLowAddress + 8;
//      _bgTileLatch._tileDataHigh = _memory.read(tileDataHighAddress);
   }

   private void fetchNextScanlineBgTileData() {
      _bgRenderPipeline.load(_bgTileLatch);
      _bgRenderPipeline.shift(8);
      fetchBgTileData();
   }
   
   private void evaluateSpritesForNextScanline(int currentScanline) {
      for (int i = 0 ; i < 32; i++) {
         _spriteTempMemory[i] = 0xFF;
      }

      // Iterate over all 64 sprites and find the first 8 that are suitable for the next scanline.
      int spriteIndexForNextScanline = 0;
      for (int i = 0; i < 64; i++) {
         int spriteMemoryIndex = i << 2; // i*4

         int yPosition = _spriteRam[spriteMemoryIndex];

         if (isSpriteInRangeForNextScanline(yPosition, currentScanline)) {

            if (spriteIndexForNextScanline < 8) {
               // 8 sprites are only visible for scanline.
               int tileIndex = _spriteRam[spriteMemoryIndex + 1];
               int attributes = _spriteRam[spriteMemoryIndex + 2];
               int xPosition = _spriteRam[spriteMemoryIndex + 3];

               int spriteTempMemoryIndex = spriteIndexForNextScanline << 2; // * 4

               _spriteTempMemory[spriteTempMemoryIndex] = yPosition;
               _spriteTempMemory[spriteTempMemoryIndex + 1] = tileIndex;
               _spriteTempMemory[spriteTempMemoryIndex + 2] = attributes;
               _spriteTempMemory[spriteTempMemoryIndex + 3] = xPosition;

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
         if (_ctrlReg.is16PixelsSprite()) {
            // 8x16 sprite
            if (0 <= spriteFineY && spriteFineY <= 15) {
               isSpriteInRange = true;
            }
         }
      }

      return isSpriteInRange;
   }

   private void fetchSpriteTileDataForNextScanline(int currentScanline) {
      resetSpritePipeline();
      
      for (int spriteIndex = 0; spriteIndex < 8; spriteIndex++) {

         int spriteAddress = spriteIndex << 2; // * 4
         int yPosition = _spriteTempMemory[spriteAddress];
         int tileIndex = _spriteTempMemory[spriteAddress + 1];
         int attributes = _spriteTempMemory[spriteAddress + 2];
         int xPosition = _spriteTempMemory[spriteAddress + 3];

         int fineY =
               currentScanline -
               ScanlineHelper.FIRST_RENDER_SCANLINE -
               yPosition;

         int spritePatternTableAddress;
         if (!_ctrlReg.is16PixelsSprite()) {
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

         SpriteRenderTileData spriteRenderData = new SpriteRenderTileData();

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

         for (int fineX = 0; fineX <= 7; fineX++) {
            int position = spriteRenderData._xPosition + fineX;
            if (position <= 255) {
               int bitPosition = 1 << (7 - fineX);
               if (_spriteRenderPipeline.data[position] == null && ((spriteRenderData.getPixel(bitPosition) & 0x3) != 0)) {
                  _spriteRenderPipeline.data[position] = spriteRenderData;
               }
            }
         }
      }
   }

   private void resetSpritePipeline() {
      Arrays.fill(_spriteRenderPipeline.data, null);
   }

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

      final void load(BackgroundTileLatch latch) {
         _tileDataLow = (BitUtil.reverseByte(latch._tileDataLow) << 8) | (_tileDataLow & 0x00FF);
         _tileDataHigh = (BitUtil.reverseByte(latch._tileDataHigh) << 8) | (_tileDataHigh & 0x00FF);

         // Load in MSB
         int attributePalleteDataLow = 0x0000;
         if ((latch._attributePaletteData & 0x01) != 0) {
            attributePalleteDataLow = 0xFF00; // replicate 8 times
         }
         _attributePalleteDataLow = attributePalleteDataLow | (_attributePalleteDataLow & 0x00FF);

         // Load in MSB
         int attributePalleteDataHigh = 0x0000;
         if ((latch._attributePaletteData & 0x02) != 0) {
            attributePalleteDataHigh = 0xFF00; // replicate 8 times
         }
         _attributePalleteDataHigh = attributePalleteDataHigh | (_attributePalleteDataHigh & 0x00FF);
      }

      final int getPixel(int bitPosition) {
         int tilePaletteDataLowBit = (_tileDataLow & bitPosition) != 0 ? 1 : 0;
         int tilePaletteDataHighBit = (_tileDataHigh & bitPosition) != 0 ? 2 : 0;
         int attributePaletteDataLowBit = (_attributePalleteDataLow & bitPosition) != 0 ? 4 : 0;
         int attributePaletteDataHighBit = (_attributePalleteDataHigh & bitPosition) != 0 ? 8 : 0;

         int paletteIndex =
               attributePaletteDataHighBit | attributePaletteDataLowBit |
               tilePaletteDataHighBit | tilePaletteDataLowBit;

         // Palette mirroring
         if (paletteIndex == 0x04 || paletteIndex == 0x08 || paletteIndex == 0x0C) {
            paletteIndex = 0x00;
         }

         return paletteIndex;
      }

      final void shift(int times) {
         _tileDataLow >>= times;
         _tileDataHigh >>= times;
         _attributePalleteDataLow >>= times;
         _attributePalleteDataHigh >>= times;
      }
   }

   private static class SpriteRenderTileData {
      int _tileDataLow = 0x0; // 8 bits (8 pixels - 1 tile row pixels)
      int _tileDataHigh = 0x0; // 8 bits (8 pixels - 1 tile row pixels)
      int _attributePaletteData = 0x0; // 2 bits (for 8 pixels)
      boolean _isHighPriority = false;
      int _xPosition = 0x0; // 8 bits // X position on the screen
      boolean _isSpriteZero = false;

      public final int getPixel(int bitPosition) {
         int tilePaletteDataLowBit = ((_tileDataLow & bitPosition) != 0) ? 1 : 0;
         int tilePaletteDataHighBit = ((_tileDataHigh & bitPosition) != 0) ? 1 : 0;
         int paleteIndex = (((_attributePaletteData << 2) | (tilePaletteDataHighBit << 1) | tilePaletteDataLowBit)) & 0xF;
         return 0x10 | paleteIndex;
      }
   }
   
   private static class SpriteRenderPipeline {
      SpriteRenderTileData[] data = new SpriteRenderTileData[256];
   }
   
   //  Attributes byte
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

   private final int[] ATTRIBUTE_PALETTE_DATA_SQUARE_BITS = new int[] {
         0x03, /* Square 0 (top left) */
         0x0C, /* Square 1 (top right) */
         0x30, /* Square 2 (bottom left) */
         0xC0  /* Square 3 (bottom right) */
         };
}