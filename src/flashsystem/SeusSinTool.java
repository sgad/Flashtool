package flashsystem;

import java.io.*;

import org.system.OS;
import org.system.ProcessBuilderWrapper;

public class SeusSinTool {

	public static void decrypt(String sinfile) {
		byte[] buf = new byte[1024];
		try {
			String folder = new File((new File(sinfile)).getParent()).getAbsolutePath();
			FileInputStream f = new FileInputStream(sinfile);
			com.sonyericsson.cs.ma3.common.security.a in = new com.sonyericsson.cs.ma3.common.security.a(f);
	        String basefile = sinfile+"_dek";
	        OutputStream out = new FileOutputStream(basefile+".zip.gz");
	        int len;
	        while((len = in.read(buf)) >= 0) {
	          if (len > 0)
	            out.write(buf, 0, len);
	        }
	        out.flush();
	        out.close();
	        in.close();
	        try {
		        File fxml = new File(folder+"\\update.xml");
		        if (fxml.isFile()) fxml.renameTo(new File(folder+"\\update1.xml"));
		        if (OS.getName().equals("windows")) {
		        	ProcessBuilderWrapper run = new ProcessBuilderWrapper(new String[] {OS.get7z(),"e", "-y", basefile+".zip.gz", "-o"+folder},false);
		        	ProcessBuilderWrapper run1 = new ProcessBuilderWrapper(new String[] {OS.get7z(), "e", "-y", basefile+".zip", "-o"+folder},false);
		        }
		        else {
		        	ProcessBuilderWrapper run = new ProcessBuilderWrapper(new String[] {"gunzip", basefile+".zip.gz"},false);
		        	ProcessBuilderWrapper run1 = new ProcessBuilderWrapper(new String[] {"unzip", "-o", basefile+".zip","-d",folder},false);
		        }
		        File fdek = new File(basefile+".zip.gz");
		        fdek.delete();
		        File ftar = new File(basefile+".zip");
		        ftar.delete();
	        }
	        catch (Exception e) {}
	      } catch(IOException e) {
	      }
	}

	  public static void encrypt(String tgzfile) {
		  byte[] buf = new byte[1024];
	      try {
	    	  String outname = tgzfile.replaceAll(".tgz", ".sin");
	    	  FileInputStream in = new FileInputStream(tgzfile);
	    	  com.sonyericsson.cs.ma3.common.security.b out = new com.sonyericsson.cs.ma3.common.security.b(new FileOutputStream(outname));
	    	  int len;
	    	  while((len = in.read(buf)) >= 0) {
	    		  if (len > 0)
	    			  out.write(buf, 0, len);
	    	  }
	    	  out.flush();
	    	  out.close();
	    	  in.close();
	      } catch(IOException e) {
	        e.printStackTrace();
	      }
	  }

}