package projekt;

import Jama.Matrix;
import ij.ImagePlus;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import javax.swing.JFileChooser;

public class MainWindow {

    private JPanel mainPanel;
    private JButton chooseImage;
    private JButton extract;
    private JButton applyMethod1;
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

    final File[] fileToSend = new File[1];
    private ImagePlus originalWithWatermark;
    private WatermarkLSB watermark;
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
                        ImagePlus originalImage = new ImagePlus(fileToSend[0].getAbsolutePath());
                        originalImage.show();
                    }
                }
            });

            applyMethod1.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    String selection = group.getSelection().getActionCommand();
                    initializeWatermarkingLSB(selection);
                }
            });
            extract.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    initializeExtraction();
                }
            });
            DCT.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    int blockSize = 8;

                    //if (a2x2RadioButton.isSelected()) blockSize = 2;
                    //if (a4x4RadioButton.isSelected()) blockSize = 4;
                    //if (a8x8RadioButton.isSelected()) blockSize = 8;

                    Matrix tMat = new TransformMatrix().getDctMatrix(blockSize);
                    ImagePlus originalImage = new ImagePlus(fileToSend[0].getAbsolutePath());
                    WatermarkDCT watermarkDCT = new WatermarkDCT(originalImage);
                    //process.transform(blockSize, tMat, Quantization.getValue());
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
        ImagePlus originalImage = new ImagePlus(fileToSend[0].getAbsolutePath());
        ImagePlus watermarkImage = new ImagePlus("watermark.png");
        h= sliderH.getValue();
        watermark = new WatermarkLSB(originalImage.getBufferedImage(), watermarkImage.getBufferedImage(), selection);
        watermark.setOriginalBits(watermark.bitsPreparationOrig(h));
        watermark.setWatermarkBits(watermark.bitsPreparationMark(h));
        watermark.insertWatermarkInImage();
        originalWithWatermark = watermark.setImageFromBits();
        originalWithWatermark.show();

    }

    private void initializeExtraction() {
        var extractedImage = this.watermark.extractWatermarkFromImage(h);
        extractedImage.show();
    }

    private void initializeWatermarkingDCT() {

    }

    private void initializeMirroring () {
        var image =watermark.mirrorImage();
        image.show();
        watermark.setMirroredImage(image.getBufferedImage());
    }
    private void revertMirroring () {
        var image =watermark.mirrorImage();
        watermark.setWatermarkImage(image.getBufferedImage());
        var extractedImage = watermark.extractWatermarkFromImage(h);
        extractedImage.show();
    }

    public void initializeRotating(int rotationType) {
        var image =watermark.rotate(rotationType);
        image.show();
        watermark.setRotatedImage(image.getBufferedImage());
    }

    public void revertRotating(int rotationType) {
        var image =watermark.rotate(-rotationType);
        watermark.setWatermarkImage(image.getBufferedImage());
        var extractedImage = watermark.extractWatermarkFromImage(h);
        extractedImage.show();
    }

}

