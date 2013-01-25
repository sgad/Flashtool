package org.system;

import gui.About;

import java.net.*;
import java.io.*;

import javax.swing.JFrame;

import org.eclipse.swt.widgets.Display;


	
public class VersionChecker extends Thread {

		JFrame _f = null;
		static org.eclipse.swt.widgets.Shell _s = null;
		
		public void setMessageFrame(JFrame f) {
			_f = f;
		}

		public void setMessageFrame(org.eclipse.swt.widgets.Shell s) {
			_s = s;
		}

		public void run() {
			System.setProperty("java.net.useSystemProxies", "true");
			boolean notchecked = true;
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
		    			   	if (_f!=null)
		    				   _f.setTitle(_f.getTitle()+"    --- New version "+version+" available ---");
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
		   		sleep(2000);
		   	}
		   	catch (Exception e) {}
			}
	   } 

}