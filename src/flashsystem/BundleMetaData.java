package flashsystem;

import java.util.Enumeration;
import java.util.Properties;
import java.util.Vector;

public class BundleMetaData {

	Properties _categ = new Properties();
	Properties _ftoint = new Properties();
	Properties _inttof = new Properties();
	Properties _pathtof = new Properties();
	
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
		else if (intname.toUpperCase().contains("ADSP"))
			add(intname,"baseband".toUpperCase());
		else if (intname.toUpperCase().contains("FOTA"))
			add(intname,"baseband".toUpperCase());
		else if (intname.toUpperCase().contains("APPS_LOG"))
			add(intname,"baseband".toUpperCase());
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

}