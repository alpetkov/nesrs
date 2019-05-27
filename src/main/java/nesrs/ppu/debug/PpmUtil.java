package nesrs.ppu.debug;

import java.io.IOException;
import java.io.OutputStream;

public class PpmUtil {

   public static void write(OutputStream out, int width, int height, int[] pixels) throws IOException {
      out.write(("P6 " + width + " " + height + " 255\n").getBytes());

      for (int rgb : pixels) {
         out.write((rgb >> 16) & 0xFF);
         out.write((rgb >> 8) & 0xFF);
         out.write(rgb & 0xFF);
      }

      out.flush();
   }

   public static void write(OutputStream out, int[][] pixels) throws IOException {
      out.write(("P6 " + pixels[0].length + " " + pixels.length + " 255\n").getBytes());

      for (int y = 0; y < pixels.length; y++) {
         for (int x = 0; x < pixels[y].length; x++) {
            int rgb = pixels[y][x];
            out.write((rgb >> 16) & 0xFF);
            out.write((rgb >> 8) & 0xFF);
            out.write(rgb & 0xFF);
         }
      }

      out.flush();
   }
}
