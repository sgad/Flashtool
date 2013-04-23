package gui.tools;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
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
		try {
			if (!Devices.getCurrent().isBusyboxInstalled(false))
				throw new Exception("Busybox must be installed");
			String ident = WidgetTask.openTABackupSet(_shell);
			if (ident.length()==0)
				throw new Exception("Operation canceled");
			String serial = AdbUtility.getDevices().nextElement();
			String folder = OS.getWorkDir()+File.separator+"custom"+File.separator+serial+File.separator+"rawta"+File.separator+OS.getTimeStamp();;
			TextFile t = new TextFile(folder+File.separator+"ident","ISO-8859-1");
			t.open(true);
			t.write(ident);
			t.close();
			new File(folder).mkdirs();
			if (!AdbUtility.exists("su -c 'ls /dev/block/platform/msm_sdcc.1/by-name/TA'"))
				throw new Exception("Your phone is not compatible");
			MyLogger.getLogger().info("Begin backup");
			AdbUtility.run("su -c 'dd if=/dev/block/platform/msm_sdcc.1/by-name/TA of=/mnt/sdcard/ta.dd && md5sum /dev/block/platform/msm_sdcc.1/by-name/TA > /mnt/sdcard/ta.md5 && md5sum /mnt/sdcard/ta.dd >> /mnt/sdcard/ta.md5'");
			MyLogger.getLogger().info("End of backup");
			AdbUtility.pull("/mnt/sdcard/ta.dd", folder);
			AdbUtility.pull("/mnt/sdcard/ta.md5", folder);
			AdbUtility.run("rm /mnt/sdcard/ta.dd && rm /mnt/sdcard/ta.md5");
			Properties hash = parseMD5(folder,"ta.md5");
			hash.setProperty("local", OS.getMD5(new File(folder+File.separator+"ta.dd")).toUpperCase());
			if (hash.size()!=3)
				throw new Exception("Expecting 3 lines, got "+hash.size()+ " ones");
			if (!allMatches(hash))
				throw new Exception("Backupset integrity is not OK");
			MyLogger.getLogger().info("Backup is OK");
		} catch (Exception ex) {
			MyLogger.getLogger().error(ex.getMessage()); 
		}
    }
    
    public void doRestore() {
		try {
			if (!Devices.getCurrent().isBusyboxInstalled(false))
				throw new Exception("Busybox must be installed");
			String backupset = WidgetTask.openTABackupSelector(_shell);
			if (backupset.length()==0)
				throw new Exception("Operation canceled");
			backupset = backupset.split(":")[0].trim();
			String serial = AdbUtility.getDevices().nextElement();
			String folder = OS.getWorkDir()+File.separator+"custom"+File.separator+serial+File.separator+"rawta"+File.separator+backupset; 
			if (!AdbUtility.exists("su -c 'ls /dev/block/platform/msm_sdcc.1/by-name/TA'"))
				throw new Exception("Your phone is not compatible");
			Properties hash = parseMD5(folder,"ta.md5");
			if (!new File(folder+File.separator+"ta.dd").exists())
				throw new Exception(folder+File.separator+"ta.dd"+" does not exist");
			hash.setProperty("local", OS.getMD5(new File(folder+File.separator+"ta.dd")).toUpperCase());
			if (hash.size()!=3)
				throw new Exception("Expecting 3 lines, got "+hash.size()+ " ones");
			MyLogger.getLogger().info("Checking TA image integrity");
			if (!allMatches(hash))
				throw new Exception("Your TA image does not match backupset MD5");
			MyLogger.getLogger().info("TA image OK. Continuing.");
			AdbUtility.push(folder+File.separator+"ta.dd","/mnt/sdcard/");
			AdbUtility.run("su -c 'md5sum /mnt/sdcard/ta.dd > /mnt/sdcard/tarestore.md5'");
			AdbUtility.run("su -c 'md5sum /dev/block/platform/msm_sdcc.1/by-name/TA >> /mnt/sdcard/tarestore.md5'");
			AdbUtility.pull("/mnt/sdcard/tarestore.md5", folder);
			hash = parseMD5(folder,"tarestore.md5");
			hash.setProperty("local", OS.getMD5(new File(folder+File.separator+"ta.dd")).toUpperCase());
			if (hash.size()!=3)
				throw new Exception("Expecting 3 lines, got "+hash.size()+ " ones");			
			if (!hash.getProperty("local").equals(hash.getProperty("/mnt/sdcard/ta.dd")))
				throw new Exception("Local and remote file do not match");
			MyLogger.getLogger().info("To be Restored file is OK. Now flashing it to device.");
			if (!hash.getProperty("/dev/block/platform/msm_sdcc.1/by-name/TA").equals(hash.getProperty("/mnt/sdcard/ta.dd"))) {
				String result = WidgetTask.openYESNOBox(_shell, "Partition and file differ.\nBe sure your backup if from the connected device.\nRestore anyway ?");
				if (Integer.parseInt(result)==SWT.NO)
					throw new Exception("Operation canceled");
			}
			MyLogger.getLogger().info("Restoring backup.");
			AdbUtility.run("su -c 'dd if=/mnt/sdcard/ta.dd of=/dev/block/platform/msm_sdcc.1/by-name/TA && sync && sync && sync && sync'");									
			AdbUtility.run("su -c 'md5sum /mnt/sdcard/ta.dd > /mnt/sdcard/taafterrestore.md5'");
			AdbUtility.run("su -c 'md5sum /dev/block/platform/msm_sdcc.1/by-name/TA >> /mnt/sdcard/taafterrestore.md5'");
			AdbUtility.pull("/mnt/sdcard/taafterrestore.md5", folder);
			hash = parseMD5(folder,"taafterrestore.md5");
			if (!allMatches(hash)) {
				throw new Exception("Partition and file differ. Restore is not OK");
			}
			else {
				MyLogger.getLogger().info("TA Restore is OK.");
			}
		} catch (Exception e) {
			MyLogger.getLogger().error(e.getMessage());
		}
    }

    public Properties parseMD5(String folder, String file) throws IOException {
    	Properties p = new Properties();
			TextFile t=new TextFile(folder+File.separator+file,"ISO-8859-1");
			Iterator i = t.getLines().iterator();
			while (i.hasNext()) {
				String line = (String)i.next();
				String[] split = line.split(" ");
				String md5 = split[0];
				String f = split[split.length-1];
				p.setProperty(f, md5.toUpperCase());
			}
		return p;
    }

    public boolean allMatches(Properties p) {
    	Enumeration e = p.keys();
    	while (e.hasMoreElements()) {
    		String key = (String)e.nextElement();
    		String value = p.getProperty(key);
    		Enumeration e1 = p.keys();
    		while (e1.hasMoreElements()) {
    			String key1 = (String)e1.nextElement();
    			if (!p.getProperty(key1).equals(value)) return false;
    		}
    	}
    	return true;
    }
}