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
    private JButton extract;
    private JButton applyMethod1;
    private JSlider sliderH;
    private JRadioButton modráRadioButton1;
    private JRadioButton zelenáRadioButton;
    private JRadioButton červenáRadioButton;

    final File[] fileToSend = new File[1];
    private ImagePlus originalWithWatermark;
    private Watermark watermark;
    private int h;


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
            group.add(modráRadioButton1);
            group.add(zelenáRadioButton);
            group.add(červenáRadioButton);
            this.červenáRadioButton.setActionCommand("red");
            this.zelenáRadioButton.setActionCommand("green");
            this.modráRadioButton1.setActionCommand("blue");
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
                    initializeWatermarking(selection);
                }
            });
            extract.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    initializeExtraction();
                }
            });
        }


    private void initializeWatermarking (String selection) {
        ImagePlus originalImage = new ImagePlus(fileToSend[0].getAbsolutePath());
        ImagePlus watermarkImage = new ImagePlus("watermark.png");
        h= sliderH.getValue();
        watermark = new Watermark(originalImage.getBufferedImage(), watermarkImage.getBufferedImage(), selection);
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
}

