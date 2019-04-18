package nesrs.apu.devices;

public class Timer extends Divider {

   public Timer(int period) {
      super(period);
   }

   @Override
   public void setPeriod(int period) {
      _period = period;
      if (_counter == 0) {
         _counter = _period;
      }
   }
}
