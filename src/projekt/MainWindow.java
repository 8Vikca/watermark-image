package projekt;

import ij.ImagePlus;

import javax.swing.*;
import java.awt.image.BufferedImage;

public class MainWindow {

    private JPanel mainPanel;

    public static void main(String[] args) {
        JFrame frame = new JFrame("Aplikace pro zpracování obrazu");
        frame.setBounds(100, 100, 450, 300);
        frame.setContentPane(new MainWindow().mainPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

        public MainWindow() {
            initialize();
        }


    private void initialize () {
        BufferedImage originalImage = new ImagePlus("Lenna(testImage).png").getBufferedImage();
        BufferedImage watermarkImage = new ImagePlus("watermark.png").getBufferedImage();
        //originalImage.show();
        Watermark watermark = new Watermark();
        watermark.setOriginalBites(watermark.getRGBinBits(originalImage));
        watermark.setWatermarkBites(watermark.getRGBinBits(watermarkImage));

    }
}

