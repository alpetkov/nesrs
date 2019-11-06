package nesrs.controller;

public class NoOpController implements Controller {
   @Override
   public void write(int value) {
   }

   @Override
   public int read() {
      return 0;
   }
}
