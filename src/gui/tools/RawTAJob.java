package gui.tools;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Properties;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.Deflater;

import org.adb.AdbUtility;
import org.apache.commons.io.FileUtils;
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

import flashsystem.BundleEntry;

public class RawTAJob extends Job {

	String _action = "";
	Shell _shell;
	String folder;
	String partition;
	
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
			String serial = Devices.getCurrent().getSerial();
			folder = OS.getWorkDir()+File.separator+"custom"+File.separator+"mydevices"+File.separator+serial+File.separator+"rawta";
			new File(folder).mkdirs();
			partition = "/dev/block/platform/msm_sdcc.1/by-name/TA";
			if (!AdbUtility.exists(partition)) {
				partition = AdbUtility.run("busybox cat /proc/partitions|busybox grep -w 2048|busybox awk '{print $4}'");
				if (partition.length()==0)
					throw new Exception("Your phone is not compatible");
				partition = "/dev/block/"+partition;
			}
			MyLogger.getLogger().info("Begin backup");
			AdbUtility.run("su -c 'dd if="+partition+" of=/mnt/sdcard/ta.dd'");
			Properties hash = new Properties();
			hash.setProperty("remote", getMD5("/mnt/sdcard/ta.dd"));
			hash.setProperty("partition", getMD5(partition));
			AdbUtility.pull("/mnt/sdcard/ta.dd", folder);
			hash.setProperty("local", OS.getMD5(new File(folder+File.separator+"ta.dd")).toUpperCase());
			MyLogger.getLogger().info("End of backup");
			if (hash.getProperty("local").equals(hash.getProperty("remote")) && hash.getProperty("remote").equals(hash.getProperty("partition"))) {
				MyLogger.getLogger().info("Backup is OK");
				createFTA();
			}
			else throw new Exception("Backup is not OK");
		} catch (Exception ex) {
			new File(folder+"ta.dd").delete();
			MyLogger.getLogger().error(ex.getMessage()); 
		}
    }
    
    public void doRestore() {
		try {
			String serial = Devices.getCurrent().getSerial();
			folder = OS.getWorkDir()+File.separator+"custom"+File.separator+"mydevices"+File.separator+serial+File.separator+"rawta";
			if (!Devices.getCurrent().isBusyboxInstalled(false))
				throw new Exception("Busybox must be installed");
			String backupset = WidgetTask.openTABackupSelector(_shell);
			if (backupset.length()==0) {
				throw new Exception("Operation canceled");
			} 
			backupset = backupset.split(":")[0].trim();
			backupset = folder+File.separator+backupset+".fta";
			File ta = new File(backupset);
			JarFile jf = new JarFile(ta);
			Attributes attr = jf.getManifest().getMainAttributes();
			String partition = attr.getValue("partition");
			File prepared = new File(folder+File.separator+"prepared");
			if (prepared.exists()) {
				FileUtils.deleteDirectory(prepared);
				if (prepared.exists())
					throw new Exception("Cannot delete previous folder : "+prepared.getAbsolutePath());
			}
			prepared.mkdirs();
			folder = prepared.getAbsolutePath();
			Enumeration<JarEntry> ents = jf.entries();
			while (ents.hasMoreElements()) {
				JarEntry entry = ents.nextElement();
				if (!entry.getName().startsWith("META"))
					saveEntry(jf,entry);
			}
			
			Properties hash = new Properties();
			hash.setProperty("stored", attr.getValue("md5"));
			
			if (!new File(folder+File.separator+"ta.dd").exists())
				throw new Exception(folder+File.separator+"ta.dd"+" does not exist");
			hash.setProperty("local", OS.getMD5(new File(folder+File.separator+"ta.dd")).toUpperCase());
			if (!hash.getProperty("stored").equals(hash.getProperty("local")))
				throw new Exception("Error during extraction. File is corrupted");

			AdbUtility.push(folder+File.separator+"ta.dd","/mnt/sdcard/");
			hash.setProperty("remote", getMD5("/mnt/sdcard/ta.dd"));
			if (!hash.getProperty("local").equals(hash.getProperty("remote")))
				throw new Exception("Local file and remote file do not match");
			hash.setProperty("partitionbefore", getMD5(partition));
			if (hash.getProperty("remote").equals(hash.getProperty("partitionbefore")))
				throw new Exception("Backup and current partition match. Nothing to be done. Aborting");
			MyLogger.getLogger().info("Making a backup on device before flashing.");
			AdbUtility.run("su -c 'dd if="+partition+" of=/mnt/sdcard/tabefore.dd'");
			hash.setProperty("remotebefore", getMD5("/mnt/sdcard/tabefore.dd"));
			if (!hash.getProperty("remotebefore").equals(hash.getProperty("partitionbefore")))
				throw new Exception("Failed to take a backup before flashing new TA. Aborting");
			MyLogger.getLogger().info("Flashing new TA.");
			AdbUtility.run("su -c 'dd if=/mnt/sdcard/ta.dd of="+partition+" && sync && sync && sync && sync'");
			hash.setProperty("partitionafter", getMD5(partition));
			if (!hash.getProperty("remote").equals(hash.getProperty("partitionafter"))) {
				MyLogger.getLogger().error("Error flashing new TA. Reverting back to the previous TA.");
				AdbUtility.run("su -c 'dd if=/mnt/sdcard/tabefore.dd of="+partition+" && sync && sync && sync && sync'");
				hash.setProperty("partitionafter", getMD5(partition));
				if (!hash.getProperty("remotebefore").equals(hash.getProperty("partitionafter")))
					throw new Exception("Failed to restore previous TA");
				MyLogger.getLogger().info("Restore previous TA OK");
			}
			else
				MyLogger.getLogger().info("Restore is OK");
		} catch (Exception e) {
			MyLogger.getLogger().error(e.getMessage());
		}
    }

    public void createFTA() {
    	File tadd = new File(folder+File.separator+"ta.dd");
    	String timestamp = OS.getTimeStamp();
		File fta = new File(folder+File.separator+timestamp+".fta");
		byte buffer[] = new byte[10240];
		StringBuffer sbuf = new StringBuffer();
		sbuf.append("Manifest-Version: 1.0\n");
		sbuf.append("Created-By: FlashTool\n");
		sbuf.append("serial: "+Devices.getCurrent().getSerial()+"\n");
		sbuf.append("build: "+Devices.getCurrent().getBuildId()+"\n");
		sbuf.append("partition: "+partition+"\n");
		sbuf.append("md5: "+OS.getMD5(tadd).toUpperCase()+"\n");
		sbuf.append("timestamp: "+timestamp+"\n");
		try {
			Manifest manifest = new Manifest(new ByteArrayInputStream(sbuf.toString().getBytes("UTF-8")));
		    FileOutputStream stream = new FileOutputStream(fta);
		    JarOutputStream out = new JarOutputStream(stream, manifest);
		    out.setLevel(Deflater.BEST_SPEED);
			MyLogger.getLogger().info("Adding ta.dd to the fta bundle");
		    JarEntry jarAdd = new JarEntry("ta.dd");
	        out.putNextEntry(jarAdd);
	        InputStream in = new FileInputStream(tadd);
	        while (true) {
	          int nRead = in.read(buffer, 0, buffer.length);
	          if (nRead <= 0)
	            break;
	          out.write(buffer, 0, nRead);
	        }
	        in.close();
	        out.flush();
	        out.close();
	        stream.flush();
		    stream.close();
		    tadd.delete();
		    MyLogger.getLogger().info("Bundle "+fta.getAbsolutePath()+" creation finished");
		}
		catch (Exception e) {
			MyLogger.getLogger().error(e.getMessage());
		}
    }

    public String getMD5(String path) throws Exception {
		return AdbUtility.run("su -c 'busybox md5sum "+path+"'").split(" ")[0].toUpperCase().trim();
    }

    private void saveEntry(JarFile jar, JarEntry entry) throws IOException {
			MyLogger.getLogger().debug("Saving entry "+entry.getName()+" to disk");
			InputStream in = jar.getInputStream(entry);
			String outname = folder+File.separator+entry.getName();
			MyLogger.getLogger().debug("Writing Entry to "+outname);
			OutputStream out = new BufferedOutputStream(new FileOutputStream(outname));
			byte[] buffer = new byte[10240];
			int len;
			while((len = in.read(buffer)) >= 0)
				out.write(buffer, 0, len);
			in.close();
			out.close();
	}

}