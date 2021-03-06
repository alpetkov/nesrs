package nesrs.cpu;

import junit.framework.TestCase;
import nesrs.cpu.Cpu;
import nesrs.cpu.CpuMemory;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class OpLSRTest extends TestCase {
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
   public void testLSR() throws Exception {
      memory.write(0x2001, 0x4A); // Op

      cpu._A = 0x81;
      cpu._PC = 0x2001;
      cpu.executeOp();

      assertEquals(2, cpu.getOpCycles());
      assertEquals(0x40, cpu._A);
      assertTrue((cpu._P & Cpu.N_FLAG) == 0);
      assertTrue((cpu._P & Cpu.Z_FLAG) == 0);
      assertTrue((cpu._P & Cpu.C_FLAG) != 0);
   }
}
