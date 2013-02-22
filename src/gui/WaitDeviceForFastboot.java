package gui;

import org.eclipse.core.runtime.Status;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import flashsystem.X10flash;
import gui.tools.SearchFastbootJob;
import gui.tools.SearchJob;
import gui.tools.WidgetsTool;

public class WaitDeviceForFastboot extends Dialog {

	protected Object result;
	protected Shell shlWaitForFastbootmode;
	protected SearchFastbootJob job;
	
	/**
	 * Create the dialog.
	 * @param parent
	 * @param style
	 */
	public WaitDeviceForFastboot(Shell parent, int style) {
		super(parent, style);
		setText("Wait for Fastboot mode");
	}

	/**
	 * Open the dialog.
	 * @return the result
	 */
	public Object open() {
		createContents();
		WidgetsTool.setSize(shlWaitForFastbootmode);
		shlWaitForFastbootmode.open();
		shlWaitForFastbootmode.layout();
		shlWaitForFastbootmode.addListener(SWT.Close, new Listener() {
		      public void handleEvent(Event event) {
					job.stopSearch();
					result = new String("Canceled");
		      }
		    });
		Display display = getParent().getDisplay();
		job = new SearchFastbootJob("Search Fastboot Job");
		job.schedule();
		while (!shlWaitForFastbootmode.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
			if (job.getState() == Status.OK) {
				result = new String("OK");
				shlWaitForFastbootmode.dispose();
			}
		}
		return result;
	}

	/**
	 * Create contents of the dialog.
	 */
	private void createContents() {
		shlWaitForFastbootmode = new Shell(getParent(), getStyle());
		shlWaitForFastbootmode.setSize(616, 429);
		shlWaitForFastbootmode.setText("Wait for Fastboot Mode");
		
		Composite composite = new Composite(shlWaitForFastbootmode, SWT.NONE);
		composite.setBounds(10, 10, 200, 348);
		
		Composite composite_1 = new Composite(shlWaitForFastbootmode, SWT.NONE);
		composite_1.setBounds(216, 10, 384, 348);
		
		Button btnCancel = new Button(shlWaitForFastbootmode, SWT.NONE);
		btnCancel.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				job.stopSearch();
				result = new String("Canceled");
				shlWaitForFastbootmode.dispose();
			}
		});
		btnCancel.setBounds(532, 364, 68, 23);
		btnCancel.setText("Cancel");
	}

}
