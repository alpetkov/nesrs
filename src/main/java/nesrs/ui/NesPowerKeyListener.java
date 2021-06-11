package nesrs.ui;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import nesrs.Nes;

public class NesPowerKeyListener implements KeyListener {

   private final Nes _nes;

   public NesPowerKeyListener(Nes nes) {
      _nes = nes;
   }

   @Override
   public void keyTyped(KeyEvent e) {
   }

   @Override
   public void keyPressed(KeyEvent e) {
   }

   @Override
   public void keyReleased(KeyEvent e) {
      if (e.getKeyCode() == KeyEvent.VK_R) {
         _nes.reset();
      }
   }
}