package gui;

import java.awt.EventQueue;
import javax.imageio.ImageIO;
import javax.swing.UIManager;
import org.simplericity.macify.eawt.Application;
import org.simplericity.macify.eawt.DefaultApplication;
import org.system.OS;

public class MainSwing {

	public void macSetup() {
		if (OS.getName().startsWith("mac")) {
			System.setProperty("apple.laf.useScreenMenuBar", "true");
			System.setProperty("com.apple.mrj.application.apple.menu.about.name","Flashtool");
		}
	}

	private void setSystemLookAndFeel() {
		if (!OS.getName().startsWith("linux")) {
		try {
				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());			
		}
		catch (Exception e) {}
		}
	}
	
	public void processSwingUI() throws Exception {
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

}
