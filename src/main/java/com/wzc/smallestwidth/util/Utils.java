package com.wzc.smallestwidth.util;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import org.dom4j.Document;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class Utils {
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
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                // 关闭操作
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
