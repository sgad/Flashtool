package gui.tools;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.logger.MyLogger;

import flashsystem.X10flash;

public class FlashJob extends Job {

	X10flash flash = null;
	boolean canceled = false;

	public FlashJob(String name) {
		super(name);
	}
	
	public void setFlash(X10flash f) {
		flash=f;
	}
	
    protected IStatus run(IProgressMonitor monitor) {
    	try {
    		if (flash.getBundle().open()) {
    			flash.openDevice();
    			flash.flashDevice();
    			flash.getBundle().close();
    		}
    		else {
    			MyLogger.getLogger().info("Cannot open bundle. Flash operation canceled");
    		}
			return Status.OK_STATUS;
    	}
    	catch (Exception e) {
    		e.printStackTrace();
    		return Status.CANCEL_STATUS;
    	}
    }
}
