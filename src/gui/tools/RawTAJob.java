package gui.tools;

import java.io.File;
import java.util.Iterator;
import java.util.Properties;

import org.adb.AdbUtility;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
import org.logger.MyLogger;
import org.system.Devices;
import org.system.OS;
import org.system.TextFile;

public class RawTAJob extends Job {

	String _action = "";
	Shell _shell;
	
	public void setAction(String action) {
		_action = action;
	}
	
	public void setShell(Shell shell) {
		_shell = shell;
	}
	
	public RawTAJob(String name) {
		super(name);
	}
	
    protected IStatus run(IProgressMonitor monitor) {
    	try {

    		if (_action.equals("doBackup"))
    			doBackup();
    		if (_action.equals("doRestore"))
    			doRestore();
    		return Status.OK_STATUS;
    	}
    	catch (Exception e) {
    		e.printStackTrace();
    		return Status.CANCEL_STATUS;
    	}
    }

    public void doBackup() {
		if (AdbUtility.exists("su -c 'ls /dev/block/platform/msm_sdcc.1/by-name/TA'")) {
			try {
				MyLogger.getLogger().info("Begin backup");
				AdbUtility.run("su -c 'dd if=/dev/block/platform/msm_sdcc.1/by-name/TA of=/mnt/sdcard/ta.dd && md5sum /dev/block/platform/msm_sdcc.1/by-name/TA > /mnt/sdcard/ta.md5 && md5sum /mnt/sdcard/ta.dd >> /mnt/sdcard/ta.md5'");
				MyLogger.getLogger().info("End of backup");
				AdbUtility.pull("/mnt/sdcard/ta.dd", OS.getWorkDir());
				AdbUtility.pull("/mnt/sdcard/ta.md5", OS.getWorkDir());
				AdbUtility.run("rm /mnt/sdcard/ta.dd && rm /mnt/sdcard/ta.md5");
				TextFile t=new TextFile(OS.getWorkDir()+File.separator+"ta.md5","ISO-8859-1");
				if (t.getLines().size()==2) {
					Properties hash = new Properties();
					Iterator i = t.getLines().iterator();
					while (i.hasNext()) {
						String line = (String)i.next();
						String[] split = line.split(" ");
						String md5 = split[0];
						String file = split[split.length-1];
						hash.setProperty(file, md5.toUpperCase());
					}
					hash.setProperty("local", OS.getMD5(new File(OS.getWorkDir()+File.separator+"ta.dd")).toUpperCase());
					if (hash.getProperty("/dev/block/platform/msm_sdcc.1/by-name/TA").equals(hash.getProperty("/mnt/sdcard/ta.dd"))) {
						if (hash.getProperty("local").equals(hash.getProperty("/mnt/sdcard/ta.dd")))
							MyLogger.getLogger().info("Backup is OK");
						else
							MyLogger.getLogger().error("Local file does not match remote file");
					}
					else
						MyLogger.getLogger().error("Backup does not match partition");
				}
				else {
					MyLogger.getLogger().error("Error while taking backup");
				}
			} catch (Exception ex) {
			}
		}
    }
    
    public void doRestore() {
		if (AdbUtility.exists("su -c 'ls /dev/block/platform/msm_sdcc.1/by-name/TA'")) {
			try {
				MyLogger.getLogger().info("Begin restore");
				AdbUtility.push(OS.getWorkDir()+File.separator+"ta.dd","/mnt/sdcard/");
				AdbUtility.run("su -c 'md5sum /mnt/sdcard/ta.dd > /mnt/sdcard/tarestore.md5'");
				AdbUtility.run("su -c 'md5sum /dev/block/platform/msm_sdcc.1/by-name/TA >> /mnt/sdcard/tarestore.md5'");
				AdbUtility.pull("/mnt/sdcard/tarestore.md5", OS.getWorkDir());
				TextFile t=new TextFile(OS.getWorkDir()+File.separator+"tarestore.md5","ISO-8859-1");
				if (t.getLines().size()==2) {
					Properties hash = new Properties();
					Iterator i = t.getLines().iterator();
					while (i.hasNext()) {
						String line = (String)i.next();
						String[] split = line.split(" ");
						String md5 = split[0];
						String file = split[split.length-1];
						hash.setProperty(file, md5.toUpperCase());
					}
					hash.setProperty("local", OS.getMD5(new File(OS.getWorkDir()+File.separator+"ta.dd")).toUpperCase());
					if (hash.getProperty("local").equals(hash.getProperty("/mnt/sdcard/ta.dd"))) {
						MyLogger.getLogger().info("To be Restored file is OK. Now flashing it to device.");
						if (!hash.getProperty("/dev/block/platform/msm_sdcc.1/by-name/TA").equals(hash.getProperty("/mnt/sdcard/ta.dd"))) {
							String result = WidgetTask.openYESNOBox(_shell, "Partition and file differ. Restore anyway ?");
							if (Integer.parseInt(result)==SWT.YES) {
								MyLogger.getLogger().info("Restoring backup.");
								AdbUtility.run("su -c 'dd if=/mnt/sdcard/ta.dd of=/dev/block/platform/msm_sdcc.1/by-name/TA && sync && sync && sync && sync'");
							}
							else {
								MyLogger.getLogger().info("Operation canceled");
							}
						}
						else {
							MyLogger.getLogger().info("Restoring backup.");
							AdbUtility.run("su -c 'dd if=/mnt/sdcard/ta.dd of=/dev/block/platform/msm_sdcc.1/by-name/TA && sync && sync && sync && sync'");									
							AdbUtility.run("su -c 'md5sum /mnt/sdcard/ta.dd > /mnt/sdcard/taafterrestore.md5'");
							AdbUtility.run("su -c 'md5sum /dev/block/platform/msm_sdcc.1/by-name/TA >> /mnt/sdcard/taafterrestore.md5'");
							AdbUtility.pull("/mnt/sdcard/taafterrestore.md5", OS.getWorkDir());
							t=new TextFile(OS.getWorkDir()+File.separator+"taafterrestore.md5","ISO-8859-1");
							if (t.getLines().size()==2) {
								hash = new Properties();
								i = t.getLines().iterator();
								while (i.hasNext()) {
									String line = (String)i.next();
									String[] split = line.split(" ");
									String md5 = split[0];
									String file = split[split.length-1];
									hash.setProperty(file, md5.toUpperCase());
								}
								if (!hash.getProperty("/dev/block/platform/msm_sdcc.1/by-name/TA").equals(hash.getProperty("/mnt/sdcard/ta.dd"))) {
									MyLogger.getLogger().info("Partition and file differs. Restore is not OK");
								}
								else {
									MyLogger.getLogger().info("TA Restore is OK.");
								}
							}
							else {
								MyLogger.getLogger().info("TA restore is not OK");
							}
						}
					}
					else
						MyLogger.getLogger().error("Restored file is corrupted");
				}
				else {
					MyLogger.getLogger().error("Error while restoring backup");
				}
			} catch (Exception ex) {
			}
	}    	
    }

}