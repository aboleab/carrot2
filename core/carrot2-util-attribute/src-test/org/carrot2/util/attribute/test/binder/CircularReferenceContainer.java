
/*
 * Carrot2 project.
 *
 * Copyright (C) 2002-2012, Dawid Weiss, Stanisław Osiński.
 * All rights reserved.
 *
 * Refer to the full license file "carrot2.LICENSE"
 * in the root folder of the repository checkout or at:
 * http://www.carrot2.org/carrot2.LICENSE
 */

package org.carrot2.util.attribute.test.binder;

import org.carrot2.util.attribute.*;

/**
 *
 */
@Bindable
public class CircularReferenceContainer
{
    /**
     * Circular reference.
     */
    @TestProcessing
    @Input
    @Attribute
    public CircularReferenceContainer circular;
}
