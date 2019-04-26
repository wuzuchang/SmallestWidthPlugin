package com.wzc.smallestwidth;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.WindowManager;
import com.wzc.smallestwidth.ui.SmallestWidthDialog;

public class SmallestWidthAction extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent event) {
        Project project = event.getProject();
        SmallestWidthDialog dialog = new SmallestWidthDialog(project);
        dialog.pack();
        dialog.setLocationRelativeTo(WindowManager.getInstance().getFrame(project));
        dialog.setVisible(true);
    }
}
