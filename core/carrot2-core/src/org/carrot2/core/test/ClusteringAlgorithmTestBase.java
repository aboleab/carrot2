/**
 *
 */
package org.carrot2.core.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.*;

import org.carrot2.core.*;
import org.carrot2.core.attribute.AttributeNames;
import org.junit.Test;

/**
 * Simple baseline tests that apply to all clustering algorithms.
 */
public abstract class ClusteringAlgorithmTestBase<T extends ClusteringAlgorithm> extends
    ProcessingComponentTestBase<T>
{
    @Test
    public void testNoDocuments()
    {
        final Collection<Cluster> clusters = cluster(Collections.<Document> emptyList());

        assertNotNull(clusters);
        assertEquals(0, clusters.size());
    }

    /**
     * Performs clustering using {@link #simpleController}.
     * 
     * @param documents documents to be clustered
     * @return clustering results
     */
    public Collection<Cluster> cluster(Collection<Document> documents)
    {
        // A little hacky, but looks like the simplest way to ensure a single
        // initialization per one test case
        if (!initAttributes.isEmpty())
        {
            simpleController.init(initAttributes);
            initAttributes.clear();
        }

        processingAttributes.put(AttributeNames.DOCUMENTS, documents);
        return simpleController.process(processingAttributes, getComponentClass())
            .getClusters();
    }

    /**
     * Recursively collects documents from clusters.
     */
    public Collection<Document> collectDocuments(Collection<Cluster> clusters)
    {
        return collectDocuments(clusters, new HashSet<Document>());
    }

    private Collection<Document> collectDocuments(Collection<Cluster> clusters,
        Collection<Document> documents)
    {
        for (final Cluster cluster : clusters)
        {
            documents.addAll(cluster.getDocuments());
            collectDocuments(cluster.getSubclusters());
        }

        return documents;
    }
}
