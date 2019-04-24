package com.wzc.smallestwidth.ui;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import org.apache.http.util.TextUtils;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.regex.Pattern;

public class SmallestWidthDialog extends JDialog {
    private Project mProject;
    private JPanel contentPane;
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
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(btGenerateDirectory);
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
        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                dispose();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }


    private void generateDirectory() {
        String sDesignWidth = designWidth.getText().trim();
        if (TextUtils.isEmpty(sDesignWidth)) {
            Messages.showMessageDialog(mProject,
                    "DesignWidth can't null",
                    "Warning",
                    Messages.getWarningIcon());
            return;
        }
        boolean b = Pattern.matches("([1-9]\\d*)(\\.\\d)?(\\d*)*", sDesignWidth);
        if (!b) {
            Messages.showMessageDialog(mProject,
                    "Please enter the correct number",
                    "Warning",
                    Messages.getWarningIcon());
            return;
        }
        ListModel fileListModel = fileList.getModel();
        int size = fileListModel.getSize();

    }

    private void addSmallestWidth() {

    }
}
