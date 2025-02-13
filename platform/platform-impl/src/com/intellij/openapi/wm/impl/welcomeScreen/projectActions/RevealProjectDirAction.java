// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.openapi.wm.impl.welcomeScreen.projectActions;

import com.intellij.ide.ReopenProjectAction;
import com.intellij.ide.actions.RevealFileAction;
import com.intellij.ide.lightEdit.LightEditCompatible;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformCoreDataKeys;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.ui.components.JBList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.io.File;

/**
 * @author Konstantin Bulenkov
 */
public class RevealProjectDirAction extends DumbAwareAction implements LightEditCompatible {
  public RevealProjectDirAction() {
    super(RevealFileAction.getActionName());
  }

  @Override
  public void actionPerformed(@NotNull AnActionEvent e) {
    ReopenProjectAction action = getReopenAction(e);
    assert action != null;
    String path = action.getProjectPath();
    RevealFileAction.selectDirectory(new File(path));
  }

  @Override
  public void update(@NotNull AnActionEvent e) {
    e.getPresentation().setEnabledAndVisible(getReopenAction(e) != null);
  }

  @Nullable
  private static ReopenProjectAction getReopenAction(AnActionEvent e) {
    Component component = e.getData(PlatformCoreDataKeys.CONTEXT_COMPONENT);
    if (component instanceof JBList) {
      JBList list = (JBList)component;
      Object value = list.getSelectedValue();
      if (value instanceof ReopenProjectAction) {
        return (ReopenProjectAction)value;
      }
    }
    return null;
  }
}