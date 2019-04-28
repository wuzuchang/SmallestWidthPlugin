package com.wzc.smallestwidth.ui;


import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.wzc.smallestwidth.util.Utils;
import org.apache.http.util.TextUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import javax.swing.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.regex.Pattern;


public class SmallestWidthDialog extends JDialog {
    private Project mProject;
    private JPanel contentPane;
    private JTextField designWidth;
    private JTextField smallestWidth;
    private JButton btAddSmallestWidth;
    private JButton btGenerateDirectory;
    private JComboBox<String> cbModuleName;
    private JButton btCancel;
    private JList folderList;
    private JProgressBar progressBar;
    private DefaultListModel folderModel;
    ArrayList<String> defaultFoldData = new ArrayList<>(Arrays.asList("300", "320", "340", "360", "380", "400", "410", "420", "440", "460", "480", "500", "520"));

    public SmallestWidthDialog(Project project) {
        if (project == null) return;
        this.mProject = project;
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(btGenerateDirectory);
        setTitle("SmallestWidth Generator");

        getModuleName();

        setFolderList();
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
        contentPane.registerKeyboardAction(e -> dispose(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    private void getModuleName() {
        String basePath = mProject.getBasePath();
        System.out.println(basePath);
        if (TextUtils.isEmpty(basePath)) {
            cbModuleName.addItem("The BasePath is null");
            return;
        } 
        String settingGradlePath = basePath + File.separator + "settings.gradle";
        VirtualFile settingGradleFile = LocalFileSystem.getInstance().findFileByPath(settingGradlePath);
        if (settingGradleFile == null) {
            cbModuleName.addItem("Unfound settings.gradle file");
            return;
        } 
        PsiFile settingGradlePsiFile = PsiManager.getInstance(mProject).findFile(settingGradleFile);
        if (settingGradlePsiFile == null) return;
        String includeString = settingGradlePsiFile.getText();
        if (includeString.isEmpty()) {
            cbModuleName.addItem("settings.gradle file content is null");
            return;
        } 
        if (includeString.contains("include")) {
            includeString = includeString.replaceAll("include", "");
        }
        if (includeString.contains(",")) {
            includeString = includeString.replaceAll(",", "");
        }
        if (includeString.contains("\n")) {
            includeString = includeString.replaceAll("\n", "");
        }
        if (includeString.contains("'")) {
            includeString = includeString.replaceAll("'", "");
        }
        if (includeString.contains(" ")) {
            includeString = includeString.replaceAll(" ", "");
        }
        String[] moduleNameList = new String[0];
        if (includeString.contains(":")) {
            moduleNameList = includeString.split(":");
        }
        VirtualFile baseVirtualFile = LocalFileSystem.getInstance().findFileByPath(basePath);
        if (baseVirtualFile == null) {
            cbModuleName.addItem("There are no files in the folder " + basePath);
            return;
        } 
        VirtualFile[] virtualFiles = baseVirtualFile.getChildren();
        if (virtualFiles == null || virtualFiles.length <= 0) {
            cbModuleName.addItem("There are no files in the folder " + basePath);
            return;
        }
        for (VirtualFile virtualFile : virtualFiles) {
            if (!virtualFile.isDirectory()) {
                continue;
            }
            String fileName = virtualFile.getName();
            for (int i = 1; i < moduleNameList.length; i++) {
                if (moduleNameList[i].trim().equals(fileName)) {
                    cbModuleName.addItem(fileName);
                }
            }
        }
    }


    private void setFolderList() {
        folderModel = new DefaultListModel();
        for (String dp : defaultFoldData) {
            folderModel.addElement("values-sw" + dp + "dp");
        }
        folderList.setModel(folderModel);
    }

    private void addSmallestWidth() {
        String inputSmallestWidth = smallestWidth.getText();
        if (TextUtils.isEmpty(inputSmallestWidth)) return;
        boolean b = Pattern.matches("^[1-9]\\d{2}", inputSmallestWidth);
        if (!b) {
            Utils.showWarningDialog(mProject, "Please enter a three-digit number", "Warning");
            return;
        }
        if (defaultFoldData.contains(inputSmallestWidth)) {
            Utils.showWarningDialog(mProject, "It's already in the default list", "Warning");
            return;
        }
        folderModel.addElement("values-sw" + inputSmallestWidth + "dp");
        defaultFoldData.add(inputSmallestWidth);
        smallestWidth.setText("");
        folderList.notify();

    }

    private void generateDirectory() {
        String moduleName = (String) cbModuleName.getSelectedItem();
        if (TextUtils.isEmpty(moduleName)) {
            Utils.showWarningDialog(mProject, "Not found Module", "Warning");
            return;
        }

        String sDesignWidth = designWidth.getText().trim();
        if (TextUtils.isEmpty(sDesignWidth)) {
            Utils.showWarningDialog(mProject, "DesignWidth can't null", "Warning");
            return;
        }
        boolean b = Pattern.matches("([1-9]\\d*)(\\.\\d)?(\\d*)*", sDesignWidth);
        if (!b) {
            Utils.showWarningDialog(mProject, "Please enter the correct number", "Warning");
            return;
        }
        progressBar.setVisible(true);
        progressBar.setValue(0);
        Collections.sort(this.defaultFoldData);
        double stage = 100 / defaultFoldData.size();
        for (int i = 0; i <= defaultFoldData.size(); i++) {
            String swFolderName;
            String sw_dp;
            if (i == defaultFoldData.size()) {
                sw_dp = defaultFoldData.get(i - 1);
                swFolderName = "values";
            } else {
                sw_dp = defaultFoldData.get(i);
                swFolderName = "values-sw" + defaultFoldData.get(i) + "dp";
            }
            String filePath = mProject.getBasePath() + File.separator + moduleName + File.separator + "src" + File.separator + "main" + File.separator + "res" + File.separator + swFolderName + File.separator + "dimens.xml";
            try {
                File file = new File(filePath);
                if (!file.getParentFile().exists()) {
                    file.getParentFile().mkdirs();
                }
                if (!file.exists()) {
                    file.createNewFile();
                    Document dimensDocument = DocumentHelper.createDocument();
                    Element resources = dimensDocument.addElement("resources");
                    BigDecimal smallestWidthdp = new BigDecimal(sw_dp);
                    BigDecimal designWidthpx = new BigDecimal(designWidth.getText());
                    double dp = smallestWidthdp.divide(designWidthpx, 3, RoundingMode.HALF_EVEN).doubleValue();
                    for (int j = 0; j <= Integer.valueOf(sw_dp); j++) {
                        progressBar.setValue((int) (i * j * (stage / Integer.valueOf(sw_dp))));
                        double value = dp * j;
                        resources.addElement("dimen").addAttribute("name", "sw_" + j + "dp").addText(value + "dp");
                    }
                    Utils.writeXml(file, dimensDocument);
                } else {
                    try {
                        SAXReader reader = new SAXReader();
                        BigDecimal smallestWidthdp = new BigDecimal(sw_dp);
                        BigDecimal designWidthpx = new BigDecimal(designWidth.getText());
                        double dp = smallestWidthdp.divide(designWidthpx, 3, RoundingMode.HALF_EVEN).doubleValue();
                        // 2.通过reader对象的read方法加载xml文件，获取Document对象
                        Document document = reader.read(file);
                        Element resources = document.getRootElement();// 通过document对象获取根节点resources
                        for (int j = 0; j <= Integer.valueOf(sw_dp); j++) {
                            progressBar.setValue((int) (i * j * (stage / Integer.valueOf(sw_dp))));
                            double value = dp * j;
                            String name = "sw_" + j + "dp";
                            //判断当前有没有名字相同的 有就修改值，没有就新增
                            Element element = distinct(name, resources);
                            if (element != null) {
                                element.setText(value + "dp");
                            } else {
                                resources.addElement("dimen").addAttribute("name", "sw_" + j + "dp").addText(value + "dp");
                            }
                        }
                        Utils.writeXml(file, document);
                    } catch (DocumentException e) {
                        e.printStackTrace();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        progressBar.setValue(100);
        ProjectManager.getInstance().reloadProject(mProject);//刷新目录
        dispose();
    }

    /**
     * 判断xml文件内是否有同name的节点
     *
     * @param name        name
     * @param rootElement 根节点
     * @return dimensElement 有  null 没有
     */
    private Element distinct(String name, Element rootElement) {
        Iterator<Element> dimensElementIterator = rootElement.elementIterator();
        while (dimensElementIterator.hasNext()) {
            Element dimensElement = dimensElementIterator.next();
            if (!"dimen".equals(dimensElement.getName()))   //如果节点名字不一样就不用比较
                continue;
            try {
                if (name.equals(dimensElement.attribute("name").getValue())) {
//                    System.out.println("dimen.xml中有 name 是 " + name + "的dimen节点");
                    return dimensElement;
                }
            } catch (NullPointerException e) {
                return null;
            }
        }
        return null;
    }
}
