package nesrs.cpu;


import junit.framework.TestCase;
import nesrs.cpu.Cpu;
import nesrs.cpu.CpuMemory;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class OpBranchTest extends TestCase {
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
   public void testBCCPositive() throws Exception {
      memory.writeCpuMemory(0x2001, 0x90); // Op
      memory.writeCpuMemory(0x2002, 0x0F);

      cpu._P = cpu._P & ~Cpu.C_FLAG;
      cpu._PC = 0x2001;
      cpu.executeOp();

      assertEquals(3, cpu.getOpCycles());
      assertEquals(0x2012, cpu._PC);
   }

   @Test
   public void testBCCNegative() throws Exception {
      memory.writeCpuMemory(0x2001, 0x90); // Op
      memory.writeCpuMemory(0x2002, 0x0F);

      cpu._P = cpu._P | Cpu.C_FLAG;
      cpu._PC = 0x2001;
      cpu.executeOp();

      assertEquals(2, cpu.getOpCycles());
      assertEquals(0x2003, cpu._PC);
   }

   @Test
   public void testBCSPositive() throws Exception {
      memory.writeCpuMemory(0x2001, 0xB0); // Op
      memory.writeCpuMemory(0x2002, 0x0F);

      cpu._P = cpu._P | Cpu.C_FLAG;
      cpu._PC = 0x2001;
      cpu.executeOp();

      assertEquals(3, cpu.getOpCycles());
      assertEquals(0x2012, cpu._PC);
   }

   @Test
   public void testBCSNegative() throws Exception {
      memory.writeCpuMemory(0x2001, 0xB0); // Op
      memory.writeCpuMemory(0x2002, 0x0F);

      cpu._P = cpu._P & ~Cpu.C_FLAG;
      cpu._PC = 0x2001;
      cpu.executeOp();

      assertEquals(2, cpu.getOpCycles());
      assertEquals(0x2003, cpu._PC);
   }

   @Test
   public void testBEQPositive() throws Exception {
      memory.writeCpuMemory(0x2001, 0xF0); // Op
      memory.writeCpuMemory(0x2002, 0x0F);

      cpu._P = cpu._P | Cpu.Z_FLAG;
      cpu._PC = 0x2001;
      cpu.executeOp();

      assertEquals(3, cpu.getOpCycles());
      assertEquals(0x2012, cpu._PC);
   }

   @Test
   public void testBEQNegative() throws Exception {
      memory.writeCpuMemory(0x2001, 0xF0); // Op
      memory.writeCpuMemory(0x2002, 0x0F);

      cpu._P = cpu._P & ~Cpu.Z_FLAG;
      cpu._PC = 0x2001;
      cpu.executeOp();

      assertEquals(2, cpu.getOpCycles());
      assertEquals(0x2003, cpu._PC);
   }

   @Test
   public void testBMIPositive() throws Exception {
      memory.writeCpuMemory(0x2001, 0x30); // Op
      memory.writeCpuMemory(0x2002, 0x0F);

      cpu._P = cpu._P | Cpu.N_FLAG;
      cpu._PC = 0x2001;
      cpu.executeOp();

      assertEquals(3, cpu.getOpCycles());
      assertEquals(0x2012, cpu._PC);
   }

   @Test
   public void testBMINegative() throws Exception {
      memory.writeCpuMemory(0x2001, 0x30); // Op
      memory.writeCpuMemory(0x2002, 0x0F);

      cpu._P = cpu._P & ~Cpu.N_FLAG;
      cpu._PC = 0x2001;
      cpu.executeOp();

      assertEquals(2, cpu.getOpCycles());
      assertEquals(0x2003, cpu._PC);
   }

   @Test
   public void testBNEPositive() throws Exception {
      memory.writeCpuMemory(0x2001, 0xD0); // Op
      memory.writeCpuMemory(0x2002, 0x0F);

      cpu._P = cpu._P & ~Cpu.Z_FLAG;
      cpu._PC = 0x2001;
      cpu.executeOp();

      assertEquals(3, cpu.getOpCycles());
      assertEquals(0x2012, cpu._PC);
   }

   @Test
   public void testBNENegative() throws Exception {
      memory.writeCpuMemory(0x2001, 0xD0); // Op
      memory.writeCpuMemory(0x2002, 0x0F);

      cpu._P = cpu._P | Cpu.Z_FLAG;
      cpu._PC = 0x2001;
      cpu.executeOp();

      assertEquals(2, cpu.getOpCycles());
      assertEquals(0x2003, cpu._PC);
   }

   @Test
   public void testBPLPositive() throws Exception {
      memory.writeCpuMemory(0x2001, 0x10); // Op
      memory.writeCpuMemory(0x2002, 0x0F);

      cpu._P = cpu._P & ~Cpu.N_FLAG;
      cpu._PC = 0x2001;
      cpu.executeOp();

      assertEquals(3, cpu.getOpCycles());
      assertEquals(0x2012, cpu._PC);
   }

   @Test
   public void testBPLNegative() throws Exception {
      memory.writeCpuMemory(0x2001, 0x10); // Op
      memory.writeCpuMemory(0x2002, 0x0F);

      cpu._P = cpu._P | Cpu.N_FLAG;
      cpu._PC = 0x2001;
      cpu.executeOp();

      assertEquals(2, cpu.getOpCycles());
      assertEquals(0x2003, cpu._PC);
   }

   @Test
   public void testBVCPositive() throws Exception {
      memory.writeCpuMemory(0x2001, 0x50); // Op
      memory.writeCpuMemory(0x2002, 0x0F);

      cpu._P = cpu._P & ~Cpu.V_FLAG;
      cpu._PC = 0x2001;
      cpu.executeOp();

      assertEquals(3, cpu.getOpCycles());
      assertEquals(0x2012, cpu._PC);
   }

   @Test
   public void testBVCNegative() throws Exception {
      memory.writeCpuMemory(0x2001, 0x50); // Op
      memory.writeCpuMemory(0x2002, 0x0F);

      cpu._P = cpu._P | Cpu.V_FLAG;
      cpu._PC = 0x2001;
      cpu.executeOp();

      assertEquals(2, cpu.getOpCycles());
      assertEquals(0x2003, cpu._PC);
   }

   @Test
   public void testBVSPositive() throws Exception {
      memory.writeCpuMemory(0x2001, 0x70); // Op
      memory.writeCpuMemory(0x2002, 0x0F);

      cpu._P = cpu._P | Cpu.V_FLAG;
      cpu._PC = 0x2001;
      cpu.executeOp();

      assertEquals(3, cpu.getOpCycles());
      assertEquals(0x2012, cpu._PC);
   }

   @Test
   public void testBVSNegative() throws Exception {
      memory.writeCpuMemory(0x2001, 0x70); // Op
      memory.writeCpuMemory(0x2002, 0x0F);

      cpu._P = cpu._P & ~Cpu.V_FLAG;
      cpu._PC = 0x2001;
      cpu.executeOp();

      assertEquals(2, cpu.getOpCycles());
      assertEquals(0x2003, cpu._PC);
   }
}
