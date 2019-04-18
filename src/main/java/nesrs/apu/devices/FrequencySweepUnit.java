package nesrs.apu.devices;

/**
 * The sweep unit can adjust a square channel's period periodically.
 */
public class FrequencySweepUnit {

   private boolean _shouldAdd1WhenDecreasing;

   private boolean _isEnabled;
   private boolean _isInDecreaseMode;
   private int _shiftCount; // 3 bits

   private Divider _divider;
   private boolean _reloadFlag;

   public static class ClockStatus {
      public boolean shouldChangeChannelTimerRawPeriod;
      public int newTimerRawPeriod;
      public boolean shouldSilenceDac;
   }

   private ClockStatus _status;

   public FrequencySweepUnit(boolean shouldAdd1WhenDecreasing) {
      _shouldAdd1WhenDecreasing = shouldAdd1WhenDecreasing;
      _divider = new Divider(0);
      _status = new ClockStatus();
   }

   public void write(int value) {
      _isEnabled = (value & 0x80) != 0;

      int period = (value & 0x70) >> 4;
      _divider.setPeriod(period + 1);

      _isInDecreaseMode = (value & 0x08) != 0;
      _shiftCount = value & 0x07;

      _reloadFlag = true;
   }

   public void write(boolean isEnabled, int period, boolean isInDecreaseMode, int shiftCount) {
      _isEnabled = isEnabled;
      _divider.setPeriod(period + 1);
      _isInDecreaseMode = isInDecreaseMode;
      _shiftCount = shiftCount;

      _reloadFlag = true;
   }

   public void clock(int channelTimerRawPeriod) {
      boolean shouldDeviderOutputClock = _divider.clock();

      _status.shouldChangeChannelTimerRawPeriod = false;
      _status.shouldSilenceDac = false;

      int newTimerRawPeriod = recalculateChannelTimerRawPeriod(channelTimerRawPeriod);
      if (channelTimerRawPeriod < 8 || newTimerRawPeriod > 0x07FF) {
         // DAC 0;
         _status.shouldSilenceDac = true;
         return;
      }

      if (shouldDeviderOutputClock) {
         if (_isEnabled && _shiftCount > 0) {
            // Overwrite
            _status.shouldChangeChannelTimerRawPeriod = true;
            _status.newTimerRawPeriod = newTimerRawPeriod;
         }
      }

      if (_reloadFlag) {
         _divider.reset();
         _reloadFlag = false;
      }
   }

   public ClockStatus getStatus() {
      return _status;
   }

   /*package*/ int recalculateChannelTimerRawPeriod(int channelTimerRawPeriod) {
      int delta = channelTimerRawPeriod >> _shiftCount;
      if (_isInDecreaseMode) {
         delta = -delta;
//         delta &= 0x07FF;
         if (_shouldAdd1WhenDecreasing) {
            delta += 1;
         }
      }

      return channelTimerRawPeriod + delta;
   }
}
