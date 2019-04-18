package nesrs.controller;

public class StandardController implements Controller {

   public enum Button {
      A,
      B,
      Select,
      Start,
      Up,
      Down,
      Left,
      Right
   }

   // CMOS 4021 shift register
   private int _buttonsLatch = 0x00;

   // Strobe control
   private boolean _strobe = false;

   // Buttons memory
   private int _pressedButtons = 0x00;

   @Override
   public int read() {
      int result = (_buttonsLatch & 0x80) != 0 ? 0x41 : 0x40;

      if (!_strobe) {
         _buttonsLatch <<= 1;
      }

      return result;
   }

   @Override
   public void write(int value) {
      _strobe = ((value & 0x01) == 1);

      if (_strobe) {
         // Strobe. Grab buttons state from memory into the read latch
         _buttonsLatch = _pressedButtons;
      }
   }

   public void pressButton(Button button) {
      switch (button) {
         case A: _pressedButtons |= 0x80; break;
         case B: _pressedButtons |= 0x40; break;
         case Select: _pressedButtons |= 0x20; break;
         case Start: _pressedButtons |= 0x10; break;
         case Up: _pressedButtons |= 0x08; break;
         case Down: _pressedButtons |= 0x04; break;
         case Left: _pressedButtons |= 0x02; break;
         case Right: _pressedButtons |= 0x01; break;
      }

      if (_strobe) {
         _buttonsLatch = _pressedButtons;
      }
   }

   public void releaseButton(Button button) {
      switch (button) {
         case A: _pressedButtons &= ~0x80; break;
         case B: _pressedButtons &= ~0x40; break;
         case Select: _pressedButtons &= ~0x20; break;
         case Start: _pressedButtons &= ~0x10; break;
         case Up: _pressedButtons &= ~0x08; break;
         case Down: _pressedButtons &= ~0x04; break;
         case Left: _pressedButtons &= ~0x02; break;
         case Right: _pressedButtons &= ~0x01; break;
      }

      if (_strobe) {
         _buttonsLatch = _pressedButtons;
      }
   }
}
