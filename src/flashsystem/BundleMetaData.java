package flashsystem;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Vector;
import org.jdom.Element;
import org.jdom.Document;
import org.jdom.output.XMLOutputter;

public class BundleMetaData {

	Properties _categ = new Properties();
	Properties _ftoint = new Properties();
	Properties _inttof = new Properties();
	Properties _pathtof = new Properties();
	Properties _categex = new Properties();
	Properties _categwipe = new Properties();
	Properties _enabled = new Properties();
	Properties _catorders = new Properties();
	
	public BundleMetaData() {
		_categex.setProperty("SYSTEM", "Exclude system");
		_categex.setProperty("BASEBAND", "Exclude baseband");
		_categex.setProperty("KERNEL", "Exclude kernel");
		_categex.setProperty("PARTITION", "Exclude partition");
		_categex.setProperty("NONE", "Exclude uncategorized");
		_categex.setProperty("FOTA", "Exclude Fota");
		_categex.setProperty("TA", "Exclude TA");
		_categwipe.setProperty("DATA", "Wipe data");
		_categwipe.setProperty("CACHE", "Wipe cache");
		_categwipe.setProperty("LOG", "Wipe apps log");
		_catorders.setProperty("1", "KERNEL");
		_catorders.setProperty("2", "FOTA");
		_catorders.setProperty("3", "BASEBAND");
		_catorders.setProperty("4", "NONE");
		_catorders.setProperty("5", "LOG");
		_catorders.setProperty("6", "SYSTEM");
		_catorders.setProperty("7", "DATA");
		_catorders.setProperty("8", "CACHE");
	}
	
	public int getNbCategs() {
		return _catorders.size();
	}
	
	public String getCagorie(int order) {
		return _catorders.getProperty(Integer.toString(order));
	}
	
	public Enumeration getCategories() {
		return _categ.keys();
	}
	
	public Enumeration getWipe() {
		Vector<String> list = new Vector<String>();
		Enumeration categlist = _categ.keys();
		while (categlist.hasMoreElements()) {
			String key = (String)categlist.nextElement();
			if (_categwipe.containsKey(key)) {
				list.add(key);
			}
		}
		return list.elements();
	}

	public String getWipeLabel(String categ) {
		return _categwipe.getProperty(categ);
	}

	public String getExcludeLabel(String categ) {
		return _categex.getProperty(categ);
	}
	
	public Enumeration getExclude() {
		Vector<String> list = new Vector<String>();
		Enumeration categlist = _categ.keys();
		while (categlist.hasMoreElements()) {
			String key = (String)categlist.nextElement();
			if (_categex.containsKey(key)) {
				list.add(key);
			}
		}
		return list.elements();
	}

	public void process(String fname,String path) throws Exception {
		String intname = fname;
		int S1pos = intname.toUpperCase().indexOf("_S1");
		if (S1pos > 0) intname = intname.substring(0,S1pos)+".sin";
		_ftoint.setProperty(fname, intname);
		_inttof.setProperty(intname, fname);
		_pathtof.setProperty(intname, path);
		if (intname.toUpperCase().contains("SYSTEM"))
			add(intname,"system".toUpperCase());
		else if (intname.toUpperCase().contains("USERDATA"))
			add(intname,"data".toUpperCase());
		else if (intname.toUpperCase().contains("AMSS"))
			add(intname,"baseband".toUpperCase());
		else if (intname.toUpperCase().contains("ADSP") || intname.toUpperCase().startsWith("DSP"))
			add(intname,"baseband".toUpperCase());
		else if (intname.toUpperCase().contains("FOTA"))
			add(intname,"fota".toUpperCase());
		else if (intname.toUpperCase().contains("APPS_LOG"))
			add(intname,"log".toUpperCase());
		else if (intname.toUpperCase().contains("CACHE"))
			add(intname,"cache".toUpperCase());
		else if (intname.toUpperCase().contains("KERNEL"))
			add(intname,"kernel".toUpperCase());
		else if (intname.toUpperCase().startsWith("PARTITION"))
			add(intname,"partition".toUpperCase());
		else if (intname.toUpperCase().startsWith("LOADER"))
			add(intname,"loader".toUpperCase());
		else if (intname.toUpperCase().endsWith(".TA"))
			add(intname,"ta".toUpperCase());
		else
			add(intname,"none".toUpperCase());
	}
	
	public void add(String intname, String categorie) throws Exception {
		Vector<String> list;
		Enumeration elem = _categ.keys();
		while (elem.hasMoreElements()) {
			String lcateg = (String)elem.nextElement();
			if (((Vector<String>)_categ.get(lcateg)).contains(intname))
				if (!lcateg.equals(categorie))
					throw new Exception("A file cannot be affected to more  than one category");
		}
		if (_categ.containsKey(categorie)) {
			list = (Vector<String>)_categ.get(categorie);
		}
		else {
			list = new Vector<String>();
		}
		list.add(intname);
		_categ.put(categorie, list);
		_enabled.put(categorie, "true");
	}
	
	public void setCategEnabled(String categ, boolean enabled) {
		if (enabled)
			_enabled.setProperty(categ, "true");
		else
			_enabled.setProperty(categ, "false");
	}
	public void remove(String name) {
		String categ = getCategorie(name);
		if (categ.length()>0) {
			Vector<String> list = (Vector<String>)_categ.get(categ);
			list.remove(name);
			if (list.size()==0)
				_categ.remove(categ);
		}
	}

	public String toString() {
		String result="";
		Enumeration elem = _categ.keys();
		while (elem.hasMoreElements()) {
			String categ = (String)elem.nextElement(); 
			result = result + categ+"\n";
			Vector<String> list = (Vector<String>)_categ.get(categ);
			Enumeration<String> listelem = list.elements();
			while (listelem.hasMoreElements()) {
				result = result+"   "+listelem.nextElement()+"\n";
			}
		}
		return result;
	}
	
	public String getInternal(String external) {
		return _ftoint.getProperty(external);
	}

	public String getExternal(String internal) {
		return _inttof.getProperty(internal);
	}

	public boolean hasCategorie(String categ) {
		return _categ.containsKey(categ.toUpperCase());
	}

	public String getCategorie(String name) {
		Enumeration elems = _categ.keys();
		while (elems.hasMoreElements()) {
			String categ = (String)elems.nextElement();
			Vector<String> list = (Vector<String>)_categ.get(categ);
			if (list.contains(name)) return categ;
		}
		return "";
	}

	public Enumeration<String> getAllEntries() {
		Vector<String> result = new Vector<String>();
		Enumeration elem = _categ.keys();
		while (elem.hasMoreElements()) {
			String categ = (String)elem.nextElement();
			if (_enabled.getProperty(categ).equals("true")) {
				Vector<String> list = (Vector<String>)_categ.get(categ);
				Enumeration<String> listelem = list.elements();
				while (listelem.hasMoreElements()) {
					result.add(listelem.nextElement());
				}
			}
		}
		return result.elements();
	}

	public Enumeration<String> getEntriesOf(String pcateg) {
		Vector<String> result = new Vector<String>();
		Enumeration elem = _categ.keys();
		while (elem.hasMoreElements()) {
			String categ = (String)elem.nextElement();
			if (categ.toUpperCase().equals(pcateg.toUpperCase())) {
				Vector<String> list = (Vector<String>)_categ.get(categ);
				Enumeration<String> listelem = list.elements();
				while (listelem.hasMoreElements()) {
					result.add(listelem.nextElement());
				}
			}
		}
		return result.elements();
	}

	public Enumeration<String> getEntries() {
		Vector<String> result = new Vector<String>();
		Enumeration elem = _categ.keys();
		while (elem.hasMoreElements()) {
			String categ = (String)elem.nextElement();
			Vector<String> list = (Vector<String>)_categ.get(categ);
			Enumeration<String> listelem = list.elements();
			while (listelem.hasMoreElements()) {
				result.add(listelem.nextElement());
			}
		}
		return result.elements();
	}

	public String getPath(String name) {
		return _pathtof.getProperty(name);
	}

	public void createXML() {
		Element root = new Element("BundleMetaData");
		Document doc = new Document(root);
		XMLOutputter outputter = new XMLOutputter();
	    try {
	      outputter.output(doc, System.out);       
	    }
	    catch (IOException e) {
	      System.err.println(e);
	    }
	}
	
	public boolean isCategEnabled(String categ) {
		if (_enabled.getProperty(categ)==null) return false;
		return _enabled.getProperty(categ).equals("true");
	}
	
	public void clear() {
		_categ.clear();
		_ftoint.clear();
		_inttof.clear();
		_pathtof.clear();
		_enabled.clear();
	}
}