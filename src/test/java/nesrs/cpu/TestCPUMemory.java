package nesrs.cpu;

public class TestCPUMemory implements CpuMemory {

   private int[][] cpuMemory = new int[0x100][0x100]; // CPU addressable memory (64Kb)

   public void setPrgRom(int startCpuAddress, int[][] prgRom) {
      int cpuAddress = startCpuAddress;
      for (int i = 0; i < prgRom.length; i++) {
         for (int j = 0; j < prgRom[i].length; j++) {
            writeCpuMemory(cpuAddress, prgRom[i][j]);
            cpuAddress++;
         }
      }
   }

   public void setPrgRam(int[] prgRam) {
      int cpuAddress = 0x6000;
      for (int i = 0; i < prgRam.length; i++) {
         writeCpuMemory(cpuAddress, prgRam[i]);
         cpuAddress++;
      }
   }

   @Override
   public int readCpuMemory(int address) {
      return cpuMemory[address >> 8][address & 0x00FF];
   }

   @Override
   public int writeCpuMemory(int address, int value) {
      cpuMemory[address >> 8][address & 0x00FF] = value & 0xFF;
      return 0;
   }
}
