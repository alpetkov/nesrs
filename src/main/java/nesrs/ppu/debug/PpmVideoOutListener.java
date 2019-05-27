package nesrs.ppu.debug;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import nesrs.ppu.VideoOutListener;

public class PpmVideoOutListener implements VideoOutListener {
   private int NES_HEIGHT = 240;
   private int NES_WIDTH = 256;

   private int[] _frame = new int[NES_WIDTH * NES_HEIGHT];

   @Override
   public void handleFrame(int[] frame) {
      System.arraycopy(frame, 0, _frame, 0, frame.length);
   }

   public void write(OutputStream out) throws IOException {
      PpmUtil.write(out, NES_WIDTH, NES_HEIGHT, _frame);
   }

   public void writeToFile(String fileName) throws IOException {
      try (FileOutputStream out = new FileOutputStream(new File(fileName))) {
         write(out);
      }
   }
}
