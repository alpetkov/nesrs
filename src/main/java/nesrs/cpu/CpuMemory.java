package nesrs.cpu;

public interface CpuMemory {
   public int readCpuMemory(int address);
   public int writeCpuMemory(int address, int value);
}
