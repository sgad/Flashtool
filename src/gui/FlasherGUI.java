package gui;

import java.awt.Color;
import java.awt.Desktop;
import java.awt.EventQueue;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileFilter;

import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.RowSpec;
import com.jgoodies.forms.factories.FormFactory;

import java.io.ByteArrayInputStream;
import java.io.File; 
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;  
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.net.URI;
import foxtrot.Job;
import foxtrot.Worker;
import javax.swing.JButton;
import org.adb.APKUtility;
import org.adb.AdbUtility;
import org.logger.MyLogger;
import org.plugins.PluginActionListener;
import org.plugins.PluginActionListenerAbout;
import org.plugins.PluginInterface;
import org.system.AdbPhoneThread;
import org.system.ClassPath;
import org.system.CommentedPropertiesFile;
import org.system.Device;
import org.system.DeviceChangedListener;
import org.system.DeviceEntry;
import org.system.DeviceProperties;
import org.system.Devices;
import org.system.ElfParser;
import org.system.FileDrop;
import org.system.GlobalConfig;
import org.system.OS;
import org.system.ProcessBuilderWrapper;
import org.system.PropertiesFile;
import org.system.RunStack;
import org.system.Shell;
import org.system.StatusEvent;
import org.system.StatusListener;
import org.system.TextFile;
import org.system.VersionChecker;

import java.util.Iterator;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.Deflater;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.ButtonGroup;
import javax.swing.JTextPane;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import org.lang.Language;
import flashsystem.Bundle;
import flashsystem.BundleException;
import flashsystem.BundleMetaData;
import flashsystem.BytesUtil;
import flashsystem.Command;
import flashsystem.FlasherConsole;
import flashsystem.HexDump;
import flashsystem.S1Packet;
import flashsystem.SeusSinTool;
import flashsystem.SinFile;
import flashsystem.TaEntry;
import flashsystem.TaFile;
import flashsystem.TaParseException;
import flashsystem.X10flash;
import gui.EncDecGUI.MyFile;
import javax.swing.JProgressBar;
import java.awt.SystemColor;
import java.lang.reflect.Constructor;
import javax.swing.JToolBar;
import javax.swing.ImageIcon;
import java.awt.Toolkit;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import linuxlib.JUsb;


public class FlasherGUI extends JFrame {

	/**
	 * 
	 */
	public static FlasherGUI _root;
	public static boolean guimode=true;
	private static String fsep = OS.getFileSeparator();
	private static final long serialVersionUID = 1L;
	private static JToolBar toolBar;
	private static JTextPane textArea = new JTextPane();
	private static AdbPhoneThread phoneWatchdog;
	private JPanel contentPane;
	private Bundle bundle;
	private ButtonGroup buttonGroupLog = new ButtonGroup();
	private ButtonGroup buttonGroupLang = new ButtonGroup();
	private JButton flashBtn;
	private JButton btnRoot;
	private JButton btnAskRootPerms;
	private JButton btnCleanroot;
	private JButton custBtn;
	private JButton btnXrecovery;
	private JButton btnKernel;
	private JMenuItem mntmSwitchPro;
	private JMenuItem mntmInstallBusybox;
	private JMenuItem mntmDumpProperties;
	private JMenuItem mntmClearCache;
	private JMenuItem mntmBuildpropEditor;
	private JMenuItem mntmBuildpropRebrand;
	private JMenuItem mntmSetDefaultRecovery;
	private JMenuItem mntmRebootDefaultRecovery;
	private JMenuItem mntmRebootIntoRecoveryT;
	private JMenuItem mntmSetDefaultKernel;
	private JMenuItem mntmRebootCustomKernel;
	private JMenuItem mntmRebootDefaultKernel;
	private JMenuItem mntmRootPsneuter;
	private JMenuItem mntmRootzergRush;
	private JMenuItem mntmRootEmulator;
	private JMenuItem mntmRootAdbRestore;
	private JMenuItem mntmBackupSystemApps;
	private JMenuItem mntmRawIO;
	private JMenuItem mntmSinEdit;
	private JMenuItem mntmElfUnpack;
	private JMenuItem mntmYaffs2Unpack;
	private JMenuItem mntmDevicesAdd;
	private JMenuItem mntmDevicesRemove;
	private JMenuItem mntmDevicesEdit;
	private JMenuItem mntmDevicesExport;
	private JMenuItem mntmDevicesImport;
	private JMenu mnPlugins;
	private String lang;
	private String ftfpath="";
	private String ftfname="";
	//private StatusListener phoneStatus;
	private JMenu mnDev;

	private static void setSystemLookAndFeel() {
		try {
			UIManager.setLookAndFeel(new com.jgoodies.looks.plastic.Plastic3DLookAndFeel());
		}
		catch (Exception e) {}
	}

	private static void initLogger() throws FileNotFoundException {
		MyLogger.appendTextArea(textArea);
		MyLogger.setLevel(GlobalConfig.getProperty("loglevel").toUpperCase());
	}

	static public void runAdb() throws Exception {
		if (!OS.getName().equals("windows")) {
			ProcessBuilderWrapper giveRights = new ProcessBuilderWrapper(new String[] {"chmod", "755", OS.getAdbPath()},false);
			giveRights = new ProcessBuilderWrapper(new String[] {"chmod", "755", OS.getFastBootPath()},false);
		}
		killAdbandFastboot();
	}

	public static void addToolbar(JButton button) {
		toolBar.add(button);
	}

	public static void main(String[] args) throws Exception {
		if (OS.getName()!="windows") JUsb.init();
		OptionParser parser = new OptionParser();
		OptionSet options;
        parser.accepts( "console" );
        try {
        	options = parser.parse(args);
        }
        catch (Exception e) {
        	parser.accepts("action").withRequiredArg().required();
        	parser.accepts("file").withOptionalArg().defaultsTo("");
        	parser.accepts("method").withOptionalArg().defaultsTo("auto");
        	parser.accepts("wipedata").withOptionalArg().defaultsTo("yes");
        	parser.accepts("wipecache").withOptionalArg().defaultsTo("yes");
        	parser.accepts("baseband").withOptionalArg().defaultsTo("yes");
        	parser.accepts("system").withOptionalArg().defaultsTo("yes");
        	parser.accepts("kernel").withOptionalArg().defaultsTo("yes");
            options = parser.parse(args);        	
        }
        Language.Init(GlobalConfig.getProperty("language").toLowerCase());
        if (options.has("console")) {
        	String action=(String)options.valueOf("action");
        	
        	if (action.toLowerCase().equals("flash")) {
        		FlasherConsole.init(false);
        		FlasherConsole.doFlash((String)options.valueOf("file"), options.valueOf("wipedata").equals("yes"), options.valueOf("wipecache").equals("yes"), options.valueOf("baseband").equals("no"), options.valueOf("kernel").equals("no"), options.valueOf("system").equals("no"));
        	}

        	if (action.toLowerCase().equals("imei")) {
        		FlasherConsole.init(false);
        		FlasherConsole.doGetIMEI();
        	}

        	if (action.toLowerCase().equals("root")) {
        		FlasherConsole.init(true);
        		FlasherConsole.doRoot();
        	}
        	
        	if (action.toLowerCase().equals("blunlock")) {
        		FlasherConsole.init(true);
        		FlasherConsole.doBLUnlock();
        		
        	}
        	
        	FlasherConsole.exit();
        }
        else {
			initLogger();
			setSystemLookAndFeel();
			runAdb();
			MyLogger.getLogger().info("Flashtool "+About.getVersion());
			MyLogger.getLogger().info("You can drag and drop ftf files here to start flashing them");
			String userdir = System.getProperty("user.dir");
			String pathsep = System.getProperty("path.separator");
			System.setProperty("java.library.path", OS.getWinDir()+pathsep+OS.getSystem32Dir()+pathsep+userdir+fsep+"x10flasher_lib");
			EventQueue.invokeLater(new Runnable() {
				public void run() {
					try {
						FlasherGUI frame = new FlasherGUI();
						frame.setVisible(true);
					} catch (Exception e) {}
				}
			});
        }
	}

	public FlasherGUI() {		
		setIconImage(Toolkit.getDefaultToolkit().getImage(FlasherGUI.class.getResource("/gui/ressources/icons/flash_32.png")));
		_root=this;
		setName("FlasherGUI");
		setTitle("SonyEricsson X10 Flasher by Bin4ry & Androxyde");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 845, 480);

		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				exitProgram();
			}
		});

		new FileDrop( null, textArea, 
        		new FileDrop.Listener() {
        			public void filesDropped(final java.io.File[] files ) {
        				if (files.length==1) {
        					if (files[0].getAbsolutePath().toUpperCase().endsWith("FTF")) {
        						try {
        							EventQueue.invokeLater(new Runnable() {
        								public void run() {
        									try {
        										doFlashmode(files[0].getParentFile().getAbsolutePath(),files[0].getName());
        									}
        									catch (Exception e) {}
        								}
        							});
        						}
        						catch (Exception e) {}
        					}
        					else
        						MyLogger.getLogger().error("You can only drop ftf files");
        				}
        				else
        					MyLogger.getLogger().error("You dropped more than one file");
            }   // end filesDropped
        }); // end FileDrop.Listener

		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);

		JMenu mnFile = new JMenu("File");
		mnFile.setName("mnFile");
		menuBar.add(mnFile);

		mntmSwitchPro = new JMenuItem(GlobalConfig.getProperty("devfeatures").equals("yes")?"Switch Simple":"Switch Pro");
		mnFile.add(mntmSwitchPro);
		JMenuItem mntmExit = new JMenuItem("Exit");
		mntmExit.setName("mntmExit");
		mnFile.add(mntmExit);

		JMenu mnAdvanced = new JMenu("Tools");
		mnAdvanced.setName("mnAdvanced");
		menuBar.add(mnAdvanced);
		mnDev = new JMenu("Advanced");
		mnDev.setVisible(false);
		menuBar.add(mnDev);

		JMenu mnLang = new JMenu("Language");
		mnLang.setName("mnLang");
		menuBar.add(mnLang);

		Enumeration<String> listlang = Language.getLanguages();
		while (listlang.hasMoreElements()) {
			lang = listlang.nextElement();
			PropertiesFile plang = Language.getProperties(lang);
			JRadioButtonMenuItem menu = new JRadioButtonMenuItem(plang.getProperty("rdbtnmntm"+lang));
			menu.setName("rdbtnmntm"+lang);
			menu.setText(Language.getMenuItem("rdbtnmntm"+lang));
			buttonGroupLang.add(menu);
			mnLang.add(menu);
			menu.setSelected(GlobalConfig.getProperty("language").equals(lang));
			menu.addActionListener(new LangActionListener(lang,buttonGroupLang,_root));
		}

		JMenuItem mntmEncryptDecrypt = new JMenuItem("Decrypt Files");
		mntmEncryptDecrypt.setName("mntmEncryptDecrypt");
		mntmEncryptDecrypt.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				doEncDec();
			}
		});

		mntmInstallBusybox = new JMenuItem("Install BusyBox");
		mntmInstallBusybox.setName("mntmInstallBusybox");
		mntmInstallBusybox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				doInstallBusyBox();
			}
		});

			mntmDumpProperties = new JMenuItem("TA Editor");
			mntmDumpProperties.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					try {
						doDumpProperties();
					}
					catch (Exception e1) {}
				}
			});
			mntmRawIO = new JMenuItem("Raw I/O Module");
			mntmRawIO.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					try {
						doRawIO();
					}
					catch (Exception e1) {}
				}
			});
			JMenuItem mntmTaBackupRestore = new JMenuItem("TA Backup & Restore");
			mntmTaBackupRestore.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					try {
						BackupRestore();
					}
					catch (Exception e1) {}
				}
			});

			mnDev.add(mntmDumpProperties);
			mnDev.add(mntmTaBackupRestore);
			mnDev.add(mntmRawIO);
		mntmSinEdit = new JMenuItem("SIN Editor");
		mntmSinEdit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					doSinEdit();
				}
				catch (Exception e1) {}
			}
		});

		mntmElfUnpack = new JMenuItem("ELF");
		mntmElfUnpack.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					doElfUnpack();
				}
				catch (Exception e1) {}
			}
		});

		mntmYaffs2Unpack = new JMenuItem("Yaffs2");
		mntmYaffs2Unpack.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					doYaffs2Unpack();
				}
				catch (Exception e1) {}
			}
		});

		JMenu mnRoot = new JMenu("Root");
		mnAdvanced.add(mnRoot);

		mntmRootPsneuter = new JMenuItem("Force psneuter");
		mntmRootPsneuter.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (!Devices.getCurrent().hasRoot())
					doRootpsneuter();
				else
					JOptionPane.showMessageDialog(null, "Your device is already rooted");
			}
		});

		mntmRootzergRush = new JMenuItem("Force zergRush");
		mntmRootzergRush.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (!Devices.getCurrent().hasRoot())
					doRootzergRush();
				else
					JOptionPane.showMessageDialog(null, "Your device is already rooted");
			}
		});

		mntmRootEmulator = new JMenuItem("Force Emulator");
		mntmRootEmulator.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (!Devices.getCurrent().hasRoot())
					doRootEmulator();
				else
					JOptionPane.showMessageDialog(null, "Your device is already rooted");
			}
		});

		mntmRootAdbRestore = new JMenuItem("Force AdbRestore hack");
		mntmRootAdbRestore.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (!Devices.getCurrent().hasRoot())
					try {
						doRootAdbRestore();
					}
					catch (Exception e) {
						MyLogger.getLogger().error(e.getMessage());
					}
				else
					JOptionPane.showMessageDialog(null, "Your device is already rooted");
			}
		});

		mnRoot.add(mntmRootPsneuter);
		mnRoot.add(mntmRootzergRush);
		mnRoot.add(mntmRootEmulator);
		mnRoot.add(mntmRootAdbRestore);
		
		JMenu mnClean = new JMenu("Clean");
		mnClean.setName("mnClean");
		mnAdvanced.add(mnClean);

		mntmClearCache = new JMenuItem("Clear cache");
		mnClean.add(mntmClearCache);
		mntmClearCache.setName("mntmClearCache");

		mntmClearCache.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				doClearCache();
			}
		});
		mntmClearCache.setEnabled(false);

		//mntmCleanUninstalled = new JMenuItem("Clean Uninstalled");
		//mnClean.add(mntmCleanUninstalled);
		//mntmCleanUninstalled.setName("mntmCleanUninstalled");
		//mntmCleanUninstalled.setEnabled(false);

		/*mntmCleanUninstalled.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				doCleanUninstall();
			}
		});*/

		JMenu mnXrecovery = new JMenu("Recovery");
		mnAdvanced.add(mnXrecovery);
		
		
		mntmSetDefaultRecovery = new JMenuItem("Set default recovery");
		mntmSetDefaultRecovery.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				doSetDefaultRecovery();
			}
		});
		mnXrecovery.add(mntmSetDefaultRecovery);
		

		/*mntmRecoveryControler = new JMenuItem("Recovery Controler");
		mntmRecoveryControler.setName("mntmRecoveryControler");
		mntmRecoveryControler.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				RecovControl control = new RecovControl();
				control.setVisible(true);
			}
		});
		mnXrecovery.add(mntmRecoveryControler);
		mntmRecoveryControler.setEnabled(false);*/
		

		JMenu mnKernel = new JMenu("Kernel");
		mnAdvanced.add(mnKernel);

		//mntmInstallBootkit = new JMenuItem("Install bootkit");
		//mntmInstallBootkit.addActionListener(new ActionListener() {
		//	public void actionPerformed(ActionEvent arg0) {
		//		doInstallBootKit();
		//	}
		//});
		//mntmInstallBootkit.setEnabled(false);
		//mnKernel.add(mntmInstallBootkit);
		
		mntmBackupSystemApps = new JMenuItem("Backup System Apps");
		mntmBackupSystemApps.setName("mntmBackupSystemApps");
		mnAdvanced.add(mntmBackupSystemApps);
		mntmBackupSystemApps.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				doBackupSystem();
			}
		});
		mntmBackupSystemApps.setEnabled(false);


		/*JMenuItem mntmInstallOnline = new JMenuItem("Download latest version");
		mntmInstallOnline.setName("mntmInstallOnline");
		mntmInstallOnline.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				doDownloadXRecovery();
			}
		});
		mnXrecovery.add(mntmInstallOnline);*/
		mnAdvanced.add(mntmInstallBusybox);

		mntmBuildpropEditor = new JMenuItem("Build.prop Editor");
		mntmBuildpropEditor.setName("mntmBuildpropEditor");
		mntmBuildpropEditor.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				BuildPropGUI propsEdit = new BuildPropGUI();
				propsEdit.setVisible(true);
			}
		});
		
		mnAdvanced.add(mntmSinEdit);
		JMenu mnExtracts = new JMenu("Extractors");
		mnExtracts.add(mntmElfUnpack);
		mnExtracts.add(mntmYaffs2Unpack);
		mnAdvanced.add(mnExtracts);		
		mnAdvanced.add(mntmBuildpropEditor);

		mntmBuildpropRebrand = new JMenuItem("Rebrand");
		//mntmBuildpropRebrand.setName("mntmBuildpropEditor");
		mntmBuildpropRebrand.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				doRebrand();
			}
		});
		mnAdvanced.add(mntmBuildpropRebrand);
		
		mnAdvanced.add(mntmEncryptDecrypt);

		/*JMenuItem mntmFilemanager = new JMenuItem("FileManager");
		mntmFilemanager.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					FileManager manager = new FileManager();
					manager.setVisible(true);
				}
				catch (Exception emanager) {}
			}
		});
		mnAdvanced.add(mntmFilemanager);*/

		JMenuItem mntmBundleCreation = new JMenuItem("Bundle Creation");
		mntmBundleCreation.setName("mntmBundleCreation");
		mntmBundleCreation.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				doBundle();
			}
		});
		mnAdvanced.add(mntmBundleCreation);

		JMenu mnReboot = new JMenu("Reboot");
		mnAdvanced.add(mnReboot);
		
		JMenu mnRRecovery = new JMenu("Recovery");
		JMenu mnRKernel = new JMenu("Kernel");
		mnReboot.add(mnRRecovery);
		mnReboot.add(mnRKernel);

		mntmRebootDefaultRecovery = new JMenuItem("Reboot default version");
		mntmRebootDefaultRecovery.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				doRebootRecovery();
			}
		});
		mnRRecovery.add(mntmRebootDefaultRecovery);

		mntmRebootIntoRecoveryT = new JMenuItem("Reboot specific version");
		mntmRebootIntoRecoveryT.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				doRebootRecoveryT();
			}
		});
		mnRRecovery.add(mntmRebootIntoRecoveryT);

		mntmRebootIntoRecoveryT.setEnabled(false);
		mntmRebootDefaultRecovery.setEnabled(false);
		mntmSetDefaultRecovery.setEnabled(false);

		mntmSetDefaultKernel = new JMenuItem("Set default kernel");
		mntmSetDefaultKernel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				doSetDefaultKernel();
			}
		});
		mnKernel.add(mntmSetDefaultKernel);
		mntmSetDefaultKernel.setEnabled(false);

		mntmRebootDefaultKernel = new JMenuItem("Reboot default version");
		mntmRebootDefaultKernel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				doReboot();
			}
		});
		mnRKernel.add(mntmRebootDefaultKernel);

		mntmRebootDefaultKernel.setEnabled(false);
		mntmRebootCustomKernel = new JMenuItem("Reboot specific version");
		mntmRebootCustomKernel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				doRebootKexec();
			}
		});
		mnRKernel.add(mntmRebootCustomKernel);
		mntmRebootCustomKernel.setEnabled(false);

		JMenu mnHelp = new JMenu("Help");
		mnHelp.setName("mnHelp");
		mnPlugins = new JMenu("Plugins");
		JMenu mnDevices = new JMenu("Devices");
		mntmDevicesAdd = new JMenuItem("Add");
		mntmDevicesAdd.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				DeviceEditorUI edit = new DeviceEditorUI();
				edit.setVisible(true);
			}
		});
		mntmDevicesRemove = new JMenuItem("Remove");
		mntmDevicesRemove.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				Devices.listDevices(true);
        		deviceSelectGui devsel = new deviceSelectGui(null);
        		String devid = devsel.getDevice();
        		if (devid.length()>0) {
        			try {
        				doDeleteDevice(devid);
        				MyLogger.getLogger().info("Device "+devid+" deleted successfully");
        			}
        			catch (Exception e) {
        				MyLogger.getLogger().error(e.getMessage());
        			}
        		}
			}
		});
		
		mntmDevicesEdit = new JMenuItem("Edit");
		mntmDevicesEdit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				Devices.listDevices(true);
        		deviceSelectGui devsel = new deviceSelectGui(null);
        		String devid = devsel.getDevice();
        		if (devid.length()>0) {
        			DeviceEditorUI edit = new DeviceEditorUI();
        			edit.setEntry(Devices.getDevice(devid));
        			edit.setVisible(true);
        		}
			}
		});		
		
		mntmDevicesExport = new JMenuItem("Export");
		mntmDevicesExport.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				Devices.listDevices(true);
        		deviceSelectGui devsel = new deviceSelectGui(null);
        		String devid = devsel.getDevice();
        		if (devid.length()>0) {
        			try {
        				doExportDevice(devid);
        				MyLogger.getLogger().info("Device "+devid+" Exported successfully");
        			}
        			catch (Exception e) {
        				MyLogger.getLogger().error(e.getMessage());
        			}
        		}
			}
		});

		mntmDevicesImport = new JMenuItem("Import");
		mntmDevicesImport.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
        				Worker.post(new Job() {
        					public Object run() {
        						try {
        							Devices.listDevices(true);
        			        		deviceSelectGui devsel = new deviceSelectGui(null);
        			        		Properties list = new Properties();
        			        		File[] lfiles = new File(OS.getWorkDir()+File.separator+"devices").listFiles();
        			        		for (int i=0;i<lfiles.length;i++) {
        			        			if (lfiles[i].getName().endsWith(".ftd")) {
        			        				String name = lfiles[i].getName();
        			        				name = name.substring(0,name.length()-4);
        			        				list.setProperty(name, "Device to import");
        			        			}
        			        		}
        			        		String devid = devsel.getDeviceFromList(list);
        			        		if (devid.length()>0) {
	        							MyLogger.getLogger().info("Beginning import of "+devid);
	        							doImportDevice(devid);
	        							MyLogger.getLogger().info("Device "+devid+" imported successfully");
        			        		}
        						}
        						catch (Exception e) {
        							MyLogger.getLogger().error(e.getMessage());
        						}
        						return null;
        					}
        				});
			}
		});

		mnDevices.add(mntmDevicesAdd);
		mnDevices.add(mntmDevicesEdit);
		mnDevices.add(mntmDevicesRemove);
		mnDevices.add(mntmDevicesExport);
		mnDevices.add(mntmDevicesImport);
		menuBar.add(mnPlugins);
		menuBar.add(mnDevices);
		menuBar.add(mnHelp);

		JMenu mnLoglevel = new JMenu("Loglevel");
		mnLoglevel.setName("mnLoglevel");
		mnHelp.add(mnLoglevel);

		/*JMenuItem mntmTestFlashMode = new JMenuItem("Test Flash Mode");
		mntmTestFlashMode.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				doTestFlash();
			}
		});
		mnHelp.add(mntmTestFlashMode);*/

		JMenuItem mntmCheckDrivers = new JMenuItem("Check Drivers");
		mntmCheckDrivers.setName("mntmCheckDrivers");
		mntmCheckDrivers.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Device.CheckAdbDrivers();
			}
		});
		mnHelp.add(mntmCheckDrivers);

		JMenuItem mntmAbout = new JMenuItem("About");
		mntmAbout.setName("mntmAbout");
		mnHelp.add(mntmAbout);	

		JRadioButtonMenuItem rdbtnmntmError = new JRadioButtonMenuItem("errors");
		rdbtnmntmError.setName("mntmError");
		buttonGroupLog.add(rdbtnmntmError);
		mnLoglevel.add(rdbtnmntmError);
		rdbtnmntmError.setSelected(GlobalConfig.getProperty("loglevel").toUpperCase().equals("ERROR"));

		JRadioButtonMenuItem rdbtnmntmWarnings = new JRadioButtonMenuItem("warnings");
		rdbtnmntmWarnings.setName("mntmWarnings");
		buttonGroupLog.add(rdbtnmntmWarnings);
		mnLoglevel.add(rdbtnmntmWarnings);
		rdbtnmntmWarnings.setSelected(GlobalConfig.getProperty("loglevel").toUpperCase().equals("WARN"));

		JRadioButtonMenuItem rdbtnmntmInfos = new JRadioButtonMenuItem("infos");
		rdbtnmntmInfos.setName("mntmInfos");
		buttonGroupLog.add(rdbtnmntmInfos);
		mnLoglevel.add(rdbtnmntmInfos);
		rdbtnmntmInfos.setSelected(GlobalConfig.getProperty("loglevel").toUpperCase().equals("INFO"));

		JRadioButtonMenuItem rdbtnmntmDebug = new JRadioButtonMenuItem("debug");
		rdbtnmntmDebug.setName("mntmDebug");
		buttonGroupLog.add(rdbtnmntmDebug);
		mnLoglevel.add(rdbtnmntmDebug);
		rdbtnmntmDebug.setSelected(GlobalConfig.getProperty("loglevel").toUpperCase().equals("DEBUG"));

		rdbtnmntmError.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				MyLogger.setLevel("ERROR");
				GlobalConfig.setProperty("loglevel", "error");
			}
		});
		
		rdbtnmntmWarnings.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				MyLogger.setLevel("WARN");
				GlobalConfig.setProperty("loglevel", "warn");
			}
		});
		
		rdbtnmntmInfos.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				MyLogger.setLevel("INFO");
				GlobalConfig.setProperty("loglevel", "info");
			}
		});

		rdbtnmntmDebug.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				MyLogger.setLevel("DEBUG");
				GlobalConfig.setProperty("loglevel", "debug");
			}
		});

		mntmExit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				RunStack.killAll();
				exitProgram();
			}
		});

		mntmSwitchPro.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				boolean ispro = GlobalConfig.getProperty("devfeatures").equals("yes");
				mntmSwitchPro.setText(ispro?"Switch Pro":"Switch SImple");
				GlobalConfig.setProperty("devfeatures", ispro?"no":"yes");
				mnDev.setVisible(!ispro);
			}
		});

		mntmAbout.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				About about = new About();
				about.setVisible(true);
			}
		});

		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(new FormLayout(new ColumnSpec[] {
				FormFactory.RELATED_GAP_COLSPEC,
				FormFactory.DEFAULT_COLSPEC,
				FormFactory.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("max(50dlu;default):grow"),
				FormFactory.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("max(78dlu;default)"),
				FormFactory.RELATED_GAP_COLSPEC,},
			new RowSpec[] {
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				RowSpec.decode("default:grow"),
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,}));						
		
		toolBar = new JToolBar();
		toolBar.setFloatable(false);
		contentPane.add(toolBar, "2, 2, left, fill");

		flashBtn = new JButton("");
		flashBtn.setToolTipText("Flash");
		flashBtn.setIcon(new ImageIcon(FlasherGUI.class.getResource("/gui/ressources/icons/flash_32.png")));
		toolBar.add(flashBtn);
		
				btnRoot = new JButton("");
				btnRoot.setToolTipText("Root");
				btnRoot.setIcon(new ImageIcon(FlasherGUI.class.getResource("/gui/ressources/icons/root_32.png")));
				toolBar.add(btnRoot);
				btnRoot.setEnabled(false);
				
						btnAskRootPerms = new JButton("");
						btnAskRootPerms.setIcon(new ImageIcon(FlasherGUI.class.getResource("/gui/ressources/icons/askroot_32.png")));
						btnAskRootPerms.setToolTipText("Ask Root Perms");
						toolBar.add(btnAskRootPerms);
						btnAskRootPerms.setBackground(SystemColor.control);
						btnAskRootPerms.setEnabled(false);
						
								btnCleanroot = new JButton("");
								btnCleanroot.setToolTipText("Clean (Root Needed)");
								btnCleanroot.setIcon(new ImageIcon(FlasherGUI.class.getResource("/gui/ressources/icons/clean_32.png")));
								toolBar.add(btnCleanroot);
								
										btnCleanroot.addActionListener(new ActionListener() {
											public void actionPerformed(ActionEvent arg0) {
												doCleanRoot();
											}
										});
										btnCleanroot.setEnabled(false);
										custBtn = new JButton("");
										custBtn.setIcon(new ImageIcon(FlasherGUI.class.getResource("/gui/ressources/icons/customize_32.png")));
										custBtn.setToolTipText("APK Installer");
										toolBar.add(custBtn);
										custBtn.setEnabled(false);
														
														btnXrecovery = new JButton("");
														btnXrecovery.setToolTipText("Recovery Installer");
														btnXrecovery.setIcon(new ImageIcon(FlasherGUI.class.getResource("/gui/ressources/icons/recovery_32.png")));
														toolBar.add(btnXrecovery);
														btnXrecovery.addActionListener(new ActionListener() {
															public void actionPerformed(ActionEvent e) {
																doInstallXRecovery();
															}
														});
														btnXrecovery.setEnabled(false);
														
														btnKernel = new JButton("");
														btnKernel.setToolTipText("Kernel Installer");
														btnKernel.setIcon(new ImageIcon(FlasherGUI.class.getResource("/gui/ressources/icons/kernel_32.png")));
														toolBar.add(btnKernel);
														btnKernel.addActionListener(new ActionListener() {
															public void actionPerformed(ActionEvent e) {
																doInstallKernel();
															}
														});
														btnKernel.setEnabled(false);
										
												custBtn.addActionListener(new ActionListener() {
													public void actionPerformed(ActionEvent arg0) {
														doCustomize();
													}
												});
						btnAskRootPerms.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent e) {
								doAskRoot();
							}
						});
				
						btnRoot.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent arg0) {
								doRoot();
							}
						});
		flashBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				try {
					doFlash();
				}
				catch (Exception eflash) {}
			}
		});
		
		JToolBar toolBar_1 = new JToolBar();
		toolBar_1.setFloatable(false);
		contentPane.add(toolBar_1, "6, 2, right, center");
		
		JButton btnDonate = new JButton("");
		toolBar_1.add(btnDonate);
		btnDonate.setIcon(new ImageIcon(FlasherGUI.class.getResource("/gui/ressources/icons/paypal.png")));
		btnDonate.setToolTipText("Donate");
		//btnDonate.setName("btnDonate");
		btnDonate.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				doConnectPaypal();
			}
		});
		
		JScrollPane scrollPane = new JScrollPane();
		contentPane.add(scrollPane, "2, 8, 5, 1, fill, fill");
		
		scrollPane.setViewportView(textArea);
		
		JButton btnSaveLog = new JButton("Save log");
		btnSaveLog.setName("btnSaveLog");
		btnSaveLog.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				MyLogger.writeFile();
			}
		});
		contentPane.add(btnSaveLog, "6, 10, right, center");
		UIManager.put("ProgressBar.background", Color.WHITE); //colour of the background
	    UIManager.put("ProgressBar.foreground", Color.LIGHT_GRAY);  //colour of progress bar
	    UIManager.put("ProgressBar.selectionBackground",Color.BLACK);  //colour of percentage counter on black background
	    UIManager.put("ProgressBar.selectionForeground",Color.BLACK);  //colour of precentage counter on red background
		JProgressBar progressBar = new JProgressBar();
		MyLogger.registerProgressBar(progressBar);
		contentPane.add(progressBar, "2, 12, 5, 1");
		setLanguage();
		mntmInstallBusybox.setEnabled(false);
		mntmBuildpropEditor.setEnabled(false);
		mntmBuildpropRebrand.setEnabled(false);
	}

	public void setVisible(boolean visible) {
		super.setVisible(visible);
		mnDev.setVisible(GlobalConfig.getProperty("devfeatures").equals("yes"));
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
		phoneWatchdog = new AdbPhoneThread();
		phoneWatchdog.start();
		phoneWatchdog.addStatusListener(phoneStatus);
		VersionChecker vcheck = new VersionChecker();
		vcheck.setMessageFrame(this);
		vcheck.start();
	}

	public void setLanguage() {
		Language.translate(this);
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

	public static void killAdbLinux() {
		try {
			ProcessBuilderWrapper cmd = new ProcessBuilderWrapper(new String[] {"/usr/bin/killall", "adb"},false);
		}
		catch (Exception e) {
		}
	}
	
	public static void killAdbWindows() {
		try {
			ProcessBuilderWrapper adb = new ProcessBuilderWrapper(new String[] {"taskkill", "/F", "/T", "/IM", "adb*"},false);
		}
		catch (Exception e) {
		}
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

	public void doRawIO() {
		CustomFlashUI raw = new CustomFlashUI();
		raw.setVisible(true);
	}

	public void doSinEdit() {
		SinEditorUI sinui = new SinEditorUI();
		sinui.setVisible(true);
	}

	public void doElfUnpack() {
		ElfUnpacker unpack = new ElfUnpacker();
		unpack.setVisible(true);
	}

	public void doYaffs2Unpack() {
		Worker.post(new Job() {
			public Object run() {
				try {
					String file = chooseYaffs2();
					if (!file.equals("ERROR")) {
						int index = file.lastIndexOf(".yaffs2");
						String folder = file.substring(0, index)+"_content";
						MyLogger.getLogger().info("Extracting " + file + " to " + folder);
						OS.unyaffs(file, folder);
						MyLogger.getLogger().info("Extraction finished");
					}
					else {
						MyLogger.getLogger().info("Canceled");
					}						
					} catch (Exception e) {}
					return null;
				}
			});
	}

	public void doCleanUninstall() {
		Worker.post(new Job() {
			public Object run() {
				try {
						PropertiesFile safeList = new PropertiesFile("org/adb/config/safelist.properties","."+fsep+"custom"+fsep+"clean"+fsep+Devices.getCurrent().getId()+fsep+"safelist.properties");
						HashSet<String> set = AdbUtility.listSysApps();
						Iterator<Object> keys = safeList.keySet().iterator();
						while (keys.hasNext()) {
							String key = (String)keys.next();
							if (safeList.getProperty(key).equals("safe") && !set.contains(key)) {
								MyLogger.getLogger().debug(key);
								if (TextFile.exists("."+fsep+"custom"+fsep+"apps_saved"+File.separator+Devices.getCurrent().getId()+fsep+key)) {
									String packageName = APKUtility.getPackageName("."+fsep+"custom"+fsep+"apps_saved"+File.separator+Devices.getCurrent().getId()+fsep+key);
									MyLogger.getLogger().debug(packageName);
									AdbUtility.uninstall(packageName,false);
								}
							}
						}
						MyLogger.getLogger().info("Clean Finished");
				} catch (Exception e) {}
				return null;
			}
		});
	}


	public void doDumpProperties() throws Exception {
		
		//firmSelect sel = new firmSelect(config);
		//bundle = sel.getBundle();
		
		bundle = new Bundle();
		if (bundle!=null) {
			Worker.post(new Job() {
				public Object run() {
						X10flash flash=null;
						try {
					    	bundle.setSimulate(GlobalConfig.getProperty("simulate").toLowerCase().equals("yes"));
							flash = new X10flash(bundle);
							MyLogger.getLogger().info("Please connect your device into flashmode.");
							if ((new WaitDeviceFlashmodeGUI(flash)).deviceFound(_root)) {
								flash.openDevice();
								flash.sendLoader();
								flash.openTA(2);
								Vector<TaEntry> v=flash.dumpProperties();
								if (v.size()>0) {
									TaEditor edit = new TaEditor(flash,v);
									edit.setVisible(true);
									flash.closeTA();
									flash.closeDevice();
								}
							}
						}
						catch (Exception e) {
							MyLogger.getLogger().error(e.getMessage());
						}
						bundle.close();
						return null;
					}
				});
			}
	}

	public void BackupRestore() throws Exception {
		TaModeSelectGUI tamode = new TaModeSelectGUI();
		String select = tamode.selectMode();
		if (select.equals("backup"))
			doBackupTa();
		if (select.equals("restore"))
			doRestoreTa();
	}	
	
	public void doBackupTa() throws Exception {
		bundle = new Bundle();
		if (bundle!=null) {
			Worker.post(new Job() {
				public Object run() {
						X10flash flash=null;
						try {
					    	bundle.setSimulate(GlobalConfig.getProperty("simulate").toLowerCase().equals("yes"));
							flash = new X10flash(bundle);
							MyLogger.getLogger().info("Please connect your device into flashmode.");
							if ((new WaitDeviceFlashmodeGUI(flash)).deviceFound(_root)) {
								flash.openDevice();
								flash.sendLoader();
								flash.BackupTA();
								flash.closeDevice();
								MyLogger.getLogger().info("Dumping TA finished.");
							}
						}
						catch (Exception e) {
							MyLogger.getLogger().error(e.getMessage());
						}
						bundle.close();
						return null;
					}
				});
			}
	}
	
	public void doRestoreTa() throws Exception {
		bundle = new Bundle();
		if (bundle!=null) {
			Worker.post(new Job() {
				public Object run() {
						X10flash flash=null;
						try {
					    	bundle.setSimulate(GlobalConfig.getProperty("simulate").toLowerCase().equals("yes"));
							flash = new X10flash(bundle);
							MyLogger.getLogger().info("Please connect your device into flashmode.");
							if ((new WaitDeviceFlashmodeGUI(flash)).deviceFound(_root)) {
								flash.openDevice();
								flash.sendLoader();
								TaSelectGUI tasel = new TaSelectGUI(".ta",flash.getPhoneProperty("MSN"));
								String result = tasel.getTa();
								if (result.length()>0) {
									String tafile = OS.getWorkDir()+"/custom/ta/"+result;
									flash.RestoreTA(tafile);
									flash.closeDevice();
									MyLogger.getLogger().info("TA Operation finished.");
								}
								else {
									MyLogger.getLogger().info("Action canceled");
								}
								flash.closeDevice();
							}
						}
						catch (Exception e) {
							MyLogger.getLogger().error(e.getMessage());
						}
						bundle.close();
						return null;
					}
				});
			}
	}
	
	public void doFlash() throws Exception {
		
		BootModeSelectGUI bootmode = new BootModeSelectGUI();
		String select = bootmode.selectMode();
		if (select.equals("flashmode"))
			doFlashmode("","");
		if (select.equals("fastboot"))
			doFastBoot();
	}
	
	public void doFastBoot() throws Exception {
		FastBootToolboxGUI box = new FastBootToolboxGUI();
		box.setVisible(true);
	}
	
	public void doFlashmode(String pftfpath, String pftfname) throws Exception {
		ftfpath=pftfpath;
		ftfname=pftfname;
		Worker.post(new Job() {
			public Object run() {
				firmSelect sel = new firmSelect(ftfpath,ftfname);
				try {
					bundle = sel.getBundle();
				}
				catch (IOException ioe) {
					bundle=null;
				}
				if (bundle!=null) {
					X10flash flash=null;
					try {
			    		MyLogger.getLogger().info("Preparing files for flashing");
			    		bundle.open();
				    	bundle.setSimulate(GlobalConfig.getProperty("simulate").toLowerCase().equals("yes"));
						flash = new X10flash(bundle);
						MyLogger.getLogger().info("Please connect your device into flashmode.");
						if ((new WaitDeviceFlashmodeGUI(flash)).deviceFound(_root)) {
				    		try {
								flash.openDevice();
								flash.flashDevice();
				    		}
				    		catch (Exception e) {
				    			e.printStackTrace();
				    		}
						}
						else MyLogger.getLogger().info("Flash canceled");
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
		});
	}

	public void doRoot() {
		if (Devices.getCurrent().getVersion().contains("2.3")) {
			doRootzergRush();
		}
		else
			if (!Devices.getCurrent().getVersion().contains("4.0") && !Devices.getCurrent().getVersion().contains("4.1"))
				doRootpsneuter();
			else {
				if (Devices.getCurrent().getVersion().contains("4.0.3"))
						doRootEmulator();
				else
					if (Devices.getCurrent().getVersion().contains("4.0"))
						doRootAdbRestore();
					else
						JOptionPane.showMessageDialog(null, "No root exploit for this version");
			}
	}

	public void doPushRootFiles() throws Exception {
		AdbUtility.push(Devices.getCurrent().getBusybox(false), GlobalConfig.getProperty("deviceworkdir")+"/busybox");
		AdbUtility.push(OS.getWorkDir()+File.separator+"custom"+File.separator+"root"+File.separator+"4.0"+File.separator+"su", GlobalConfig.getProperty("deviceworkdir")+"/su");
		AdbUtility.push(OS.getWorkDir()+File.separator+"custom"+File.separator+"root"+File.separator+"4.0"+File.separator+"Superuser.apk", GlobalConfig.getProperty("deviceworkdir")+"/Superuser.apk");
		AdbUtility.run("chown shell.shell "+GlobalConfig.getProperty("deviceworkdir")+"/busybox && chmod 755 " + GlobalConfig.getProperty("deviceworkdir")+"/busybox",true);
	}
	

	public void doInstallRootFiles() throws Exception {
		AdbUtility.run("/data/local/tmp/busybox mount -o remount,rw /system && /data/local/tmp/busybox mv /data/local/tmp/su /system/xbin/su && /data/local/tmp/busybox mv /data/local/tmp/Superuser.apk /system/app/Superuser.apk && /data/local/tmp/busybox cp /data/local/tmp/busybox /system/xbin/busybox && chown root.root /system/xbin/su && chmod 06755 /system/xbin/su && chmod 655 /system/app/Superuser.apk && chmod 755 /system/xbin/busybox && rm /data/local.prop && reboot");
	}
	
	public void doRootAdbRestore() {
		Worker.post(new Job() {
			public Object run() {
				try {
					doPushRootFiles();
					String backuppackage = AdbUtility.run("pm list package -f com.sonyericsson.backuprestore");
					if (backuppackage.contains("backuprestore")) {
						AdbUtility.push(OS.getWorkDir()+File.separator+"custom"+File.separator+"root"+File.separator+"AdbRestore"+File.separator+"RootMe.tar", GlobalConfig.getProperty("deviceworkdir")+"/RootMe.tar");
						AdbUtility.run("mkdir /mnt/sdcard/.semc-fullbackup > /dev/null 2>&1");
						AdbUtility.run("rm -r /mnt/sdcard/.semc-fullbackup/RootMe* > /dev/null 2>&1");
						AdbUtility.run("cd /mnt/sdcard/.semc-fullbackup;/data/local/tmp/busybox tar xf /data/local/tmp/RootMe.tar");
						AdbUtility.run("am start com.sonyericsson.vendor.backuprestore/.ui.BackupActivity");
						MyLogger.getLogger().info("Now open your device and restore \"RootMe\" backup. Waiting ...");						
					}
					else {
						AdbUtility.restore(OS.getWorkDir()+File.separator+"custom"+File.separator+"root"+File.separator+"AdbRestore"+File.separator+"fakebackup.ab");
						MyLogger.getLogger().info("Please look at your device and click RESTORE!");
					}
					String delay = "60";
					MyLogger.getLogger().info("You have "+delay+" seconds to follow the restore advice");
					Shell exploit = new Shell("adbrestoreexploit");
					exploit.setProperty("DELAY", delay);
					String result = exploit.run(false);
					if (result.contains("Success")) {
						MyLogger.getLogger().info("Restore worked fine. Rebooting device. Please wait ...");
						Devices.getCurrent().reboot();
						Devices.waitForReboot(false);
						if (Devices.getCurrent().hasRoot()) {
							MyLogger.getLogger().info("Root achieved. Installing root files. Device will reboot. Please wait.");
							doInstallRootFiles();
							Devices.waitForReboot(false);
							MyLogger.getLogger().info("Cleaning hack files");
							AdbUtility.run("rm /data/local/tmp/busybox;rm -r /mnt/sdcard/.semc-fullbackup/RootMe;rm /data/local/tmp/RootMe.tar;rm /data/local/tmp/su;rm /data/local/tmp/Superuser.apk;rm /data/local/tmp/adbrestoreexploit");
							MyLogger.getLogger().info("Finished.");
						}
						else {
							MyLogger.getLogger().info("Root hack did not work.");
							MyLogger.getLogger().info("Cleaning hack files");
							AdbUtility.run("rm /data/local/tmp/busybox;rm -r /mnt/sdcard/.semc-fullbackup/RootMe;rm /data/local/tmp/RootMe.tar;rm /data/local/tmp/su;rm /data/local/tmp/Superuser.apk;rm /data/local/tmp/adbrestoreexploit");
						}
						MyLogger.getLogger().info("Rebooting device. Please wait.");
						Devices.getCurrent().reboot();
					}
					else {
						MyLogger.getLogger().info("Root hack did not work. Cleaning hack files");
						AdbUtility.run("rm /data/local/tmp/busybox;rm -r /mnt/sdcard/.semc-fullbackup/RootMe;rm /data/local/tmp/RootMe.tar;rm /data/local/tmp/su;rm /data/local/tmp/Superuser.apk;rm /data/local/tmp/adbrestoreexploit");
					}
				}
				catch (Exception e) {
				}
				return null;
			}
		});
	}

	public void doRootEmulator() {
		Worker.post(new Job() {
			public Object run() {
				try {
					MyLogger.getLogger().info("Preparing first part of the hack");
					AdbUtility.run("cd /data/local && mkdir tmp");
					AdbUtility.run("cd /data/local/tmp/ && rm *");
					AdbUtility.run("mv /data/local/tmp /data/local/tmp.bak");
					AdbUtility.run("ln -s /data /data/local/tmp");
					MyLogger.getLogger().info("Rebooting device. Please wait");
					Devices.getCurrent().reboot();
					Devices.waitForReboot(false);
					MyLogger.getLogger().info("Preparing second part of the hack");
					AdbUtility.run("rm /data/local.prop");
					AdbUtility.run("echo \"ro.kernel.qemu=1\" > /data/local.prop");
					MyLogger.getLogger().info("Rebooting device. Please wait");
					Devices.getCurrent().reboot();
					Devices.waitForReboot(false);
					if (Devices.getCurrent().hasRoot()) {
						MyLogger.getLogger().info("Now you have root");
						MyLogger.getLogger().info("Remounting system r/w");
						AdbUtility.run("mount -o remount,rw /system");
						MyLogger.getLogger().info("Installing root package");
						AdbUtility.push(OS.getWorkDir()+File.separator+"custom"+File.separator+"root"+File.separator+"4.0"+File.separator+"su", "/system/xbin");
						AdbUtility.push(Devices.getCurrent().getBusybox(false), "/system/xbin");
						AdbUtility.push(OS.getWorkDir()+File.separator+"custom"+File.separator+"root"+File.separator+"4.0"+File.separator+"Superuser.apk", "/system/app");
						AdbUtility.run("chown root.shell /system/xbin/su");
						AdbUtility.run("chmod 06755 /system/xbin/su");
						AdbUtility.run("chown root.shell /system/xbin/busybox");
						AdbUtility.run("chmod 755 /system/xbin/busybox");
						MyLogger.getLogger().info("Cleaning hack");
						AdbUtility.run("rm /data/local.prop");
						AdbUtility.run("rm /data/local/tmp");
						AdbUtility.run("mv /data/local/tmp.bak /data/local/tmp");
						MyLogger.getLogger().info("Rebooting device. Please wait. Your device is now rooted");
						Devices.getCurrent().reboot();
					}
					else {
						AdbUtility.run("rm /data/local.prop");
						AdbUtility.run("rm /data/local/tmp");
						AdbUtility.run("mv /data/local/tmp.bak /data/local/tmp");
						MyLogger.getLogger().info("Hack did not work. Cleaning and rebooting");
						Devices.getCurrent().reboot();
					}
				}
				catch (Exception e) {
					e.printStackTrace();
				}
				return null;
			}
		});
	}
	
	public void doRootzergRush() {
		Worker.post(new Job() {
			public Object run() {
				try {
					AdbUtility.push(Devices.getCurrent().getBusybox(false), GlobalConfig.getProperty("deviceworkdir")+"/busybox");
					Shell shell = new Shell("busyhelper");
					shell.run(true);
					AdbUtility.push(new File("."+fsep+"custom"+fsep+"root"+fsep+"zergrush.tar.uue").getAbsolutePath(),GlobalConfig.getProperty("deviceworkdir"));
					shell = new Shell("rootit");
					MyLogger.getLogger().info("Running part1 of Root Exploit, please wait");
					shell.run(true);
					Devices.waitForReboot(true);
					if (Devices.getCurrent().hasRoot()) {
						MyLogger.getLogger().info("Running part2 of Root Exploit");
						shell = new Shell("rootit2");
						shell.run(false);
						MyLogger.getLogger().info("Finished!.");
						MyLogger.getLogger().info("Root should be available after reboot!");
					}
					else {
						MyLogger.getLogger().error("The part1 exploit did not work");
					}
				}
				catch (Exception e) {
					MyLogger.getLogger().error(e.getMessage());}
				return null;
			}
		});
	}

	public void doRootpsneuter() {
		Worker.post(new Job() {
			public Object run() {
				try {
					AdbUtility.push(Devices.getCurrent().getBusybox(false), GlobalConfig.getProperty("deviceworkdir")+"/busybox");
					Shell shell = new Shell("busyhelper");
					shell.run(true);
					AdbUtility.push("."+fsep+"custom"+fsep+"root"+fsep+"psneuter.tar.uue",GlobalConfig.getProperty("deviceworkdir"));
					shell = new Shell("rootit");
					MyLogger.getLogger().info("Running part1 of Root Exploit, please wait");
					shell.run(false);
					Devices.waitForReboot(true);
					if (Devices.getCurrent().hasRoot()) {
						MyLogger.getLogger().info("Running part2 of Root Exploit");
						shell = new Shell("rootit2");
						shell.run(false);
						MyLogger.getLogger().info("Finished!.");
						MyLogger.getLogger().info("Root should be available after reboot!");		
					}
					else {
						MyLogger.getLogger().error("The part1 exploit did not work");
					}
				}
				catch (Exception e) {
					MyLogger.getLogger().error(e.getMessage());}
				return null;
			}
		});
	}

	public void doCustomize() {
		Worker.post(new Job() {
			public Object run() {
				try {
						ApkInstallGUI instgui = new ApkInstallGUI("."+fsep+"custom"+fsep+"apps");
						String folder = instgui.getFolder();
						if (folder.length()>0) {
							File files = new File(folder);
							File[] chld = files.listFiles();
							for(int i = 0; i < chld.length; i++){
								if (chld[i].getName().endsWith(".apk"))
									org.adb.AdbUtility.install(chld[i].getPath());
							}
							MyLogger.getLogger().info("APK Installation finished");
						}
						else MyLogger.getLogger().info("APK Installation canceled");
					}
				catch (Exception e) {}
				return null;
			}
		});
	}

	public void doRebrand() {
		Worker.post(new Job() {
			public Object run() {
			try {
						Devices.getCurrent().doBusyboxHelper();
						if (AdbUtility.Sysremountrw()) {
							AdbUtility.pull("/system/build.prop", Devices.getCurrent().getWorkDir()+fsep+"build.prop");
							CommentedPropertiesFile build = new CommentedPropertiesFile();
							build.load(new File(Devices.getCurrent().getWorkDir()+fsep+"build.prop"));
							String current = build.getProperty("ro.semc.version.cust");
							if (current!=null) {
								rebrandGUI gui = new rebrandGUI(current);
								String newid = gui.getId();
								if (newid.length()>0) {							
									build.store(new FileOutputStream(new File(Devices.getCurrent().getWorkDir()+fsep+"buildnew.prop")), "");
									TextFile tf = new TextFile(Devices.getCurrent().getWorkDir()+fsep+"buildnew.prop","ISO-8859-1");
									tf.setProperty(current,newid);
									AdbUtility.push(Devices.getCurrent().getWorkDir()+fsep+"buildnew.prop",GlobalConfig.getProperty("deviceworkdir")+"/build.prop");
									Shell shell = new Shell("rebrand");
									shell.runRoot();
									MyLogger.getLogger().info("Rebrand finished. Rebooting phone ...");
								}
							}
							else {MyLogger.getLogger().error("You are not on a stock ROM");}
						}
						else MyLogger.getLogger().error("Error mounting /system rw");
				}
				catch (Exception e) {
					MyLogger.getLogger().error(e.getMessage());
				}
				return null;
			}
		});
	}
	
	public void doCleanRoot() {
		Worker.post(new Job() {
			public Object run() {
				try {
					Devices.getCurrent().doBusyboxHelper();
					if (AdbUtility.Sysremountrw()) {
						apkClean sel = new apkClean();
						sel.setVisible(true);
						boolean somethingdone = false;
						if (TextFile.exists(OS.getWorkDir()+fsep+"custom"+fsep+"clean"+fsep+"listappsadd")) {
							TextFile t = new TextFile(OS.getWorkDir()+fsep+"custom"+fsep+"clean"+fsep+"listappsadd","ASCII");
							Iterator<String> i = t.getLines().iterator();
							while (i.hasNext()) {
								if (!TextFile.exists(OS.getWorkDir()+fsep+"custom"+fsep+"apps_saved"+File.separator+Devices.getCurrent().getId()+fsep+i.next())) {
									t.close();
									throw new Exception("File "+OS.getWorkDir()+fsep+"custom"+fsep+"apps_saved"+File.separator+Devices.getCurrent().getId()+fsep+i.next()+" does not exist");
								}
							}
							t.close();
						}
						if (TextFile.exists(OS.getWorkDir()+fsep+"custom"+fsep+"clean"+fsep+"listappsadd")) {
							AdbUtility.push(OS.getWorkDir()+fsep+"custom"+fsep+"clean"+fsep+"listappsadd", GlobalConfig.getProperty("deviceworkdir"));
							TextFile t = new TextFile(OS.getWorkDir()+fsep+"custom"+fsep+"clean"+fsep+"listappsadd","ASCII");
							Iterator<String> i = t.getLines().iterator();
							while (i.hasNext()) {
								AdbUtility.push(OS.getWorkDir()+fsep+"custom"+fsep+"apps_saved"+File.separator+Devices.getCurrent().getId()+fsep+i.next(), GlobalConfig.getProperty("deviceworkdir"));
							}
							t.delete();
							Devices.getCurrent().doBusyboxHelper();
							Shell shell1 = new Shell("sysadd");
							shell1.runRoot();
							somethingdone = true;
						}
						if (TextFile.exists(OS.getWorkDir()+fsep+"custom"+fsep+"clean"+fsep+"listappsremove")) {
							AdbUtility.push(OS.getWorkDir()+fsep+"custom"+fsep+"clean"+fsep+"listappsremove", GlobalConfig.getProperty("deviceworkdir"));
							TextFile t = new TextFile(OS.getWorkDir()+fsep+"custom"+fsep+"clean"+fsep+"listappsremove","ASCII");
							Iterator<String> i = t.getLines().iterator();
							while (i.hasNext()) {
								AdbUtility.pull("/system/app/"+i.next(),OS.getWorkDir()+fsep+"custom"+fsep+"apps_saved"+File.separator+Devices.getCurrent().getId());
							}
							Devices.getCurrent().doBusyboxHelper();
							Shell shell2 = new Shell("sysremove");
							shell2.runRoot();
							t.delete();
							somethingdone = true;
						}
						if (somethingdone) {
							AdbUtility.clearcache();
							MyLogger.getLogger().info("Clean finished. Rebooting phone ...");
						}
						else MyLogger.getLogger().info("Clean canceled");
					}
					else 
						MyLogger.getLogger().info("Error mounting /system rw");
				} catch (Exception e) {
					MyLogger.getLogger().error(e.getMessage());
				}
				return null;
			}
		});
	}

	public void doRebootRecoveryT() {
		Worker.post(new Job() {
			public Object run() {
				try {
					Devices.getCurrent().rebootSelectedRecovery();
				}
				catch (Exception e) {}
				return null;
			}
		});		
	}

	public void doSetDefaultRecovery() {
		Worker.post(new Job() {
			public Object run() {
				try {
					Devices.getCurrent().setDefaultRecovery();
				}
				catch (Exception e) {}
				return null;
			}
		});		
	}

	public void doSetDefaultKernel() {
		Worker.post(new Job() {
			public Object run() {
				try {
					KernelBootSelectGUI rsel = new KernelBootSelectGUI();
					String current = rsel.getVersion();
					if (current.length()>0) {
						if (AdbUtility.Sysremountrw()) {
						MyLogger.getLogger().info("Setting default kernel");
						Shell shell = new Shell("setdefaultkernel");
						shell.setProperty("KERNELTOBOOT", current);
						shell.runRoot();
						MyLogger.getLogger().info("Done");
						}
					}
				}
				catch (Exception e) {
					MyLogger.getLogger().error(e.getMessage());
				}
				return null;
			}
		});		
	}

	public void doRebootRecovery() {
		Worker.post(new Job() {
			public Object run() {
				try {
					MyLogger.getLogger().info("Rebooting into recovery mode");
					Shell shell = new Shell("rebootrecovery");
					shell.runRoot();
					MyLogger.getLogger().info("Phone will reboot into recovery mode");
				}
				catch (Exception e) {}
				return null;
			}
		});		
	}

	public void doRebootKexec() {
		Worker.post(new Job() {
			public Object run() {
				try {
					KernelBootSelectGUI ksel = new KernelBootSelectGUI();
					String current = ksel.getVersion();
					if (current.length()>0) {
						MyLogger.getLogger().info("Rebooting into kexec mode");
						Shell shell = new Shell("rebootkexect");
						shell.setProperty("KERNELTOBOOT", current);
						shell.runRoot();
						MyLogger.getLogger().info("Phone will reboot into kexec mode");
					}
					else {
						MyLogger.getLogger().info("Reboot canceled");
					}
				}
				catch (Exception e) {}
				return null;
			}
		});		
	}

	public void doReboot() {
		Worker.post(new Job() {
			public Object run() {
				try {
						MyLogger.getLogger().info("Rebooting into stock mode");
						Shell shell = new Shell("reboot");
						shell.runRoot();
						MyLogger.getLogger().info("Phone will reboot now");
				}
				catch (Exception e) {}
				return null;
			}
		});		
	}

	public void doInstallXRecovery() {
		Worker.post(new Job() {
			public Object run() {
				try {
						MyLogger.getLogger().info("Installing Recovery to device...");
						Devices.getCurrent().doBusyboxHelper();
						if (AdbUtility.Sysremountrw()) {
							RecoverySelectGUI sel = new RecoverySelectGUI(Devices.getCurrent().getId());
							String selVersion = sel.getVersion();
							if (selVersion.length()>0) {
								doInstallCustKit();
								AdbUtility.push("./devices/"+Devices.getCurrent().getId()+"/recovery/"+selVersion+"/recovery.tar",GlobalConfig.getProperty("deviceworkdir")+"/recovery.tar");
								Shell shell = new Shell("installrecovery");
								shell.runRoot();
								MyLogger.getLogger().info("Recovery successfully installed");
							}
							else {
								MyLogger.getLogger().info("Canceled");
							}
						}
						else MyLogger.getLogger().error("Error mounting /system rw");
					}
				catch (Exception e) {
					MyLogger.getLogger().error(e.getMessage());
				}
				return null;
				
			}
		});
	}	

    public void doEncDec() {
		Worker.post(new Job() {
			public Object run() {
	        	EncDecGUI encdec = new EncDecGUI();
	        	encdec.setVisible(true);
	        	Object[] list = encdec.getList();
	        	if (list!=null) {
	        		String folder=null;
    				for (int i=0;i<list.length;i++) {
    					MyLogger.getLogger().info("Decrypting "+list[i]);
    					folder = ((MyFile)list[i]).getParent();
    	        		SeusSinTool.decrypt(((MyFile)list[i]).getAbsolutePath());
    				}

    				MyLogger.getLogger().info("Decryption finished");
    				try {
					BundleGUI bcre = new BundleGUI(folder);
					Bundle b = bcre.getBundle("flashmode");
					if (b!=null) {
    					MyLogger.getLogger().info("Starting bundle creation");
    					b.createFTF();
    					MyLogger.getLogger().info("Finished bundle creation");
					}
    				}
    				catch (Exception e) {}
    				
	        	}
	 			return null;
			}
		});
   }

    public void doInstallBusyBox() {
		Worker.post(new Job() {
			public Object run() {
	        	try {
	        		String busybox = Devices.getCurrent().getBusybox(true);
	        		if (busybox.length()>0) {
		        		AdbUtility.push(busybox, GlobalConfig.getProperty("deviceworkdir"));
		        		Shell shell = new Shell("busyhelper");
		        		shell.run(false);
		        		shell = new Shell("instbusybox");
						shell.setProperty("BUSYBOXINSTALLPATH", Devices.getCurrent().getBusyBoxInstallPath());
						shell.runRoot();
				        MyLogger.getLogger().info("Installed version of busybox : " + Devices.getCurrent().getInstalledBusyboxVersion(true));
				        MyLogger.getLogger().info("Finished");
	        		}
	        		else {
	        			MyLogger.getLogger().info("Busybox installation canceled");
	        		}
		        }
	        	catch (Exception e) {
	        		MyLogger.getLogger().error(e.getMessage());
	        	}
	 			return null;
			}
		});
    }

    public void doClearCache() {
		Worker.post(new Job() {
			public Object run() {
	        	try {
						AdbUtility.clearcache();
						MyLogger.getLogger().info("Finished");
				}
				catch (Exception e) {}
	 			return null;
			}
		});
	}

    public void doConnectPaypal() {
    	showInBrowser("https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=PPWH7M9MNCEPA");
    }

    private boolean showInBrowser(String url){
		try {
			  Desktop.getDesktop().browse(new URI(url));
		} 
		catch (Exception e) {
		} 
        return true;
        // some mod here
	}

    public void doDisableIdent() {
    	if (guimode) {
			btnCleanroot.setEnabled(false);
			mntmInstallBusybox.setEnabled(false);
			mntmClearCache.setEnabled(false);
			mntmRootzergRush.setEnabled(false);
			mntmRootPsneuter.setEnabled(false);
			mntmRootEmulator.setEnabled(false);
			mntmRootAdbRestore.setEnabled(false);
			mntmBuildpropEditor.setEnabled(false);
			mntmBuildpropRebrand.setEnabled(false);
			mntmRebootIntoRecoveryT.setEnabled(false);
			mntmRebootDefaultRecovery.setEnabled(false);
			mntmSetDefaultRecovery.setEnabled(false);
			mntmSetDefaultKernel.setEnabled(false);
			mntmRebootCustomKernel.setEnabled(false);
			mntmRebootDefaultKernel.setEnabled(false);
			//mntmInstallBootkit.setEnabled(false);
			btnRoot.setEnabled(false);
			btnXrecovery.setEnabled(false);
			btnKernel.setEnabled(false);
			btnAskRootPerms.setEnabled(false);
			custBtn.setEnabled(false);
			//mntmCleanUninstalled.setEnabled(false);
	    	mntmBackupSystemApps.setEnabled(false);
    	}
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
			        		String devid="";
			        		deviceSelectGui devsel = new deviceSelectGui(null);
			        		devid = devsel.getDevice(founditems);
			    			if (devid.length()>0) {
			        			found = true;
			        			Devices.setCurrent(devid);
			        			String prop = DeviceProperties.getProperty(Devices.getCurrent().getBuildProp());
			        			if (!Devices.getCurrent().getRecognition().contains(prop)) {
			        				String reply = AskBox.getReplyOf("Do you want to permanently identify this device as \n"+Devices.getCurrent().getName()+"?");
			        				if (reply.equals("yes"))
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
		        			if (!Devices.isWaitingForReboot()) {
		        				MyLogger.getLogger().info("Installed version of busybox : " + Devices.getCurrent().getInstalledBusyboxVersion(false));
		        				MyLogger.getLogger().info("Android version : "+Devices.getCurrent().getVersion()+" / kernel version : "+Devices.getCurrent().getKernelVersion());
		        			}
		        			if (Devices.getCurrent().isRecovery()) {
		        				MyLogger.getLogger().info("Phone in recovery mode");
		        				btnRoot.setEnabled(false);
		        				btnAskRootPerms.setEnabled(false);
		        				doGiveRoot();
		        			}
		        			else {
		        				boolean hasSU = Devices.getCurrent().hasSU();
		        				btnRoot.setEnabled(!hasSU);
		        				if (hasSU) {
		        					boolean hasRoot = Devices.getCurrent().hasRoot();
		        					if (hasRoot) {
		        						doInstFlashtool();
		        						doGiveRoot();
		        					}
		        					btnAskRootPerms.setEnabled(!hasRoot);
		        				}
		        			}
		        			MyLogger.getLogger().debug("Now setting buttons availability - btnRoot");
		        			MyLogger.getLogger().debug("mtmRootzergRush menu");
		        			mntmRootzergRush.setEnabled(true);
		        			MyLogger.getLogger().debug("mtmRootPsneuter menu");
		        			mntmRootPsneuter.setEnabled(true);
		        			MyLogger.getLogger().debug("mtmRootEmulator menu");
		        			mntmRootEmulator.setEnabled(true);
		        			MyLogger.getLogger().debug("mtmRootAdbRestore menu");
		        			mntmRootAdbRestore.setEnabled(true);

		        			boolean flash = Devices.getCurrent().canFlash();
		        			MyLogger.getLogger().debug("flashBtn button "+flash);
		        			flashBtn.setEnabled(flash);
		        			MyLogger.getLogger().debug("custBtn button");
		        			custBtn.setEnabled(true);
		        			MyLogger.getLogger().debug("Now adding plugins");
		        			mnPlugins.removeAll();
		        			addDevicesPlugins();
		        			addGenericPlugins();
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
		btnCleanroot.setEnabled(true);
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
		btnKernel.setEnabled(Devices.getCurrent().canKernel());
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

    public void doBundle() {
		Worker.post(new Job() {
			public Object run() {
				try {
					BundleGUI bcre = new BundleGUI();
					Bundle b = null;
					//BootModeSelectGUI bootmode = new BootModeSelectGUI();
					//String select = bootmode.selectMode();
					//if (select.equals("flashmode"))g
						b = bcre.getBundle("flashmode");
					//if (select.equals("fastboot"))
						//b = bcre.getBundle("fastboot");
					if (b!=null) {
    					MyLogger.getLogger().info("Starting bundle creation");
    					b.createFTF();
    					MyLogger.getLogger().info("Finished bundle creation");
					}
				}
				catch (Exception e) {
					e.printStackTrace();
					MyLogger.getLogger().error(e.getMessage());}
				return null;
			}
		});
    }

    public void doBackupSystem() {
		Worker.post(new Job() {
			public Object run() {
				try {
					X10Apps apps = new X10Apps();
					Iterator<String> ic = apps.getCurrent().iterator();
					while (ic.hasNext()) {
						String app = ic.next();
						try {
							AdbUtility.pull("/system/app/"+app, "."+fsep+"custom"+fsep+"apps_saved"+File.separator+Devices.getCurrent().getId());
						}
						catch (Exception e) {}
					}
					MyLogger.getLogger().info("Backup Finished");
				}
				catch (Exception e) {
					MyLogger.getLogger().error(e.getMessage());}
				return null;
			}
		});
	}

    public void doInstallCustKit() {
		Worker.post(new Job() {
			public Object run() {
				try {
						MyLogger.getLogger().info("Installing chargemon feature / kernel bootkit to device...");
						Devices.getCurrent().doBusyboxHelper();
						if (AdbUtility.Sysremountrw()) {
							AdbUtility.push("."+fsep+"devices"+fsep+Devices.getCurrent().getId()+fsep+"bootkit"+fsep+"bootkit.tar",GlobalConfig.getProperty("deviceworkdir"));
							Shell shell = new Shell("installbootkit");
							shell.runRoot();
							MyLogger.getLogger().info("bootkit successfully installed");
						}
						else MyLogger.getLogger().error("Error mounting /system rw");
					}
				catch (Exception e) {
					MyLogger.getLogger().error(e.getMessage());}
				return null;
			}
		});
    }

    public void doInstallKernel() {
		Worker.post(new Job() {
			public Object run() {
				try {
						MyLogger.getLogger().info("Installing kernel to device...");
						Devices.getCurrent().doBusyboxHelper();
						if (AdbUtility.Sysremountrw()) {
							KernelSelectGUI sel = new KernelSelectGUI(Devices.getCurrent().getId());
							String selVersion = sel.getVersion();
							if (selVersion.length()>0) {
								doInstallCustKit();
								AdbUtility.push("."+fsep+"devices"+fsep+Devices.getCurrent().getId()+fsep+"kernel"+fsep+selVersion+fsep+"kernel.tar",GlobalConfig.getProperty("deviceworkdir"));
								Shell shell = new Shell("installkernel");
								shell.runRoot();
								MyLogger.getLogger().info("kernel successfully installed");
							}
							else {
								MyLogger.getLogger().info("Canceled");
							}
						}
						else MyLogger.getLogger().error("Error mounting /system rw");
					}
				catch (Exception e) {
					MyLogger.getLogger().error(e.getMessage());}
				return null;
			}
		});
    }
    
    public void addDevicesPlugins() {
    	try {
	    	File dir = new File(Devices.getCurrent().getDeviceDir()+fsep+"features");
		    File[] chld = dir.listFiles();
		    MyLogger.getLogger().debug("Found "+chld.length+" device plugins to add");
		    for(int i = 0; i < chld.length; i++){
		    	if (chld[i].isDirectory()) {
		    		try {
		    			Properties p = new Properties();
		    			p.load(new FileInputStream(new File(chld[i].getAbsolutePath()+fsep+"feature.properties")));
		    			MyLogger.getLogger().debug("Registering "+p.getProperty("classname"));
		    			ClassPath.addFile(chld[i].getAbsolutePath()+fsep+p.getProperty("plugin"));
		    			registerPlugin("device",p.getProperty("classname"),chld[i].getAbsolutePath());
		    		}
		    		catch (IOException ioe) {
		    		}
		    	}
		    }
    	}
    	catch (Exception e) {}
    }

    public void addGenericPlugins() {
    	try {
	    	File dir = new File(OS.getWorkDir()+fsep+"custom"+fsep+"features");
		    File[] chld = dir.listFiles();
		    MyLogger.getLogger().debug("Found "+chld.length+" generic plugins to add");
		    for(int i = 0; i < chld.length; i++){
		    	if (chld[i].isDirectory()) {
		    		try {
		    			Properties p = new Properties();
		    			p.load(new FileInputStream(new File(chld[i].getAbsolutePath()+fsep+"feature.properties")));
		    			ClassPath.addFile(chld[i].getAbsolutePath()+fsep+p.getProperty("plugin"));
		    			registerPlugin("generic",p.getProperty("classname"),chld[i].getAbsolutePath());
		    		}
		    		catch (IOException ioe) {
		    		}
		    	}
		    }
    	}
    	catch (Exception e) {
    		MyLogger.getLogger().debug(e.getMessage());
    	}
    }

    public void registerPlugin(String type, String classname, String workdir) {
	    try {
	    	MyLogger.getLogger().debug("Creating instance of "+classname);
	    	Class<?> pluginClass = Class.forName(classname);
	    	MyLogger.getLogger().debug("Getting constructor of "+classname);
            Constructor<?> constr = pluginClass.getConstructor();
            MyLogger.getLogger().debug("Now instanciating object of class "+classname);
            PluginInterface pluginObject = (PluginInterface)constr.newInstance();
            MyLogger.getLogger().debug("Setting plugin workdir");
            pluginObject.setWorkdir(workdir);
            MyLogger.getLogger().debug("Now giving rights to plugin");
            boolean aenabled = false;
            String aversion = Devices.getCurrent().getVersion();
            Enumeration <String> e1 = pluginObject.getCompatibleAndroidVersions();
            while (e1.hasMoreElements()) {
            	String pversion = e1.nextElement();
            	if (aversion.startsWith(pversion) || pversion.equals("any")) aenabled=true;
            }
            
            boolean kenabled = false;
            String kversion = Devices.getCurrent().getKernelVersion();
            Enumeration <String> e2 = pluginObject.getCompatibleKernelVersions();
            while (e2.hasMoreElements()) {
            	String pversion = e2.nextElement();
            	if (kversion.equals(pversion) || pversion.equals("any")) kenabled=true;
            }
            
            boolean denabled = false;
            if (type.equals("generic")) {
	            String currdevid = Devices.getCurrent().getId();
	            Enumeration <String> e3 = pluginObject.getCompatibleDevices();
	            while (e3.hasMoreElements()) {
	            	String pversion = e3.nextElement();
	            	if (currdevid.equals(pversion) || pversion.equals("any")) denabled=true;
	            }
            }
            else
            	denabled=true;

            boolean hasroot=false;
            if (pluginObject.isRootNeeded()) hasroot=Devices.getCurrent().hasRoot();
            else hasroot = true;
            JMenu pluginmenu = new JMenu(pluginObject.getName());

            JMenuItem run = new JMenuItem("Run");
            run.setEnabled(aenabled&&kenabled&&denabled&&hasroot);
            PluginActionListener p =  new PluginActionListener(pluginObject);
            run.addActionListener(p);

            JMenuItem about = new JMenuItem("About");
            PluginActionListenerAbout p1 = new PluginActionListenerAbout(pluginObject);
            about.addActionListener(p1);
            pluginmenu.add(run);
            pluginObject.setMenu(pluginmenu);
            pluginmenu.addSeparator();
            pluginmenu.add(about);

            if (type.equals("device")&&aenabled&&kenabled&&denabled&&hasroot) {
            	JMenu deviceMenu = new JMenu(Devices.getCurrent().getId());
            	deviceMenu.add(pluginmenu);
            	mnPlugins.add(deviceMenu);
            }
            else
            	if (aenabled&&kenabled&&denabled&&hasroot)
            		mnPlugins.add(pluginmenu);
	    }
	    catch (Exception e) {
	    	MyLogger.getLogger().error(e.getMessage());
	    }    	
    }

    public void doInstFlashtool() {
		try {
			if (!AdbUtility.exists("/system/flashtool")) {
				Devices.getCurrent().doBusyboxHelper();
				MyLogger.getLogger().info("Installing toolbox to device...");
				AdbUtility.push(OS.getWorkDir()+fsep+"custom"+fsep+"root"+fsep+"ftkit.tar",GlobalConfig.getProperty("deviceworkdir"));
				Shell shell = new Shell("installftkit");
				shell.runRoot();
			}
		}
		catch (Exception e) {
			MyLogger.getLogger().error(e.getMessage());
		}
    }

    public static void doImportDevice(String device) throws Exception {
    	File ftd = new File(OS.getWorkDir()+OS.getFileSeparator()+"devices"+OS.getFileSeparator()+device+".ftd");
    	String destDir = OS.getWorkDir()+java.io.File.separator+"devices";
    	new File(destDir+File.separator+device).mkdir();
    	JarFile jar = new JarFile(ftd);
    	boolean alldirs=false;
    	Enumeration e;
    	while (!alldirs) {
	    	e = jar.entries();
	    	alldirs=true;
	    	while (e.hasMoreElements()) {
	    	    JarEntry file = (JarEntry) e.nextElement();
	    	    File f = new File(destDir + File.separator + file.getName());
	    	    if (file.isDirectory()) { // if its a directory, create it
	    	    	if (!f.exists())
	    	    		if (!f.mkdir()) alldirs=false;
	    	    }
	    	}
    	}
    	e = jar.entries();
    	while (e.hasMoreElements()) {
    	    JarEntry file = (JarEntry) e.nextElement();
    	    File f = new File(destDir + File.separator + file.getName());
    	    if (!file.isDirectory()) { // if its a directory, create it
	    	    InputStream is = jar.getInputStream(file); // get the input stream
	    	    FileOutputStream fos = new FileOutputStream(f);
	    	    while (is.available() > 0) {  // write contents of 'is' to 'fos'
	    	        fos.write(is.read());
	    	    }
	    	    fos.close();
	    	    is.close();
    	    }
    	}
    }
    
    public static void doExportDevice(String device) throws Exception {
		File ftd = new File(OS.getWorkDir()+OS.getFileSeparator()+"devices"+OS.getFileSeparator()+device+".ftd");
		byte buffer[] = new byte[10240];
	    FileOutputStream stream = new FileOutputStream(ftd);
	    JarOutputStream out = new JarOutputStream(stream);
	    out.setLevel(Deflater.BEST_SPEED);
	    File root = new File(OS.getWorkDir()+OS.getFileSeparator()+"devices"+OS.getFileSeparator()+device);
	    int rootindex = root.getAbsolutePath().length();
		Collection<File> c = OS.listFileTree(root);
		Iterator<File> i = c.iterator();
		while (i.hasNext()) {
			File entry = i.next();
			String name = entry.getAbsolutePath().substring(rootindex-device.length());
			if (entry.isDirectory()) name = name+"/";
		    JarEntry jarAdd = new JarEntry(name);
	        out.putNextEntry(jarAdd);
	        if (!entry.isDirectory()) {
	        InputStream in = new FileInputStream(entry);
	        while (true) {
	          int nRead = in.read(buffer, 0, buffer.length);
	          if (nRead <= 0)
	            break;
	          out.write(buffer, 0, nRead);
	        }
	        in.close();
	        }
		}
		out.close();
	    stream.close();
	}
    
    public static void doDeleteDevice(String device) {
    	String destDir = OS.getWorkDir()+File.separator+"devices"+File.separator+device;
    	File folder = new File(destDir);
    	deleteFolder(folder);
    }
    
    public static void deleteFolder(File folder) {
        File[] files = folder.listFiles();
        if(files!=null) { //some JVMs return null for empty dirs
            for(File f: files) {
                if(f.isDirectory()) {
                    deleteFolder(f);
                } else {
                    f.delete();
                }
            }
        }
        folder.delete();    	
    }
    
	public String chooseYaffs2() {
		JFileChooser chooser = new JFileChooser(new java.io.File(".")); 

		FileFilter ff = new FileFilter(){
			public boolean accept(File f){
				if(f.isDirectory()) return true;
				else if(f.getName().endsWith(".yaffs2")) return true;
				else return false;
			}
			public String getDescription(){
				return "*.yaffs2";
			}
		};
		 
		chooser.removeChoosableFileFilter(chooser.getAcceptAllFileFilter());
		chooser.setFileFilter(ff);
		
	    chooser.setDialogTitle("Choose sin file)");
	    chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
	    //chooser.setFileFilter(newkernelimgFileFilter);
	    //
	    // disable the "All files" option.
	    //
	    chooser.setAcceptAllFileFilterUsed(false);
	    //    
	    if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
	    	return chooser.getSelectedFile().getAbsolutePath();
	    }
	    return "ERROR";
	}

}
