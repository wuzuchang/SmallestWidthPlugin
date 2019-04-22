package com.wzc.ui;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MainActivity {
    private JPanel panel1;
    private JTextField draftSize;
    private JTextField dpPxDpi160TextField;
    private JButton btAddSmallestWidth;
    private JList list1;
    private JButton btCreateDirectory;
    private JComboBox comboBox1;

    public static void main(String[] args) {
        JFrame frame = new JFrame("MainActivity");
        frame.setContentPane(new MainActivity().panel1);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

    public MainActivity() {
        btCreateDirectory.addActionListener(e -> {

        });
        btAddSmallestWidth.addActionListener(e -> {

        });
    }

}
