package nesrs.apu.devices;

public class Divider {

   protected int _period;
   protected int _counter;

   public Divider(int period) {
      _period = period;
      _counter = period;
   }

   public int getPeriod() {
      return _period;
   }

   /**
    * Changing a divider's period doesn't affect its current count.
    */
   public void setPeriod(int period) {
      _period = period;
   }

   /**
    * When the divider is clocked (by input clock), it can produce output clock
    * only when its counter reaches 0.
    */
   public boolean clock() {
      if (_period == 0) {
         return false;
      }

      _counter--;
      boolean outputClock = false;

      if (_counter == 0) {
         _counter = _period;
         outputClock = true;
      }

      return outputClock;
   }

   /**
    * Resetting a divider reloads its counter without generating an output clock.
    */
   public void reset() {
      _counter = _period;
   }
}