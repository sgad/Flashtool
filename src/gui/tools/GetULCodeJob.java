package gui.tools;

import java.io.File;
import java.io.FileWriter;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.logger.MyLogger;
import org.system.DeviceChangedListener;
import org.system.OS;
import org.system.TextFile;

import flashsystem.TaEntry;
import flashsystem.X10flash;

public class GetULCodeJob extends Job {

	X10flash flash = null;
	boolean canceled = false;
	String blstatus = "";
	String ulcode = "";
	String imei = "";
	String serial = "";
	boolean alreadyunlocked = false;
	boolean relocked = false;

	
	public String getBLStatus() {
		return blstatus;
	}
	
	public String getULCode() {
		return ulcode;
	}

	public String getSerial() {
		return serial;
	}
	
	public String getIMEI() {
		return imei;
	}
	
	public boolean alreadyUnlocked() {
		return alreadyunlocked;
	}

	public GetULCodeJob(String name) {
		super(name);
	}
	
	public void setFlash(X10flash f) {
		flash=f;
	}
	
    protected IStatus run(IProgressMonitor monitor) {
    	try {
			flash.openDevice();
			flash.sendLoader();
			blstatus = flash.getPhoneProperty("ROOTING_STATUS");
			imei = flash.getPhoneProperty("IMEI");
			flash.openTA(2);
			TaEntry ta=flash.dumpProperty(2226);
			flash.closeTA();
			serial = flash.getSerial();
			if (ta==null) {
				File f = new File(OS.getWorkDir()+File.separator+"custom"+File.separator+serial+File.separator+"ulcode.txt");
				if (f.exists()) {
					TextFile t = new TextFile(f.getAbsolutePath(),"ISO-8859-1");
					ulcode = t.getLines().iterator().next();
					alreadyunlocked=true;
					relocked=true;
				}
				else {
					ulcode="";
					alreadyunlocked=false;
					flash.closeDevice();
					MyLogger.initProgress(0);
					DeviceChangedListener.pause(false);
				}
			}
			else {
				alreadyunlocked=true;
				if (ta.getDataSize()<=2) {
					relocked = true;
					File f = new File(OS.getWorkDir()+File.separator+"custom"+File.separator+serial+File.separator+"ulcode.txt");
					if (f.exists()) {
						TextFile t = new TextFile(f.getAbsolutePath(),"ISO-8859-1");
						ulcode = t.getLines().iterator().next();
					}
					else
						ulcode="";
				}
				else {
					ulcode = ta.getDataString();
					File f = new File(OS.getWorkDir()+File.separator+"custom"+File.separator+serial);
					if (!f.exists()) f.mkdir();
					File serial = new File(f.getAbsolutePath()+File.separator+"ulcode.txt");
					if (!serial.exists()) {
						FileWriter out = new FileWriter(serial);
						out.write(ulcode);
						out.flush();
						out.close();
						MyLogger.getLogger().info("Unlock code saved to "+serial.getAbsolutePath());
					}
				}
			}
			return Status.OK_STATUS;
    	}
    	catch (Exception e) {
    		e.printStackTrace();
    		MyLogger.getLogger().error(e.getMessage());
    		return Status.CANCEL_STATUS;
    	}
    }
    
    public boolean isRelocked() {
    	return relocked;
    }

}
