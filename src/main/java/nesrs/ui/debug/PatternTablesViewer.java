package nesrs.ui.debug;

import java.awt.Canvas;
import java.awt.Graphics;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;

public class PatternTablesViewer extends Canvas {

   private static final long serialVersionUID = 1L;

   private static final int NES_HEIGHT = 128 * 2 + 1;
   private static final int NES_WIDTH = 128;

   private final BufferedImage _frameBufferImage =
         new BufferedImage(NES_WIDTH, NES_HEIGHT, BufferedImage.TYPE_INT_RGB);
   private final int scale = 2;

   public PatternTablesViewer() {
      setSize(NES_WIDTH * scale, NES_HEIGHT * scale);
   }

   public void handleNameTables(int[][][] nametables) {
      for (int y = 0; y < NES_HEIGHT; y++) {
         for (int x = 0; x < NES_WIDTH; x++) {
            int value;
            if (y <= 127) {
               value = nametables[0][y][x];
            } else if (y == 128) {
               value = 0xFFFFFF;
            } else { //if (y <= 256) {
               value = nametables[1][y - 129][x];
            } /*
               * else if (y == 257) { value = 0xFFFFFF;
               *
               * } else if (y <= 385) { value = nametables[2][y - 258][x]; }
               * else if (y == 386) { value = 0xFFFFFF;
               *
               * } else { value = nametables[3][y - 387][x]; }
               */

            _frameBufferImage.setRGB(x, y, value);
         }
      }

      render();
   }

   public void render() {
      BufferStrategy buffer = getBufferStrategy();

      Graphics graphics = buffer.getDrawGraphics();
      try {
         graphics.drawImage(
               _frameBufferImage,
               0,
               0,
               NES_WIDTH * scale,
               NES_HEIGHT * scale,
               null);

      } finally {
         graphics.dispose();
      }

      buffer.show();
   }
}
