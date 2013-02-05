package gui;

import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class BLUWizard extends Dialog {

	protected Object result;
	protected Shell shlBootloaderUnlockWizard;
	private Text textIMEI;
	private Text textULCODE;
	private Button btnGetUnlock;
	private Button btnUnlock;

	/**
	 * Create the dialog.
	 * @param parent
	 * @param style
	 */
	public BLUWizard(Shell parent, int style) {
		super(parent, style);
		setText("Bootmode chooser");
	}

	/**
	 * Open the dialog.
	 * @return the result
	 */
	public Object open(String imei, String ulcode) {
		createContents();
		textIMEI.setText(imei);
		textULCODE.setText(ulcode);
		if (ulcode.length()>0) {
			btnUnlock.setEnabled(false);
			btnGetUnlock.setEnabled(false);
			textULCODE.setEditable(false);
		}
		shlBootloaderUnlockWizard.open();
		shlBootloaderUnlockWizard.layout();
		Display display = getParent().getDisplay();
		while (!shlBootloaderUnlockWizard.isDisposed()) {
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
		shlBootloaderUnlockWizard = new Shell(getParent(), getStyle());
		shlBootloaderUnlockWizard.addListener(SWT.Close, new Listener() {
		      public void handleEvent(Event event) {
		    	  result = "";
		    	  event.doit = true;
		      }
		    });
		shlBootloaderUnlockWizard.setSize(286, 219);
		shlBootloaderUnlockWizard.setText("BootLoader Unlock Wizard");
		
		Label lblImei = new Label(shlBootloaderUnlockWizard, SWT.NONE);
		lblImei.setBounds(10, 10, 55, 15);
		lblImei.setText("IMEI : ");
		
		textIMEI = new Text(shlBootloaderUnlockWizard, SWT.BORDER);
		textIMEI.setEditable(false);
		textIMEI.setBounds(92, 7, 164, 21);
		
		btnGetUnlock = new Button(shlBootloaderUnlockWizard, SWT.NONE);
		btnGetUnlock.setBounds(110, 40, 118, 25);
		btnGetUnlock.setText("Get Unlock Code");
		
		Label lblUnlockCode = new Label(shlBootloaderUnlockWizard, SWT.NONE);
		lblUnlockCode.setBounds(10, 82, 85, 15);
		lblUnlockCode.setText("Unlock Code :");
		
		textULCODE = new Text(shlBootloaderUnlockWizard, SWT.BORDER);
		textULCODE.setBounds(92, 79, 164, 21);
		
		btnUnlock = new Button(shlBootloaderUnlockWizard, SWT.NONE);
		btnUnlock.setBounds(133, 114, 75, 25);
		btnUnlock.setText("Unlock");
		
		Button btnNewButton_2 = new Button(shlBootloaderUnlockWizard, SWT.NONE);
		btnNewButton_2.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				shlBootloaderUnlockWizard.dispose();
			}
		});
		btnNewButton_2.setBounds(195, 155, 75, 25);
		btnNewButton_2.setText("Cancel");

	}
}
