
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

package com.dawidweiss.carrot.controller.carrot2.xmlbinding;

import java.io.Reader;
import java.util.*;

import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

/**
 * Service descriptor deserializer.
 * 
 * @author Dawid Weiss
 */
public class Service {
    private List componentDescriptors;

    public Service(List componentDescriptors) {
        this.componentDescriptors = componentDescriptors;
    }

    public List getComponentDescriptors() {
        return componentDescriptors;
    }
    
    public static Service unmarshal(Reader stream) throws DocumentException {
        final SAXReader reader = new SAXReader();
        final Element root = reader.read(stream).getRootElement();

        final List componentDescriptors = new ArrayList();
        for (Iterator i = root.elements("component").iterator(); i.hasNext();) {
            final Element component = (Element) i.next();
            componentDescriptors.add(ComponentDescriptor.unmarshall(component));
        }
        
        return new Service(componentDescriptors);
    }
}