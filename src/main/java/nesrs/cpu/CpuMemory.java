package nesrs.cpu;

public interface CpuMemory {
   public int read(int address);
   public int write(int address, int value);
}
