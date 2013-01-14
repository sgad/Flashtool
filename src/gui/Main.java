package gui;

import java.awt.EventQueue;
import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;

import javax.imageio.ImageIO;
import javax.swing.UIManager;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import linuxlib.JUsb;

import org.lang.Language;
import org.logger.MyLogger;
import org.simplericity.macify.eawt.Application;
import org.simplericity.macify.eawt.DefaultApplication;
import org.system.GlobalConfig;
import org.system.OS;
import org.system.ProcessBuilderWrapper;

import flashsystem.FlasherConsole;

public class Main {

	public static void main(String[] args) {
		initLinuxUsb();		
		try {
			OptionSet options = parseCmdLine(args);
			if (options.has("console")) {
				processConsole(options);
			}
			else {
				processSwingUI();
				processSwtUI();
	        }
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void macSetup() {
		if (OS.getName().startsWith("mac")) {
			System.setProperty("apple.laf.useScreenMenuBar", "true");
			System.setProperty("com.apple.mrj.application.apple.menu.about.name","Flashtool");
		}
	}

	private static void setSystemLookAndFeel() {
		if (!OS.getName().startsWith("linux")) {
		try {
				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());			
		}
		catch (Exception e) {}
		}
	}
	
	private static void initLinuxUsb() {
		try {
			if (OS.getName()!="windows") JUsb.init();
		}
		catch (UnsatisfiedLinkError e) {
			e.printStackTrace();
			System.out.println("libusbx 1.0 not installed. 1.0.14 version mandatory");
			System.out.println("It can be downloaded on http://www.libusbx.org");
			System.exit(1);
		}
	}

	private static OptionSet parseCmdLine(String[] args) {
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
		return options;
	}

	public static void processConsole(OptionSet options) throws Exception {
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
	
	public static void processSwingUI() throws Exception {
		macSetup();
		final Application app = new DefaultApplication();
		app.setApplicationIconImage(ImageIO.read(FlasherGUI.class.getResource("/gui/ressources/icons/flash_512.png")));
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					setSystemLookAndFeel();
					FlasherGUI frame = new FlasherGUI(app);					
					frame.setVisible(true);
				}
				catch (Exception e) {}
			}
		});
	}

	public static void processSwtUI() {
		try {
			FlashtoolSWT shell = new FlashtoolSWT();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
