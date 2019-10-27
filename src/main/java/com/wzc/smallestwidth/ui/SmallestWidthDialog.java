package com.wzc.smallestwidth.ui;


import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.wzc.smallestwidth.listeners.JTextFieldHintListener;
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
import java.util.*;
import java.util.regex.Pattern;


public class SmallestWidthDialog extends JDialog {
    private final String UNFUNDMODULE = "Specify the module relative path";
    private final String UNFUNDMODULE_HINT = "ex:project/.../moduleName,input:/.../moduleName";
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
    private JTextField mix_dp;
    private JTextField mix_sp;
    private JTextField max_dp;
    private JTextField max_sp;
    private JTextField tf_module_path;
    private JPanel jp_module_path;
    private DefaultListModel folderModel;
    private ArrayList<Integer> defaultFoldData = new ArrayList<>(Arrays.asList(300, 320, 340, 360, 380, 400, 410, 420, 440, 460, 480, 500, 520));
    private int mixSP;
    private int maxSP;
    private int mixDP;
    private int maxDP;

    public SmallestWidthDialog(Project project) {
        if (project == null) return;
        this.mProject = project;
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(btGenerateDirectory);
        setTitle("SmallestWidth Generator");
        tf_module_path.addFocusListener(new JTextFieldHintListener(tf_module_path, UNFUNDMODULE_HINT));
        mix_sp.addFocusListener(new JTextFieldHintListener(mix_sp, "5"));
        max_sp.addFocusListener(new JTextFieldHintListener(max_sp, "100"));
        mix_dp.addFocusListener(new JTextFieldHintListener(mix_dp, "-1080"));
        max_dp.addFocusListener(new JTextFieldHintListener(max_dp, "1080"));
        getModuleName();
        refreshFolderList();
        cbModuleName.addItemListener(e -> selectModuleName());
        btGenerateDirectory.addActionListener(e -> generateDirectory());
        btAddSmallestWidth.addActionListener(e -> addSmallestWidth());
        btCancel.addActionListener(e -> dispose());
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                dispose();
            }
        });
        contentPane.registerKeyboardAction(e -> dispose(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    private void selectModuleName() {
        String moduleName = (String) cbModuleName.getSelectedItem();
        if (moduleName != null && moduleName.equals(UNFUNDMODULE)) {
            jp_module_path.setVisible(true);
        } else {
            jp_module_path.setVisible(false);
        }
        refreshFolderList();
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
            cbModuleName.addItem("unfound settings.gradle file");
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
        cbModuleName.addItem(UNFUNDMODULE);  //指定module路径，以便settings.gradle文件下获取不到ModuleName
    }

    /**
     * 初始化或者选择module后，刷新要生成的文件列表
     */
    private void refreshFolderList() {
        if (folderModel==null){
            folderModel = new DefaultListModel();
        }
        String moduleName = (String) cbModuleName.getSelectedItem();
        if (moduleName == null) {
            return;
        }
        if (moduleName.equals(UNFUNDMODULE) && tf_module_path.getText().equals(UNFUNDMODULE_HINT)) {
            folderModel.removeAllElements();
            for (int dp : defaultFoldData) {
                folderModel.addElement("values-sw" + dp + "dp");
            }
        } else {
            String resFilePath = "";
            if (moduleName.equals(UNFUNDMODULE)) {
                String filePath = tf_module_path.getText();
                boolean inputPath = Pattern.matches("^\\/(\\w+\\/?)+$", filePath);
                if (inputPath) {
                    resFilePath = mProject.getBasePath() + File.separator + moduleName + File.separator + "src" + File.separator + "main" + File.separator + "res";
                }
            }else {
                resFilePath = mProject.getBasePath() + File.separator + moduleName + File.separator + "src" + File.separator + "main" + File.separator + "res";
            }
            File resFile = new File(resFilePath);
            if (!resFile.exists()) {
                resFile.mkdirs();
            }
            // get the folder list
            File[] fileArray = resFile.listFiles();
            List<String> fileNameList = new ArrayList<>();
            if (fileArray == null || fileArray.length <= 0) {
                for (int dp : defaultFoldData) {
                    folderModel.addElement("values-sw" + dp + "dp");
                }
            } else {  //读取res文件夹列表
                for (File file : fileArray) {
                    fileNameList.add(file.getName());
                }
                for (String fileName : fileNameList) {
                    if (fileName.contains("values-sw")) {
                        folderModel.addElement(fileName);
                    }
                }
                if (folderModel.getSize() <= 0) {
                    for (int dp : defaultFoldData) {
                        folderModel.addElement("values-sw" + dp + "dp");
                    }
                }
            }
        }
        folderList.setModel(folderModel);
    }

    private void addSmallestWidth() {
        String inputSmallestWidth = smallestWidth.getText().trim();
        if (TextUtils.isEmpty(inputSmallestWidth)) return;
        String[] addString;
        if (inputSmallestWidth.contains(",")) {
            addString = inputSmallestWidth.split(",");
        } else if (inputSmallestWidth.contains("，")) {
            addString = inputSmallestWidth.split("，");
        } else {
            addString = new String[]{inputSmallestWidth};
        }
        for (String dp : addString) {
            boolean b = Pattern.matches("^([1-9]\\d{2,3})", dp);
            if (!b) {
                Utils.showWarningDialog(mProject, dp + " is not a correct number,99 < number < 10000", "Warning");
                continue;
            }
            if (folderModel.contains("values-sw" + dp + "dp")) {
                Utils.showWarningDialog(mProject, dp + " it's already in the default list", "Warning");
                continue;
            }
            folderModel.addElement("values-sw" + dp + "dp");
            defaultFoldData.add(Integer.valueOf(dp));
        }
        smallestWidth.setText("");
        folderList.notify();
    }

    /**
     * 生成
     */
    private void generateDirectory() {
        addSmallestWidth();
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
        boolean b = Pattern.matches("^([1-9]\\d{0,3})(\\.\\d{0,2})?", sDesignWidth);
        if (!b) {
            Utils.showWarningDialog(mProject, " DesignWidth please enter the correct number,0 < number <=9999.99 ", "Warning");
            return;
        }
        boolean b1 = Pattern.matches("^-[1-9]\\d{0,3}|[1-9]\\d{0,3}|0$", mix_sp.getText().trim());
        boolean b2 = Pattern.matches("^-[1-9]\\{0,3}|[1-9]\\d{0,3}|0$", max_sp.getText().trim());
        if (!b1 || !b2) {
            Utils.showWarningDialog(mProject, "sp please enter the correct number,-10000 < number < 10000", "Warning");
            return;
        }
        mixSP = TextUtils.isEmpty(mix_sp.getText().trim()) ? 5 : Integer.parseInt(mix_sp.getText().trim());
        maxSP = TextUtils.isEmpty(max_sp.getText().trim()) ? 100 : Integer.parseInt(max_sp.getText().trim());
        if (mixSP > maxSP) {
            int temp = maxSP;
            maxSP = mixSP;
            mixSP = temp;
        }
        boolean b3 = Pattern.matches("^-[1-9]\\d{0,3}|[1-9]\\d{0,3}|0$", mix_dp.getText().trim());
        boolean b4 = Pattern.matches("^-[1-9]\\d{0,3}|[1-9]\\d{0,3}|0$", max_dp.getText().trim());
        if (!b3 || !b4) {
            Utils.showWarningDialog(mProject, "dp please enter the correct number,-10000 < number < 10000", "Warning");
            return;
        }
        mixDP = TextUtils.isEmpty(mix_dp.getText().trim()) ? -1080 : Integer.parseInt(mix_dp.getText().trim());
        maxDP = TextUtils.isEmpty(max_dp.getText().trim()) ? 1080 : Integer.parseInt(max_dp.getText().trim());
        if (mixDP > maxDP) {
            int temp = maxDP;
            maxDP = mixDP;
            mixDP = temp;
        }
        progressBar.setVisible(true);
        progressBar.setValue(0);
        int folderModelSize = folderModel.getSize();
        BigDecimal max = new BigDecimal(100);//进度条最大值
        BigDecimal swFolderSize = new BigDecimal(folderModelSize);
        new Thread(() -> {
            Enumeration<String> swEnumeration = folderModel.elements();
            defaultFoldData.clear();
            while (swEnumeration.hasMoreElements()) {
                String swFileName = swEnumeration.nextElement();
                int number = Integer.parseInt(swFileName.split("sw")[1].split("dp")[0]);
                defaultFoldData.add(number);
            }
            Collections.sort(defaultFoldData); //将sw<N>dp文件夹排序

            double stage = max.divide(swFolderSize, 2, RoundingMode.HALF_EVEN).doubleValue();   //要生成多少个文件夹就将进度条分为多少个阶段
            for (int i = 0; i <= folderModelSize; i++) {
                String swFolderName;
                int sw_dp;    //文件夹sw<N>dp中的N值
                if (i == defaultFoldData.size()) {
                    sw_dp = defaultFoldData.get(i - 1);
                    swFolderName = "values";
                } else {
                    sw_dp = defaultFoldData.get(i);
                    swFolderName = "values-sw" + defaultFoldData.get(i) + "dp";
                }
                String filePath;
                if (moduleName.equals(UNFUNDMODULE)) {
                    String input_filePath = tf_module_path.getText();
                    boolean inputPath = Pattern.matches("^\\/(\\w+\\/?)+$", input_filePath);
                    if (inputPath) {
                        filePath = mProject.getBasePath() + input_filePath + File.separator + "src" + File.separator + "main" + File.separator + "res" + File.separator + swFolderName + File.separator + "dimens.xml";
                    } else {
                        return;
                    }
                } else {
                    filePath = mProject.getBasePath() + File.separator + moduleName + File.separator + "src" + File.separator + "main" + File.separator + "res" + File.separator + swFolderName + File.separator + "dimens.xml";
                }
                try {
                    File file = new File(filePath);
                    if (!file.getParentFile().exists()) {
                        file.getParentFile().mkdirs();
                    }
                    boolean deleteFile = true;
                    if (file.exists()) {
                        deleteFile = file.delete();
                    }
                    if (deleteFile) {
                        file.createNewFile();
                        Document dimensDocument = DocumentHelper.createDocument();
                        Element resources = dimensDocument.addElement("resources");
                        BigDecimal smallestWidthdp = new BigDecimal(sw_dp);
                        BigDecimal designWidthpx = new BigDecimal(designWidth.getText());
                        double dp = smallestWidthdp.divide(designWidthpx, 3, RoundingMode.HALF_EVEN).doubleValue();  // 屏幕最小宽度dp/设计稿最小宽度px
                        for (int j = mixDP; j <= maxDP; j++) {
                            double data = dp * j;
                            BigDecimal bigDecimal = new BigDecimal(data);
                            double value = bigDecimal.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
                            if (j < 0) {
                                resources.addElement("dimen").addAttribute("name", "_sw_" + Math.abs(j) + "dp").addText(value + "dp");
                            } else {
                                resources.addElement("dimen").addAttribute("name", "sw_" + j + "dp").addText(value + "dp");
                            }
                        }
                        for (int sp = mixSP; sp <= maxSP; sp++) {
                            double sp_value = dp * sp;
                            BigDecimal bigDecimal = new BigDecimal(sp_value);
                            double value = bigDecimal.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
                            resources.addElement("dimen").addAttribute("name", "sw_" + sp + "sp").addText(value + "sp");
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
                            for (int j = mixDP; j <= maxDP; j++) {
                                double data = dp * j;
                                BigDecimal bigDecimal = new BigDecimal(data);
                                double value = bigDecimal.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
                                String name;
                                if (j < 0) {
                                    name = "_sw_" + Math.abs(j) + "dp";
                                } else {
                                    name = "sw_" + j + "dp";
                                }
                                //判断当前有没有名字相同的 有就修改值，没有就新增
                                Element element = distinct(name, resources);
                                if (element != null) {
                                    element.setText(value + "dp");
                                } else {
                                    resources.addElement("dimen").addAttribute("name", name).addText(value + "dp");
                                }
                            }
                            for (int sp = mixSP; sp <= maxSP; sp++) {
                                double sp_value = dp * sp;
                                BigDecimal bigDecimal = new BigDecimal(sp_value);
                                double value = bigDecimal.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
                                String sp_name = "sw_" + sp + "sp";
                                //判断当前有没有名字相同的 有就修改值，没有就新增
                                Element element = distinct(sp_name, resources);
                                if (element != null) {
                                    element.setText(value + "sp");
                                } else {
                                    resources.addElement("dimen").addAttribute("name", sp_name).addText(value + "sp");
                                }
                            }

                            Utils.writeXml(file, document);
                        } catch (DocumentException e) {
                            e.printStackTrace();
                        }
                    }
                    progressBar.setValue((int) (i * stage));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            ProjectManager.getInstance().reloadProject(mProject);//刷新目录
            dispose();
        }).start();


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
