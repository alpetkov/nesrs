package nesrs.cpu;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests {

	public static Test suite() {
		TestSuite suite = new TestSuite("Test for nes.cpu");
		//$JUnit-BEGIN$
		suite.addTestSuite(AddressingModesTest.class);
      suite.addTestSuite(BootTest.class);
      suite.addTestSuite(OpADCTest.class);
		suite.addTestSuite(OpANDTest.class);
		suite.addTestSuite(OpASLTest.class);
		suite.addTestSuite(OpBITTest.class);
		suite.addTestSuite(OpBranchTest.class);
		suite.addTestSuite(OpCompareTest.class);
		suite.addTestSuite(OpDecreaseTest.class);
		suite.addTestSuite(OpEORTest.class);
		suite.addTestSuite(OpIncreaseTest.class);
		suite.addTestSuite(OpJMPTest.class);
		suite.addTestSuite(OpJSRTest.class);
		suite.addTestSuite(OpLoadTest.class);
		suite.addTestSuite(OpLSRTest.class);
		suite.addTestSuite(OpORATest.class);
		suite.addTestSuite(OpROLTest.class);
		suite.addTestSuite(OpRORTest.class);
		suite.addTestSuite(StackTest.class);

		//$JUnit-END$
		return suite;
	}

}
