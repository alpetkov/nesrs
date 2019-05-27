package nesrs.cpu;


import junit.framework.TestCase;
import nesrs.cpu.Cpu;
import nesrs.cpu.CpuMemory;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class OpASLTest extends TestCase {
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
   public void testASL_ACC() throws Exception {
      memory.writeCpuMemory(0x2001, 0x0A); // Op

      cpu._A = 0x80;
      cpu._PC = 0x2001;
      cpu.executeOp();

      assertEquals(2, cpu.getOpCycles());
      assertEquals(0x00, cpu._A);
      assertTrue((cpu._P & Cpu.N_FLAG) == 0);
      assertTrue((cpu._P & Cpu.Z_FLAG) != 0);
      assertTrue((cpu._P & Cpu.C_FLAG) != 0);
   }

   @Test
   public void testASL_ZP() throws Exception {
      memory.writeCpuMemory(0x2001, 0x06); // Op
      cpu._PC = 0x2001;
      cpu.executeOp();
      assertEquals(5, cpu.getOpCycles());
   }

   @Test
   public void testASL_ZPX() throws Exception {
      memory.writeCpuMemory(0x2001, 0x16); // Op
      cpu._PC = 0x2001;
      cpu.executeOp();
      assertEquals(6, cpu.getOpCycles());
   }

   @Test
   public void testASL_ABS() throws Exception {
      memory.writeCpuMemory(0x2001, 0x0E); // Op
      cpu._PC = 0x2001;
      cpu.executeOp();
      assertEquals(6, cpu.getOpCycles());
   }

   @Test
   public void testASL_ABSX() throws Exception {
      memory.writeCpuMemory(0x2001, 0x1E); // Op
      cpu._PC = 0x2001;
      cpu.executeOp();
      assertEquals(7, cpu.getOpCycles());
   }

}
