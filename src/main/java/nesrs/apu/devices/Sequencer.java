package nesrs.apu.devices;

public class Sequencer {
   private int[] _sequence;
   private int _counter;

   private int _currentValue;

   public Sequencer(int[] sequence) {
      _sequence = sequence;
      _counter = 0;
   }

   public void setSequence(int[] sequence) {
      _sequence = sequence;
   }

   public int clock() {
      if (_sequence == null || _sequence.length == 0) {
         return -1;
      }

      _currentValue = _sequence[_counter];

      _counter++;
      if (_counter == _sequence.length) {
         _counter = 0;
      }

      return _currentValue;
   }

   public int getCurrentValue() {
      return _currentValue;
   }

   public void reset() {
      _counter = 0;
      _currentValue = -1;
   }
}
