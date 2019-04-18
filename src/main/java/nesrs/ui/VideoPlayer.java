package nesrs.ui;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;

import nesrs.ppu.VideoOutListener;

public class VideoPlayer extends Canvas implements VideoOutListener {
   private static final long serialVersionUID = 1L;

   private static final int NES_HEIGHT = 240;
   private static final int NES_WIDTH = 256;

   private final BufferedImage _frameBufferImage =
         new BufferedImage(NES_WIDTH, NES_HEIGHT, BufferedImage.TYPE_INT_RGB);
   private final int scale = 2;

   private long _lastFrameEnd = 0;

   public VideoPlayer() {
      setSize(NES_WIDTH * scale, NES_HEIGHT * scale);
   }

   @Override
   public void handleFrame(int[] scanlinePixels) {
      _frameBufferImage.setRGB(0, 0, NES_WIDTH, NES_HEIGHT, scanlinePixels, 0, NES_WIDTH);
      render();
   }

   public final void render() {
      BufferStrategy buffer = getBufferStrategy();

      final Graphics graphics = buffer.getDrawGraphics();
      try {
         graphics.drawImage(
               _frameBufferImage,
               0,
               0,
               NES_WIDTH * scale,
               NES_HEIGHT * scale,
               null);

         long currentFrameEnd = System.currentTimeMillis();
         if (_lastFrameEnd != 0) {
            long currentFrameDuration = currentFrameEnd - _lastFrameEnd;
            int fps = (int) (1000f / currentFrameDuration);
            graphics.setColor(Color.GREEN);
            graphics.drawString("FPS: " + fps, 16, 16);
         }
         _lastFrameEnd = currentFrameEnd;
      } finally {
         graphics.dispose();
      }

      buffer.show();
   }
}
