package nesrs;

import java.awt.FlowLayout;
import java.io.File;
import java.io.FileInputStream;

import javax.sound.sampled.LineUnavailableException;
import javax.swing.JFrame;

import nesrs.controller.StandardController;
import nesrs.ui.AudioPlayer;
import nesrs.ui.StandardControllerKeyListener;
import nesrs.ui.VideoPlayer;
import nesrs.util.Util;

public class Main {

   public static void main(String[] args) throws Exception {
      String nesRomFilePath = args[0];
      byte[] nesRom;
      try (FileInputStream in = new FileInputStream(new File(nesRomFilePath))) {
         nesRom = Util.toByteArray(in);
      }

      new Application().start(nesRom);
   }

   public static class Application {
      public void start(byte[] nesRom) throws LineUnavailableException {
         JFrame window = new JFrame();

         window.setTitle("NESRS");
         window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         window.setLayout(new FlowLayout());

         // Create video player
         VideoPlayer videoPlayer = new VideoPlayer(true);
         videoPlayer.setEnabled(false);
         window.add(videoPlayer);

         // Controller 1
         StandardController controller1 = new StandardController(true);
         window.addKeyListener(new StandardControllerKeyListener(controller1));

         // Show window
         window.pack();
         window.setLocationRelativeTo(null);
         window.setVisible(true);

         // Set page-flip buffer strategy.
         videoPlayer.createBufferStrategy(2);

         // Create audio player.
         AudioPlayer audioPlayer = new AudioPlayer();
//         AudioPlayer audioPlayer = null;

         // Start NES
         Nes nes = new Nes(nesRom, videoPlayer, audioPlayer, controller1);
         nes.start();
      }
   }
}
