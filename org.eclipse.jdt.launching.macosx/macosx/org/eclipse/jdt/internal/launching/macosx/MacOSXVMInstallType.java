/*
 * (c) Copyright IBM Corp. 2002, 2003.
 * All Rights Reserved.
 */
package org.eclipse.jdt.internal.launching.macosx;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import org.eclipse.jdt.internal.launching.LibraryInfo;
import org.eclipse.jdt.internal.launching.StandardVMType;

import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.VMStandin;

/**
 * This plugins into the org.eclipse.jdt.launching.vmInstallTypes extension point
 */
public class MacOSXVMInstallType extends StandardVMType {
	
	/*
	 * The directory structure for Java VMs is as follows:
	 * 	/System/Library/Frameworks/JavaVM.framework/Versions/
	 * 		1.3.1
	 * 			Classes
	 * 			Home
	 * 		1.4.1
	 * 			Classes
	 * 			Home
	 * 		CurrentJDK -> 1.3.1
	 */
	 
	private static final String JAVA_VM_NAME= "Java HotSpot(TM) Client VM";	//$NON-NLS-1$
	
	/** The OS keeps all the JVM versions in this directory */
	private static final String JVM_VERSION_LOC= "/System/Library/Frameworks/JavaVM.framework/Versions/";	//$NON-NLS-1$
	/** The name of a Unix link to MacOS X's default VM */
	private static final String CURRENT_JVM= "CurrentJDK";	//$NON-NLS-1$
	/** The root of a JVM */
	private static final String JVM_ROOT= "Home";	//$NON-NLS-1$
	/** The doc (for all JVMs) lives here (if the developer kit has been expanded)*/
	private static final String JAVADOC_LOC= "/Developer/Documentation/Java/Reference/";	//$NON-NLS-1$
		
				
	public String getName() {
		return MacOSXLaunchingPlugin.getString("MacOSXVMType.name"); //$NON-NLS-1$
	}
	
	public IVMInstall doCreateVMInstall(String id) {
		return new MacOSXVMInstall(this, id);
	}
			
	/**
	 * @see IVMInstallType#detectInstallLocation()
	 */
	public File detectInstallLocation() {
		
		String javaVMName= System.getProperty("java.vm.name");	//$NON-NLS-1$
		if (javaVMName == null || !JAVA_VM_NAME.equals(javaVMName)) 
			return null;

		// find all installed VMs
		File defaultLocation= null;
		File versionDir= new File(JVM_VERSION_LOC);
		if (versionDir.exists() && versionDir.isDirectory()) {
			File currentJDK= new File(versionDir, CURRENT_JVM);
			try {
				currentJDK= currentJDK.getCanonicalFile();
			} catch (IOException ex) {
			}
			File[] versions= versionDir.listFiles();
			for (int i= 0; i < versions.length; i++) {
				String version= versions[i].getName();
				File home=  new File(versions[i], JVM_ROOT);
				if (home.exists() && findVMInstall(version) == null && !CURRENT_JVM.equals(version)) {
					
					boolean isDefault= currentJDK.equals(versions[i]);
					
					VMStandin vm= new VMStandin(this, version);
					vm.setInstallLocation(home);
					String format= MacOSXLaunchingPlugin.getString(isDefault
												? "MacOSXVMType.jvmDefaultName"		//$NON-NLS-1$
												: "MacOSXVMType.jvmName");				//$NON-NLS-1$
					vm.setName(MessageFormat.format(format, new Object[] { version } ));
					vm.setLibraryLocations(getDefaultLibraryLocations(home));
					URL doc= getDefaultJavadocLocation(home);
					if (doc != null)
						vm.setJavadocLocation(doc);
					
					IVMInstall rvm= vm.convertToRealVM();
					
					if (isDefault) {
						defaultLocation= home;
						try {
							JavaRuntime.setDefaultVMInstall(rvm, null);
						} catch (CoreException e) {
							// exception intentionally ignored
						}
					}
				}
			}
		}
		return defaultLocation;
	}

	/**
	 * Returns default library info for the given install location.
	 * 
	 * @param installLocation
	 * @return LibraryInfo
	 */
	protected LibraryInfo getDefaultLibraryInfo(File installLocation) {

		File classes = new File(installLocation, "../Classes"); //$NON-NLS-1$
		File lib1= new File(classes, "classes.jar"); //$NON-NLS-1$
		File lib2= new File(classes, "ui.jar"); //$NON-NLS-1$
		
		String[] libs = new String[] { lib1.toString(),lib2.toString() };
		
		File lib = new File(installLocation, "lib"); //$NON-NLS-1$
		File extDir = new File(lib, "ext"); //$NON-NLS-1$
		String[] dirs = null;
		if (extDir == null)
			dirs = new String[0];
		else
			dirs = new String[] {extDir.getAbsolutePath()};

		File endDir = new File(lib, "endorsed"); //$NON-NLS-1$
		String[] endDirs = null;
		if (endDir == null)
			endDirs = new String[0]; 
		else
			endDirs = new String[] {endDir.getAbsolutePath()};
		
		return new LibraryInfo("???", libs, dirs, endDirs);		 //$NON-NLS-1$
	}
	
	/**
	 * @see org.eclipse.jdt.launching.IVMInstallType#validateInstallLocation(java.io.File)
	 */
	public IStatus validateInstallLocation(File javaHome) {
		/*
		IStatus status = null;
		File javaExecutable = findJavaExecutable(javaHome);
		if (javaExecutable == null) {
			status = new Status(IStatus.ERROR, LaunchingPlugin.getUniqueIdentifier(), 0, LaunchingMessages.getString("StandardVMType.Not_a_JDK_Root;_Java_executable_was_not_found_1"), null); //$NON-NLS-1$			
		} else {
			if (canDetectDefaultSystemLibraries(javaHome, javaExecutable)) {
				status = new Status(IStatus.OK, LaunchingPlugin.getUniqueIdentifier(), 0, LaunchingMessages.getString("StandardVMType.ok_2"), null); //$NON-NLS-1$
			} else {
				status = new Status(IStatus.ERROR, LaunchingPlugin.getUniqueIdentifier(), 0, LaunchingMessages.getString("StandardVMType.Not_a_JDK_root._System_library_was_not_found._1"), null); //$NON-NLS-1$
			}
		}
		return status;
		*/
		String id= MacOSXLaunchingPlugin.getUniqueIdentifier();
		File java= new File(javaHome, "bin"+File.separator+"java"); //$NON-NLS-2$ //$NON-NLS-1$
		if (java.isFile())
			return new Status(IStatus.OK, id, 0, "ok", null); //$NON-NLS-1$
		return new Status(IStatus.ERROR, id, 0, MacOSXLaunchingPlugin.getString("MacOSXVMType.error.notRoot"), null); //$NON-NLS-1$
	}
	
	/**
	 * @see org.eclipse.jdt.launching.AbstractVMInstallType#getDefaultJavadocLocation(java.io.File)
	 */
	public URL getDefaultJavadocLocation(File installLocation) {
		
		// try in local filesystem
		String id= null;	
		try {
			String post= File.separator + JVM_ROOT;
			String path= installLocation.getCanonicalPath();
			if (path.startsWith(JVM_VERSION_LOC) && path.endsWith(post))
				id= path.substring(JVM_VERSION_LOC.length(), path.length()-post.length());
		} catch (IOException ex) {
		}
		if (id != null) {
			File docLocation= new File(JAVADOC_LOC + id);
			if (docLocation.exists()) {
				try {
					return new URL("file", "", JAVADOC_LOC + id);	//$NON-NLS-1$ //$NON-NLS-2$
				} catch (MalformedURLException ex) {
				}
			}
		}
		
		// fall back
		return super.getDefaultJavadocLocation(installLocation);
	}
}
