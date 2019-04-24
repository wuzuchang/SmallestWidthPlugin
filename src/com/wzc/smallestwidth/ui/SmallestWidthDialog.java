package com.wzc.smallestwidth.ui;


import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.xml.XmlDocument;
import com.intellij.psi.xml.XmlTag;
import org.apache.http.util.TextUtils;

import javax.swing.*;
import java.awt.event.*;
import java.io.File;
import java.util.ArrayList;
import java.util.regex.Pattern;

public class SmallestWidthDialog extends JDialog {
    private Project mProject;
    private JPanel contentPane;
    private JTextField designWidth;
    private JTextField smallestWidth;
    private JButton btAddSmallestWidth;
    private JButton btGenerateDirectory;
    private JComboBox<String> comboBox1;
    private JButton btCancel;
    private JList fileList;

    public static final String SELECTED_MODULE_PROPERTY = SmallestWidthDialog.class.getCanonicalName() + "-SELECTED_MODULE";

    public SmallestWidthDialog(Project project) {
        if (project == null) return;
        this.mProject = project;
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(btGenerateDirectory);
        setTitle("SmallestWidth Generator");

        getModuleName();


        btGenerateDirectory.addActionListener(e -> {
            generateDirectory();
        });
        btAddSmallestWidth.addActionListener(e -> {
            addSmallestWidth();
        });
        btCancel.addActionListener(e -> dispose());
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

    private void getModuleName() {
        String basePath = mProject.getBasePath();
        if (TextUtils.isEmpty(basePath)) return;
        String settingGradlePath = basePath + File.separator + "settings.gradle";
        VirtualFile settingGradleFile = LocalFileSystem.getInstance().findFileByPath(settingGradlePath);
        if (settingGradleFile == null) return;
        PsiFile settingGradlePsiFile = PsiManager.getInstance(mProject).findFile(settingGradleFile);
        if (settingGradlePsiFile == null) return;
        String includeString = settingGradlePsiFile.getText().trim();
        if (includeString.isEmpty()) return;
        String[] moduleNameList = new String[0];
        if (includeString.contains("'")) {
            moduleNameList = includeString.replaceAll("'", "").split(":");
        }
        VirtualFile baseVirtualFile = LocalFileSystem.getInstance().findFileByPath(basePath);
        if (baseVirtualFile == null) return;
        VirtualFile[] virtualFiles = baseVirtualFile.getChildren();
        if (virtualFiles == null || virtualFiles.length <= 0) {
            return;
        }
        for (VirtualFile virtualFile : virtualFiles) {
            if (!virtualFile.isDirectory()) {
                return;
            }
            String fileName = virtualFile.getName();
            if (fileName.startsWith(".") || "gradle".equals(fileName)) {
                continue;
            }
            if (moduleNameList.length < 1) {
                return;
            }
            for (int i = 1; i < moduleNameList.length; i++) {
                if (moduleNameList[i].trim().equals(fileName)) {
                    comboBox1.addItem(fileName);
                }
            }

        }
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
        DefaultListModel fileListModel = (DefaultListModel) fileList.getModel();
        int size = fileListModel.getSize();
        DefaultListModel listModel = new DefaultListModel();
    }

    private void addSmallestWidth() {

    }
}
