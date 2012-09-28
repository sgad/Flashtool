package org.adb;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Properties;
import java.util.Scanner;
import java.util.Vector;

import org.logger.MyLogger;
import org.system.Devices;
import org.system.GlobalConfig;
import org.system.OS;
import org.system.ProcessBuilderWrapper;
import org.system.Shell;

public class AdbUtility  {

	static Properties build = new Properties();
	static boolean rootnative=false;
	static boolean rootperms=false;
	
	private static String fsep = OS.getFileSeparator();
	private static String shellpath = OS.getWorkDir()+fsep+"custom"+fsep+"shells";
	private static String adbpath = OS.getAdbPath();
	private static String shpath ="";

	public static String getShellPath() {
		return shellpath;
	}
	
	public static void setShellPath(String path) {
		shellpath = path;
	}
	
	public static boolean exists(String path) {
		try {
		ProcessBuilderWrapper command = new ProcessBuilderWrapper(new String[] {adbpath,"shell", "ls -l "+path},false);
		return !command.getStdOut().toLowerCase().contains("no such file or directory");
		}
		catch (Exception e) {
			return false;
		}
	}
	public static String getShPath(boolean force) {
		if (shpath==null || force) {
			try {
				if (exists("/system/flashtool/sh"))
					shpath="/system/flashtool/sh";
				else {
					ProcessBuilderWrapper command1 = new ProcessBuilderWrapper(new String[] {adbpath,"shell", "echo $0"},false);
					shpath = command1.getStdOut().trim();
				}
			}
			catch (Exception e) {
				shpath = "";
			}
		}
		MyLogger.getLogger().debug("Default shell for scripts : "+shpath);
		return shpath;
	}

	public static boolean hasRootNative(boolean force) {
		try {
			if (!force && rootnative) return true;
			ProcessBuilderWrapper command = new ProcessBuilderWrapper(new String[] {adbpath,"shell","id"},false);
			rootnative=command.getStdOut().contains("uid=0");
		}
		catch (Exception e) {
		}
		return rootnative;
	}
	
	public static void forward(String type,String local, String remote) throws Exception {
		ProcessBuilderWrapper command = new ProcessBuilderWrapper(new String[] {adbpath,"forward "+type.toUpperCase()+":"+local+" "+type.toUpperCase()+":"+remote},false);
	}
	
	public static HashSet<String> listSysApps() throws Exception {
		ProcessBuilderWrapper command = new ProcessBuilderWrapper(new String[] {adbpath,"shell", "ls /system/app/*apk"},false);
		String[] result = command.getStdOut().split("\n");
		HashSet<String> set = new HashSet<String>();
		for (int i=0;i<result.length;i++) {
			String apk = result[i].substring(result[i].lastIndexOf('/')+1);
			if (!apk.contains("No such file or directory"))
				set.add(apk.substring(0,apk.lastIndexOf(".apk")+4));
		}
		command = new ProcessBuilderWrapper(new String[] {adbpath,"shell", "ls /system/app/*odex"},false);
		result = command.getStdOut().split("\n");
		for (int i=0;i<result.length;i++) {
			String apk = result[i].substring(result[i].lastIndexOf('/')+1);
			if (!apk.contains("No such file or directory"))
				set.add(apk.substring(0,apk.lastIndexOf(".odex")+5));
		}
		return set;
	}
	
	public static HashSet<String> listKernels() throws Exception {
		ProcessBuilderWrapper command = new ProcessBuilderWrapper(new String[] {adbpath,"shell", "find /system/kernel -name 'kernel.desc' -type f"},false);
		String[] result = command.getStdOut().split("\n");
		HashSet<String> set = new HashSet<String>();
		for (int i=0;i<result.length;i++) {
			int first = result[i].indexOf('/', 1);
			first = result[i].indexOf('/', first+1);
			int last = result[i].indexOf('/', first+1);
			set.add(result[i].substring(first+1,last));
		}
		return set;
	}

	public static HashSet<String> listRecoveries() throws Exception {
		ProcessBuilderWrapper command = new ProcessBuilderWrapper(new String[] {adbpath,"shell", "find /system/recovery -name 'recovery.desc' -type f"},false);
		String[] result = command.getStdOut().split("\n");
		HashSet<String> set = new HashSet<String>();
		for (int i=0;i<result.length;i++) {
			int first = result[i].indexOf('/', 1);
			first = result[i].indexOf('/', first+1);
			int last = result[i].indexOf('/', first+1);
			set.add(result[i].substring(first+1,last));
		}
		return set;
	}

	/*public static boolean isSystemMounted() throws Exception {
		if (systemmounted==null) {
			systemmounted = isMounted("/system");
		}
		return systemmounted.booleanValue();
	}*/
	
	public static void init() {
		rootnative=false;
		rootperms=false;
		shpath=null;
	}
	
	public static boolean isMounted(String mountpoint) throws Exception {
		boolean result = false;
		ProcessBuilderWrapper command = new ProcessBuilderWrapper(new String[] {adbpath,"shell", "mount"},false);
		Scanner sc = new Scanner(command.getStdOut());
		while (sc.hasNextLine()) {
			String line = sc.nextLine();
			if (line.contains(mountpoint)) {
				result = true;
			}
		}
		return result;
	}
	
	public static boolean hasSU() {
		boolean result = true;
		try {
		ProcessBuilderWrapper command = new ProcessBuilderWrapper(new String[] {adbpath,"shell", "type su"},false);
		Scanner sc = new Scanner(command.getStdOut());
		while (sc.hasNextLine()) {
			String line = sc.nextLine();
			if (line.toLowerCase().contains("not found")) {
				result = false;
			}
		}
		}
		catch (Exception e) {
			return false;
		}
		return result;	
	}
	
	public static String getFilePerms(String file) {
		try {
			ProcessBuilderWrapper command = new ProcessBuilderWrapper(new String[] {adbpath,"shell", "stat "+file},false);
			Scanner sc = new Scanner(command.getStdOut());
			while (sc.hasNextLine()) {
				String line = sc.nextLine();
				if (line.contains("Uid")) {
					return line;
				}
			}
			return "   ";
		}
		catch (Exception e) {
			return "   ";
		}
	}
	
	public static ByteArrayInputStream getBuildProp() throws Exception {
		ProcessBuilderWrapper command = new ProcessBuilderWrapper(new String[] {adbpath,"shell", "cat /system/build.prop"},false);
		return new ByteArrayInputStream(command.getStdOut().getBytes());
	}
	
	public static boolean hasRootPerms() {
		if (hasRootNative(false)) return true;
		if (rootperms) return true;
		try {
			Shell shell = new Shell("checkperms");
			String result=shell.runRoot(false);
			while (result.toLowerCase().contains("segmentation fault")) {
				Thread.sleep(10000);
				result=shell.runRoot(false);
			}
			rootperms=result.contains("uid=0");
			return rootperms;
		}
		catch (Exception e) {
			return false;
		}
	}

	public static HashSet<String> ls(String basedir,String type) throws Exception {
		ProcessBuilderWrapper command = new ProcessBuilderWrapper(new String[] {adbpath+"shell", "find "+basedir+" -maxdepth 1 -type "+type},false);
		String[] result = command.getStdOut().split("\n");
		HashSet<String> set = new HashSet<String>();
		for (int i=0;i<result.length;i++) {
			if (result[i].substring(result[i].lastIndexOf('/')+1).length()>0 && !result[i].substring(result[i].lastIndexOf('/')+1).equals("/"))
				set.add(result[i].substring(result[i].lastIndexOf('/')+1));
		}
		return set;		
	}
	
	public static void uninstall(String apk, boolean silent) throws Exception {
		if (!silent)
			MyLogger.getLogger().info("Uninstalling "+apk);
		ProcessBuilderWrapper command = new ProcessBuilderWrapper(new String[] {adbpath,"uninstall",apk},false);
	}

	public static void killServer() throws Exception {
		MyLogger.getLogger().info("Killing adb service");
		ProcessBuilderWrapper command = null;
		if (OS.getName().equals("windows"))
			command = new ProcessBuilderWrapper(new String[] {adbpath,"kill-server"},false);
		else
			command = new ProcessBuilderWrapper(new String[] {"killall","adb"},false);
	}

	public static void startServer() throws Exception {
			MyLogger.getLogger().info("Starting adb service");
			ProcessBuilderWrapper command = new ProcessBuilderWrapper(new String[] {adbpath,"start-server"},false);
	}

	public static void push(String source, String destination) throws Exception {
		push(source, destination, true);
	}

	public static void restore(String source) throws Exception {
		File f = new File(source);
		if (!f.exists()) throw new AdbException(source+" : Not found");		
		ProcessBuilderWrapper command = new ProcessBuilderWrapper(new String[] {adbpath,"restore",f.getAbsolutePath()},false);
		if (command.getStatus()!=0) {
			throw new AdbException(command.getStdOut()+ " " + command.getStdErr());
		}
	}

	public static void push(String source, String destination, boolean log) throws Exception {
		File f = new File(source);
		if (!f.exists()) throw new AdbException(source+" : Not found");
		if (log) MyLogger.getLogger().info("Pushing "+f.getAbsolutePath()+" to "+destination);
		else MyLogger.getLogger().debug("Pushing "+f.getAbsolutePath()+" to "+destination);
		ProcessBuilderWrapper command = new ProcessBuilderWrapper(new String[] {adbpath,"push",f.getAbsolutePath(),destination},false);
		if (command.getStatus()!=0) {
			throw new AdbException(command.getStdOut()+ " " + command.getStdErr());
		}
	}

	
	public static String getBusyboxVersion(String path) {
		try {
			ProcessBuilderWrapper command;
			if (isMounted("/system")) {
				command = new ProcessBuilderWrapper(new String[] {adbpath,"shell",path+"/busybox"},false);
			}
			else {
				command = new ProcessBuilderWrapper(new String[] {adbpath,"shell","/sbin/busybox"},false);
			}
			Scanner sc = new Scanner(command.getStdOut());
			if (sc.hasNextLine()) {	
				String line = sc.nextLine();
				if (line.contains("BusyBox v1") && line.contains("multi-call")) return line;
			}
			return "";
		}
		catch (Exception e) {
			return "";
		}
	}
	
	public static void mount(String mountpoint,String options, String type) throws Exception {
		if (hasRootNative(false)) {
			ProcessBuilderWrapper command = new ProcessBuilderWrapper(new String[] {adbpath,"shell","mount -o "+options+" -t "+type+" "+mountpoint},false);			
		}
	}
	
	public static void umount(String mountpoint) throws Exception {
		if (hasRootNative(false)) {
			ProcessBuilderWrapper command = new ProcessBuilderWrapper(new String[] {adbpath,"shell", "umount "+mountpoint},false);
		}		
	}
	
	public static void pull(String source, String destination) throws Exception {
		pull(source,destination,true);
	}

	public static void pull(String source, String destination, boolean log) throws Exception {
		if (log)
			MyLogger.getLogger().info("Pulling "+source+" to "+destination);
		else
			MyLogger.getLogger().debug("Pulling "+source+" to "+destination);
		ProcessBuilderWrapper command = new ProcessBuilderWrapper(new String[] {adbpath,"pull",source, destination},false);
		if (command.getStatus()!=0) {
			throw new AdbException(command.getStdOut()+ " " + command.getStdErr());
		}
	}

	public static String getKernelVersion(boolean hasbusybox) {
		try {
			String result = "";
			if (!hasbusybox) {
				AdbUtility.push(Devices.getCurrent().getBusybox(false), GlobalConfig.getProperty("deviceworkdir")+"/busybox1",false);
				AdbUtility.run("chmod 755 "+GlobalConfig.getProperty("deviceworkdir")+"/busybox1");
				result = run(GlobalConfig.getProperty("deviceworkdir")+"/busybox1 uname -r");
				run("rm -r "+GlobalConfig.getProperty("deviceworkdir")+"/busybox1");
			}
			else result = run("busybox uname -r");
			return result;
		}
		catch (Exception e) {
			return "";
		}
	}
	
	public static String run(Shell shell, boolean debug) throws Exception {
		push(shell.getPath(),GlobalConfig.getProperty("deviceworkdir")+"/"+shell.getName(),false);
		if (debug)
			MyLogger.getLogger().debug("Running "+shell.getName());
		else
			MyLogger.getLogger().info("Running "+shell.getName());
		ProcessBuilderWrapper command = new ProcessBuilderWrapper(new String[] {adbpath,"shell", "sh "+GlobalConfig.getProperty("deviceworkdir")+"/"+shell.getName()+";exit $?"},false);
		if (command.getStdOut().contains("FTError")) throw new Exception(command.getStdErr()+" "+command.getStdOut());
		return command.getStdOut();
	}

	public static String run(String com, boolean debug) throws Exception {
		if (debug)
			MyLogger.getLogger().debug("Running "+ com + " command");
		else
			MyLogger.getLogger().info("Running "+ com + " command");
		ProcessBuilderWrapper command = new ProcessBuilderWrapper(new String[] {adbpath,"shell",com},false);
		return command.getStdOut().trim();
	}

	public static String run(String com) throws Exception {
		return run(com,true);
	}

	public static String runRoot(Shell shell) throws Exception {
		return runRoot(shell,true);
	}
	
	public static String runRoot(Shell shell,boolean log) throws Exception {
		Shell s=new Shell("sysrun");
		s.save();
		push(s.getPath(),GlobalConfig.getProperty("deviceworkdir")+"/sysrun",false);
		s.clean();
		push(shell.getPath(),GlobalConfig.getProperty("deviceworkdir")+"/runscript",false);
		if (log)
			MyLogger.getLogger().info("Running "+shell.getName()+"  as root thru sysrun");
		else
			MyLogger.getLogger().debug("Running "+shell.getName()+"  as root thru sysrun");
		ProcessBuilderWrapper command;
		if (rootnative)
			command=new ProcessBuilderWrapper(new String[] {adbpath,"shell", "sh "+GlobalConfig.getProperty("deviceworkdir")+"/sysrun"},false);
		else
			command=new ProcessBuilderWrapper(new String[] {adbpath,"shell", "su -c 'sh "+GlobalConfig.getProperty("deviceworkdir")+"/sysrun'"},false);
		return command.getStdOut();
	}

	public static boolean Sysremountrw() throws Exception {
		MyLogger.getLogger().info("Remounting system read-write");
		Shell shell = new Shell("remount");
		return !shell.runRoot(false).contains("FTError");
	}

	public static void clearcache() throws Exception {
		MyLogger.getLogger().info("Clearing dalvik cache and rebooting");
		Shell shell = new Shell("clearcache");
		shell.runRoot(false);
	}

	public static void install(String apk) throws Exception {
		MyLogger.getLogger().info("Installing "+apk);
		ProcessBuilderWrapper command = new ProcessBuilderWrapper(new String[] {adbpath,"install", "\""+apk+"\""},false);
		if (command.getStdOut().contains("Failure")) {
			uninstall(APKUtility.getPackageName(apk),true);
			command = new ProcessBuilderWrapper(new String[] {adbpath,"install","\""+apk+"\""},false);
			if (command.getStdOut().contains("Failure")) {
				Scanner sc = new Scanner(command.getStdOut());
				sc.nextLine();
				MyLogger.getLogger().error(sc.nextLine());
			}
		}
	}

	public static void scanStatus() throws Exception {
		ProcessBuilderWrapper command = new ProcessBuilderWrapper(new String[] {adbpath,"status-window"},false);
	}

	public static boolean isConnected() {
		try {
			MyLogger.getLogger().debug("Testing if device is connected");
			return AdbUtility.getDevices().hasMoreElements();
		}
		catch (Exception e) {
			return false;
		}
	}
/*	public static boolean isConnected() {
		try {
			MyLogger.getLogger().info("Searching Adb Device");
			String res =Device.AdbId();
			if (res.equals("ErrNotPlugged")) {
				MyLogger.error("Please plug your device with USB Debugging and Unknown sources on");
				return false;
			}
			else if (res.equals("ErrDriverError")) {
				MyLogger.error("ADB drivers are not installed");
				return false;
			}
			boolean connected = false;
			ProcessBuilderWrapper command = new ProcessBuilderWrapper(adbpath+" devices");
			command.run();
			String[] result = command.getStdOut().split("\n");
			for (int i=1;i<result.length; i++) {
				connected=result[i].contains("device");
			}
			if (!connected) {
				MyLogger.error("Please plug your device with USB Debugging and Unknown sources turned on");
			}
			return connected;
		}
		catch (Exception e) {
			MyLogger.error("Please plug your device with USB Debugging and Unknown sources turned on");
			return false;
		}
	}*/

	public static Enumeration<String> getDevices() {
		Vector<String> v = new Vector<String>();
		try {
			ProcessBuilderWrapper command = new ProcessBuilderWrapper(new String[] {adbpath,"devices"},false);
			Scanner sc = new Scanner(command.getStdOut());
			while (sc.hasNextLine()) {
				String line = sc.nextLine();
				if (!line.startsWith("List")) {
					v.add(line.trim().substring(0,line.indexOf(9)));
				}
			}
		}
		catch (Exception e) {}
		return v.elements();
	}
}