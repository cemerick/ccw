/*******************************************************************************
 * Copyright (c) 2009 Casey Marshall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: 
 *    Casey Marshall - initial API and implementation
 *******************************************************************************/
package ccw.editors.rulesbased;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.TextUtilities;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.ui.IStorageEditorInput;
import org.eclipse.ui.editors.text.FileDocumentProvider;

import ccw.StorageMarkerAnnotationModel;
import ccw.editors.antlrbased.ClojureSourceViewer;

public class ClojureDocumentProvider extends FileDocumentProvider {
    
    /**
     * Configures the given document to be useful for Clojure content.
     * Not necessary in an editor context (this is applied automatically
     * by this provider in that case).
     */
    public static IDocument configure (IDocument document) {
        IDocumentPartitioner partitioner = new ClojurePartitioner(new ClojurePartitionScanner(), 
                ClojurePartitionScanner.CLOJURE_CONTENT_TYPES);

        Map<String, IDocumentPartitioner> m = new HashMap<String, IDocumentPartitioner>();
        m.put(ClojurePartitionScanner.CLOJURE_PARTITIONING, partitioner);
        
        TextUtilities.addDocumentPartitioners(document, m);
        
        return document;
    }
    
    @Override
    protected IDocument createDocument(Object element) throws CoreException {
        IDocument document = super.createDocument(element);
        if (document != null) document = configure(document);
        
        return document;
    }

    @Override
    public IAnnotationModel getAnnotationModel(Object element) {
    	IAnnotationModel annotationModel = super.getAnnotationModel(element);
    	if (annotationModel != null) 
    		return annotationModel;
    	if (element instanceof IStorageEditorInput) {
			IStorageEditorInput input = (IStorageEditorInput) element;
			try {
				return new StorageMarkerAnnotationModel(input.getStorage());
			} catch (CoreException e) {
				throw new RuntimeException(e);
			}
		}
		return null;
    }
}
