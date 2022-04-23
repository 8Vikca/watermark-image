package projekt;

import ij.ImagePlus;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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

    final File[] fileToSend = new File[1];
    private ImagePlus originalImage;
    private ImagePlus originalWithWatermark;
    private ImagePlus watermarkImage;
    private ImagePlus mirroredImage;
    private ImagePlus rotatedImage;
    private WatermarkLSB watermark;
    private int [][][] imageWithWatermarkBits;
    private int h;
    private int rotatingSelection;


    public static void main(String[] args) {
        JFrame frame = new JFrame("Aplikace pro zpracování obrazu");
        frame.setBounds(100, 100, 450, 300);
        frame.setContentPane(new MainWindow().mainPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }
        public MainWindow() {
            ButtonGroup group = new ButtonGroup();
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
                    String selection = group.getSelection().getActionCommand();
                    initializeWatermarkingLSB(selection);
                }
            });
            extractLSB.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    initializeExtraction();
                }
            });
            DCT.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    int blockSize = 8;
                    int h = 2;
                    int u1 = 1;
                    int v1 = 4;
                    int u2 = 3;
                    int v2 = 3;

                    initializeWatermarkingDCT(blockSize, h, u1 ,v1, u2, v2);
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
        h= sliderH.getValue();
        watermark = new WatermarkLSB(originalImage.getBufferedImage(), watermarkImage.getBufferedImage(), selection);
        watermark.getRGB(originalImage.getBufferedImage());
        int[][][] selectedComponentBits3D = watermark.convertComponentIntoBits(h, originalImage.getBufferedImage());
        int[][][] watermarkBits = watermark.convertWatermarkIntoToBits(h, watermarkImage.getBufferedImage());
        imageWithWatermarkBits = watermark.insertWatermarkInImage(selectedComponentBits3D, watermarkBits);
        originalWithWatermark = watermark.setImageFromBits(imageWithWatermarkBits);
        originalWithWatermark.show();

    }

    private void initializeExtraction() {
        var extractedImage = this.watermark.extractWatermarkFromImage(h, imageWithWatermarkBits);
        extractedImage.show();
    }

    private void initializeWatermarkingDCT(int blockSize, int h, int u1, int v1, int u2, int v2) {
        WatermarkDCT watermarkDCT = new WatermarkDCT(originalImage, watermarkImage);
        var origWithWatermark = watermarkDCT.insertWatermarkDCT(blockSize, h, u1, v1, u2, v2);
        origWithWatermark.show();
    }

    private void initializeMirroring () {
        mirroredImage =watermark.mirrorImage(this.originalWithWatermark.getBufferedImage());
        mirroredImage.show();
    }
    private void revertMirroring () {
        var revertedImage =watermark.mirrorImage(mirroredImage.getBufferedImage());
        revertedImage.show();
        watermark.getRGB(revertedImage.getBufferedImage());
        var imageBits = watermark.convertImageToBits(h, revertedImage.getBufferedImage());
        var extractedWatermark = watermark.extractWatermarkFromImage(h, imageBits);
        extractedWatermark.show();
    }

    public void initializeRotating(int rotationType) {
        rotatedImage =watermark.rotate(rotationType, this.originalWithWatermark.getBufferedImage());
        rotatedImage.show();
    }

    public void revertRotating(int rotationType) {
        var revertedImage =watermark.rotate(-rotationType, this.rotatedImage.getBufferedImage());
        revertedImage.show();
        watermark.getRGB(revertedImage.getBufferedImage());
        var imageBits = watermark.convertImageToBits(h, revertedImage.getBufferedImage());
        var extractedWatermark = watermark.extractWatermarkFromImage(h, imageBits);
        extractedWatermark.show();
        //watermark.setWatermarkImage(image.getBufferedImage());
        //var extractedImage = watermark.extractWatermarkFromImage(h);
        //extractedImage.show();
    }

}

