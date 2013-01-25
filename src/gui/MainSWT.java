package gui;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Properties;
import linuxlib.JUsb;
import org.adb.AdbUtility;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.wb.swt.SWTResourceManager;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.lang.Language;
import org.logger.MyLogger;
import org.system.AdbPhoneThread;
import org.system.DeviceChangedListener;
import org.system.DeviceEntry;
import org.system.DeviceProperties;
import org.system.Devices;
import org.system.GlobalConfig;
import org.system.OS;
import org.system.StatusEvent;
import org.system.StatusListener;
import org.system.VersionChecker;

import flashsystem.Bundle;
import flashsystem.BundleException;
import flashsystem.X10flash;
import foxtrot.Job;
import foxtrot.Worker;
import gui.tools.WidgetTask;
import org.eclipse.swt.custom.ScrolledComposite;

public class MainSWT {

	protected Shell shell;
	private static AdbPhoneThread phoneWatchdog;
	private static boolean guimode=false;
	protected ToolItem tltmFlash;
	protected ToolItem tltmRoot;
	protected ToolItem tltmAskRoot;
	
	/**
	 * Open the window.
	 */
	public void open() {
		Display.setAppName("Flashtool");
		Display display = Display.getDefault();
		createContents();
		guimode=true;
		StatusListener phoneStatus = new StatusListener() {
			public void statusChanged(StatusEvent e) {
				if (!e.isDriverOk()) {
					MyLogger.getLogger().error("Drivers need to be installed for connected device.");
					MyLogger.getLogger().error("You can find them in the drivers folder of Flashtool.");
				}
				else {
					if (e.getNew().equals("adb")) {
						MyLogger.getLogger().info("Device connected with USB debugging on");
						MyLogger.getLogger().debug("Device connected, continuing with identification");
						doIdent();
					}
					if (e.getNew().equals("none")) {
						MyLogger.getLogger().info("Device disconnected");
						doDisableIdent();
					}
					if (e.getNew().equals("flash")) {
						MyLogger.getLogger().info("Device connected in flash mode");
						doDisableIdent();
					}
					if (e.getNew().equals("fastboot")) {
						MyLogger.getLogger().info("Device connected in fastboot mode");
						doDisableIdent();
					}
					if (e.getNew().equals("normal")) {
						MyLogger.getLogger().info("Device connected with USB debugging off");
						MyLogger.getLogger().info("For 2011 devices line, be sure you are not in MTP mode");
						doDisableIdent();
					}
				}
			}
		};
		killAdbandFastboot();
		phoneWatchdog = new AdbPhoneThread();
		phoneWatchdog.start();
		phoneWatchdog.addStatusListener(phoneStatus);
		VersionChecker vcheck = new VersionChecker();
		vcheck.setMessageFrame(shell);
		vcheck.start();
		shell.open();
		shell.layout();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		exitProgram();
	}

	public void doDisableIdent() {
		WidgetTask.setEnabled(tltmFlash,true);
		WidgetTask.setEnabled(tltmRoot,false);
		WidgetTask.setEnabled(tltmAskRoot,false);
	}
	
	/**
	 * Create contents of the window.
	 * @wbp.parser.entryPoint
	 */
	protected void createContents() {
		shell = new Shell();
		shell.setSize(794, 460);
		shell.setText("SWT Application");
		shell.setImage(SWTResourceManager.getImage(FlashtoolSWT.class, "/gui/ressources/icons/flash_32.png"));
		shell.setLayout(new FormLayout());
		
		Menu menu = new Menu(shell, SWT.BAR);
		shell.setMenuBar(menu);
		
		MenuItem mntmFile = new MenuItem(menu, SWT.CASCADE);
		mntmFile.setText("File");
		
		Menu menu_1 = new Menu(mntmFile);
		mntmFile.setMenu(menu_1);
		
		MenuItem mntmExit = new MenuItem(menu_1, SWT.NONE);
		mntmExit.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				shell.dispose();
			}
		});
		mntmExit.setText("Exit");
		
		MenuItem mntmPlugins = new MenuItem(menu, SWT.CASCADE);
		mntmPlugins.setText("Plugins");
		
		Menu menu_2 = new Menu(mntmPlugins);
		mntmPlugins.setMenu(menu_2);

		ToolBar toolBar = new ToolBar(shell, SWT.FLAT | SWT.RIGHT);
		FormData fd_toolBar = new FormData();
		fd_toolBar.right = new FormAttachment(0, 158);
		fd_toolBar.top = new FormAttachment(0, 10);
		fd_toolBar.left = new FormAttachment(0, 10);
		toolBar.setLayoutData(fd_toolBar);
		
		tltmFlash = new ToolItem(toolBar, SWT.NONE);
		tltmFlash.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					doFlash();
				} catch (Exception ex) {}
			}
		});
		tltmFlash.setImage(SWTResourceManager.getImage(MainSWT.class, "/gui/ressources/icons/flash_32.png"));
		tltmFlash.setToolTipText("Flash device");
		
		tltmRoot = new ToolItem(toolBar, SWT.NONE);
		tltmRoot.setImage(SWTResourceManager.getImage(MainSWT.class, "/gui/ressources/icons/root_32.png"));
		tltmRoot.setEnabled(false);
		tltmRoot.setToolTipText("Root device");
		
		Button btnSaveLog = new Button(shell, SWT.NONE);
		FormData fd_btnSaveLog = new FormData();
		fd_btnSaveLog.bottom = new FormAttachment(100, -33);
		fd_btnSaveLog.left = new FormAttachment(100, -66);
		fd_btnSaveLog.right = new FormAttachment(100, -10);
		btnSaveLog.setLayoutData(fd_btnSaveLog);
		btnSaveLog.setText("Save log");
		MyLogger.setLevel(GlobalConfig.getProperty("loglevel").toUpperCase());
		try {
		Language.Init(GlobalConfig.getProperty("language").toLowerCase());
		} catch (Exception e) {
			MyLogger.getLogger().info("Language files not installed");
		}
		MyLogger.getLogger().info("Flashtool "+About.getVersion());
		if (JUsb.version.length()>0)
			MyLogger.getLogger().info(JUsb.version);
		
		tltmAskRoot = new ToolItem(toolBar, SWT.NONE);
		tltmAskRoot.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				doAskRoot();
			}
		});
		tltmAskRoot.setImage(SWTResourceManager.getImage(MainSWT.class, "/gui/ressources/icons/askroot_32.png"));
		tltmAskRoot.setEnabled(false);
		tltmAskRoot.setToolTipText("Ask for root permissions");
		
		ProgressBar progressBar = new ProgressBar(shell, SWT.NONE);
		FormData fd_progressBar = new FormData();
		fd_progressBar.right = new FormAttachment(btnSaveLog, 0, SWT.RIGHT);
		fd_progressBar.top = new FormAttachment(btnSaveLog, 6);
		fd_progressBar.left = new FormAttachment(0, 10);
		progressBar.setLayoutData(fd_progressBar);
		
		ScrolledComposite scrolledComposite = new ScrolledComposite(shell, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		FormData fd_scrolledComposite = new FormData();
		fd_scrolledComposite.bottom = new FormAttachment(btnSaveLog, -6);
		fd_scrolledComposite.right = new FormAttachment(btnSaveLog, 0, SWT.RIGHT);
		fd_scrolledComposite.top = new FormAttachment(toolBar, 6);
		fd_scrolledComposite.left = new FormAttachment(toolBar, 0, SWT.LEFT);
		scrolledComposite.setLayoutData(fd_scrolledComposite);
		scrolledComposite.setExpandHorizontal(true);
		scrolledComposite.setExpandVertical(true);
		
		StyledText logWindow = new StyledText(scrolledComposite, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		logWindow.setEditable(false);
		MyLogger.appendTextArea(logWindow);
		scrolledComposite.setContent(logWindow);
		scrolledComposite.setMinSize(logWindow.computeSize(SWT.DEFAULT, SWT.DEFAULT));
	}

	public static void stopPhoneWatchdog() {
		DeviceChangedListener.stop();
		if (phoneWatchdog!=null) {
			phoneWatchdog.done();
			try {
				phoneWatchdog.join();
			}
			catch (Exception e) {
			}
		}
	}

	public static void killAdbandFastboot() {
		stopPhoneWatchdog();
	}

	public void exitProgram() {
		try {
			MyLogger.getLogger().info("Stopping watchdogs and exiting ...");
			if (GlobalConfig.getProperty("killadbonexit").equals("yes")) {
				killAdbandFastboot();
			}
			System.exit(0);
		}
		catch (Exception e) {}		
	}

	public void doIdent() {
    	if (guimode) {
    		Enumeration<Object> e = Devices.listDevices(true);
    		if (!e.hasMoreElements()) {
    			MyLogger.getLogger().error("No device is registered in Flashtool.");
    			MyLogger.getLogger().error("You can only flash devices.");
    			return;
    		}
    		boolean found = false;
    		Properties founditems = new Properties();
    		founditems.clear();
    		Properties buildprop = new Properties();
    		buildprop.clear();
    		while (e.hasMoreElements()) {
    			DeviceEntry current = Devices.getDevice((String)e.nextElement());
    			String prop = current.getBuildProp();
    			if (!buildprop.containsKey(prop)) {
    				String readprop = DeviceProperties.getProperty(prop);
    				buildprop.setProperty(prop,readprop);
    			}
    			Iterator<String> i = current.getRecognitionList().iterator();
    			String localdev = buildprop.getProperty(prop);
    			while (i.hasNext()) {
    				String pattern = i.next().toUpperCase();
    				if (localdev.toUpperCase().equals(pattern)) {
    					founditems.put(current.getId(), current.getName());
    				}
    			}
    		}
    		if (founditems.size()==1) {
    			found = true;
    			Devices.setCurrent((String)founditems.keys().nextElement());
    			if (!Devices.isWaitingForReboot())
    				MyLogger.getLogger().info("Connected device : " + Devices.getCurrent().getId());
    		}
    		else {
    			MyLogger.getLogger().error("Cannot identify your device.");
        		MyLogger.getLogger().info("Selecting from user input");
        		String devid=(String)WidgetTask.openDeviceSelector(shell);
        		deviceSelectGui devsel = new deviceSelectGui(null);
        		devid = devsel.getDeviceFromList(founditems);
    			if (devid.length()>0) {
        			found = true;
        			Devices.setCurrent(devid);
        			String prop = DeviceProperties.getProperty(Devices.getCurrent().getBuildProp());
        			if (!Devices.getCurrent().getRecognition().contains(prop)) {
        				String[] choices = {"Yes", "No"};
        				MessageBox messageBox = new MessageBox(shell, SWT.ICON_QUESTION |SWT.YES | SWT.NO);
        			    messageBox.setMessage("Do you want to permanently identify this device as \n"+Devices.getCurrent().getName()+"?");
        			    int response = messageBox.open();
        				if (response == SWT.YES)
        					Devices.getCurrent().addRecognitionToList(prop);
        			}
	        		if (!Devices.isWaitingForReboot())
	        			MyLogger.getLogger().info("Connected device : " + Devices.getCurrent().getId());
        		}
        		else {
        			MyLogger.getLogger().error("You can only flash devices.");
        		}
    		}
    		if (found) {
    			try {
    				// show device menu here
    			}catch (Exception e1) {}
    			if (!Devices.isWaitingForReboot()) {
    				MyLogger.getLogger().info("Installed version of busybox : " + Devices.getCurrent().getInstalledBusyboxVersion(false));
    				MyLogger.getLogger().info("Android version : "+Devices.getCurrent().getVersion()+" / kernel version : "+Devices.getCurrent().getKernelVersion()+" / Build number : "+Devices.getCurrent().getBuildId());
    			}
    			if (Devices.getCurrent().isRecovery()) {
    				MyLogger.getLogger().info("Phone in recovery mode");
    				WidgetTask.setEnabled(tltmRoot,false);
    				WidgetTask.setEnabled(tltmAskRoot,false);
    				doGiveRoot();
    			}
    			else {
    				boolean hasSU = Devices.getCurrent().hasSU();
    				WidgetTask.setEnabled(tltmRoot, !hasSU);
    				if (hasSU) {
    					boolean hasRoot = Devices.getCurrent().hasRoot();
    					if (hasRoot) {
    						doInstFlashtool();
    						doGiveRoot();
    					}
    					WidgetTask.setEnabled(tltmAskRoot,!hasRoot);
    				}
    			}
    			MyLogger.getLogger().debug("Now setting buttons availability - btnRoot");
    			MyLogger.getLogger().debug("mtmRootzergRush menu");
    			/*mntmRootzergRush.setEnabled(true);
    			MyLogger.getLogger().debug("mtmRootPsneuter menu");
    			mntmRootPsneuter.setEnabled(true);
    			MyLogger.getLogger().debug("mtmRootEmulator menu");
    			mntmRootEmulator.setEnabled(true);
    			MyLogger.getLogger().debug("mtmRootAdbRestore menu");
    			mntmRootAdbRestore.setEnabled(true);
    			MyLogger.getLogger().debug("mtmUnRoot menu");
    			mntmUnRoot.setEnabled(true);*/

    			boolean flash = Devices.getCurrent().canFlash();
    			MyLogger.getLogger().debug("flashBtn button "+flash);
    			WidgetTask.setEnabled(tltmFlash,flash);
    			//MyLogger.getLogger().debug("custBtn button");
    			//custBtn.setEnabled(true);
    			MyLogger.getLogger().debug("Now adding plugins");
    			//mnPlugins.removeAll();
    			//addDevicesPlugins();
    			//addGenericPlugins();
    			MyLogger.getLogger().debug("Stop waiting for device");
    			if (Devices.isWaitingForReboot())
    				Devices.stopWaitForReboot();
    			MyLogger.getLogger().debug("End of identification");
    		}
    	}
    	File f = new File(OS.getWorkDir()+File.separator+"custom"+File.separator+"apps_saved"+File.separator+Devices.getCurrent().getId());
    	f.mkdir();
    	f = new File(OS.getWorkDir()+File.separator+"custom"+File.separator+"clean"+File.separator+Devices.getCurrent().getId());
    	f.mkdir();
	}

	public void doGiveRoot() {
		/*btnCleanroot.setEnabled(true);
		mntmInstallBusybox.setEnabled(true);
		mntmClearCache.setEnabled(true);
		mntmBuildpropEditor.setEnabled(true);
		if (new File(OS.getWorkDir()+fsep+"devices"+fsep+Devices.getCurrent().getId()+fsep+"rebrand").isDirectory())
			mntmBuildpropRebrand.setEnabled(true);
		mntmRebootIntoRecoveryT.setEnabled(Devices.getCurrent().canRecovery());
		mntmRebootDefaultRecovery.setEnabled(true);
		mntmSetDefaultRecovery.setEnabled(Devices.getCurrent().canRecovery());
		mntmSetDefaultKernel.setEnabled(Devices.getCurrent().canKernel());
		mntmRebootCustomKernel.setEnabled(Devices.getCurrent().canKernel());
		mntmRebootDefaultKernel.setEnabled(true);
		//mntmInstallBootkit.setEnabled(true);
		//mntmRecoveryControler.setEnabled(true);
		mntmBackupSystemApps.setEnabled(true);
		btnXrecovery.setEnabled(Devices.getCurrent().canRecovery());
		btnKernel.setEnabled(Devices.getCurrent().canKernel());*/
		if (!Devices.isWaitingForReboot())
			MyLogger.getLogger().info("Root Access Allowed");  	
    }

	public void doAskRoot() {
		Worker.post(new Job() {
			public Object run() {
				MyLogger.getLogger().warn("Please check your Phone and 'ALLOW' Superuseraccess!");
        		if (!AdbUtility.hasRootPerms()) {
        			MyLogger.getLogger().error("Please Accept root permissions on the phone");
        		}
        		else {
        			doGiveRoot();
        		}
        		return null;
			}
		});
	}
    
	public void doInstFlashtool() {
		try {
			if (!AdbUtility.exists("/system/flashtool")) {
				Devices.getCurrent().doBusyboxHelper();
				MyLogger.getLogger().info("Installing toolbox to device...");
				AdbUtility.push(OS.getWorkDir()+File.pathSeparator+"custom"+File.pathSeparator+"root"+File.pathSeparator+"ftkit.tar",GlobalConfig.getProperty("deviceworkdir"));
				org.system.Shell ftshell = new org.system.Shell("installftkit");
				ftshell.runRoot();
			}
		}
		catch (Exception e) {
			MyLogger.getLogger().error(e.getMessage());
		}
    }

	public void doFlash() throws Exception {
		String select = WidgetTask.openBootModeSelector(shell);
		if (select.equals("flashmode"))
			doFlashmode("","");
		if (select.equals("fastboot"))
			doFastBoot();
	}
	
	public void doFastBoot() throws Exception {
		FastBootToolboxGUI box = new FastBootToolboxGUI();
		box.setVisible(true);
	}
	
	public void doFlashmode(final String pftfpath, final String pftfname) throws Exception {
		try {
			FTFSelector ftfsel = new FTFSelector(shell,SWT.PRIMARY_MODAL | SWT.SHEET);
			final Bundle bundle = (Bundle)ftfsel.open(pftfpath, pftfname);
			if (bundle !=null) {
				X10flash flash=null;
	    		bundle.open();
		    	bundle.setSimulate(GlobalConfig.getProperty("simulate").toLowerCase().equals("yes"));
				flash = new X10flash(bundle);
				MyLogger.getLogger().info("Please connect your device into flashmode.");
				WidgetTask.openWaitDeviceForFlashmode(shell,flash);
				bundle.close();
			}
			else
				MyLogger.getLogger().info("Flash canceled");

		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		

		
		/*Worker.post(new Job() {
			public Object run() {
				System.out.println("flashmode");
				if (bundle!=null) {
					X10flash flash=null;
					try {
			    		MyLogger.getLogger().info("Preparing files for flashing");
			    		bundle.open();
				    	bundle.setSimulate(GlobalConfig.getProperty("simulate").toLowerCase().equals("yes"));
						flash = new X10flash(bundle);
						
						/*if ((new WaitDeviceFlashmodeGUI(flash)).deviceFound(_root)) {
				    		try {
								flash.openDevice();
								flash.flashDevice();
				    		}
				    		catch (Exception e) {
				    			e.printStackTrace();
				    		}
						}
					}
					catch (BundleException ioe) {
						MyLogger.getLogger().error("Error preparing files");
					}
					catch (Exception e) {
						MyLogger.getLogger().error(e.getMessage());
					}
					bundle.close();
				}
				else MyLogger.getLogger().info("Flash canceled");
				return null;
			}
		});*/
	}

}