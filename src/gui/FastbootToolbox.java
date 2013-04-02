package gui;

import gui.tools.FastBootToolBoxJob;
import gui.tools.WidgetsTool;

import org.adb.AdbUtility;
import org.adb.FastbootUtility;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.logger.MyLogger;
import org.system.Devices;

public class FastbootToolbox extends Dialog {

	protected Object result;
	protected Shell shlFastbootToolbox;

	/**
	 * Create the dialog.
	 * @param parent
	 * @param style
	 */
	public FastbootToolbox(Shell parent, int style) {
		super(parent, style);
		setText("SWT Dialog");
	}

	/**
	 * Open the dialog.
	 * @return the result
	 */
	public Object open() {
		createContents();
		WidgetsTool.setSize(shlFastbootToolbox);
		shlFastbootToolbox.open();
		shlFastbootToolbox.layout();
		Display display = getParent().getDisplay();
		while (!shlFastbootToolbox.isDisposed()) {
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
		shlFastbootToolbox = new Shell(getParent(), getStyle());
		shlFastbootToolbox.setSize(629, 241);
		shlFastbootToolbox.setText("Fastboot Toolbox");
		shlFastbootToolbox.setLayout(new GridLayout(3, false));
		new Label(shlFastbootToolbox, SWT.NONE);
		new Label(shlFastbootToolbox, SWT.NONE);
		new Label(shlFastbootToolbox, SWT.NONE);
		
		Label lblVersion = new Label(shlFastbootToolbox, SWT.NONE);
		lblVersion.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
		lblVersion.setText("Version 1.0");
		
		Button btnNewButton_4 = new Button(shlFastbootToolbox, SWT.NONE);
		btnNewButton_4.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				doCheckDeviceStatus();
			}
		});
		btnNewButton_4.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
		btnNewButton_4.setText("Check Current Device Status");
		
		Label lblByDooMLoRD = new Label(shlFastbootToolbox, SWT.NONE);
		lblByDooMLoRD.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
		lblByDooMLoRD.setText("By DooMLoRD");
		
		Button btnNewButton = new Button(shlFastbootToolbox, SWT.NONE);
		btnNewButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				doRebootFastbootViaAdb();
			}
		});
		btnNewButton.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
		btnNewButton.setText("Reboot into fastboot mode (via ADB)");
		new Label(shlFastbootToolbox, SWT.NONE);
		
		Button btnRebootIntoFastboot = new Button(shlFastbootToolbox, SWT.NONE);
		btnRebootIntoFastboot.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				doRebootBackIntoFastbootMode();
			}
		});
		btnRebootIntoFastboot.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
		btnRebootIntoFastboot.setText("Reboot into fastboot mode (via Fastboot)");
		
		Button btnNewButton_1 = new Button(shlFastbootToolbox, SWT.NONE);
		btnNewButton_1.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				FileDialog dlg = new FileDialog(shlFastbootToolbox);
		        dlg.setFilterExtensions(new String[]{"*.sin","*.elf","*.img"});
		        dlg.setText("Kernel Chooser");
		        String dir = dlg.open();
		        if (dir!=null)
		        	doHotBoot(dir);
			}
		});
		btnNewButton_1.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
		btnNewButton_1.setText("Select kernel to HotBoot");
		
		Button btnNewButton_3 = new Button(shlFastbootToolbox, SWT.NONE);
		btnNewButton_3.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				FileDialog dlg = new FileDialog(shlFastbootToolbox);
		        dlg.setFilterExtensions(new String[]{"*.sin","*.img","*.ext4","*.yaffs2"});
		        dlg.setText("System Chooser");
		        String dir = dlg.open();
		        if (dir!=null)
		        	doHotBoot(dir);
			}
		});
		btnNewButton_3.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
		btnNewButton_3.setText("Select system to Flash");
		
		Button btnSelectKernelTo = new Button(shlFastbootToolbox, SWT.NONE);
		btnSelectKernelTo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				FileDialog dlg = new FileDialog(shlFastbootToolbox);
		        dlg.setFilterExtensions(new String[]{"*.sin","*.elf","*.img"});
		        dlg.setText("Kernel Chooser");
		        String dir = dlg.open();
		        if (dir!=null)
		        	doFlashKernel(dir);
			}
		});
		btnSelectKernelTo.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
		btnSelectKernelTo.setText("Select kernel to Flash");
		
		Button btnNewButton_2 = new Button(shlFastbootToolbox, SWT.NONE);
		btnNewButton_2.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				doGetFastbootVerInfo();
			}
		});
		btnNewButton_2.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
		btnNewButton_2.setText("Get Ver Info");
		new Label(shlFastbootToolbox, SWT.NONE);
		
		Button btnGetDeviceInfo = new Button(shlFastbootToolbox, SWT.NONE);
		btnGetDeviceInfo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				doGetConnectedDeviceInfo();
			}
		});
		btnGetDeviceInfo.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
		btnGetDeviceInfo.setText("Get Device Info");
		new Label(shlFastbootToolbox, SWT.NONE);
		
		Button btnRebootDeviceInto = new Button(shlFastbootToolbox, SWT.NONE);
		btnRebootDeviceInto.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				doFastbootReboot();
			}
		});
		btnRebootDeviceInto.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
		btnRebootDeviceInto.setText("Reboot device into system");
		new Label(shlFastbootToolbox, SWT.NONE);
		new Label(shlFastbootToolbox, SWT.NONE);
		new Label(shlFastbootToolbox, SWT.NONE);
		
		Button btnClose = new Button(shlFastbootToolbox, SWT.NONE);
		btnClose.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				shlFastbootToolbox.dispose();
			}
		});
		btnClose.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		btnClose.setText("Close");

	}

	public void doRebootFastbootViaAdb() {
		FastBootToolBoxJob job = new FastBootToolBoxJob("Reboot fastboot via ADB");
		job.setAction("doRebootFastbootViaAdb");
		job.schedule();
	}
	
	public void doCheckDeviceStatus(){
		FastBootToolBoxJob job = new FastBootToolBoxJob("Check Device Status");
		job.setAction("doCheckDeviceStatus");
		job.schedule();
	}

	public void doGetConnectedDeviceInfo() {
		FastBootToolBoxJob job = new FastBootToolBoxJob("Get Device Infos");
		job.setAction("doGetConnectedDeviceInfo");
		job.schedule();
	}

	public void doGetFastbootVerInfo() {
		FastBootToolBoxJob job = new FastBootToolBoxJob("Get Device Vers Infos");
		job.setAction("doGetFastbootVerInfo");
		job.schedule();
	}
	
	public void doRebootBackIntoFastbootMode() {
		FastBootToolBoxJob job = new FastBootToolBoxJob("Reboot device into fastboot");
		job.setAction("doRebootBackIntoFastbootMode");
		job.schedule();
	}

	public void doFastbootReboot() {
		FastBootToolBoxJob job = new FastBootToolBoxJob("Reboot device");
		job.setAction("doFastbootReboot");
		job.schedule();
	}

	public void doHotBoot(String kernel) {
		FastBootToolBoxJob job = new FastBootToolBoxJob("Hotboot device");
		job.setAction("doHotbootKernel");
		job.setImage(kernel);
		job.schedule();
	}

	public void doFlashKernel(String kernel) {
		FastBootToolBoxJob job = new FastBootToolBoxJob("Flash kernel to device");
		job.setAction("doFlashKernel");
		job.setImage(kernel);
		job.schedule();
	}

	public void doFlashSystem(String system) {
		FastBootToolBoxJob job = new FastBootToolBoxJob("Flash system to device");
		job.setAction("doFlashSystem");
		job.setImage(system);
		job.schedule();
	}

}
