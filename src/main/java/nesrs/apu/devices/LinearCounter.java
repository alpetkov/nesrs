package nesrs.apu.devices;

public class LinearCounter {
   private boolean _controlFlag;
   private int _reloadValue;
   private boolean _isHalted;
   private int _count;

   public void setControlFlag(boolean isSet) {
      _controlFlag = isSet;
   }

   public void setReloadValue(int reloadValue) {
      _reloadValue = reloadValue;
   }

   public void setHalted() {
      _isHalted = true;
   }

   public void clock() {
      if (_isHalted) {
         _count = _reloadValue;
      } else {
         if (_count > 0) {
            _count--;
         }
      }

      if (!_controlFlag) {
         _isHalted = false;
      }
   }

   public int getCount() {
      return _count;
   }
}