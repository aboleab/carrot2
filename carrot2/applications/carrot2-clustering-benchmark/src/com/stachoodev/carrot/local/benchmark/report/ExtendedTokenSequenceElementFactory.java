
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

package com.stachoodev.carrot.local.benchmark.report;

import org.dom4j.*;

import com.dawidweiss.carrot.core.local.linguistic.tokens.*;
import com.dawidweiss.carrot.util.common.*;

/**
 * Converts
 * {@link com.dawidweiss.carrot.core.local.linguistic.tokens.ExtendedTokenSequence}
 * s to XML elements.
 * 
 * @author Stanislaw Osinski
 * @version $Revision$
 */
public class ExtendedTokenSequenceElementFactory implements ElementFactory
{
    /*
     * (non-Javadoc)
     * 
     * @see com.stachoodev.carrot.local.benchmark.report.ElementFactory#createElement(java.lang.Object)
     */
    public Element createElement(Object object)
    {
        ExtendedTokenSequence extendedTokenSequence = (ExtendedTokenSequence) object;
        Element tokenSequenceElement = DocumentHelper
            .createElement("token-sequence");

        tokenSequenceElement.addElement("image").addText(
            extendedTokenSequence.toString());

        tokenSequenceElement.addElement("tf").addText(
            StringUtils.toString(new Double(extendedTokenSequence
                .getDoubleProperty(ExtendedToken.PROPERTY_TF, -1)), "#.##"));

        return tokenSequenceElement;
    }
}