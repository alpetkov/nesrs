package testroms.nestest;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.util.ArrayList;

import org.junit.Test;

import junit.framework.TestCase;
import nesrs.cartridge.Cartridge;
import nesrs.cartridge.INesRomCartridgeReader;
import nesrs.cpu.Cpu;
import nesrs.cpu.TestCPUMemory;
import nesrs.util.Util;

public class Nestest extends TestCase {

   @Test
	public void testLog() throws Exception {
		String[] nestestLogLines = parseNestestLogFile();

		Cartridge cartridge = loadNestestCartridge();

		TestCPUMemory cpuMemory = new TestCPUMemory();
		cpuMemory.setPrgRom(0x8000, cartridge.getCartridgeMemory().prgRom);
		cpuMemory.setPrgRom(0xC000, cartridge.getCartridgeMemory().prgRom);

		Cpu cpu = new Cpu(cpuMemory);

		int A = 0x00;
		int X = 0x00;
		int Y = 0x00;
		int S = 0xFD;
		int P = Cpu.R_FLAG | Cpu.I_FLAG;
		int PC = 0xC000;
		cpu.init(A, X, Y, S, P, PC);

		int cycles = 0;
		for (int i = 0; i < nestestLogLines.length; i++) {
			StringBuilder expectedLogLine = new StringBuilder();
			expectedLogLine.append(Util.toHex(cpu.getPC()));
			expectedLogLine.append("    ");
			expectedLogLine.append("A:" + Util.toHex(cpu.getA()));
			expectedLogLine.append(" ");
			expectedLogLine.append("X:" + Util.toHex(cpu.getX()));
			expectedLogLine.append(" ");
			expectedLogLine.append("Y:" + Util.toHex(cpu.getY()));
			expectedLogLine.append(" ");
			expectedLogLine.append("P:" + Util.toHex(cpu.getP()));
			expectedLogLine.append(" ");
			expectedLogLine.append("SP:" + Util.toHex(cpu.getS()));
			cycles += cpu.getOpCycles() * 3;
			cycles %= 341;
			expectedLogLine.append(" ");
			expectedLogLine.append("CYC:" + pad(cycles));

			assertEquals(nestestLogLines[i], expectedLogLine.toString());

			cpu.executeOp();
		}
   }

	private String pad(int value) {
		if (value < 10) {
			return "  " + value;
		} else if (value < 100) {
			return " " + value;
		} else {
			return "" + value;
		}
	}

	private Cartridge loadNestestCartridge() throws Exception {
		File file = new File(this.getClass().getResource("/testroms/nestest/nestest.nes").toURI());

		FileInputStream in = new FileInputStream(file);

		INesRomCartridgeReader builder = new INesRomCartridgeReader(in);

		try {
			return builder.readCartridge();
		} finally {
			in.close();
		}
	}

	private static String[] parseNestestLogFile() throws Exception {
		BufferedReader reader = new BufferedReader(new FileReader(
				new File(Nestest.class.getResource("/testroms/nestest/nestest.log").toURI())));

		ArrayList<String> nestestLogLines = new ArrayList<String>(8991);

		try {
			String line = reader.readLine();
			while (line != null) {
				StringBuilder newLine = new StringBuilder(line.substring(0, 4));
				int regAIndex = line.indexOf("A:");
				newLine.append("    ");
				//newLine.append(line.substring(regAIndex,  line.indexOf(" CYC")));
				newLine.append(line.substring(regAIndex,  line.indexOf(" SL")));
				nestestLogLines.add(newLine.toString());
				line = reader.readLine();
			}
		} finally {
			reader.close();
		}

		return nestestLogLines.toArray(new String[0]);
	}
}
