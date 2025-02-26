// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub;

import java.io.File;
import java.net.URL;

import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.osgi.framework.Bundle;

public class EclipseUtil {

	private static final IProgressMonitor NULL_MONITOR = new NullProgressMonitor();

	public static IFile toIFileOrNull(java.nio.file.Path path) {
		if (path == null) {
			return null;
		}
		return toIFileOrNull(path.toFile());
	}

	/**
	 * Get image by path from image registry. If not already registered a new image
	 * will be created and registered. If not createable a fallback image is used
	 * instead
	 * 
	 * @param path
	 * @param pluginId - plugin id to identify which plugin image should be loaded
	 * @return image
	 */
	public static Image getImage(String path, String pluginId) {
		ImageRegistry imageRegistry = getImageRegistry();
		if (imageRegistry == null) {
			return null;
		}
		Image image = imageRegistry.get(path);
		if (image == null) {
			ImageDescriptor imageDesc = createImageDescriptor(path, pluginId);
			image = imageDesc.createImage();
			if (image == null) {
				image = PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJS_ERROR_TSK);
			}
			imageRegistry.put(path, image);
		}
		return image;
	}
	
	public static ImageDescriptor createImageDescriptor(String path) {
		return createImageDescriptor(path, SecHubActivator.PLUGIN_ID);
	}

	public static ImageDescriptor createImageDescriptor(String path, String pluginId) {
		if (path == null) {
			/* fall back if path null , so avoid NPE in eclipse framework */
			return ImageDescriptor.getMissingImageDescriptor();
		}
		if (pluginId == null) {
			/* fall back if pluginId null , so avoid NPE in eclipse framework */
			return ImageDescriptor.getMissingImageDescriptor();
		}
		Bundle bundle = Platform.getBundle(pluginId);
		if (bundle == null) {
			/*
			 * fall back if bundle not available, so avoid NPE in eclipse framework
			 */
			return ImageDescriptor.getMissingImageDescriptor();
		}
		URL url = FileLocator.find(bundle, new Path(path), null);

		ImageDescriptor imageDesc = ImageDescriptor.createFromURL(url);
		return imageDesc;
	}

	/**
	 * Returns active workbench shell - or <code>null</code>
	 * 
	 * @return active workbench shell - or <code>null</code>
	 */
	public static Shell getActiveWorkbenchShell() {
		IWorkbench workbench = getWorkbench();
		if (workbench == null) {
			return null;
		}
		IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
		if (window == null) {
			return null;
		}
		Shell shell = window.getShell();
		return shell;
	}

	public static IWorkbenchWindow getActiveWorkbenchWindow() {
		IWorkbench workbench = getWorkbench();
		if (workbench == null) {
			return null;
		}
		IWorkbenchWindow workbenchWindow = workbench.getActiveWorkbenchWindow();

		if (workbenchWindow != null) {
			return workbenchWindow;
		}
		/* fall back - try to execute in UI */
		WorkbenchWindowRunnable wwr = new WorkbenchWindowRunnable();
		getSafeDisplay().syncExec(wwr);
		return wwr.workbenchWindowFromUI;
	}

	/**
	 * Returns workbench or <code>null</code>
	 * 
	 * @return workbench or <code>null</code>
	 */
	private static IWorkbench getWorkbench() {
		if (!PlatformUI.isWorkbenchRunning()) {
			return null;
		}
		IWorkbench workbench = PlatformUI.getWorkbench();
		return workbench;
	}

	private static class WorkbenchWindowRunnable implements Runnable {
		IWorkbenchWindow workbenchWindowFromUI;

		@Override
		public void run() {
			IWorkbench workbench = getWorkbench();
			if (workbench == null) {
				return;
			}
			workbenchWindowFromUI = workbench.getActiveWorkbenchWindow();
		}

	}

	public static File toFileOrNull(IPath path) throws CoreException {
		if (path == null) {
			return null;
		}
		IFileStore fileStore = FileBuffers.getFileStoreAtLocation(path);

		File file = null;
		file = fileStore.toLocalFile(EFS.NONE, NULL_MONITOR);
		return file;
	}

	public static File toFileOrNull(IResource resource) throws CoreException {
		if (resource == null) {
			return null;
		}
		return toFileOrNull(resource.getLocation());
	}

	public static IFile toIFileOrNull(File file) {
		IFileStore fileStore = EFS.getLocalFileSystem().getStore(file.toURI());

		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IFile[] fileResults = workspace.getRoot().findFilesForLocationURI(fileStore.toURI());
		if (fileResults == null || fileResults.length == 0) {
			return null;
		}
		return fileResults[0];
	}

	public static Display getSafeDisplay() {
		Display display = Display.getCurrent();
		if (display == null) {
			display = Display.getDefault();
		}
		return display;
	}

	public static void safeAsyncExec(Runnable runnable) {
		getSafeDisplay().asyncExec(runnable);
	}

	/**
	 * Returns active page or <code>null</code>
	 * 
	 * @return active page or <code>null</code>
	 */
	public static IWorkbenchPage getActivePage() {
		if (!PlatformUI.isWorkbenchRunning()) {
			return null;
		}
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if (window != null) {
			return window.getActivePage();
		}
		final EclipseSubContext subContext = new EclipseSubContext();
		getSafeDisplay().syncExec(() -> {
			subContext.window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
			if (subContext.window != null) {
				subContext.activePage = subContext.window.getActivePage();
			}
		});
		return subContext.activePage;
	}

	private static class EclipseSubContext {
		IWorkbenchWindow window;
		IWorkbenchPage activePage;
	}

	public static ImageDescriptor createDescriptor(String path) {
		URL url = FileLocator.find(Platform.getBundle(SecHubActivator.PLUGIN_ID), new Path(path), null);

		if (url == null) {
			return null;
		}
		return ImageDescriptor.createFromURL(url);

	}

	public static Image getImage(String path) {
		ImageRegistry imageRegistry = getImageRegistry();
		if (imageRegistry == null) {
			return null;
		}
		Image image = imageRegistry.get(path);
		if (image == null) {
			ImageDescriptor imageDesc = createDescriptor(path);
			image = imageDesc.createImage();
			if (image == null) {
				image = getSharedImage(ISharedImages.IMG_OBJS_ERROR_TSK);
			}
			imageRegistry.put(path, image);
		}
		return image;
	}

	public static Image getSharedImage(String symbolicName) {
		return PlatformUI.getWorkbench().getSharedImages().getImage(symbolicName);
	}

	public static ImageDescriptor getSharedImageDescriptor(String symbolicName) {
		return PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(symbolicName);
	}

	private static ImageRegistry getImageRegistry() {
		return SecHubActivator.getDefault().getImageRegistry();
	}

	public static IStatus createErrorStatus(String message) {
		return createErrorStatus(message, null);
	}

	public static IStatus createErrorStatus(String message, Throwable throwable) {
		return new Status(IStatus.ERROR, SecHubActivator.PLUGIN_ID, message, throwable);
	}
}
