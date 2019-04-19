package nesrs.ppu;

import java.util.Arrays;

import nesrs.cartridge.Cartridge;
import nesrs.ppu.registers.CtrlRegister;
import nesrs.ppu.registers.MaskRegister;
import nesrs.ppu.registers.SpriteRamAddressRegister;
import nesrs.ppu.registers.StatusRegister;
import nesrs.ppu.registers.VramAddressScrollRegister;
import nesrs.ppu.renderers.BackgroundRenderer;
import nesrs.ppu.renderers.Palette;
import nesrs.ppu.renderers.ScanlineHelper;
import nesrs.ppu.renderers.SpriteRenderer;

public class Ppu implements PpuPin {

   // Registers
   private final CtrlRegister _ctrlReg = new CtrlRegister();
   private final MaskRegister _maskReg = new MaskRegister();
   private final StatusRegister _statusReg = new StatusRegister();
   private final SpriteRamAddressRegister _sprRamAddressReg = new SpriteRamAddressRegister();
   final VramAddressScrollRegister _vramAddressScrollReg = new VramAddressScrollRegister();

   // Memory
   private final PpuMemory _memory; // VRAM
   private final SpriteMemory _sprMemory = new SpriteMemory(); // Sprite Memory

   // Rendering
   private static final int NES_HEIGHT = 240;
   private static final int NES_WIDTH = 256;

   private final BackgroundRenderer _backgroundRenderer;
   private final SpriteRenderer _spriteRenderer;
   private final int[] _scanlineOffscreenBuffer = new int[NES_WIDTH]; // 256 pixels per scanline
   private final int[] _frameBuffer = new int[NES_HEIGHT * NES_WIDTH];

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

      _backgroundRenderer =
            new BackgroundRenderer(_ctrlReg, _maskReg, _vramAddressScrollReg, _memory);
      _spriteRenderer =
            new SpriteRenderer(_ctrlReg, _maskReg, _statusReg, _memory, _sprMemory);
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

      _vramAddressScrollReg._tempAddress = 0x0;
      _vramAddressScrollReg._bgFineX = 0;
      _vramAddressScrollReg._toggle = false;
      _vramAddressScrollReg._address = 0x0;
      _vramAddressScrollReg._lastValue = 0x0;

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

      _vramAddressScrollReg._tempAddress = 0x0;
      _vramAddressScrollReg._bgFineX = 0;
      _vramAddressScrollReg._toggle = false;
      _vramAddressScrollReg._lastValue = 0x0;
   }

   @Override
   public void executeCycles(int ppuCycles) {
      for (int ppuCycle = 0; ppuCycle < ppuCycles; ppuCycle++) {
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

         if (ScanlineHelper.VBLANK_START_SCANLINE == _currentScanline) {
            if (_currentCycle == 0) {
               // Set VBlank flag
               if (_canSetVblForFrame) {
                  _statusReg.setInVblank(true);
               }

   //            // Clear VBL lock
   //            _canSetVblForFrame = true;

               // Clear spr ram address
               _sprRamAddressReg.value = 0;

               // Clear sprite renderer pipeline
               _spriteRenderer.reset();

            } else if (_currentCycle == 2) {
               if (_ctrlReg.isExecNmiOnVblEnabled()) {
                  _vblListener.handleVbl();
               }

               // Clear VBL lock
               _canSetVblForFrame = true;
            }

         } else if (ScanlineHelper.DUMMY_RENDER_SCANLINE == _currentScanline) {
            if (_currentCycle == 0) {
               // Clear VBlank flag
               _statusReg.setInVblank(false);

               // Fix flags
               _statusReg.value &= ~StatusRegister.SCANLINE_SPRITE_COUNT;
               _statusReg.value &= ~StatusRegister.SPRITE_ZERO_OCCURRENCE;

               // ???
               //_statusReg._value = 0;

               // Clear sprite zero flag
               _spriteRenderer.clearSpriteZeroInRangeFlag();

               if (_isOddFrame && _maskReg.isBackgroundVisibilityEnabled()) {
                  _currentScanlineCyclesCount = ScanlineHelper.CYCLES_COUNT_IN_SCANLINE - 1;
               }

            } else if (_currentCycle == 256/*or 257*/) {
               if (isRenderingEnabled()) {
                  // v:0000010000011111=t:0000010000011111
                  _vramAddressScrollReg._address &= 0xFBE0;
                  _vramAddressScrollReg._address |= (_vramAddressScrollReg._tempAddress & 0x041F);
               }

            } else if (_currentCycle == 303/*or 304*/) {
               // Frame start
               if (isRenderingEnabled()) {
                  _vramAddressScrollReg._address = _vramAddressScrollReg._tempAddress;
               }
            }

            // Render
            if (isRenderingEnabled()) {
               _backgroundRenderer.executeScanlineBackgroundCycle(
                     _currentCycle,
                     _scanlineOffscreenBuffer,
                     false);

               _spriteRenderer.executeScanlineSpriteCycle(
                     _currentCycle,
                     _currentScanline,
                     _scanlineOffscreenBuffer);
            }

         } else if (ScanlineHelper.FIRST_RENDER_SCANLINE <= _currentScanline &&
               _currentScanline <= ScanlineHelper.LAST_RENDER_SCANLINE) {

            if (_currentCycle == 0) {
               // Clear offscreen buffers
               Arrays.fill(_scanlineOffscreenBuffer, 0x00);

            } else if (_currentCycle == 256/*or 257*/) {
               if (isRenderingEnabled()) {
                  // v:0000010000011111=t:0000010000011111
                  _vramAddressScrollReg._address &= 0xFBE0;
                  _vramAddressScrollReg._address |= (_vramAddressScrollReg._tempAddress & 0x041F);
               }
            }

            // Render
            if (isRenderingEnabled()) {
               _backgroundRenderer.executeScanlineBackgroundCycle(
                     _currentCycle,
                     _scanlineOffscreenBuffer,
                     true);

               _spriteRenderer.executeScanlineSpriteCycle(
                     _currentCycle,
                     _currentScanline,
                     _scanlineOffscreenBuffer);
            }

            // Send to video
            if (_currentCycle == _currentScanlineCyclesCount - 1) {
               int[] scanlineBuffer = getScanlineVideo();

               System.arraycopy(
                     scanlineBuffer,
                     0,
                     _frameBuffer,
                     (_currentScanline - ScanlineHelper.FIRST_RENDER_SCANLINE) * NES_WIDTH,
                     NES_WIDTH);

               if (_currentScanline == ScanlineHelper.LAST_RENDER_SCANLINE) {
                  _videoOutListener.handleFrame(_frameBuffer);
               }
            }
         }
      }
   }

   @Override
   public int readRegister(int register) {
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

         return status;
      }

      case PpuPin.REG_SPR_RAM_IO: {
         return _sprMemory.readMemory(_sprRamAddressReg.value);
      }

      case PpuPin.REG_VRAM_IO: {
         int result;

         int vramAddress = _vramAddressScrollReg._address & 0x3FFF;
         if (0 <= vramAddress && vramAddress < 0x3F00) {
            result = _vramAddressScrollReg._lastValue;
            _vramAddressScrollReg._lastValue = _memory.readMemory(vramAddress);
         } else { // Background palette
            result = _memory.readMemory(vramAddress);
            // $2000-$2fff are mirrored at $3000-$3fff
            _vramAddressScrollReg._lastValue = _memory.readMemory(vramAddress - 0x1000);
         }

         _vramAddressScrollReg._address += getVramAddressInc();
         _vramAddressScrollReg._address &= 0x7FFF;

         return result;
      }

      default: {
         // ???
         return 0;
      }
      }
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
         _sprMemory.writeMemory(_sprRamAddressReg.value, value);

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
            _memory.writeMemory(_vramAddressScrollReg._address & 0x3FFF, value);
         }

         _vramAddressScrollReg._address += getVramAddressInc();
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
      return _memory.readMemory(address);
   }

   @Override
   public void writeMemory(int address, int value) {
      _memory.writeMemory(address, value);
   }

   private int getVramAddressInc() {
      return ((_ctrlReg.value & CtrlRegister.ADDR_INC) != 0) ? 32 : 1;
   }

   private int[] getScanlineVideo() {
      int[] scanlineVideoBuffer = new int[_scanlineOffscreenBuffer.length];
      for (int i = 0; i < _scanlineOffscreenBuffer.length; i++) {
         int paletteAddress = 0x3F00 | (_scanlineOffscreenBuffer[i] & 0x1F);
         int colorIndex = _memory.readMemory(paletteAddress);
         int rgb = Palette.RGB[colorIndex & 0x3F];
         scanlineVideoBuffer[i] = rgb;
      }

      return scanlineVideoBuffer;
   }

   private boolean isRenderingEnabled() {
      return _maskReg.isBackgroundVisibilityEnabled() || _maskReg.isSpriteVisibilityEnabled();
   }
}