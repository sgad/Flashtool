package gui;

import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import flashsystem.X10flash;
import gui.tools.SearchJob;

public class WaitDeviceForFlashmode extends Dialog {

	protected Object result;
	protected Shell shlWaitForFlashmode;
	protected SearchJob job;
	
	/**
	 * Create the dialog.
	 * @param parent
	 * @param style
	 */
	public WaitDeviceForFlashmode(Shell parent, int style) {
		super(parent, style);
		setText("Wait for Flashmode");
	}

	/**
	 * Open the dialog.
	 * @return the result
	 */
	public Object open(final X10flash flash) {
		createContents();
		shlWaitForFlashmode.open();
		shlWaitForFlashmode.layout();
		Display display = getParent().getDisplay();
		job = new SearchJob("First Job");
		job.setFlash(flash);
		job.schedule();
		while (!shlWaitForFlashmode.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		return result;
	}

	/**
	 * Create contents of the dialog.
	 */
	private void createContents() {
		shlWaitForFlashmode = new Shell(getParent(), getStyle());
		shlWaitForFlashmode.setSize(616, 429);
		shlWaitForFlashmode.setText("Wait for Flashmode");
		
		Composite composite = new Composite(shlWaitForFlashmode, SWT.NONE);
		composite.setBounds(10, 10, 200, 348);
		
		Composite composite_1 = new Composite(shlWaitForFlashmode, SWT.NONE);
		composite_1.setBounds(216, 10, 384, 348);
		
		final GifCLabel lbl = new GifCLabel(composite_1, SWT.CENTER);
		lbl.setText("");
		lbl.setGifImage(this.getClass().getResourceAsStream("/gui/ressources/flashmode.gif"));
		lbl.setBounds(10, 35, 350, 346);
		
		Button btnCancel = new Button(shlWaitForFlashmode, SWT.NONE);
		btnCancel.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				job.stopSearch();
				shlWaitForFlashmode.dispose();
			}
		});
		btnCancel.setBounds(532, 364, 68, 23);
		btnCancel.setText("Cancel");
	}

}
