package flashsystem;

import java.util.Enumeration;
import java.util.Properties;

public class LoaderInfo {

	Properties p = new Properties();
	
	public LoaderInfo(String phoneloader) {
		String[] result = phoneloader.split(";");
		p.clear();
		for (int i=0;i<result.length;i++) {
			try {
				p.put(result[i].split("=")[0], result[i].split("=")[1].replaceAll("\"", ""));
			}
			catch (Exception e) {}
		}
	}
	
	public Enumeration<Object> getKeys() {
		return p.keys();
	}
	
	public String getProperty(String key) {
		return p.getProperty(key);
	}
}
