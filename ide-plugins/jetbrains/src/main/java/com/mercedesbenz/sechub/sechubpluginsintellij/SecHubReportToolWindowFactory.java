package com.mercedesbenz.sechub.sechubpluginsintellij;

import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.openapi.wm.ex.ToolWindowEx;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.ui.jcef.JBCefBrowser;
import com.intellij.ui.jcef.JBCefBrowserBase;
import com.intellij.ui.jcef.JBCefCookie;
import com.intellij.ui.jcef.JBCefJSQuery;
import org.cef.browser.CefBrowser;
import org.cef.browser.CefFrame;
import org.cef.handler.CefLoadHandler;
import org.cef.network.CefCookie;
import org.cef.network.CefRequest;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


final class SecHubReportToolWindowFactory implements ToolWindowFactory {

    private static CefCookie secHubCookie;

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        String theme = "jetbrains";
        String projectId = "test";

        /* Create a WebView using JCEF */
        JBCefBrowser jbCefBrowser = new JBCefBrowser();

        CefBrowser cefBrowser = jbCefBrowser.getCefBrowser();

        /* Add a JS bridge to receive messages */

        JBCefJSQuery startScanQuery = JBCefJSQuery.create((JBCefBrowserBase) jbCefBrowser);

        startScanQuery.addHandler(data -> {
            // TODO: ZIP the content in memory ...
            // TODO: Use OpenAPI client to start scan ...
            ApplicationManager.getApplication().invokeLater(() -> Messages.showInfoMessage("Uploading project files...", "Start Scan"));
            return null;
        });

        JBCefJSQuery goToWebUi = JBCefJSQuery.create((JBCefBrowserBase) jbCefBrowser);

        goToWebUi.addHandler(data -> {
            BrowserUtil.browse("https://sechub-dev.app.corpintra.net:4443/index.html");
            return null;
        });

        JBCefJSQuery markFalsePositiveQuery = JBCefJSQuery.create((JBCefBrowserBase) jbCefBrowser);

        markFalsePositiveQuery.addHandler(data -> {
            ApplicationManager.getApplication().invokeLater(() -> Messages.showInfoMessage("Marking false positive for finding with id %s".formatted(data), "Mark False Positive"));
            // TODO: Use OpenAPI client to execute mark false postiive...
            return null;
        });

        JBCefJSQuery unmarkFalsePositiveQuery = JBCefJSQuery.create((JBCefBrowserBase) jbCefBrowser);

        unmarkFalsePositiveQuery.addHandler(data -> {
            ApplicationManager.getApplication().invokeLater(() -> Messages.showInfoMessage("Remove false positive for finding with id %s".formatted(data), "Remove False Positive"));
            // TODO: Use OpenAPI client to execute unmark false postiive...
            return null;
        });

        JBCefJSQuery jumpToLocationQuery = JBCefJSQuery.create((JBCefBrowserBase) jbCefBrowser);

        jumpToLocationQuery.addHandler(data -> {
            openFileAtLine(project, "/src/Useless.java", 69);
            return null;
        });

        jbCefBrowser.getJBCefClient().addLoadHandler(new CefLoadHandler() {
            @Override
            public void onLoadingStateChange(CefBrowser cefBrowser, boolean isLoading, boolean canGoBack, boolean canGoForward) {
                extractSecHubCookieAsync(jbCefBrowser).thenAccept(optCefCookie -> {
                    optCefCookie.ifPresentOrElse(cefCookie -> secHubCookie = cefCookie, () -> secHubCookie = null);
                });
            }

            @Override
            public void onLoadStart(CefBrowser cefBrowser, CefFrame cefFrame, CefRequest.TransitionType transitionType) {
                extractSecHubCookieAsync(jbCefBrowser).thenAccept(optCefCookie -> {
                    optCefCookie.ifPresentOrElse(cefCookie -> {
                        if (!cefBrowser.getURL().contains("/api/project")) {
                            // TODO: handle error cases like 401 or 403...
                            jbCefBrowser.loadURL("https://localhost:8443/api/project/%s/report?theme=%s".formatted(projectId, theme));
                        }
                    }, () -> {
                        if (!cefBrowser.getURL().contains("/login")) {
                            jbCefBrowser.loadURL("https://localhost:8443/login?theme=%s&redirectUri=/api/project/%s/report".formatted(theme, projectId));
                        }
                    });
                });
            }

            @Override
            public void onLoadEnd(CefBrowser cefBrowser, CefFrame cefFrame, int httpStatusCode) {
                cefBrowser.executeJavaScript("window.addEventListener('START_SCAN', function(event) { " + startScanQuery.inject("event") + " });", cefBrowser.getURL(), 0);
                cefBrowser.executeJavaScript("window.addEventListener('GO_TO_WEB_UI', function(event) { " + goToWebUi.inject("event") + " });", cefBrowser.getURL(), 0);
                cefBrowser.executeJavaScript("window.addEventListener('MARK_FALSE_POSITIVE', function(event) { " + markFalsePositiveQuery.inject("event.detail.findingId") + " });", cefBrowser.getURL(), 0);
                cefBrowser.executeJavaScript("window.addEventListener('UNMARK_FALSE_POSITIVE', function(event) { " + unmarkFalsePositiveQuery.inject("event.detail.findingId") + " });", cefBrowser.getURL(), 0);
                cefBrowser.executeJavaScript("window.addEventListener('JUMP_TO_LOCATION', function(event) { " + jumpToLocationQuery.inject("event.detail") + " });", cefBrowser.getURL(), 0);
            }

            @Override
            public void onLoadError(CefBrowser cefBrowser, CefFrame cefFrame, ErrorCode errorCode, String errorText, String failedUrl) {}
        }, cefBrowser);

        /* Wrap it in a content panel */
        ContentFactory contentFactory = ContentFactory.getInstance();
        Content content = contentFactory.createContent(jbCefBrowser.getComponent(), "WebView", false);

        /* Add to ToolWindow */
        toolWindow.getContentManager().addContent(content);

        /* Set initial width */
        ToolWindowManager instance = ToolWindowManager.getInstance(project);
        ToolWindowEx tw = (ToolWindowEx) instance.getToolWindow("com.mercedesbenz.sechub.sechubpluginsintellij.SecHubReportToolWindowFactory");
        tw.stretchWidth((int)(Toolkit.getDefaultToolkit().getScreenSize().width * 0.3));

        /* Add a Chrome DevTools WebView */
        CefBrowser devTools = cefBrowser.getDevTools();
        JBCefBrowser devToolsBrowser = JBCefBrowser.createBuilder()
                .setCefBrowser(devTools)
                .setClient(jbCefBrowser.getJBCefClient())
                .build();

        Content content2 = contentFactory.createContent(devToolsBrowser.getComponent(), "WebViewDevTools", false);

        toolWindow.getContentManager().addContent(content2);

        /* Trigger browser initialization using dummy content */
        String dummyContent = "<html><head><title></title></head><body></body></html>";
        jbCefBrowser.loadHTML(dummyContent);
    }

    /**
     * Extracts the SecHub cookie from the browser asynchronously.
     * This method is designed to run asynchronously because the main thread is responsible for initializing the browser,
     * and {@link com.intellij.ui.jcef.JBCefBrowser#getJBCefCookieManager()} can only retrieve cookies once the browser is ready.
     * <p>
     * The method returns a {@link java.util.concurrent.CompletableFuture} containing an {@link java.util.Optional} that may hold
     * the found cookie or be empty if no SecHub-related cookie is present.
     * </p>
     *
     * @param jbCefBrowser the browser instance
     * @return a {@link java.util.concurrent.CompletableFuture} containing an {@link java.util.Optional} of the SecHub cookie if found,
     *         or an empty {@link java.util.Optional} if the cookie is not present.
     */
    private static CompletableFuture<Optional<CefCookie>> extractSecHubCookieAsync(JBCefBrowser jbCefBrowser) {
        CompletableFuture<Optional<CefCookie>> completableFuture = new CompletableFuture<>();

        completableFuture.completeAsync(() -> {
            try {
                Future<List<JBCefCookie>> future = jbCefBrowser.getJBCefCookieManager().getCookies(null, true);
                List<JBCefCookie> jbCefCookies = future.get(5L, TimeUnit.SECONDS);
                for (JBCefCookie jbCefCookie : jbCefCookies) {
                    CefCookie cefCookie = jbCefCookie.getCefCookie();
                    if ("SECHUB_CLASSIC_AUTH_CREDENTIALS".equals(cefCookie.name) || "SECHUB_OAUTH2_ACCESS_TOKEN".equals(cefCookie.name)) {
                        return Optional.of(cefCookie);
                    }
                }
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                return Optional.empty();
            }
            return Optional.empty();
        });

        return completableFuture;
    }

    private static void openFileAtLine(Project project, String filePath, int line) {
        ApplicationManager.getApplication().invokeLater(() -> {
            VirtualFile virtualFile = project.getBaseDir().findFileByRelativePath(filePath);
            if (virtualFile != null) {
                FileEditorManager fileEditorManager = FileEditorManager.getInstance(project);

                fileEditorManager.openFile(virtualFile, true);

                Editor editor = fileEditorManager.getSelectedTextEditor();
                if (editor != null) {
                    editor.getCaretModel().moveToOffset(editor.getDocument().getLineStartOffset(line - 1));
                    editor.getScrollingModel().scrollToCaret(com.intellij.openapi.editor.ScrollType.CENTER);
                }
            } else {
                Messages.showErrorDialog(project, "File not found: " + filePath, "Error");
            }
        });
    }
}