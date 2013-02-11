package gui.tools;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import flashsystem.SinFile;

public class ExtractSinDataJob extends Job {

	boolean canceled = false;
	SinFile sin;

	public ExtractSinDataJob(String name) {
		super(name);
	}
	
	public void setSin(SinFile f) {
		sin=f;
	}
	
    protected IStatus run(IProgressMonitor monitor) {
    	try {
    		sin.dumpImage();
			return Status.OK_STATUS;
    	}
    	catch (Exception e) {
    		e.printStackTrace();
    		return Status.CANCEL_STATUS;
    	}
    }
}
