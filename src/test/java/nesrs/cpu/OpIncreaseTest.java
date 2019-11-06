package nesrs.cpu;

import junit.framework.TestCase;
import nesrs.cpu.Cpu;
import nesrs.cpu.CpuMemory;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class OpIncreaseTest extends TestCase {
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
   public void testINC() throws Exception {
      memory.write(0x2001, 0xE6); // Op
      memory.write(0x2002, 0x11);
      memory.write(0x0011, 0x8E);
      cpu._PC = 0x2001;
      cpu.executeOp();

      assertEquals(5, cpu.getOpCycles());
      assertEquals(0x8F, memory.read(0x0011));
      assertTrue((cpu._P & Cpu.N_FLAG) != 0);
      assertTrue((cpu._P & Cpu.Z_FLAG) == 0);
   }

   @Test
   public void testINX() throws Exception {
      memory.write(0x2001, 0xE8); // Op

      cpu._X = 0x8E;
      cpu._PC = 0x2001;
      cpu.executeOp();

      assertEquals(2, cpu.getOpCycles());
      assertEquals(0x8F, cpu._X);
      assertTrue((cpu._P & Cpu.N_FLAG) != 0);
      assertTrue((cpu._P & Cpu.Z_FLAG) == 0);
   }

   @Test
   public void testINY() throws Exception {
      memory.write(0x2001, 0xC8); // Op

      cpu._Y = 0x8E;
      cpu._PC = 0x2001;
      cpu.executeOp();

      assertEquals(2, cpu.getOpCycles());
      assertEquals(0x8F, cpu._Y);
      assertTrue((cpu._P & Cpu.N_FLAG) != 0);
      assertTrue((cpu._P & Cpu.Z_FLAG) == 0);
   }
}

