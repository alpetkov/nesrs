package nesrs.apu.devices;

/**
 * A length counter allows automatic duration control.
 */
public class LengthCounter {

   private static final int[] COUNT_MAP = new int[] {
      /*       0     1     2     3     4     5     6     7     8     9     A     B     C     D     E     F*/
      /*0x00*/ 0x0A, 0xFE, 0x14, 0x02, 0x28, 0x04, 0x50, 0x06, 0xA0, 0x08, 0x3C, 0x0A, 0x0E, 0x0C, 0x1A, 0x0E,
      /*0x10*/ 0x0C, 0x10, 0x18, 0x12, 0x30, 0x14, 0x60, 0x16, 0xC0, 0x18, 0x48, 0x1A, 0x10, 0x1C, 0x20, 0x1E,
   };

   private int _count;
   private boolean _isHalted;
   private boolean _isEnabled = true;

   public void setHalted(boolean isHalted) {
      _isHalted = isHalted;
   }

   public void setEnabled(boolean isEnabled) {
      _isEnabled = isEnabled;
      if (!isEnabled) {
         // When disabled counting is reset to 0 and stays that way.
         _count = 0;
      }
   }

   public void setCount(int countKey) {
      if (_isEnabled) {
         _count = COUNT_MAP[countKey];
      }
   }

   public void clock() {
      if (!_isHalted && _count > 0) {
         _count--;
      }
   }

   public int getCount() {
      return _count;
   }
}