package com.wzc.smallestwidth.util;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.PsiFile;
import org.dom4j.Document;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {

    private static final Pattern INCLUDE_PATTERN =
            Pattern.compile("include\\s*[\\(]?\\s*['\"]([^'\"]+)['\"]");


    public static void showWarningDialog(Project project, String message, String title) {
        Messages.showMessageDialog(project, message, title, Messages.getWarningIcon());
    }
    /**
     * 将xml数据写入文件
     *
     * @param file     目标文件
     * @param document 要写入的xml数据
     */
    public static void writeXml(File file, Document document) {
        // 排版缩进的格式
        OutputFormat format = OutputFormat.createPrettyPrint();
        // 设置编码
        format.setEncoding("UTF-8");
        XMLWriter writer = null;
        try {
            writer = new XMLWriter(new OutputStreamWriter(
                    new FileOutputStream(file), StandardCharsets.UTF_8), format);
            // 写入
            writer.write(document);
            // 立即写入
            writer.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                // 关闭操作
                writer.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    public static List<String> getIncludedModules(PsiFile settingsGradleFile) {
        List<String> modules = new ArrayList<>();
        String fileText = settingsGradleFile.getText();
        Matcher matcher = INCLUDE_PATTERN.matcher(fileText);
        while (matcher.find()) {
            String moduleWithColon = matcher.group(1);
            String module = moduleWithColon.replace(":", ""); // 去掉所有 ":"
            modules.add(module);
        }
        return modules;
    }
}
