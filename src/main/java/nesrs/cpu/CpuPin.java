package nesrs.cpu;

public interface CpuPin {
   //
   // From outside
   //

   int init();

   // /RST
   void reset();
   // /NMI
   void nmi();
   // /IRQ
   void irq();

   // CLK (kind of)
   int executeOp();
   int getOpCycles();

   //
   // From inside
   //

   // R/W, A00-A15, D0-D7
   int readMemory(int address);
   void writeMemory(int address, int value);
}