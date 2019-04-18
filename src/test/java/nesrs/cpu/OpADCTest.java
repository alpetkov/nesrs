package nesrs.cpu;

import junit.framework.TestCase;
import nesrs.cpu.Cpu;
import nesrs.cpu.CpuMemory;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class OpADCTest extends TestCase {
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
	public void testADC_IMM() throws Exception {
		memory.writeCpuMemory(0x2001, 0x69); // Op

		memory.writeCpuMemory(0x2002, 0xC0);

		cpu._A = 0xB8;
		cpu._PC = 0x2001;
		cpu.executeOp();

		assertEquals(2, cpu.getOpCycles());

		assertEquals(0x78, cpu._A);
		assertTrue((cpu._P & Cpu.C_FLAG) != 0);
		assertTrue((cpu._P & Cpu.V_FLAG) != 0);
		assertTrue((cpu._P & Cpu.N_FLAG) == 0);
		assertTrue((cpu._P & Cpu.Z_FLAG) == 0);
	}

	@Test
	public void testADC_ZP() throws Exception {
		memory.writeCpuMemory(0x2001, 0x65); // Op
		cpu._PC = 0x2001;
		cpu.executeOp();
		assertEquals(3, cpu.getOpCycles());
	}

	@Test
	public void testADC_ZPX() throws Exception {
		memory.writeCpuMemory(0x2001, 0x75); // Op
		cpu._PC = 0x2001;
		cpu.executeOp();
		assertEquals(4, cpu.getOpCycles());
	}

	@Test
	public void testADC_ABS() throws Exception {
		memory.writeCpuMemory(0x2001, 0x6D); // Op
		cpu._PC = 0x2001;
		cpu.executeOp();
		assertEquals(4, cpu.getOpCycles());
	}

	@Test
	public void testADC_ABSX() throws Exception {
		memory.writeCpuMemory(0x2001, 0x7D); // Op
		cpu._PC = 0x2001;
		cpu.executeOp();
		assertEquals(4, cpu.getOpCycles());
	}

	@Test
	public void testADC_ABSY() throws Exception {
		memory.writeCpuMemory(0x2001, 0x79); // Op
		cpu._PC = 0x2001;
		cpu.executeOp();
		assertEquals(4, cpu.getOpCycles());
	}

	@Test
	public void testADC_IND_X() throws Exception {
		memory.writeCpuMemory(0x2001, 0x61); // Op
		cpu._PC = 0x2001;
		cpu.executeOp();
		assertEquals(6, cpu.getOpCycles());
	}

	@Test
	public void testADC_IND_Y() throws Exception {
		memory.writeCpuMemory(0x2001, 0x71); // Op
		cpu._PC = 0x2001;
		cpu.executeOp();
		assertEquals(5, cpu.getOpCycles());
	}
}
