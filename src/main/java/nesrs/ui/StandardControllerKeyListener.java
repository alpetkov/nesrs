package nesrs.ui;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.HashMap;
import java.util.Map;

import nesrs.controller.StandardController;
import nesrs.controller.StandardController.Button;

public class StandardControllerKeyListener implements KeyListener {

   private final StandardController _standardController;
   private final Map<Integer, Button> _keyCodeToButton;

   public StandardControllerKeyListener(StandardController standardController) {
      _standardController = standardController;
      _keyCodeToButton = buildKeyCodeToButtonMapForStandardController1();
   }

   @Override
   public void keyTyped(KeyEvent e) {
   }

   @Override
   public void keyPressed(KeyEvent e) {
      Button b = _keyCodeToButton.get(e.getKeyCode());
      if (b != null) {
         _standardController.pressButton(b);
      }

   }

   @Override
   public void keyReleased(KeyEvent e) {
      Button b = _keyCodeToButton.get(e.getKeyCode());
      if (b != null) {
         _standardController.releaseButton(b);
      }
   }

   Map<Integer, Button> buildKeyCodeToButtonMapForStandardController1() {
      final Map<Integer, Button> keyCodeToButton = new HashMap<Integer, Button>();

      keyCodeToButton.put(KeyEvent.VK_UP, Button.Up);
      keyCodeToButton.put(KeyEvent.VK_DOWN, Button.Down);
      keyCodeToButton.put(KeyEvent.VK_LEFT, Button.Left);
      keyCodeToButton.put(KeyEvent.VK_RIGHT, Button.Right);
      keyCodeToButton.put(KeyEvent.VK_Z, Button.A);
      keyCodeToButton.put(KeyEvent.VK_X, Button.B);
      keyCodeToButton.put(KeyEvent.VK_C, Button.Select);
      keyCodeToButton.put(KeyEvent.VK_V, Button.Start);

      return keyCodeToButton;
   }
}
