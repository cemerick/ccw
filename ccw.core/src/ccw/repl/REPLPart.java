package ccw.repl;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.Document;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

import ccw.CCWPlugin;
import ccw.clojure.util.ClojurePlugin;
import ccw.editors.antlrbased.ClojureSourceViewer;
import ccw.editors.antlrbased.ClojureSourceViewerConfiguration;
import ccw.editors.rulesbased.ClojureDocumentProvider;

public class REPLPart extends ViewPart {
    private ClojureSourceViewer viewer;
    private ClojureSourceViewerConfiguration viewerConfig;
    
    public REPLPart () {
    }
    
    @Override
    public void createPartControl(Composite parent) {
        IPreferenceStore prefs = CCWPlugin.getDefault().getCombinedPreferenceStore();
        viewer = new ClojureSourceViewer(parent, null, null, false,
                SWT.V_SCROLL | SWT.H_SCROLL, prefs);
        viewerConfig = new ClojureSourceViewerConfiguration(prefs, viewer);
        viewer.configure(viewerConfig);
        getViewSite().setSelectionProvider(viewer);
        viewer.setDocument(ClojureDocumentProvider.configure(new Document()));
    }

    @Override
    public void setFocus() {
        viewer.getTextWidget().setFocus();
    }

    
}
