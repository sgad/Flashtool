package org.system;

import gui.About;

import java.net.*;
import java.io.*;

import javax.swing.JFrame;


	
public class VersionChecker extends Thread {

		JFrame _f;
		
		public void setMessageFrame(JFrame f) {
			_f = f;
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
		    		   String version = inputLine.substring(inputLine.indexOf("value")+7);
		    		   version = version.substring(0,version.indexOf("\""));
		    		   if (!About.build.contains(version))
		    			   _f.setTitle(_f.getTitle()+"    --- New version "+version+" available ---");
		    	   }
		       in.close();
		   }
		   catch (Exception e) {
			   System.out.println(e.getMessage());
		   }
		   try {
		   sleep(1000);
		   }
		   catch (Exception e) {}
			}
	   } 

}
