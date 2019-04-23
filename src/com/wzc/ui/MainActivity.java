package com.wzc.ui;

import javax.swing.*;

public class MainActivity {
    private JPanel contentPanel;
    private JTextField designWidth;
    private JTextField smallestWidth;
    private JButton btAddSmallestWidth;
    private JButton btCreateDirectory;
    private JComboBox comboBox1;
    private JButton btCancel;
    private JList fileList;

    public static void main(String[] args) {
        JFrame frame = new JFrame("MainActivity");
        frame.setContentPane(new MainActivity().contentPanel);
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
