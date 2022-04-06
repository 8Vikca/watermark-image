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
        ImagePlus originalImage = new ImagePlus("Lenna(testImage).png");
        ImagePlus watermarkImage = new ImagePlus("watermark.png");
        originalImage.show();
        int h= 4;
        Watermark watermark = new Watermark(originalImage.getBufferedImage(), watermarkImage.getBufferedImage());
        watermark.setOriginalBits(watermark.bitsPreparationOrig(h));
        watermark.setWatermarkBits(watermark.bitsPreparationMark(h));

        watermark.insertWatermarkInImage();
        var originalWithWatermark = watermark.setImageFromBits();
        originalWithWatermark.show();




    }
}

