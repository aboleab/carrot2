
/*
 * Carrot2 project.
 *
 * Copyright (C) 2002-2006, Dawid Weiss, Stanisław Osiński.
 * Portions (C) Contributors listed in "carrot2.CONTRIBUTORS" file.
 * All rights reserved.
 *
 * Refer to the full license file "carrot2.LICENSE"
 * in the root folder of the repository checkout or at:
 * http://www.carrot2.org/carrot2.LICENSE
 */

package com.dawidweiss.carrot.lucene;

import com.dawidweiss.carrot.core.local.clustering.RawDocument;
import com.dawidweiss.carrot.core.local.clustering.RawDocumentBase;

/**
 * An adapter class that implements {@link RawDocument} used by most
 * Carrot2 components.
 *
 * @author Dawid Weiss
 * @version $Id$
 */
public class DocumentAdapter extends RawDocumentBase {

	private final Integer id;
	
	/**
	 * Creates a new document with the given id, title and summary.
	 */
	public DocumentAdapter(int id, String url, String title, String summary) {
        super(url, title, summary);

		this.id = new Integer(id);
	}
	
	/*
	 * @see com.dawidweiss.carrot.core.local.clustering.RawDocument#getId()
	 */
	public Object getId() {
		return id;
	}
}