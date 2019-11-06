package nesrs.ppu.registers;

public class VramAddressScrollRegister {
   // 0x0 | 0x1
   // 0x2 | 0x3
   private static final int[] NAMETABLE_IDX_TO_HORIZONTAL_NAMETABLE_IDX =
         new int[] { 0x1, 0x0, 0x3, 0x2 };
   private static final int[] NAMETABLE_IDX_TO_VERTICAL_NAMETABLE_IDX =
         new int[] { 0x2, 0x3, 0x0, 0x1 };
   
   // VRAM Scroll Register (W2)
   // VRAM Address Register (W2)
   public int _address; // VRAM address entered.
   public int _lastValue; // Stores the last read VRAM value
   public int _tempAddress; // VRAM temp address. VRAM is entered in two steps. Also can be interpret as 0yyy NNYY YYYX XXXX (fineY, name table, tileY, tileX)
   public boolean _toggle; // VRAM address step toggle
   public int _bgFineX; // xxx (Background fineX)

   public void init() {
      _tempAddress = 0x0;
      _bgFineX = 0;
      _toggle = false;
      _address = 0x0;
      _lastValue = 0x0;
   }
   
   public void reset() {
      _tempAddress = 0x0;
      _bgFineX = 0;
      _toggle = false;
      _lastValue = 0x0;
   }
   
   public final int getBackgroundFineX() {
      return _bgFineX;
   }

   public final int getBackgroundFineY() {
      // temp 0yyy NNYY YYYX XXXX
      return (_address >> 12) & 0x7;
   }

   public final void setBackgroundFineY(int fineY) {
      // temp 0yyy NNYY YYYX XXXX
      _address &= 0x8FFF;
      _address |= fineY << 12;
   }

   public final int getBackgroundTileX() {
      // temp 0yyy NNYY YYYX XXXX
      return _address & 0x1F;
   }

   public final void setBackgroundTileX(int tileX) {
      // temp 0yyy NNYY YYYX XXXX
      _address &= 0xFFE0;
      _address |= tileX;
   }

   public final int getBackgroundTileY() {
      // temp 0yyy NNYY YYYX XXXX
      return (_address >> 5) & 0x1F;
   }

   public final void setBackgroundTileY(int tileY) {
      // temp 0yyy NNYY YYYX XXXX
      _address &= 0xFC1F;
      _address |= tileY << 5;
   }

   public final int getNameTableIndex() {
      return (_address >> 10) & 0x3;
   }

   public final void setNameTableIndex(int nameTableIndex) {
      _address &= 0xF3FF;
      _address |= nameTableIndex << 10;
   }
   
   public final void incrementBackgroundTileX() {
      int tileX = getBackgroundTileX();
      int nameTableIndex = getNameTableIndex();

      tileX++;
      if (tileX > 31) {
         tileX = 0;
         nameTableIndex = NAMETABLE_IDX_TO_HORIZONTAL_NAMETABLE_IDX[nameTableIndex];
         setNameTableIndex(nameTableIndex);
      }

      setBackgroundTileX(tileX);
   }

   public final void incrementBackgroundFineY() {
      int fineY = getBackgroundFineY();
      int nameTableIndex = getNameTableIndex();
      int tileY = getBackgroundTileY();

      fineY++;
      if (fineY > 7) {
         fineY = 0;

         tileY++;
         if (tileY == 31) {
            tileY = 0;
         } else if (tileY == 30) {
            tileY = 0;

            nameTableIndex = NAMETABLE_IDX_TO_VERTICAL_NAMETABLE_IDX[nameTableIndex];
            setNameTableIndex(nameTableIndex);
         }
         setBackgroundTileY(tileY);
      }

      setBackgroundFineY(fineY);
   }
}
