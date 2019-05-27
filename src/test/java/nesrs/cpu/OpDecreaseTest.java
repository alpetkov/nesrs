package nesrs.cpu;

import junit.framework.TestCase;
import nesrs.cpu.Cpu;
import nesrs.cpu.CpuMemory;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class OpDecreaseTest extends TestCase {
   private CpuMemory memory;
   private Cpu cpu;

   @Before
   public void setUp() throws Exception {
      memory = new TestCPUMemory();
      cpu = new Cpu(memory, false);
      cpu.init();
   }

   @After
   public void tearDown() throws Exception {
   }

   @Test
   public void testDEC() throws Exception {
      memory.writeCpuMemory(0x2001, 0xC6); // Op
      memory.writeCpuMemory(0x2002, 0x11);
      memory.writeCpuMemory(0x0011, 0x8E);
      cpu._PC = 0x2001;
      cpu.executeOp();

      assertEquals(5, cpu.getOpCycles());
      assertEquals(0x8D, memory.readCpuMemory(0x0011));
      assertTrue((cpu._P & Cpu.N_FLAG) != 0);
      assertTrue((cpu._P & Cpu.Z_FLAG) == 0);
   }

   @Test
   public void testDEX() throws Exception {
      memory.writeCpuMemory(0x2001, 0xCA); // Op

      cpu._X = 0x8E;
      cpu._PC = 0x2001;
      cpu.executeOp();

      assertEquals(2, cpu.getOpCycles());
      assertEquals(0x8D, cpu._X);
      assertTrue((cpu._P & Cpu.N_FLAG) != 0);
      assertTrue((cpu._P & Cpu.Z_FLAG) == 0);
   }

   @Test
   public void testDEY() throws Exception {
      memory.writeCpuMemory(0x2001, 0x88); // Op

      cpu._Y = 0x8E;
      cpu._PC = 0x2001;
      cpu.executeOp();

      assertEquals(2, cpu.getOpCycles());
      assertEquals(0x8D, cpu._Y);
      assertTrue((cpu._P & Cpu.N_FLAG) != 0);
      assertTrue((cpu._P & Cpu.Z_FLAG) == 0);
   }
}
