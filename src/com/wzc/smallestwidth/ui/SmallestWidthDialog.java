package com.wzc.smallestwidth.ui;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import org.apache.http.util.TextUtils;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class SmallestWidthDialog extends JDialog {
    private Project mProject;
    private JPanel contentPanel;
    private JTextField designWidth;
    private JTextField smallestWidth;
    private JButton btAddSmallestWidth;
    private JButton btGenerateDirectory;
    private JComboBox comboBox1;
    private JButton btCancel;
    private JList fileList;


    public SmallestWidthDialog(Project project) {
        if (project == null) return;
        this.mProject = project;
        setContentPane(contentPanel);
        setModal(true);
        setTitle("SmallestWidth Generator");
        btGenerateDirectory.addActionListener(e -> {
            generateDirectory();
        });
        btAddSmallestWidth.addActionListener(e -> {
            addSmallestWidth();
        });
        btCancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
    }


    private void generateDirectory() {
        String sDesignWidth = designWidth.getText();
        if (TextUtils.isEmpty(sDesignWidth)) {
            Messages.showMessageDialog(mProject,
                    "DesignWidth can't null",
                    "Warning",
                    Messages.getWarningIcon());
        }
    }

    private void addSmallestWidth() {

    }
}
