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

import flashsystem.X10flash;

public class GetULCodeJob extends Job {

	X10flash flash = null;
	boolean canceled = false;
	String blstatus = "";
	String ulcode = "";
	String imei = "";
	boolean alreadyunlocked = false;

	
	public String getBLStatus() {
		return blstatus;
	}
	
	public String getULCode() {
		return ulcode;
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
			if (blstatus.equals("ROOTED")) {
				flash.openTA(2);
				ulcode=flash.dumpProperty(2226,"string");
				flash.closeTA();
				if (ulcode.length()>0) {
					File f = new File(OS.getWorkDir()+File.separator+"custom"+File.separator+flash.getSerial());
					if (!f.exists()) f.mkdir();
					File serial = new File(f.getAbsolutePath()+File.separator+"ulcode.txt");
					FileWriter out = new FileWriter(serial);
					out.write(ulcode);
					out.flush();
					out.close();
					MyLogger.getLogger().info("Unlock code saved to "+serial.getAbsolutePath());
				}
			}
			else {
				File f = new File(OS.getWorkDir()+File.separator+"custom"+File.separator+flash.getSerial()+File.separator+"ulcode.txt");
				if (f.exists()) {
					TextFile t = new TextFile(f.getAbsolutePath(),"ISO-8859-1");
					ulcode = t.getLines().iterator().next();
					alreadyunlocked=true;
				}
				else {
					flash.closeDevice();
					MyLogger.initProgress(0);
					DeviceChangedListener.pause(false);
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
}
