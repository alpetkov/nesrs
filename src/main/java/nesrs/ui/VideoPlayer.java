package nesrs.ui;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;

import nesrs.ppu.VideoOutListener;

public class VideoPlayer extends Canvas implements VideoOutListener {
   private static final long serialVersionUID = 1L;

   private static final int NES_HEIGHT = 240;
   private static final int NES_WIDTH = 256;

   private final BufferedImage _frameBufferImage =
         new BufferedImage(NES_WIDTH, NES_HEIGHT, BufferedImage.TYPE_INT_RGB);
   private final int _scale = 2;
   private final boolean _debug = true;

   private long _lastFrameEnd = 0;
   
   private boolean _explicitRender;

   public VideoPlayer(boolean explicitRender) {
      _explicitRender = explicitRender;
      setSize(NES_WIDTH * _scale, NES_HEIGHT * _scale);
   }

   @Override
   public void handleFrame(int[] framePixels) {
      WritableRaster raster = _frameBufferImage.getRaster();
      raster.setDataElements(0, 0, NES_WIDTH, NES_HEIGHT, framePixels);
      if (!_explicitRender) {
         render();
      }
   }

   @Override
   public final void render() {
      BufferStrategy buffer = getBufferStrategy();

      final Graphics graphics = buffer.getDrawGraphics();
      try {
         graphics.drawImage(
               _frameBufferImage,
               0,
               0,
               NES_WIDTH * _scale,
               NES_HEIGHT * _scale,
               null);

         long currentFrameEnd = System.currentTimeMillis();
         if (_debug && _lastFrameEnd != 0) {
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
