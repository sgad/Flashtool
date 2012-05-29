package flashsystem;

import gui.FirmwareFileFilter;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Vector;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import org.logger.MyLogger;
import org.system.OS;

public final class Bundle {

	private JarFile _firmware;
    private boolean _simulate=false;
    private Properties bundleList=new Properties();
    private String _version;
    private String _branding;
    private String _device;
    private String _cmd25;
    public final static int JARTYPE=1;
    public final static int FOLDERTYPE=2;
    private BundleMetaData _meta;

    public Bundle() {
    	_meta = new BundleMetaData();
    }
    
    public Bundle(String path, int type) {
    	feed(path,type);
    }

    public void setMeta(BundleMetaData meta) {
    	_meta = meta;
    	feedFromMeta();
    }
    
    public Bundle(String path, int type, BundleMetaData meta) {
    	_meta = meta;
    	feed(path,type);
    }
    
    private void feed(String path, int type) {
    	if (type==JARTYPE) feedFromJar(path);
    	if (type==FOLDERTYPE) feedFromFolder(path);    	
    }
    
	private void feedFromJar(String path) {
		try {
			_firmware = new JarFile(path);
			_meta = new BundleMetaData();
			MyLogger.getLogger().debug("Creating bundle from ftf file : "+_firmware.getName());
			Enumeration<JarEntry> e = _firmware.entries();
			while (e.hasMoreElements()) {
				BundleEntry entry = new BundleEntry(this,e.nextElement());
				if (entry.getName().toUpperCase().endsWith("SIN") || entry.getName().toUpperCase().endsWith("TA")) {
					try {
						_meta.process(entry.getName(), "");
					}
					catch (Exception e1) {
					}
					bundleList.put(entry.getName(), entry);
					MyLogger.getLogger().debug("Added this entry to the bundle list : "+entry.getName());
				}
			}
		}
		catch (IOException ioe) {
			MyLogger.getLogger().error("Cannot open the file "+path);
		}
	}

	private void feedFromFolder(String path) {
		File[] list = (new File(path)).listFiles(new FirmwareFileFilter());
		for (int i=0;i<list.length;i++) {
			BundleEntry entry = new BundleEntry(list[i],list[i].getName());
			bundleList.put(entry.getName(), entry);
			MyLogger.getLogger().debug("Added this entry to the bundle list : "+entry.getName());
		}
	}

	private void feedFromMeta() {
		bundleList.clear();
		Enumeration<String> all = _meta.getAllEntries();
		while (all.hasMoreElements()) {
			String name = all.nextElement();
			BundleEntry entry = new BundleEntry(new File(_meta.getPath(name)),name);
			bundleList.put(entry.getName(), entry);
			MyLogger.getLogger().debug("Added this entry to the bundle list : "+entry.getName());
		}
	}

	public void setLoader(File loader) {
		try {
			if (_meta!=null)
				_meta.process("loader.sin", loader.getAbsolutePath());
		}
		catch (Exception e) {
		}
		BundleEntry entry = new BundleEntry(loader,"loader.sin");
		bundleList.put("loader.sin", entry);
	}

	public void setSimulate(boolean simulate) {
		_simulate = simulate;
	}

	public BundleEntry getEntry(String name) {
		return (BundleEntry)bundleList.get(name);
	}
	
	public Enumeration <BundleEntry> allEntries() {
		Vector<BundleEntry> v = new Vector<BundleEntry>();
		Enumeration<Object> e = bundleList.keys();
		while (e.hasMoreElements()) {
			String key = (String)e.nextElement();
			BundleEntry entry = (BundleEntry)bundleList.get(key);
			v.add(entry);
		}
		return v.elements();
	}

	public InputStream getImageStream(JarEntry j) throws IOException {
		return _firmware.getInputStream(j);
	}
		
	public boolean hasTA() {
		return _meta.hasCategorie("TA");
	}

	public BundleEntry getLoader() throws IOException, FileNotFoundException {
		return (BundleEntry)bundleList.get("loader.sin");
	}

	public boolean hasLoader() {
		return _meta.hasCategorie("LOADER");
	}

	public BundleEntry getPartition() throws IOException, FileNotFoundException {
		return (BundleEntry)bundleList.get(_meta.getEntriesOf("PARTITION").nextElement());
	}

	public boolean hasPartition() {
		return _meta.hasCategorie("PARTITION");
	}
	
	public boolean simulate() {
		return _simulate;
	}
	
	public void setVersion(String version) {
		_version=version;
	}
	
	public void setBranding(String branding) {
		_branding=branding;
		
	}
	
	public void setDevice(String device) {
		_device=device;
	}
	
	public void setCmd25(String value) {
		_cmd25 = value;
		if (_cmd25==null) _cmd25="false";
	}
	
	public boolean hasCmd25() {
		return _cmd25.equals("true");
	}
	
	public void createFTF() throws Exception {
		File ftf = new File("./firmwares/"+_device+"_"+_version+"_"+_branding+".ftf");
		byte buffer[] = new byte[10240];
		StringBuffer sbuf = new StringBuffer();
		sbuf.append("Manifest-Version: 1.0\n");
		sbuf.append("Created-By: FlashTool\n");
		sbuf.append("version: "+_version+"\n");
		sbuf.append("branding: "+_branding+"\n");
		sbuf.append("device: "+_device+"\n");
		sbuf.append("cmd25: "+_cmd25+"\n");
		Manifest manifest = new Manifest(new ByteArrayInputStream(sbuf.toString().getBytes("UTF-8")));
	    FileOutputStream stream = new FileOutputStream(ftf);
	    JarOutputStream out = new JarOutputStream(stream, manifest);
	    out.setLevel(JarOutputStream.STORED);
		Enumeration<BundleEntry> e = allEntries();
		while (e.hasMoreElements()) {
			BundleEntry entry = e.nextElement();
			String name = entry.getName();
			int S1pos = name.toUpperCase().indexOf("_S1");
			if (S1pos > 0) name = name.substring(0,S1pos)+".sin";
			MyLogger.getLogger().info("Adding "+entry.getName()+" to the bundle");
		    JarEntry jarAdd = new JarEntry(name);
	        out.putNextEntry(jarAdd);
	        InputStream in = entry.getInputStream();
	        while (true) {
	          int nRead = in.read(buffer, 0, buffer.length);
	          if (nRead <= 0)
	            break;
	          out.write(buffer, 0, nRead);
	        }
	        in.close();
		}
		out.close();
	    stream.close();
	}

	private void saveEntry(BundleEntry entry) throws IOException {
		if (entry.isJarEntry()) {
			MyLogger.getLogger().debug("Saving entry "+entry.getName()+" to disk");
			InputStream in = entry.getInputStream();
			String outname = "."+OS.getFileSeparator()+"firmwares"+OS.getFileSeparator()+"prepared"+OS.getFileSeparator()+entry.getName();
			MyLogger.getLogger().debug("Writing Entry to "+outname);
			OutputStream out = new BufferedOutputStream(new FileOutputStream(outname));
			byte[] buffer = new byte[1024];
			int len;
			while((len = in.read(buffer)) >= 0)
				out.write(buffer, 0, len);
			in.close();
			out.close();
			bundleList.put(entry.getName(), new BundleEntry(new File(outname),entry.getName()));
		}
	}

	public void open() throws BundleException {
		try {
			File f = new File("."+OS.getFileSeparator()+"firmwares"+OS.getFileSeparator()+"prepared");
			f.mkdir();
			MyLogger.getLogger().debug("Created the "+f.getName()+" folder");
			Enumeration<String> entries = _meta.getAllEntries();
			while (entries.hasMoreElements()) {
				saveEntry(getEntry(entries.nextElement()));
			}
			if (hasLoader())
				saveEntry(getLoader());
		}
		catch (Exception e) {
			throw new BundleException(e.getMessage());
		}
    }

	public void close() {
		if (_firmware !=null) {
			Enumeration<JarEntry> e=_firmware.entries();
			while (e.hasMoreElements()) {
				JarEntry entry = e.nextElement();
				if (entry.getName().toUpperCase().endsWith("SIN") || entry.getName().toUpperCase().endsWith("TA")) {
					String outname = "."+OS.getFileSeparator()+"firmwares"+OS.getFileSeparator()+"prepared"+OS.getFileSeparator()+entry.getName();
					File f = new File(outname);
					f.delete();
				}
			}
			File f = new File("."+OS.getFileSeparator()+"firmwares"+OS.getFileSeparator()+"prepared");
			f.delete();
			try {
				_firmware.close();
			}
			catch (IOException ioe) {}
		}
	}
	
	public void removeEntry(String name) {
		bundleList.remove(name);
	}
	
	public BundleMetaData getMeta() {
		return _meta;
	}

}