package gui;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;

import org.adb.AdbUtility;
import org.system.Devices;
import org.system.OS;
import org.system.PropertiesFile;

public class X10Apps {

	private static String fsep = OS.getFileSeparator();
	private PropertiesFile deviceList=new PropertiesFile("","."+fsep+"custom"+fsep+"clean"+fsep+Devices.getCurrent().getId()+fsep+Devices.getCurrent().getId()+"list.properties");
	private PropertiesFile customList=new PropertiesFile("","."+fsep+"custom"+fsep+"clean"+fsep+Devices.getCurrent().getId()+fsep+Devices.getCurrent().getId()+"customlist.properties");
	//private Properties x10List = new Properties();
	private PropertiesFile safeList;
	private HashSet<String> currentList;
	private String currentProfile="";
	private Properties realnames = new Properties();
	private HashMap<String,PropertiesFile> Allsafelist = new HashMap<String,PropertiesFile>();

	// Copies src file to dst file.
	// If the dst file does not exist, it is created
	private void copyToAppsSaved(File src) throws IOException {
		File dst = new File("./custom/apps_saved/"+Devices.getCurrent().getId()+fsep+src.getName());
		if (!dst.exists()) {
		    InputStream in = new FileInputStream(src);
		    OutputStream out = new FileOutputStream(dst);
	
		    // Transfer bytes from in to out
		    byte[] buf = new byte[1024];
		    int len;
		    while ((len = in.read(buf)) > 0) {
		        out.write(buf, 0, len);
		    }
		    in.close();
		    out.close();
		}
	}
	
	public Properties deviceList() {
		return deviceList.getProperties();
	}

	public Properties customList() {
		return customList.getProperties();
	}

	public void addApk(File apk, String desc) {
		try {
			copyToAppsSaved(apk);
			customList.setProperty(apk.getName(), desc);
			customList.write("UTF-8");
			realnames.setProperty(desc, apk.getName());
			rescan();
		}
		catch (Exception e) {
		}
	}
	
	public void modApk(String apkname, String desc) {
		if (customList.getProperties().containsKey(apkname)) {
			customList.setProperty(apkname,desc);
			customList.write("UTF-8");
		}
		if (deviceList.getProperties().containsKey(apkname)) {
			deviceList.setProperty(apkname,desc);
			deviceList.write("UTF-8");
		}
		realnames.setProperty(desc, apkname);
		rescan();
	}
	
	private void rescan() {
		File[] dirlist = (new File("."+fsep+"custom"+fsep+"clean"+fsep+Devices.getCurrent().getId())).listFiles();
		for (int i=0;i<dirlist.length;i++) {
			if (dirlist[i].getName().contains("safelist")) {
				String key = dirlist[i].getName().replace((CharSequence)"safelist", (CharSequence)"");
				key=key.replace((CharSequence)".properties", (CharSequence)"");
				if (!Allsafelist.containsKey(key.toLowerCase()))
					Allsafelist.put(key.toLowerCase(),new PropertiesFile("",dirlist[i].getPath()));
			}
		}
	}
	
	public String getCurrentProfile() {
		return currentProfile;
	}
	
	public X10Apps() {
		try {
			currentList = AdbUtility.listSysApps();
			Iterator ic = currentList.iterator();
			while (ic.hasNext()) {
				String next = (String)ic.next();
				if (!deviceList.getProperties().containsKey(next))
					deviceList.setProperty(next, next);
			}
			deviceList.write("UTF-8");
			safeList = new PropertiesFile();
			Iterator i = deviceList.getProperties().keySet().iterator();
			while (i.hasNext()) {
				String key = (String)i.next();
				safeList.setProperty(key, "unsafe");
			}
			currentProfile="default";
			Allsafelist.put("default",safeList);
			if (!new File("."+fsep+"custom"+fsep+"clean"+fsep+Devices.getCurrent().getId()+fsep+"safelist"+currentProfile+".properties").exists())
				saveProfile(currentProfile);
			Iterator i1 = customList.getProperties().keySet().iterator();
			while (i1.hasNext()) {
				String key = (String)i1.next();
			}
			rescan();
		}
		catch (Exception e) {
		}
	}
	
	public void setProfile(String profile) {
		currentProfile=profile;
		safeList = Allsafelist.get(profile);
		fillSet();
		deviceList.write("UTF-8");
		customList.write("UTF-8");
	}

	public void saveProfile() {
		Allsafelist.get(currentProfile).write("UTF-8");
	}

	public void saveProfile(String name) {
		Allsafelist.get(currentProfile).write("."+fsep+"custom"+fsep+"clean"+fsep+Devices.getCurrent().getId()+fsep+"safelist"+name+".properties","UTF-8");
		rescan();
		setProfile(name.toLowerCase());
	}
	
	private void fillSet() {
		try {
			Iterator<String> i = currentList.iterator();
			while (i.hasNext()) {
				String apk = i.next();
				if (safeList.getProperty(apk)==null) {
					safeList.setProperty(apk,"unsafe");
				}
			}
			Enumeration<Object> e = safeList.getProperties().keys();
			while (e.hasMoreElements()) {
				String apk = (String)e.nextElement();
			}
			Iterator<Object> i1 = deviceList.keySet().iterator();
			while (i1.hasNext()) {
				String key = (String)i1.next();
				realnames.setProperty(deviceList.getProperty(key), key);
			}
		}
		catch (Exception e) {
		}
	}

	public Set<String> getProfiles() {
		return Allsafelist.keySet();
	}
	
	public HashSet<String> getCurrent() {
		return currentList;
	}
	
	public String getRealName(String apk) {
		return deviceList.getProperty(apk);
	}
	
	public String getApkName(String realName) {
		return realnames.getProperty(realName);
	}

	public void setSafe(String apkName) {
		safeList.setProperty(apkName, "safe");
	}
	
	public void setUnsafe(String apkName) {
		safeList.setProperty(apkName, "unsafe");
	}
	
	public Enumeration<String> getToBeRemoved() {
		Vector<String> v = new Vector<String>();
		Iterator<String> ic = currentList.iterator();
		while (ic.hasNext()) {
			String apk=ic.next();
			if (safeList.getProperty(apk).equals("safe")) v.add(apk);
		}
		return v.elements();
	}

	public Enumeration<String> getRemoved() {
		Vector<String> v = new Vector<String>();
		Iterator<Object> il = safeList.keySet().iterator();
		while (il.hasNext()) {
			String apk=(String)il.next();
			if (!currentList.contains(apk) && safeList.getProperty(apk).equals("safe")) v.add(apk);
		}
		return v.elements();		
	}

	public Enumeration<String> getToBeInstalled() {
		Vector<String> v = new Vector<String>();
		Iterator<Object> il = safeList.keySet().iterator();
		while (il.hasNext()) {
			String apk=(String)il.next();
			if (!currentList.contains(apk) && safeList.getProperty(apk).equals("unsafe")) v.add(apk);
		}
		return v.elements();		
	}

	public Enumeration<String> getInstalled() {
		Vector<String> v = new Vector<String>();
		Iterator<String> ic = currentList.iterator();
		while (ic.hasNext()) {
			String apk=ic.next();
			if (safeList.getProperty(apk).equals("unsafe")) v.add(apk);
		}
		return v.elements();
	}
	
}