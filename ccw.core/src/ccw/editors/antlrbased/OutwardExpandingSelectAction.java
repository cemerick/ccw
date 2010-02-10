package ccw.editors.antlrbased;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewerExtension5;
import org.eclipse.jface.text.source.ICharacterPairMatcher;
import org.eclipse.jface.text.source.ISourceViewer;

public class OutwardExpandingSelectAction extends Action {
    public final static String ID = "ClojureSelectToMatchingBracket"; //$NON-NLS-1$
    private final AntlrBasedClojureEditor editor;

    public OutwardExpandingSelectAction(AntlrBasedClojureEditor editor) {
        super(ClojureEditorMessages.SelectToMatchingBracket_label);
        Assert.isNotNull(editor);
        this.editor = editor;
        setEnabled(true);
    }

    @Override
    public void run() {
        selectToMatchingBracket();
    }

    private void selectToMatchingBracket() {
        ISourceViewer sourceViewer = editor.sourceViewer();
        IRegion selection = editor.getUnSignedSelection(sourceViewer);
        boolean previousSelectionExists = Math.abs(selection.getLength()) > 1;
        {
            int caretOffset = selection.getOffset();
            if (previousSelectionExists) {
                caretOffset = selection.getOffset() - 1;
            }
            int originalSelectionEnd = selection.getOffset() + selection.getLength();
            IRegion region = null;
            while (region == null && caretOffset >= 0) {
                region = editor.getPairsMatcher().match(editor.getDocument(), caretOffset);
                if (region != null) {
                    int newSelectionEnd = region.getOffset() + region.getLength();
                    if (newSelectionEnd < originalSelectionEnd) {
                        region = null;
                    }
                }
                if (region == null) {
                    caretOffset--;
                }
            }
            if (region == null) {
                String error = ClojureEditorMessages.GotoMatchingBracket_error_noMatchingBracket;
                showError(sourceViewer, error);
            } else {
                int offset = region.getOffset();
                int length = region.getLength();
                if (length >= 1) {
                    int anchor = editor.getPairsMatcher().getAnchor();
                    int targetOffset = ICharacterPairMatcher.RIGHT == anchor ? offset + 1 : offset + length;
                    if (visible(sourceViewer, targetOffset)) {
                        actualSelection(editor.getDocument(), sourceViewer, caretOffset, offset, length, anchor, targetOffset);
                    } else {
                        showError(sourceViewer, ClojureEditorMessages.GotoMatchingBracket_error_bracketOutsideSelectedElement);
                    }
                }
            }
        }
    }

    public void showError(ISourceViewer sourceViewer, String error) {
        editor.setStatusLineErrorMessage(error);
        sourceViewer.getTextWidget().getDisplay().beep();
    }

    public void actualSelection(IDocument document, ISourceViewer sourceViewer, int sourceCaretOffset, int offset, int length, int anchor, int targetOffset) {
        int distanceBetweenBrackets = sourceCaretOffset - targetOffset + offsetAdjustment(sourceCaretOffset, offset, length, anchor);
        int adjustedTargetOffset = targetOffset + targetOffsetAdjustment(anchor);
        if (distanceBetweenBrackets < 0) {
            adjustedTargetOffset = adjustedTargetOffset + distanceBetweenBrackets;
            distanceBetweenBrackets = Math.abs(distanceBetweenBrackets);
        }
        if (previousCharacterIsPound(document, adjustedTargetOffset)) {
            adjustedTargetOffset--;
            distanceBetweenBrackets++;
        }
        sourceViewer.setSelectedRange(adjustedTargetOffset, distanceBetweenBrackets);
        sourceViewer.revealRange(adjustedTargetOffset, distanceBetweenBrackets);
    }

    public boolean previousCharacterIsPound(IDocument document, int adjustedTargetOffset) {
        try {
            String previousCharacter = document.get(adjustedTargetOffset - 1, 1);
            return "#".equals(previousCharacter);
        } catch (BadLocationException e) {
            throw new RuntimeException(e);
        }
    }

    public int targetOffsetAdjustment(int anchor) {
        return ICharacterPairMatcher.RIGHT == anchor ? -1 : 0;
    }

    public int offsetAdjustment(int sourceCaretOffset, int offset, int length, int anchor) {
        switch (anchor) {
        case ICharacterPairMatcher.LEFT:
            if (offset != sourceCaretOffset) {
                return -1;
            }
            return 0;
        case ICharacterPairMatcher.RIGHT:
            if (offset + length != sourceCaretOffset) {
                return 2;
            }
            return 1;
        default:
            throw new IllegalArgumentException("anchor is not a valid value! " + anchor);
        }
    }

    public boolean visible(ISourceViewer sourceViewer, int targetOffset) {
        if (sourceViewer instanceof ITextViewerExtension5) {
            ITextViewerExtension5 extension = (ITextViewerExtension5) sourceViewer;
            return extension.modelOffset2WidgetOffset(targetOffset) > -1;
        } else {
            IRegion visibleRegion = sourceViewer.getVisibleRegion();
            return targetOffset >= visibleRegion.getOffset() && targetOffset <= visibleRegion.getOffset() + visibleRegion.getLength();
        }
    }
}