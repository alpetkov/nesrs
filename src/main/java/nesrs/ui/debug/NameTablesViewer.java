package nesrs.ui.debug;

import java.awt.Canvas;
import java.awt.Graphics;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;

public class NameTablesViewer extends Canvas {
   private static final long serialVersionUID = 1L;

   private static final int NES_HEIGHT = 240 * 2 + 1;
   private static final int NES_WIDTH = 256 * 2 + 1;

   private final BufferedImage _frameBufferImage =
         new BufferedImage(NES_WIDTH, NES_HEIGHT, BufferedImage.TYPE_INT_RGB);
   private final int scale = 2;

   public NameTablesViewer() {
      setSize(NES_WIDTH * scale, NES_HEIGHT * scale);
   }

   public void handleNameTables(int[][][] nametables) {
      for (int y = 0; y < NES_HEIGHT; y++) {
         for (int x = 0; x < NES_WIDTH; x++) {
            int value;
            if (x < NES_WIDTH / 2) {
               if (y < NES_HEIGHT / 2) {
                  value = nametables[0][y][x];
               } else if (y == 240) {
                  value = 0;
               } else {
                  value = nametables[1][y - 241][x];
               }
            } else if (x == 256) {
               value = 0;
            } else {
               if (y < NES_HEIGHT / 2) {
                  value = nametables[2][y][x - 257];
               } else if (y == 240) {
                  value = 0;
               } else {
                  value = nametables[3][y - 241][x - 257];
               }
            }

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
