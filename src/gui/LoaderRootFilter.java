package gui;

import java.io.File;
import java.io.FileFilter;

public class LoaderRootFilter implements FileFilter
{

	private String _filter = "";
	
	public LoaderRootFilter(String filter) {
		_filter = filter;
	}
  
	public boolean accept(File file) {
		if (file.getName().toLowerCase().contains(_filter.toLowerCase()) && file.isFile())
			return true;
		return false;
	}

}
