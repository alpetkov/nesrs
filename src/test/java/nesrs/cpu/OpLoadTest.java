package nesrs.cpu;

import junit.framework.TestCase;
import nesrs.cpu.Cpu;
import nesrs.cpu.CpuMemory;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class OpLoadTest extends TestCase {
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
	public void testLDA() throws Exception {
		memory.writeCpuMemory(0x2001, 0xA9); // Op
		memory.writeCpuMemory(0x2002, 0x81);

		cpu._PC = 0x2001;
		cpu.executeOp();

		assertEquals(2, cpu.getOpCycles());
		assertEquals(0x81, cpu._A);
		assertTrue((cpu._P & Cpu.N_FLAG) != 0);
		assertTrue((cpu._P & Cpu.Z_FLAG) == 0);
	}

	@Test
	public void testLDX() throws Exception {
		memory.writeCpuMemory(0x2001, 0xA2); // Op
		memory.writeCpuMemory(0x2002, 0x81);

		cpu._PC = 0x2001;
		cpu.executeOp();

		assertEquals(2, cpu.getOpCycles());
		assertEquals(0x81, cpu._X);
		assertTrue((cpu._P & Cpu.N_FLAG) != 0);
		assertTrue((cpu._P & Cpu.Z_FLAG) == 0);
	}

	@Test
	public void testLDY() throws Exception {
		memory.writeCpuMemory(0x2001, 0xA0); // Op
		memory.writeCpuMemory(0x2002, 0x81);

		cpu._PC = 0x2001;
		cpu.executeOp();

		assertEquals(2, cpu.getOpCycles());
		assertEquals(0x81, cpu._Y);
		assertTrue((cpu._P & Cpu.N_FLAG) != 0);
		assertTrue((cpu._P & Cpu.Z_FLAG) == 0);
	}
}
