package nesrs.controller;

public interface Controller {
   void write(int value);
   int read();
   default void captureState() {}
}
