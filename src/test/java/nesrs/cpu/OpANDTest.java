package nesrs.cpu;


import junit.framework.TestCase;
import nesrs.cpu.Cpu;
import nesrs.cpu.CpuMemory;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class OpANDTest extends TestCase {
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
   public void testAND_IMM() throws Exception {
      memory.writeCpuMemory(0x2001, 0x29); // Op
      memory.writeCpuMemory(0x2002, 0xC0);

      cpu._A = 0xB8;
      cpu._PC = 0x2001;
      cpu.executeOp();

      assertEquals(2, cpu.getOpCycles());
      assertEquals(0x80, cpu._A);
      assertTrue((cpu._P & Cpu.N_FLAG) != 0);
      assertTrue((cpu._P & Cpu.Z_FLAG) == 0);
   }

   @Test
   public void testAND_ZP() throws Exception {
      memory.writeCpuMemory(0x2001, 0x25); // Op
      cpu._PC = 0x2001;
      cpu.executeOp();
      assertEquals(3, cpu.getOpCycles());
   }

   @Test
   public void testAND_ZPX() throws Exception {
      memory.writeCpuMemory(0x2001, 0x35); // Op
      cpu._PC = 0x2001;
      cpu.executeOp();
      assertEquals(4, cpu.getOpCycles());
   }

   @Test
   public void testAND_ABS() throws Exception {
      memory.writeCpuMemory(0x2001, 0x2D); // Op
      cpu._PC = 0x2001;
      cpu.executeOp();
      assertEquals(4, cpu.getOpCycles());
   }

   @Test
   public void testAND_ABSX() throws Exception {
      memory.writeCpuMemory(0x2001, 0x3D); // Op
      cpu._PC = 0x2001;
      cpu.executeOp();
      assertEquals(4, cpu.getOpCycles());
   }

   @Test
   public void testAND_ABSY() throws Exception {
      memory.writeCpuMemory(0x2001, 0x39); // Op
      cpu._PC = 0x2001;
      cpu.executeOp();
      assertEquals(4, cpu.getOpCycles());
   }

   @Test
   public void testAND_IND_X() throws Exception {
      memory.writeCpuMemory(0x2001, 0x21); // Op
      cpu._PC = 0x2001;
      cpu.executeOp();
      assertEquals(6, cpu.getOpCycles());
   }

   @Test
   public void testAND_INDY() throws Exception {
      memory.writeCpuMemory(0x2001, 0x31); // Op
      cpu._PC = 0x2001;
      cpu.executeOp();
      assertEquals(5, cpu.getOpCycles());
   }
}
