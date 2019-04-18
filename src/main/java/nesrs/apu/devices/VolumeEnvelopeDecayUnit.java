package nesrs.apu.devices;

/**
 * An envelope generator can generate a constant volume or a saw envelope with
 * optional looping.
 */
public class VolumeEnvelopeDecayUnit {

	private boolean _isLoopingEnabled;
	private boolean _isDisabled;
	private int _volume;

	private Divider _divider;
	private int _counter;

	private boolean _writeFlag;

   public VolumeEnvelopeDecayUnit() {
      _divider = new Divider(0);
   }

   public void write(boolean isLoopingEnabled, boolean isDisabled, int volume) {
      _isLoopingEnabled = isLoopingEnabled;
      _isDisabled = isDisabled;
      _volume = volume;
      _divider.setPeriod(volume + 1);
   }

   public void setDirty() {
      _writeFlag = true;
   }

   public void clock() {
      if (_writeFlag) {
         _counter = 15;
         _divider.reset();
         _writeFlag = false;
         return;
      }

      boolean shouldDeviderOutputClock = _divider.clock();
      if (shouldDeviderOutputClock) {
         if (_isLoopingEnabled && _counter == 0) {
            _counter = 15;
         } else if (_counter > 0) {
            _counter--;
         }
      }
   }

   public int getVolume() {
      if (_isDisabled) {
         return _volume;
      } else {
         return _counter;
      }
   }
}
