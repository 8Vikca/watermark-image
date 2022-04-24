package projekt;

import ij.ImagePlus;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.swing.JFileChooser;

public class MainWindow {

    private JPanel mainPanel;
    private JButton chooseImage;
    private JButton extractLSB;
    private JButton LSB;
    private JSlider sliderH;
    private JRadioButton modráRadioButton;
    private JRadioButton zelenáRadioButton;
    private JRadioButton červenáRadioButton;
    private JButton DCT;
    private JButton mirroringButton;
    private JButton deMirroringButton;
    private JButton deRotatingButton;
    private JButton rotatingButton;
    private JRadioButton a45RadioButton;
    private JRadioButton a90RadioButton;
    private JButton extractDCT;
    private JRadioButton first;
    private JRadioButton second;
    private JRadioButton third;
    private JSlider sliderH2;

    final File[] fileToSend = new File[1];
    private ImagePlus originalImage;
    private ImagePlus originalWithWatermark;
    private ImagePlus watermarkImage;
    private ImagePlus mirroredImage;
    private ImagePlus rotatedImage;
    private WatermarkLSB watermark;
    private WatermarkDCT watermarkDCT;
    private int [][][] imageWithWatermarkBits;
    private int hLSB;
    private int hDCT;
    private int rotatingSelection;
    private int blockSize = 8;
    private int u1 =3,v1 =1,u2 =4,v2 =1;
    private  Quality quality;
    private String selection;
    private ButtonGroup group;

    public static void main(String[] args) {
        JFrame frame = new JFrame("Aplikace pro zpracování obrazu");
        frame.setBounds(100, 100, 450, 300);
        frame.setContentPane(new MainWindow().mainPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }
        public MainWindow() {
            group = new ButtonGroup();
            group.add(modráRadioButton);
            group.add(zelenáRadioButton);
            group.add(červenáRadioButton);
            this.červenáRadioButton.setActionCommand("red");
            this.zelenáRadioButton.setActionCommand("green");
            this.modráRadioButton.setActionCommand("blue");
            ButtonGroup group2 = new ButtonGroup();
            group2.add(a45RadioButton);
            group2.add(a90RadioButton);
            this.a45RadioButton.setActionCommand("45");
            this.a90RadioButton.setActionCommand("90");
            ButtonGroup group3 = new ButtonGroup();
            group3.add(first);
            group3.add(second);
            group3.add(third);
            this.first.setActionCommand("first");
            this.second.setActionCommand("second");
            this.third.setActionCommand("third");
            quality = new Quality();
            chooseImage.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    JFileChooser fileChooser = new JFileChooser();
                    fileChooser.setCurrentDirectory((new File(""))); //kde sa otvori chooser
                    fileChooser.setDialogTitle("Choose a file to send");

                    int respone = fileChooser.showSaveDialog(null);
                    if(respone == JFileChooser.APPROVE_OPTION) { //ci sa vlozil subor alebo sa okno zavrelo
                        fileToSend[0] = fileChooser.getSelectedFile();
                        originalImage = new ImagePlus(fileToSend[0].getAbsolutePath());
                        watermarkImage = new ImagePlus("watermark.png");
                        originalImage.show();
                    }
                }
            });

            LSB.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    selection = group.getSelection().getActionCommand();
                    initializeWatermarkingLSB(selection);
                }
            });
            extractLSB.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    initializeExtractionLSB();
                }
            });
            DCT.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    hDCT = sliderH2.getValue();

                    String selection = group3.getSelection().getActionCommand();
                    switch (selection) {
                        case "first":
                            u1 = 3; v1 = 1; u2 = 4; v2 = 1;
                            break;
                        case "second":
                            u1 = 4; v1 = 3; u2 = 5; v2 = 2;
                            break;
                        case "third":
                            u1 = 1; v1 = 4; u2 = 3; v2 = 3;
                            break;
                    }
                    initializeWatermarkingDCT(blockSize, hDCT, u1 ,v1, u2, v2);
                }
            });
            extractDCT.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    initializeExtractionDCT(blockSize, hDCT, u1 ,v1, u2, v2);
                }
            });
            mirroringButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    initializeMirroring();
                }
            });
            deMirroringButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    revertMirroring();
                }
            });
            rotatingButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    rotatingSelection = Integer.parseInt(group2.getSelection().getActionCommand());
                    initializeRotating(rotatingSelection);
                }
            });
            deRotatingButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    revertRotating(rotatingSelection);
                }
            });
        }


    private void initializeWatermarkingLSB (String selection) {
        ImagePlus watermarkImage = new ImagePlus("watermark.png");
        hLSB = sliderH.getValue();
        watermark = new WatermarkLSB(originalImage.getBufferedImage(), watermarkImage.getBufferedImage(), selection);
        watermark.getRGB(originalImage.getBufferedImage());
        int[][][] selectedComponentBits3D = watermark.convertComponentIntoBits(hLSB, originalImage.getBufferedImage());
        int[][][] watermarkBits = watermark.convertWatermarkIntoToBits(hLSB, watermarkImage.getBufferedImage());
        imageWithWatermarkBits = watermark.insertWatermarkInImage(selectedComponentBits3D, watermarkBits);
        originalWithWatermark = watermark.setImageFromBits(imageWithWatermarkBits);
        originalWithWatermark.show();
    }

    private void initializeExtractionLSB() {
        var extractedImage = this.watermark.extractWatermarkFromImage(hLSB, imageWithWatermarkBits);
        extractedImage.show();

        System.out.println(this.getPsnr("LSB", selection, hLSB, watermarkImage.getBufferedImage(), extractedImage.getBufferedImage()));
    }

    private void initializeWatermarkingDCT(int blockSize, int h, int u1, int v1, int u2, int v2) {
        watermarkDCT = new WatermarkDCT(originalImage, watermarkImage);
        var origWithWatermark = watermarkDCT.insertWatermarkDCT(blockSize, h, u1, v1, u2, v2);
        origWithWatermark.show();
    }
    private void initializeExtractionDCT(int blockSize, int h, int u1, int v1, int u2, int v2) {
        var extractedImage = this.watermarkDCT.extractWatermarkFromImage(blockSize, u1, v1, u2, v2);
        extractedImage.show();

        selection = group.getSelection().getActionCommand();
        String koeficient = "[" + u1 + "," + v1 + "]" + ";" + "[" + u2 + "," + v2 + "]";
        System.out.println("Koeficient: " + koeficient + " - " + this.getPsnr("DCT", selection, h,  watermarkImage.getBufferedImage(), extractedImage.getBufferedImage()));

    }

    private void initializeMirroring () {
        mirroredImage =watermark.mirrorImage(this.originalWithWatermark.getBufferedImage());
        mirroredImage.show();
    }
    private void revertMirroring () {
        var revertedImage =watermark.mirrorImage(mirroredImage.getBufferedImage());
        revertedImage.show();
        watermark.getRGB(revertedImage.getBufferedImage());
        var imageBits = watermark.convertImageToBits(hLSB, revertedImage.getBufferedImage());
        var extractedWatermark = watermark.extractWatermarkFromImage(hLSB, imageBits);
        extractedWatermark.show();

        System.out.println(this.getPsnr("LSB Mirroring", selection, hLSB, watermarkImage.getBufferedImage(), extractedWatermark.getBufferedImage()));
    }

    public void initializeRotating(int rotationType) {
        rotatedImage =watermark.rotate(rotationType, this.originalWithWatermark.getBufferedImage());
        rotatedImage.show();
    }

    public void revertRotating(int rotationType) {
        var revertedImage =watermark.rotate(-rotationType, this.rotatedImage.getBufferedImage());
        revertedImage.show();
        watermark.getRGB(revertedImage.getBufferedImage());
        var imageBits = watermark.convertImageToBits(hLSB, revertedImage.getBufferedImage());
        var extractedWatermark = watermark.extractWatermarkFromImage(hLSB, imageBits);
        extractedWatermark.show();

        System.out.println(this.getPsnr("LSB Rotating", selection, hLSB, watermarkImage.getBufferedImage(), extractedWatermark.getBufferedImage()));
    }

    private String getPsnr(String method, String selection, int h, BufferedImage origImage, BufferedImage origWithWatermarkImage) {
        var colorTransformOrig = new ColorTransform(origImage);
        var colorTransformOrigWithWatermark = new ColorTransform(origWithWatermarkImage);
        colorTransformOrig.getRGB();
        colorTransformOrigWithWatermark.getRGB();
        int[][] componentOrig; int[][] componentOrigWithWatermark;

        switch (selection) {
            case "green":
                componentOrig = colorTransformOrig.getGreen();
                componentOrigWithWatermark = colorTransformOrig.getGreen();
                break;
            case "blue":
                componentOrig = colorTransformOrig.getBlue();
                componentOrigWithWatermark = colorTransformOrig.getBlue();
                break;
            default:
                componentOrig = colorTransformOrig.getRed();
                componentOrigWithWatermark = colorTransformOrig.getRed();
                break;
        }
        return ("Metoda: " +  method + ", zlozka: " + selection + ", hlbka: " + h + ", PSNR: " + quality.getPsnr(componentOrig, componentOrigWithWatermark));

    }
}

