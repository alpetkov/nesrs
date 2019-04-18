package testroms;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.junit.Assert;

import nesrs.Nes;
import nesrs.ppu.PpmVideoOutListener;
import nesrs.util.Util;

public abstract class AbstractRomTest {

   public void testRom(String romPath, String screenshotPath, int numberOfSeconds) throws Exception {
      byte[] romBytes = buildCartridge(romPath);

      PpmVideoOutListener ppmVideoOutListener = new PpmVideoOutListener();

      Nes nes = new Nes(romBytes, ppmVideoOutListener, null, null);
      nes.start();

      long start = System.currentTimeMillis();
      long end = start;

      while (end - start < numberOfSeconds * 1000) {
         Thread.sleep(200);
         end = System.currentTimeMillis();
      }
      nes.stop();

      ByteArrayOutputStream out = new ByteArrayOutputStream();
      ppmVideoOutListener.write(out);
      byte[] actualContent = out.toByteArray();

      byte[] expectedContent = null;
      InputStream in = AbstractRomTest.class.getResourceAsStream(screenshotPath);
      try {
         expectedContent = getBytesFromInputStream(in);
      } finally {
         in.close();
      }

      try {
         Assert.assertArrayEquals(expectedContent, actualContent);
      } catch (AssertionError e) {
         // Dump actual content so it can be reviewed.
         String fileName = new File(screenshotPath).getName();
         File tmpFile = File.createTempFile(fileName, ".ppm");
         System.out.println("Writing dump to: " + tmpFile.getAbsolutePath());
         ppmVideoOutListener.writeToFile(tmpFile.getAbsolutePath());

         throw e;
      }
   }

   private static byte[] getBytesFromInputStream(InputStream is) throws IOException {
      ByteArrayOutputStream os = new ByteArrayOutputStream();
      byte[] buffer = new byte[0xFFFF];
      for (int len = is.read(buffer); len != -1; len = is.read(buffer)) {
         os.write(buffer, 0, len);
      }
      return os.toByteArray();
   }

//   private static Cartridge buildCartridge(String romPath) throws IOException {
//      InputStream in = AbstractRomTest.class.getResourceAsStream(romPath);
//
//      INesRomCartridgeReader reader = new INesRomCartridgeReader(in);
//
//      try {
//         return reader.readCartridge();
//      } finally {
//         in.close();
//      }
//   }

   private static byte[] buildCartridge(String romPath) throws IOException {
      try (InputStream in = AbstractRomTest.class.getResourceAsStream(romPath)) {
         return Util.toByteArray(in);
      }
   }
}
