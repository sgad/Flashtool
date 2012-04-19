package flashsystem;

import java.util.Enumeration;
import java.util.Properties;

public class LoaderInfo extends Properties {
	
	public LoaderInfo(String phoneloader) {
		update(phoneloader);
	}
	
	public void update(String ident) {
		String[] result = ident.split(";");
		for (int i=0;i<result.length;i++) {
			try {
				setProperty(result[i].split("=")[0], result[i].split("=")[1].replaceAll("\"", ""));
			}
			catch (Exception e) {}
		}
		if (getProperty("LOADER_ROOT")==null) setProperty("LOADER_ROOT",getProperty("S1_ROOT"));
	}
}
