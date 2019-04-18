package nesrs.apu;

import nesrs.cpu.IrqListener;

public interface ApuPin {
   //
   // From outside
   //

   int readRegister(int register);
   void writeRegister(int register, int value);

   void init();
   void reset();

   void executeCycles(int cycles);

   //
   // From inside
   //
   // AD1 and AD2 (kind of)
   void setAudioOutListener(AudioOutListener audioOutListener);

   // IRQ
   void setIrqListener(IrqListener irqListener);
}
