package com.wzc.smallestwidth.listeners;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

public class JTextFieldHintListener implements FocusListener {

    private String hintText;
    private JTextField textField;
    private Color textColor;

    public JTextFieldHintListener(JTextField jTextField, String hintText) {
        this.textField = jTextField;
        this.hintText = hintText;
        jTextField.setText(hintText);  //默认直接显示
        jTextField.setForeground(Color.GRAY);
    }

    @Override
    public void focusGained(FocusEvent e) {
        //获取焦点时，清空提示内容
        setText();
    }

    @Override
    public void focusLost(FocusEvent e) {
        //失去焦点时，没有输入内容，显示提示内容
        setText();
    }

    private void setText() {
        String temp = textField.getText();
        if (temp.equals("")) {
            textField.setText(hintText);
        } else if (temp.equals(hintText)) {
            textField.setText("");
        }
        textColor = new Color(184, 207, 229);
        textField.setForeground(textColor);
    }
}
