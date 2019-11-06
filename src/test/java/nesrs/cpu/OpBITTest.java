package nesrs.cpu;

import junit.framework.TestCase;
import nesrs.cpu.Cpu;
import nesrs.cpu.CpuMemory;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class OpBITTest extends TestCase {
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
   public void testBIT_ZP() throws Exception {
      memory.write(0x2001, 0x24); // Op
      memory.write(0x2002, 0x0A);
      memory.write(0x000A, 0x80);
      
      cpu._A = 0xC0;
      cpu._PC = 0x2001;
      cpu.executeOp();
      
      assertEquals(3, cpu.getOpCycles());
      assertTrue((cpu._P & Cpu.N_FLAG) != 0);
      assertTrue((cpu._P & Cpu.V_FLAG) == 0);
      assertTrue((cpu._P & Cpu.Z_FLAG) == 0);
   }

   @Test
   public void testBIT_ABS() throws Exception {
      memory.write(0x2001, 0x2C); // Op
      cpu._PC = 0x2001;
      cpu.executeOp();
      assertEquals(4, cpu.getOpCycles());
   }
}
