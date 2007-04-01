/*
 * Created by IntelliJ IDEA.
 * User: max
 * Date: Nov 23, 2001
 * Time: 10:31:03 PM
 * To change template for new class use 
 * Code Style | Class Templates options (Tools | IDE Options).
 */
package com.intellij.codeInspection.ui;

import com.intellij.codeInspection.CommonProblemDescriptor;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ex.SingleInspectionProfilePanel;
import com.intellij.codeInspection.offline.OfflineProblemDescriptor;
import com.intellij.codeInspection.offlineViewer.OfflineProblemDescriptorNode;
import com.intellij.codeInspection.offlineViewer.OfflineRefElementNode;
import com.intellij.codeInspection.reference.RefElement;
import com.intellij.codeInspection.reference.RefEntity;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;

import java.util.Comparator;

public class InspectionResultsViewComparator implements Comparator {
  private static final Logger LOG = Logger.getInstance("#com.intellij.codeInspection.ui.InspectionResultsViewComparator");

  private static InspectionResultsViewComparator ourInstance = null;

  public int compare(Object o1, Object o2) {
    InspectionTreeNode node1 = (InspectionTreeNode)o1;
    InspectionTreeNode node2 = (InspectionTreeNode)o2;
    
    if (node1 instanceof InspectionSeverityGroupNode && node2 instanceof InspectionSeverityGroupNode) {
      return -((InspectionSeverityGroupNode)node1).getSeverityLevel().getSeverity().compareTo(((InspectionSeverityGroupNode)node2).getSeverityLevel().getSeverity());
    }

    if (node1 instanceof InspectionGroupNode && node2 instanceof InspectionGroupNode) {
      return ((InspectionGroupNode)node1).getGroupTitle().compareToIgnoreCase(((InspectionGroupNode)node2).getGroupTitle());
    }
    if (node1 instanceof InspectionNode && node2 instanceof InspectionGroupNode) return -1;
    if (node2 instanceof InspectionNode && node1 instanceof InspectionGroupNode) return 1;

    if (node1 instanceof EntryPointsNode) return -1;
    if (node2 instanceof EntryPointsNode) return 1;

    if (node1 instanceof InspectionNode && node2 instanceof InspectionNode)
      return SingleInspectionProfilePanel.getDisplayTextToSort(node1.toString())
      .compareToIgnoreCase(SingleInspectionProfilePanel.getDisplayTextToSort(node2.toString()));

    if ((node1 instanceof OfflineRefElementNode && node2 instanceof OfflineRefElementNode) ||
        (node1 instanceof OfflineProblemDescriptorNode && node2 instanceof OfflineProblemDescriptorNode)) {
      final Object userObject1 = node1.getUserObject();
      final Object userObject2 = node2.getUserObject();
      if (userObject1 instanceof OfflineProblemDescriptor && userObject2 instanceof OfflineProblemDescriptor) {
        final OfflineProblemDescriptor descriptor1 = (OfflineProblemDescriptor)userObject1;
        final OfflineProblemDescriptor descriptor2 = (OfflineProblemDescriptor)userObject2;
        if (descriptor1.getLine() != descriptor2.getLine()) return descriptor1.getLine() - descriptor2.getLine();
        return descriptor1.getFQName().compareTo(descriptor2.getFQName());
      }
      if (userObject1 instanceof OfflineProblemDescriptor) {
        return compareLineNumbers(userObject2, (OfflineProblemDescriptor)userObject1);
      }
      if (userObject2 instanceof OfflineProblemDescriptor) {
        return -compareLineNumbers(userObject1, (OfflineProblemDescriptor)userObject2);
      }
    }

    if (node1 instanceof RefElementNode && node2 instanceof RefElementNode){   //sort by filename and inside file by start offset
      return compareEntities(((RefElementNode)node1).getElement(), ((RefElementNode)node2).getElement());
    }
    if (node1 instanceof ProblemDescriptionNode && node2 instanceof ProblemDescriptionNode) {
      final CommonProblemDescriptor descriptor1 = ((ProblemDescriptionNode)node1).getDescriptor();
      final CommonProblemDescriptor descriptor2 = ((ProblemDescriptionNode)node2).getDescriptor();
      if (descriptor1 instanceof ProblemDescriptor && descriptor2 instanceof ProblemDescriptor) {
        return ((ProblemDescriptor)descriptor1).getLineNumber() - ((ProblemDescriptor)descriptor2).getLineNumber();
      }
      if (descriptor1 != null && descriptor2 != null) {
        return descriptor1.getDescriptionTemplate().compareToIgnoreCase(descriptor2.getDescriptionTemplate());
      }
      return 0;
    }

    if (node1 instanceof RefElementNode && node2 instanceof ProblemDescriptionNode) {
      final CommonProblemDescriptor descriptor = ((ProblemDescriptionNode)node2).getDescriptor();
      if (descriptor instanceof ProblemDescriptor) {
        return compareEntity(((RefElementNode)node1).getElement(), ((ProblemDescriptor)descriptor).getPsiElement());
      }
      return compareEntities(((RefElementNode)node1).getElement(), ((ProblemDescriptionNode)node2).getElement());
    }

    if (node2 instanceof RefElementNode && node1 instanceof ProblemDescriptionNode) {
      final CommonProblemDescriptor descriptor = ((ProblemDescriptionNode)node1).getDescriptor();
      if (descriptor instanceof ProblemDescriptor) {
        return -compareEntity(((RefElementNode)node2).getElement(), ((ProblemDescriptor)descriptor).getPsiElement());
      }
      return -compareEntities(((RefElementNode)node2).getElement(), ((ProblemDescriptionNode)node1).getElement());
    }

    return 0;
  }

  private static int compareEntity(final RefEntity entity, final PsiElement element) {
    if (entity instanceof RefElement) {
      return compareElements(((RefElement)entity).getElement(), element);
    }
    return -1;
  }

  private static int compareEntities(final RefEntity entity1, final RefEntity entity2) {
    if (entity1 instanceof RefElement && entity2 instanceof RefElement) {
      return compareElements(((RefElement)entity1).getElement(), ((RefElement)entity2).getElement());
    } else if (entity1 != null && entity2 != null) {
      return entity1.getName().compareToIgnoreCase(entity2.getName());
    }
    return 0;
  }

  private static int compareElements(final PsiElement element1, final PsiElement element2) {
    if (element1 != null && element2 != null) {
      final PsiFile psiFile1 = element1.getContainingFile();
      final PsiFile psiFile2 = element2.getContainingFile();
      if (Comparing.equal(psiFile1, psiFile2)){
        final TextRange textRange1 = element1.getTextRange();
        final TextRange textRange2 = element2.getTextRange();
        if (textRange1 != null && textRange2 != null) {
          return textRange1.getStartOffset() - textRange2.getStartOffset();
        }
      } else if (psiFile1 != null && psiFile2 != null){
        final String name1 = psiFile1.getName();
        LOG.assertTrue(name1 != null);
        final String name2 = psiFile2.getName();
        LOG.assertTrue(name2 != null);
        return name1.compareToIgnoreCase(name2);
      }
    }
    return 0;
  }

  private static int compareLineNumbers(final Object userObject, final OfflineProblemDescriptor descriptor) {
    if (userObject instanceof RefElement) {
      final RefElement refElement = (RefElement)userObject;
      final PsiElement psiElement = refElement.getElement();
      if (psiElement != null) {
        Document document = PsiDocumentManager.getInstance(psiElement.getProject()).getDocument(psiElement.getContainingFile());
        if (document != null) {
          return descriptor.getLine() - document.getLineNumber(psiElement.getTextOffset()) -1;
        }
      }
    }
    return -1;
  }

  public static InspectionResultsViewComparator getInstance() {
    if (ourInstance == null) {
      ourInstance = new InspectionResultsViewComparator();
    }

    return ourInstance;
  }
}
