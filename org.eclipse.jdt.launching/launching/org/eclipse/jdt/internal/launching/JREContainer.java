/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.launching;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.IVMInstallChangedListener;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.LibraryLocation;
import org.eclipse.jdt.launching.PropertyChangeEvent;

/** 
 * JRE Container - resolves a classpath container variable to a JRE
 */
public class JREContainer implements IClasspathContainer {

	/**
	 * Corresponding JRE
	 */
	private IVMInstall fVMInstall = null;
	
	/**
	 * Container path used to resolve to this JRE
	 */
	private IPath fPath = null;
	
	/**
	 * Cache of classpath entries per VM install. Cleared when a VM changes.
	 */
	private static Map fgClasspathEntries = null;
	
	/**
	 * Returns the classpath entries associated with the given VM.
	 * 
	 * @param vm
	 * @return classpath entries
	 */
	private static IClasspathEntry[] getClasspathEntries(IVMInstall vm) {
		if (fgClasspathEntries == null) {
			fgClasspathEntries = new HashMap(10);
			// add a listener to clear cached value when a VM changes or is removed
			IVMInstallChangedListener listener = new IVMInstallChangedListener() {
				public void defaultVMInstallChanged(IVMInstall previous, IVMInstall current) {
				}

				public void vmChanged(PropertyChangeEvent event) {
					if (event.getSource() != null) {
						fgClasspathEntries.remove(event.getSource());
					}
				}

				public void vmAdded(IVMInstall vm) {
				}

				public void vmRemoved(IVMInstall vm) {
					fgClasspathEntries.remove(vm);
				}
			};
			JavaRuntime.addVMInstallChangedListener(listener);
		}
		IClasspathEntry[] entries = (IClasspathEntry[])fgClasspathEntries.get(vm);
		if (entries == null) {
			entries = computeClasspathEntries(vm);
			fgClasspathEntries.put(vm, entries);
		}
		return entries;
	}
	
	/**
	 * Computes the classpath entries associated with a VM - one entry per library.
	 * 
	 * @param vm
	 * @return classpath entries
	 */
	private static IClasspathEntry[] computeClasspathEntries(IVMInstall vm) {
		LibraryLocation[] libs = JavaRuntime.getLibraryLocations(vm);
		List entries = new ArrayList(libs.length);
		for (int i = 0; i < libs.length; i++) {
			if (!libs[i].getSystemLibraryPath().isEmpty()) {
				entries.add(JavaCore.newLibraryEntry(libs[i].getSystemLibraryPath(), libs[i].getSystemLibrarySourcePath(), libs[i].getPackageRootPath()));
			}
		}
		return (IClasspathEntry[])entries.toArray(new IClasspathEntry[entries.size()]);		
	}
	
	/**
	 * Constructs a JRE classpath conatiner on the given VM install
	 * 
	 * @param vm vm install - cannot be <code>null</code>
	 * @param path container path used to resolve this JRE
	 */
	public JREContainer(IVMInstall vm, IPath path) {
		fVMInstall = vm;
		fPath = path;
	}
	
	/**
	 * @see IClasspathContainer#getClasspathEntries()
	 */
	public IClasspathEntry[] getClasspathEntries() {
		return getClasspathEntries(fVMInstall);
	}

	/**
	 * @see IClasspathContainer#getDescription()
	 */
	public String getDescription() {
		StringBuffer desc = new StringBuffer(LaunchingMessages.getString("JREContainer.JRE_System_Library_1")); //$NON-NLS-1$
		desc.append(" ["); //$NON-NLS-1$
		desc.append(fVMInstall.getName());
		desc.append("]"); //$NON-NLS-1$
		return desc.toString();
	}

	/**
	 * @see IClasspathContainer#getKind()
	 */
	public int getKind() {
		return IClasspathContainer.K_DEFAULT_SYSTEM;
	}

	/**
	 * @see IClasspathContainer#getPath()
	 */
	public IPath getPath() {
		return fPath;
	}

}
