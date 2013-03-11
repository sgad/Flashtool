package gui;

import flashsystem.TaEntry;
import flashsystem.X10flash;
import gui.tools.BLUnlockJob;
import gui.tools.WidgetsTool;

import org.adb.FastbootUtility;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.logger.MyLogger;
import org.system.RunOutputs;

public class BLUWizard extends Dialog {

	protected Object result;
	protected Shell shlBootloaderUnlockWizard;
	private Text textIMEI;
	private Text textULCODE;
	private Button btnGetUnlock;
	private Button btnUnlock;
	private X10flash _flash;
	private String _action;

	/**
	 * Create the dialog.
	 * @param parent
	 * @param style
	 */
	public BLUWizard(Shell parent, int style) {
		super(parent, style);
	}

	/**
	 * Open the dialog.
	 * @return the result
	 */
	public Object open(String imei, String ulcode,X10flash flash, String action) {
		_action = action;
		_flash = flash;
		createContents();
		textIMEI.setText(imei);
		textULCODE.setText(ulcode);
		if (ulcode.length()>0) {
			btnUnlock.setEnabled(true);
			if (_action.equals("R")) {
				btnUnlock.setText("Relock");
			}
			btnGetUnlock.setEnabled(false);
			textULCODE.setEditable(false);
		}
		WidgetsTool.setSize(shlBootloaderUnlockWizard);
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
		shlBootloaderUnlockWizard.setSize(286, 183);
		shlBootloaderUnlockWizard.setText("BootLoader Unlock Wizard");
		
		Label lblImei = new Label(shlBootloaderUnlockWizard, SWT.NONE);
		lblImei.setBounds(10, 10, 55, 15);
		lblImei.setText("IMEI : ");
		
		textIMEI = new Text(shlBootloaderUnlockWizard, SWT.BORDER);
		textIMEI.setEditable(false);
		textIMEI.setBounds(106, 7, 164, 21);
		
		btnGetUnlock = new Button(shlBootloaderUnlockWizard, SWT.NONE);
		btnGetUnlock.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Program.launch("http://unlockbootloader.sonymobile.com/unlock/step1");
			}
		});
		btnGetUnlock.setBounds(127, 34, 118, 25);
		btnGetUnlock.setText("Get Unlock Code");
		
		Label lblUnlockCode = new Label(shlBootloaderUnlockWizard, SWT.NONE);
		lblUnlockCode.setBounds(10, 68, 85, 15);
		lblUnlockCode.setText("Unlock Code :");
		
		textULCODE = new Text(shlBootloaderUnlockWizard, SWT.BORDER);
		textULCODE.setBounds(106, 65, 164, 21);
		
		btnUnlock = new Button(shlBootloaderUnlockWizard, SWT.NONE);
		btnUnlock.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (textULCODE.getText().length()==0) {
					showErrorMessageBox("Your must enter an unlock code");
					return;
				}
				if (_flash==null) {
					BLUnlockJob bj = new BLUnlockJob("Unlock Job");
					bj.setULCode(textULCODE.getText());
					bj.schedule();
					btnUnlock.setEnabled(false);
				}
				else {
					if (_action.equals("R")) {
						TaEntry ta = new TaEntry();
						ta.setPartition(2226);
						byte[] data = new byte[2];data[0]=0;data[1]=0;
						ta.setData(data);
						try {
							MyLogger.getLogger().info("Relocking device");
							_flash.openTA(2);
							_flash.sendTAUnit(ta);
							_flash.closeTA();
							MyLogger.getLogger().info("Relock finished");
							btnUnlock.setEnabled(false);
						}
						catch (Exception exc) {
							exc.printStackTrace();
						}
					}
					else {
						TaEntry ta = new TaEntry();
						ta.setPartition(2226);
						ta.setData(textULCODE.getText().getBytes());
						try {
							MyLogger.getLogger().info("Unlocking device");
							_flash.openTA(2);
							_flash.sendTAUnit(ta);
							_flash.closeTA();
							MyLogger.getLogger().info("Unlock finished");
							btnUnlock.setEnabled(false);
						}
						catch (Exception exc) {
							exc.printStackTrace();
						}
					}
				}
			}
		});
		btnUnlock.setBounds(144, 92, 75, 25);
		btnUnlock.setText("Unlock");
		
		Button btnNewButton_2 = new Button(shlBootloaderUnlockWizard, SWT.NONE);
		btnNewButton_2.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				shlBootloaderUnlockWizard.dispose();
			}
		});
		btnNewButton_2.setBounds(195, 123, 75, 25);
		btnNewButton_2.setText("Close");

	}

	public void showErrorMessageBox(String message) {
		MessageBox mb = new MessageBox(shlBootloaderUnlockWizard,SWT.ICON_ERROR|SWT.OK);
		mb.setText("Errorr");
		mb.setMessage(message);
		int result = mb.open();
	}

}
