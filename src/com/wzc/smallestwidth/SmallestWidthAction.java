package com.wzc.smallestwidth;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.psi.PsiFile;
import com.wzc.smallestwidth.ui.SmallestWidthDialog;

public class SmallestWidthAction extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent event) {
        Project project = event.getProject();
        PsiFile psiFile = event.getData(CommonDataKeys.PSI_FILE);
        Editor editor = event.getData(CommonDataKeys.EDITOR);


        SmallestWidthDialog dialog = new SmallestWidthDialog(project);
        dialog.pack();
        dialog.setLocationRelativeTo(WindowManager.getInstance().getFrame(project));
        dialog.setVisible(true);
    }
}
