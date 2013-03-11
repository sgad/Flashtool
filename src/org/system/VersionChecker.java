package org.system;

import gui.About;
import java.net.*;
import java.io.*;
import org.eclipse.swt.widgets.Display;


	
public class VersionChecker extends Thread {

		static org.eclipse.swt.widgets.Shell _s = null;
		private boolean notchecked=true;

		public void setMessageFrame(org.eclipse.swt.widgets.Shell s) {
			_s = s;
		}

		public void run() {
			this.setName("Version Checker");
			System.setProperty("java.net.useSystemProxies", "true");
			notchecked = true;
			while (notchecked) {
			try {
		       URL oracle = new URL("https://github.com/Androxyde/Flashtool/raw/master/deploy-release.xml");
		       BufferedReader in = new BufferedReader(
		       new InputStreamReader(oracle.openStream()));
	
		       String inputLine;
		       while ((inputLine = in.readLine()) != null)
		    	   if (inputLine.contains("property") && inputLine.contains("version") && inputLine.contains("value")) {
		    		   notchecked=false;
		    		   String version1 = inputLine.substring(inputLine.indexOf("value")+7);
		    		   final String version = version1.substring(0,version1.indexOf("\""));
		    		   if (!About.build.contains(version))
		    			   if (!About.build.contains("beta"))
		    		   		if (_s!=null) {
		    		   			Display.getDefault().syncExec(
		    		   					new Runnable() {
		    		   						public void run() {
		    		   							_s.setText(_s.getText()+"    --- New version "+version+" available ---");
		    		   						}
		    		   					}
		    		   			);
		    		   		}
		    	   }
		       in.close();
		   	}
		   	catch (Exception e) {
		   	}
		   	try {
				while (notchecked) {
					int count=1;
					while (count<2001 && notchecked) {
						sleep(1);
						count++;
					}
				}
		   	}
		   	catch (Exception e) {}
			}
	   } 

		public void done() {
			notchecked=false;
		}
}