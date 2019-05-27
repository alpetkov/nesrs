package nesrs.cpu;

import junit.framework.TestCase;
import nesrs.cpu.Cpu;
import nesrs.cpu.CpuMemory;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class OpCompareTest extends TestCase {
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
   public void testCMPPositive() throws Exception {
      memory.writeCpuMemory(0x2001, 0xC9); // Op
      memory.writeCpuMemory(0x2002, 0x8E);

      cpu._A = 0x1F;
      cpu._PC = 0x2001;
      cpu.executeOp();

      assertEquals(2, cpu.getOpCycles());
      assertTrue((cpu._P & Cpu.N_FLAG) != 0);
      assertTrue((cpu._P & Cpu.Z_FLAG) == 0);
      assertTrue((cpu._P & Cpu.C_FLAG) == 0);
   }

   @Test
   public void testCMPNegative() throws Exception {
      memory.writeCpuMemory(0x2001, 0xC9); // Op
      memory.writeCpuMemory(0x2002, 0x0F);

      cpu._A = 0x8F;
      cpu._PC = 0x2001;
      cpu.executeOp();

      assertEquals(2, cpu.getOpCycles());
      assertTrue((cpu._P & Cpu.N_FLAG) != 0);
      assertTrue((cpu._P & Cpu.Z_FLAG) == 0);
      assertTrue((cpu._P & Cpu.C_FLAG) != 0);
   }

   @Test
   public void testCPXPositive() throws Exception {
      memory.writeCpuMemory(0x2001, 0xE0); // Op
      memory.writeCpuMemory(0x2002, 0x8E);

      cpu._X = 0x1F;
      cpu._PC = 0x2001;
      cpu.executeOp();

      assertEquals(2, cpu.getOpCycles());
      assertTrue((cpu._P & Cpu.N_FLAG) != 0);
      assertTrue((cpu._P & Cpu.Z_FLAG) == 0);
      assertTrue((cpu._P & Cpu.C_FLAG) == 0);
   }

   @Test
   public void testCPXNegative() throws Exception {
      memory.writeCpuMemory(0x2001, 0xE0); // Op
      memory.writeCpuMemory(0x2002, 0x0F);

      cpu._X = 0x8F;
      cpu._PC = 0x2001;
      cpu.executeOp();

      assertEquals(2, cpu.getOpCycles());
      assertTrue((cpu._P & Cpu.N_FLAG) != 0);
      assertTrue((cpu._P & Cpu.Z_FLAG) == 0);
      assertTrue((cpu._P & Cpu.C_FLAG) != 0);
   }

   @Test
   public void testCPYPositive() throws Exception {
      memory.writeCpuMemory(0x2001, 0xC0); // Op
      memory.writeCpuMemory(0x2002, 0x8E);

      cpu._Y = 0x1F;
      cpu._PC = 0x2001;
      cpu.executeOp();

      assertEquals(2, cpu.getOpCycles());
      assertTrue((cpu._P & Cpu.N_FLAG) != 0);
      assertTrue((cpu._P & Cpu.Z_FLAG) == 0);
      assertTrue((cpu._P & Cpu.C_FLAG) == 0);
   }

   @Test
   public void testCPYNegative() throws Exception {
      memory.writeCpuMemory(0x2001, 0xC0); // Op
      memory.writeCpuMemory(0x2002, 0x0F);

      cpu._Y = 0x8F;
      cpu._PC = 0x2001;
      cpu.executeOp();

      assertEquals(2, cpu.getOpCycles());
      assertTrue((cpu._P & Cpu.N_FLAG) != 0);
      assertTrue((cpu._P & Cpu.Z_FLAG) == 0);
      assertTrue((cpu._P & Cpu.C_FLAG) != 0);
   }
}