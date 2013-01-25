package gui.tools;

import flashsystem.X10flash;
import gui.BootModeSelector;
import gui.DeviceSelector;
import gui.LoaderSelect;
import gui.WaitDeviceForFlashmode;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.Widget;

public class WidgetTask {
	
	public static void setEnabled(final ToolItem item, final boolean status) {
		Display.getDefault().asyncExec(
				new Runnable() {
					public void run() {
						item.setEnabled(status);
					}
				}
		);
	}

	public static String openDeviceSelector(final Shell parent) {
		final Result res = new Result();
		Display.getDefault().syncExec(
				new Runnable() {
					public void run() {
			    		DeviceSelector dial = new DeviceSelector(parent,SWT.PRIMARY_MODAL | SWT.SHEET);
			    		Object obj = dial.open();
						res.setResult(obj);
						
					}
				}
		);
		return (String)res.getResult();
	}

	public static String openLoaderSelect(final Shell parent) {
		final Result res = new Result();
		Display.getDefault().syncExec(
				new Runnable() {
					public void run() {
			    		LoaderSelect dial = new LoaderSelect(parent,SWT.PRIMARY_MODAL | SWT.SHEET);
			    		Object obj = dial.open();
						res.setResult(obj);
						
					}
				}
		);
		return (String)res.getResult();
	}

	public static String openBootModeSelector(final Shell parent) {
		final Result res = new Result();
		Display.getDefault().syncExec(
				new Runnable() {
					public void run() {
			    		BootModeSelector dial = new BootModeSelector(parent,SWT.PRIMARY_MODAL | SWT.SHEET);
			    		Object obj = dial.open();
						res.setResult(obj);
						
					}
				}
		);
		return (String)res.getResult();
	}

	public static String openWaitDeviceForFlashmode(final Shell parent, final X10flash flash) {
		final Result res = new Result();
		Display.getDefault().syncExec(
				new Runnable() {
					public void run() {
			    		WaitDeviceForFlashmode dial = new WaitDeviceForFlashmode(parent,SWT.PRIMARY_MODAL | SWT.SHEET);
			    		Object obj = dial.open(flash);
						res.setResult(obj);
						
					}
				}
		);
		return (String)res.getResult();
	}

	public static class Result {
		
		private Object _res=null;
		
		public Object getResult() {
			return _res;
		}
		
		public void setResult(Object res) {
			_res = res;
		}
	
	}
}
