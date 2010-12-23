package org.carrot2.workbench.vis;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.carrot2.core.Cluster;
import org.carrot2.core.Document;
import org.carrot2.core.ProcessingResult;
import org.carrot2.workbench.core.WorkbenchCorePlugin;
import org.carrot2.workbench.core.helpers.PostponableJob;
import org.carrot2.workbench.core.helpers.Utils;
import org.carrot2.workbench.core.ui.SearchEditor;
import org.carrot2.workbench.core.ui.SearchEditorSelectionProvider;
import org.carrot2.workbench.core.ui.SearchResultListenerAdapter;
import org.eclipse.core.runtime.IAdapterManager;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.BrowserFunction;
import org.eclipse.swt.browser.ProgressAdapter;
import org.eclipse.swt.browser.ProgressEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.browser.IWorkbenchBrowserSupport;
import org.eclipse.ui.part.Page;
import org.eclipse.ui.progress.UIJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

public abstract class FlashViewPage extends Page
{
    /**
     * Delay between the update event and refreshing the browser view.
     */
    protected static final int BROWSER_REFRESH_DELAY = 750;

    /**
     * Delay between the selection event and refreshing the browser view.
     */
    protected static final int BROWSER_SELECTION_DELAY = 250;

    /**
     * The editor associated with this page.
     */
    public final SearchEditor editor;

    /**
     * Internal HTML browser.
     */
    private Browser browser;

    /**
     * A flag indicating the browser's applet has finished loading.
     */
    private volatile boolean browserInitialized;

    /**
     * Visualization entry page URI.
     */
    private final String entryPageUri;

    /**
     * Unique ID associated with this page's editor.
     */
    private final int id;

    /**
     * This visualization's logger.
     */
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * Browser refresh job. Postponed a bit to make the user interface more responsive.
     */
    private PostponableJob refreshJob = new PostponableJob(new UIJob(
        "Browser (refresh)...")
    {
        public IStatus runInUIThread(IProgressMonitor monitor)
        {
            return doRefresh();
        }
    });

    /**
     * Selection refresh job.
     */
    private PostponableJob selectionJob = new PostponableJob(new UIJob(
        "Browser (selection)...")
    {
        public IStatus runInUIThread(IProgressMonitor monitor)
        {
            return doSelectionRefresh();
        }
    });

    /**
     * Sync with search result updated event.
     */
    private final SearchResultListenerAdapter editorSyncListener = new SearchResultListenerAdapter()
    {
        public void processingResultUpdated(ProcessingResult result)
        {
            refreshJob.reschedule(BROWSER_REFRESH_DELAY);
        }
    };

    /**
     * Editor selection listener.
     */
    private final ISelectionChangedListener selectionListener = new ISelectionChangedListener()
    {
        /* */
        public void selectionChanged(SelectionChangedEvent event)
        {
            final ISelection selection = event.getSelection();
            if (selection != null && selection instanceof IStructuredSelection)
            {
                final IStructuredSelection ss = (IStructuredSelection) selection;
                logger.debug("Selection, editor->visualization: " + ss);

                final IAdapterManager mgr = Platform.getAdapterManager();
                final ArrayList<Cluster> selectedGroups = Lists.newArrayList();

                final Object [] selected = ss.toArray();
                for (Object ob : selected)
                {
                    final Cluster cluster = (Cluster) mgr.getAdapter(ob, Cluster.class);

                    if (cluster != null) selectedGroups.add(cluster);
                }

                selectionJob.reschedule(BROWSER_SELECTION_DELAY);
            }
        }
    };

    /*
     * 
     */
    public FlashViewPage(SearchEditor editor, String entryPageUri)
    {
        this.entryPageUri = entryPageUri;
        this.editor = editor;
        this.id = Activator.getInstance().registerEditor(editor);
    }

    /*
     * 
     */
    protected IStatus doSelectionRefresh()
    {
        final IStructuredSelection sel = (IStructuredSelection) editor.getSite()
            .getSelectionProvider().getSelection();

        @SuppressWarnings("unchecked")
        final List<Cluster> selected = (List<Cluster>) sel.toList();

        if (browser.isDisposed())
        {
            return Status.OK_STATUS;
        }

        browser.execute("javascript:clearSelection();");
        for (Cluster cluster : selected)
        {
            browser.execute("javascript:selectGroupById(" + cluster.getId() + ", true);");
        }

        return Status.OK_STATUS;
    }

    /**
     * @see #refreshJob
     */
    protected IStatus doRefresh()
    {
        // If there is no search result, quit. Search result listener will reschedule.
        if (getProcessingResult() == null)
        {
            // No search result yet.
            return Status.OK_STATUS;
        }

        // If browser disposed, quit.
        if (browser.isDisposed())
        {
            return Status.OK_STATUS;
        }

        // TODO: Workaround for http://issues.carrot2.org/browse/CARROT-546
        // Instead of calling external interface's reload function, reload the entire URL.

        final Activator plugin = Activator.getInstance();
        final String refreshURL = plugin.getFullURL(entryPageUri) + "?page=" + getId();
        browserInitialized = false;
        browser.setUrl(refreshURL);

        /*
        // If the page has not finished loading, reschedule.
        if (!browserInitialized)
        {
            this.schedule(BROWSER_REFRESH_DELAY);
            return Status.OK_STATUS;
        }

        final String refreshURL = plugin.getFullURL("/servlets/pull?page=" + getId()); 
        org.slf4j.LoggerFactory.getLogger("browser").info("Refreshing: " + refreshURL);
        try
        {
            Object out = browser.evaluate("javascript:loadDataFromURL('" + refreshURL + "')");
            org.slf4j.LoggerFactory.getLogger("browser").info("Out: " + out);
        }
        catch (SWTException e)
        {
            org.slf4j.LoggerFactory.getLogger("browser").info("Err: ", e);
        }
        */

        return Status.OK_STATUS;
    }

    private static void openURL(String location)
    {
        try
        {
            WorkbenchCorePlugin
                .getDefault().getWorkbench().getBrowserSupport()
                .createBrowser(
                    IWorkbenchBrowserSupport.AS_EDITOR |
                    IWorkbenchBrowserSupport.LOCATION_BAR |
                    IWorkbenchBrowserSupport.NAVIGATION_BAR |
                    IWorkbenchBrowserSupport.STATUS, null, null, null)
                .openURL(new URL(location));
        }
        catch (Exception e)
        {
            Utils.logError("Couldn't open internal browser", e, false);
        }
    }

    @Override
    public void createControl(Composite parent)
    {
        /*
         * Open the browser and redirect it to the internal HTTP server.
         */
        browser = new Browser(parent, SWT.NONE);
        browser.addProgressListener(new ProgressAdapter()
        {
            public void completed(ProgressEvent event)
            {
                // When the page loads, try to refresh clusters immediately.
                browserInitialized = true;

                // TODO: Uncomment when fixed: http://issues.carrot2.org/browse/CARROT-546
                // refreshJob.reschedule(0);
            }
        });
    
        // TODO: Workaround for: http://issues.carrot2.org/browse/CARROT-546
        // browser.setUrl(refreshURL);
        if (getProcessingResult() != null)
        {
            refreshJob.reschedule(BROWSER_REFRESH_DELAY);
        }

        /*
         * Register custom callback functions.
         */
        new BrowserFunction(browser, "swt_groupClicked")
        {
            public Object function(Object [] arguments)
            {
                if (!browserInitialized) return null;

                // clusterId, isSelected, docList
                if (arguments.length == 3)
                {
                    final int groupId = (int) Double.parseDouble(arguments[0].toString());
                    doGroupSelection(groupId,
                        Boolean.parseBoolean(arguments[1].toString()));
                }

                return null;
            }
        };

        new BrowserFunction(browser, "swt_documentClicked")
        {
            public Object function(Object [] arguments)
            {
                if (!browserInitialized) return null;

                if (arguments.length == 1)
                {
                    final int documentId = (int) Double.parseDouble(arguments[0]
                        .toString());
                    doDocumentSelection(documentId);
                }

                return null;
            }
        };

        new BrowserFunction(browser, "swt_onModelChanged")
        {
            public Object function(Object [] arguments)
            {
                if (!browserInitialized) return null;
                selectionJob.reschedule(BROWSER_SELECTION_DELAY);
                return null;
            }
        };
    
        editor.getSearchResult().addListener(editorSyncListener);
        editor.getSite().getSelectionProvider().addSelectionChangedListener(
            selectionListener);
    }

    @Override
    public Control getControl()
    {
        return browser;
    }

    @Override
    public void setFocus()
    {
        browser.setFocus();
    }

    @Override
    public void dispose()
    {
        editor.getSearchResult().removeListener(editorSyncListener);
        editor.getSite().getSelectionProvider()
            .removeSelectionChangedListener(selectionListener);
        browser.dispose();

        super.dispose();
    }

    public int getId()
    {
        return id;
    }

    private void doGroupSelection(int groupId, boolean selected)
    {
        logger.debug("Selection visualization->editor: " + groupId + " " + selected);

        SearchEditorSelectionProvider prov = (SearchEditorSelectionProvider) editor
            .getSite().getSelectionProvider();

        prov.toggleSelected(groupId, selected, selectionListener);
    }

    private void doDocumentSelection(int documentId)
    {
        final ProcessingResult pr = getProcessingResult();
        if (pr == null) return;

        for (Document d : pr.getDocuments())
        {
            if (documentId == d.getId())
            {
                final String url = d.getField(Document.CONTENT_URL);
                if (!StringUtils.isEmpty(url))
                {
                    openURL(url);
                }
            }
        }
    }

    /**
     * Returns the current processing result (must be called from the GUI thread).
     */
    private ProcessingResult getProcessingResult()
    {
        assert Display.getCurrent() != null;

        final ProcessingResult pr = editor.getSearchResult().getProcessingResult();
        if (pr == null || pr.getClusters() == null) return null;

        return pr;
    }
}