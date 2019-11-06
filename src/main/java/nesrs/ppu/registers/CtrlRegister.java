package nesrs.ppu.registers;

public class CtrlRegister {
   // Control Register (W) flags
   //
   // bit 7   Execute NMI on VBlank                  0 = Disabled; 1 = Enabled
   // bit 6   PPU Master/Slave Selection (UNUSED)    0 = Master; 1 = Slave
   // bit 5   Sprite Size                            0 = 8x8; 1 = 8x16
   // bit 4   Background Pattern Table Address       0 = $0000 (VRAM); 1 = $1000 (VRAM)
   // bit 3   Sprite Pattern Table Address for 8x8   0 = $0000 (VRAM); 1 = $1000 (VRAM)
   // bit 2   Address increment per R/W              0 = Increment by 1; going across
   //                                                1 = Increment by 32; going down
   // bit 1,0   Name table address                   00 = $2000 (VRAM);
   //                                                01 = $2400 (VRAM);
   //                                                10 = $2800 (VRAM);
   //                                                11 = $2C00 (VRAM)

   public static final int EXEC_NMI_ON_VBLANK = 0x80;            // bit 7
   public static final int MASTER_SLAVE_SELECTION = 0x40;        // bit 6
   public static final int SPRITE_SIZE = 0x20;                   // bit 5
   public static final int BACKGROUND_PATTERN_TABLE_ADDR = 0x10; // bit 4
   public static final int SPRITE_PATTERN_TABLE_ADDR = 0x08;     // bit 3
   public static final int ADDR_INC = 0x04;                      // bit 2
   public static final int NAME_TABLE_ADDR = 0x03;               // bit 1 & 0

   public int value;

   public final boolean isExecNmiOnVblEnabled() {
      return (value & EXEC_NMI_ON_VBLANK) != 0;
   }

   public final void setExecNmiOnVblEnabled(boolean enabled) {
      if (enabled) {
         value |= EXEC_NMI_ON_VBLANK;
      } else {
         value &= ~EXEC_NMI_ON_VBLANK;
      }
   }
   
   public final int getBackgroundPatternTableAddress() {
      return ((value & BACKGROUND_PATTERN_TABLE_ADDR) != 0) ? 0x1000 : 0x0000;
   }
   
   public final boolean is16PixelsSprite() {
      return (value & CtrlRegister.SPRITE_SIZE) != 0;
   }
   
   public final int getVramAddressInc() {
      return ((value & ADDR_INC) != 0) ? 32 : 1;
   }
}
