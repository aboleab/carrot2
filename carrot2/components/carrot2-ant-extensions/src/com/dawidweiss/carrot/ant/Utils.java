
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

package com.dawidweiss.carrot.ant;

import java.io.File;
import java.util.*;

import org.apache.tools.ant.*;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.util.FileUtils;

import com.dawidweiss.carrot.ant.deps.ComponentDependency;

/**
 * Utility methods that didn't fit anywhere else.
 *  
 * @author Dawid Weiss
 */
class Utils {

    /**
     * Converts all Path objects that point to directories to 
     * filesets including "*.dep.xml". 
     */
    public static LinkedList convertPathDependencies(Project prj, LinkedList deps) {
        LinkedList output = new LinkedList();
        FileUtils futils = FileUtils.newFileUtils();
        for (Iterator i = deps.iterator(); i.hasNext(); ) {
            Object x = i.next();
            if (x instanceof FileSet) {
                output.add(x);
            } else if (x instanceof Path) {
                Path p = (Path) x;
                String [] lst = p.list();
                for (int j=0;j<lst.length;j++) {
                    File f = new File( lst[j] );
                    if (!f.exists()) { 
                        prj.log("File does not exist and was removed from path: "
                                + f.getAbsolutePath(), Project.MSG_VERBOSE);
                        continue;
                    }
                    if (f.isFile()) {
                        // ok, it is a file. Add it immediately.
                        FileSet fs = new FileSet();
                        fs.setProject(prj);
                        fs.setDir(f.getParentFile());
                        fs.setIncludes(futils.removeLeadingPath(f.getParentFile(), f));
                        output.add(fs);
                    } else if (f.isDirectory()) {
                        // add a fileset to include all dependencies
                        FileSet fs = new FileSet();
                        fs.setProject(prj);
                        fs.setDir(f);
                        fs.setIncludes("**/*.dep.xml");
                        fs.setIncludes("*.dep.xml");
                        output.add(fs);
                    } else {
                        prj.log("Unknown file type: "
                                + f.getAbsolutePath(), Project.MSG_VERBOSE);
                        continue;
                    }
                }
            } else throw new BuildException("Unknown part of path: "
                    + x.getClass().getName());
        }
        return output;
    }

    public static void loadComponentDependencies(FileUtils futils, 
        Project project, List listOfFileSetsWithDependencies, 
        HashMap components)
        throws Exception
    {
        for (Iterator i = listOfFileSetsWithDependencies.iterator(); i.hasNext();) {
            FileSet fs = (FileSet) i.next();
            DirectoryScanner ds = fs.getDirectoryScanner(project);
            File fromDir = fs.getDir(project);
            String[] srcFiles = ds.getIncludedFiles();
    
            for (int j = 0; j < srcFiles.length; j++) {
                ComponentDependency dep = 
                    new ComponentDependency(
                        project, futils.resolveFile(fromDir, srcFiles[j]).getCanonicalFile());
    
                if (components.containsKey(dep.getName())) {
                    ComponentDependency existingDependency = (ComponentDependency) components.get(dep.getName());
                    if (!existingDependency.getFile().getCanonicalPath().equals(
                        dep.getFile().getCanonicalPath())) {
                        throw new BuildException("Component name duplicated: " +
                            dep.getFile() + " and " +
                            ((ComponentDependency) components.get(dep.getName())).getFile());
                    }
                    // just silently ignore -- it is the same file.
                } else {
                    components.put(dep.getName(), dep);
                }
            }
        }
    }
}