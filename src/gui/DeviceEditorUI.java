package gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileFilter;

import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.RowSpec;
import com.jgoodies.forms.factories.FormFactory;

import flashsystem.BundleEntry;
import flashsystem.SinFile;

import javax.swing.JLabel;
import javax.swing.JTextField;

import org.logger.MyLogger;
import org.system.DeviceEntry;
import org.system.Devices;
import org.system.OS;
import org.system.PropertiesFile;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Properties;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.Deflater;

import javax.swing.JCheckBox;

public class DeviceEditorUI extends JDialog {

	private final JPanel contentPanel = new JPanel();
	private JTextField deviceid;
	private JTextField devicename;
	private JTextField buildprop;
	private JTextField recognition;
	private JTextField loaderpath;
	private JTextField busyboxinstall;
	private JTextField busyboxversion;
	private PropertiesFile config;
	private JCheckBox canflashmode;
	private JCheckBox canfastboot;
	private DeviceEntry _ent = null;
	private Properties BusyboxBag = new Properties();

	/**
	 * Create the dialog.
	 */
	public DeviceEditorUI() {
		setModal(true);
		config = new PropertiesFile("org/system/ressources/deviceTemplate.properties","");
		setTitle("Device Editor");
		setBounds(100, 100, 461, 368);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new FormLayout(new ColumnSpec[] {
				FormFactory.RELATED_GAP_COLSPEC,
				FormFactory.DEFAULT_COLSPEC,
				FormFactory.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("right:max(31dlu;default):grow"),
				FormFactory.RELATED_GAP_COLSPEC,
				FormFactory.DEFAULT_COLSPEC,},
			new RowSpec[] {
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,}));
		{
			JLabel lblDeviceId = new JLabel("Device ID");
			contentPanel.add(lblDeviceId, "2, 4, right, default");
		}
		{
			deviceid = new JTextField();
			contentPanel.add(deviceid, "4, 4, fill, default");
			deviceid.setColumns(10);
		}
		{
			JLabel lblDeviceName = new JLabel("Device Name");
			contentPanel.add(lblDeviceName, "2, 6, right, default");
		}
		{
			devicename = new JTextField();
			contentPanel.add(devicename, "4, 6, fill, default");
			devicename.setColumns(10);
		}
		{
			JLabel lblBuildpropProperty = new JLabel("Build.prop property");
			contentPanel.add(lblBuildpropProperty, "2, 8, right, default");
		}
		{
			buildprop = new JTextField();
			contentPanel.add(buildprop, "4, 8, fill, default");
			buildprop.setColumns(10);
		}
		{
			JLabel lblRecognitionPatterns = new JLabel("Recognition patterns");
			contentPanel.add(lblRecognitionPatterns, "2, 10, right, default");
		}
		{
			recognition = new JTextField();
			contentPanel.add(recognition, "4, 10, fill, default");
			recognition.setColumns(10);
		}
		{
			JLabel lblLoader = new JLabel("Loader");
			contentPanel.add(lblLoader, "2, 12, right, default");
		}
		{
			loaderpath = new JTextField();
			loaderpath.setEditable(false);
			contentPanel.add(loaderpath, "4, 12, fill, default");
			loaderpath.setColumns(10);
		}
		{
			JButton btnNewButton = new JButton("...");
			btnNewButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					loaderpath.setText(doChoose());
					try {
						SinFile l = new SinFile(loaderpath.getText());
						if (l.getSinHeader().getNbHashBlocks()>1) {
							JOptionPane.showMessageDialog(null, "This file is not a loader");
							loaderpath.setText("");
						}
					}
					catch (IOException ioe) {
					}
				}
			});
			contentPanel.add(btnNewButton, "6, 12");
		}
		{
			JLabel lblBusyboxInstallPath = new JLabel("Busybox install path");
			contentPanel.add(lblBusyboxInstallPath, "2, 14, right, default");
		}
		{
			busyboxinstall = new JTextField();
			contentPanel.add(busyboxinstall, "4, 14, fill, default");
			busyboxinstall.setColumns(10);
		}
		{
			JLabel lblDefaultBusybox = new JLabel("Default busybox");
			contentPanel.add(lblDefaultBusybox, "2, 16, right, default");
		}
		{
			busyboxversion = new JTextField();
			busyboxversion.setEditable(false);
			contentPanel.add(busyboxversion, "4, 16, fill, default");
			busyboxversion.setColumns(10);
		}
		{
			JButton button = new JButton("...");
			button.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					fillBusybox();
					BusyBoxSelectGUI bui = new BusyBoxSelectGUI(BusyboxBag);
					String result = bui.getVersion();
					if (result.length()>0)
						busyboxversion.setText(result);
				}
			});
			contentPanel.add(button, "6, 16");
		}
		{
			canflashmode = new JCheckBox("Flash mode");
			contentPanel.add(canflashmode, "2, 18");
		}
		{
			canfastboot = new JCheckBox("Fastboot mode");
			contentPanel.add(canfastboot, "2, 20");
		}
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton("OK");
				okButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent arg0) {
						if (deviceid.getText().length()==0) {
							JOptionPane.showMessageDialog(null, "The deviceID must be set");
							return;
						}
						boolean deviceExists = false;
						File[] list = (new File(OS.getWorkDir()+OS.getFileSeparator()+"devices")).listFiles();
						for (int i=0;i<list.length;i++) {
							if (list[i].getName().toUpperCase().equals(deviceid.getText().toUpperCase()))
									deviceExists=true;
						}
						if (deviceExists && _ent==null) {
							JOptionPane.showMessageDialog(null, "This deviceID already exists");
							return;
						}
						config.setProperty("internalname", deviceid.getText().toUpperCase());
						if (devicename.getText().length()==0) {
							JOptionPane.showMessageDialog(null, "The device name must be set");
							return;								
						}
						Enumeration e = Devices.listDevices(true);
						while (e.hasMoreElements()) {
							String dev = (String)e.nextElement();
							DeviceEntry ent = Devices.getDevice(dev);
							if (devicename.getText().toUpperCase().equals(ent.getName().toUpperCase())) {
								if (_ent!=null) {
									if (!_ent.getName().toUpperCase().equals(ent.getName().toUpperCase())) {
										JOptionPane.showMessageDialog(null, "The given name already exists for another device in your collection");
										return;
									}
								}
								else {
									JOptionPane.showMessageDialog(null, "The given name already exists for another device in your collection");
									return;									
								}
							}
						}
						config.setProperty("realname",devicename.getText());
						if (buildprop.getText().length()==0) {
							JOptionPane.showMessageDialog(null, "The build.prop property must be set");
							return;
						}
						config.setProperty("buildprop",buildprop.getText());
						if (recognition.getText().length()==0) {
							JOptionPane.showMessageDialog(null, "The recognition pattern list must be set");
							return;
						}
						config.setProperty("recognition",recognition.getText());
						if (loaderpath.getText().length()==0) {
							JOptionPane.showMessageDialog(null, "You must add a loader");
							return;
						}
						config.setProperty("loader",OS.getMD5(new File(loaderpath.getText())));
						if (busyboxversion.getText().length()==0) {
							JOptionPane.showMessageDialog(null, "You must choose a busybox default version");
							return;
						}
						config.setProperty("busyboxhelper",busyboxversion.getText());
						if (busyboxinstall.getText().length()==0) {
							JOptionPane.showMessageDialog(null, "You must set busybox install path on the phone");
							return;
						}
						config.setProperty("busyboxinstallpath",busyboxinstall.getText());
						config.setProperty("canflash",canflashmode.isSelected()?"true":"false");
						config.setProperty("canfastboot",canfastboot.isSelected()?"true":"false");
						File f = new File(OS.getWorkDir()+"/devices/"+deviceid.getText().toUpperCase());
						File f1 = new File(f.getAbsolutePath()+OS.getFileSeparator()+"busybox");
						File f2 = new File(f.getAbsolutePath()+OS.getFileSeparator()+"features");
						
						if (!f.exists()) f.mkdir();
						if (!f1.exists()) f1.mkdir();
						if (!f2.exists()) f2.mkdir();

						config.write(OS.getWorkDir()+"/devices/"+deviceid.getText().toUpperCase()+"/"+deviceid.getText().toUpperCase()+".properties", "UTF-8");
						OS.copyfile(loaderpath.getText(), f.getAbsolutePath()+"/loader.sin");
						Enumeration en = BusyboxBag.keys();
						while (en.hasMoreElements()) {
							String version = (String)en.nextElement();
							String file = f1.getAbsolutePath()+OS.getFileSeparator()+version+OS.getFileSeparator()+"busybox";
							if (!new File(file).exists()) {
								new File(f1.getAbsolutePath()+OS.getFileSeparator()+version).mkdir();
								OS.copyfile(BusyboxBag.getProperty(version), file);
							}
						}
						dispose();
					}
				});
				okButton.setActionCommand("OK");
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
			{
				JButton cancelButton = new JButton("Cancel");
				cancelButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent arg0) {
						dispose();
					}
				});
				cancelButton.setActionCommand("Cancel");
				buttonPane.add(cancelButton);
			}
		}
	}
	
	public String doChoose() {
		JFileChooser chooser = new JFileChooser(new java.io.File(".")); 

		FileFilter ff = new FileFilter(){
			public boolean accept(File f){
				if(f.isDirectory()) return true;
				else if(f.getName().toUpperCase().endsWith(".SIN")) return true;
				else return false;
			}
			public String getDescription(){
				return "*.sin";
			}
		};
		 
		chooser.removeChoosableFileFilter(chooser.getAcceptAllFileFilter());
		chooser.setFileFilter(ff);
		
	    chooser.setDialogTitle("Choose loader");
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
	    return "";
	}
	
	public void setEntry(DeviceEntry ent) {
		_ent=ent;
		deviceid.setText(ent.getId());
		deviceid.setEditable(false);
		devicename.setText(ent.getName());
		buildprop.setText(ent.getBuildProp());
		recognition.setText(ent.getRecognition());
		loaderpath.setText(ent.getLoader());
		busyboxinstall.setText(ent.getBusyBoxInstallPath());
		busyboxversion.setText(new File(ent.getBusybox(false)).getParentFile().getName());
		canflashmode.setSelected(ent.canFlash());
		canfastboot.setSelected(ent.canFastboot());
	}
	
	public void fillBusybox() {
    	File dir = new File(OS.getWorkDir()+"/devices/"+deviceid.getText()+"/busybox");
	    File[] chld = dir.listFiles();
	    if (chld != null)
	    for(int i = 0; i < chld.length; i++){
	    	if (chld[i].isDirectory()) {
	    		BusyboxBag.setProperty(chld[i].getName(), chld[i].getAbsolutePath()+"/busybox");
	    	}
	    }
	}

}
