package nesrs.cpu;


import junit.framework.TestCase;
import nesrs.cpu.Cpu;
import nesrs.cpu.CpuMemory;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class AddressingModesTest extends TestCase {

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
   public void testIMM() throws Exception {
      memory.write(0x2001, 0x69); // Op

      memory.write(0x2002, 0xC0);

      cpu._A = 0xB8;
      cpu._PC = 0x2001;
      cpu.executeOp();

      assertEquals(2, cpu.getOpCycles());
      asserts();
   }

   @Test
   public void testZP() throws Exception {
      memory.write(0x2001, 0x65); // Op

      memory.write(0x2002, 0x22);
      memory.write(0x0022, 0xC0);

      cpu._A = 0xB8;
      cpu._PC = 0x2001;
      cpu.executeOp();

      assertEquals(3, cpu.getOpCycles());
      asserts();
   }

   @Test
   public void testZPX() throws Exception {
      memory.write(0x2001, 0x75); // Op

      memory.write(0x2002, 0x22);
      memory.write(0x0027, 0xC0);

      cpu._X = 0x05;
      cpu._A = 0xB8;
      cpu._PC = 0x2001;
      cpu.executeOp();

      assertEquals(4, cpu.getOpCycles());
      asserts();
   }

   @Test
   public void testABS() throws Exception {
      memory.write(0x2001, 0x6D); // Op

      memory.write(0x2002, 0x22);
      memory.write(0x2003, 0x33);
      memory.write(0x3322, 0xC0);

      cpu._A = 0xB8;
      cpu._PC = 0x2001;
      cpu.executeOp();

      assertEquals(4, cpu.getOpCycles());
      asserts();
   }

   @Test
   public void testABSX() throws Exception {
      memory.write(0x2001, 0x7D); // Op
      memory.write(0x2002, 0xF2);
      memory.write(0x2003, 0x33);
      memory.write(0x3401, 0xC0);

      cpu._X = 0x0F;
      cpu._A = 0xB8;
      cpu._PC = 0x2001;
      cpu.executeOp();

      assertEquals(5, cpu.getOpCycles());
      asserts();
   }

   @Test
   public void testABSY() throws Exception {
      memory.write(0x2001, 0x79); // Op
      memory.write(0x2002, 0xF2);
      memory.write(0x2003, 0x33);
      memory.write(0x3401, 0xC0);

      cpu._Y = 0x0F;
      cpu._A = 0xB8;
      cpu._PC = 0x2001;
      cpu.executeOp();

      assertEquals(5, cpu.getOpCycles());
      asserts();
   }

   @Test
   public void testRel() throws Exception {
      memory.write(0x2001, 0x90); // Op
      memory.write(0x2002, 0x0F);

      cpu._P = cpu._P & ~Cpu.C_FLAG;
      cpu._PC = 0x2001;
      cpu.executeOp();

      assertEquals(3, cpu.getOpCycles());
      assertEquals(0x2012, cpu._PC);
   }

   @Test
   public void testIND_X() throws Exception {
      memory.write(0x2001, 0x61); // Op
      memory.write(0x2002, 0xF2);
      memory.write(0x00F7, 0x33);
      memory.write(0x00F8, 0x44);
      memory.write(0x4433, 0xC0);

      cpu._X = 0x05;
      cpu._A = 0xB8;
      cpu._PC = 0x2001;
      cpu.executeOp();

      assertEquals(6, cpu.getOpCycles());
      asserts();
   }

   @Test
   public void testIND_Y() throws Exception {
      memory.write(0x2001, 0x71); // Op
      memory.write(0x2002, 0xF2);
      memory.write(0x00F2, 0x33);
      memory.write(0x00F3, 0x44);
      memory.write(0x4438, 0xC0);

      cpu._Y = 0x05;
      cpu._A = 0xB8;
      cpu._PC = 0x2001;
      cpu.executeOp();

      assertEquals(5, cpu.getOpCycles());
      asserts();
   }

   private void asserts() throws Exception {
      assertEquals(0x78, cpu._A);
      assertTrue((cpu._P & Cpu.C_FLAG) != 0);
      assertTrue((cpu._P & Cpu.V_FLAG) != 0);
      assertTrue((cpu._P & Cpu.N_FLAG) == 0);
      assertTrue((cpu._P & Cpu.Z_FLAG) == 0);
   }
}
