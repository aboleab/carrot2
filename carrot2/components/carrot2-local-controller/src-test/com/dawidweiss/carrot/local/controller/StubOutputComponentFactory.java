
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

package com.dawidweiss.carrot.local.controller;

import com.dawidweiss.carrot.core.local.LocalComponent;
import com.dawidweiss.carrot.core.local.LocalComponentFactoryBase;

import java.util.*;


/**
 * A stub implementation of a component factory that produces output
 * components
 */
public class StubOutputComponentFactory extends LocalComponentFactoryBase {
    /* */
    private List createdInstances = new ArrayList();

    /* */
    public LocalComponent getInstance() {
        LocalComponent instance = new StubOutputComponent(Collections.EMPTY_SET,
                Collections.EMPTY_SET, Collections.EMPTY_SET);

        createdInstances.add(instance);

        return instance;
    }

    /* */
    public List getCreatedInstances() {
        return createdInstances;
    }
}