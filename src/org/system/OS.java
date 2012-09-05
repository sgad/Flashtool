package org.system;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import flashsystem.HexDump;

public class OS {

	public static String getName() {
		  String os = "";
		  if (System.getProperty("os.name").toLowerCase().indexOf("windows") > -1) {
		    os = "windows";
		  } else if (System.getProperty("os.name").toLowerCase().indexOf("linux") > -1) {
		    os = "linux";
		  } else if (System.getProperty("os.name").toLowerCase().indexOf("mac") > -1) {
		    os = "mac";
		  }
		  return os;
	}
	
	public static String getAdbPath() {
		String fsep = OS.getFileSeparator();
		if (OS.getName().equals("windows"))
			return new File(System.getProperty("user.dir")+fsep+"x10flasher_lib"+fsep+"adb.exe").getAbsolutePath();
		else
			return new File(System.getProperty("user.dir")+fsep+"x10flasher_lib"+fsep+"adb."+OS.getName()).getAbsolutePath();
	}

	public static String getBin2SinPath() {
		String fsep = OS.getFileSeparator();
		if (OS.getName().equals("windows"))
			return new File(System.getProperty("user.dir")+fsep+"x10flasher_lib"+fsep+"bin2sin.exe").getAbsolutePath();
		else
			return new File(System.getProperty("user.dir")+fsep+"x10flasher_lib"+fsep+"bin2sin").getAbsolutePath();
	}

	public static String getBin2ElfPath() {
		String fsep = OS.getFileSeparator();
		if (OS.getName().equals("windows"))
			return new File(System.getProperty("user.dir")+fsep+"x10flasher_lib"+fsep+"bin2elf.exe").getAbsolutePath();
		else
			return new File(System.getProperty("user.dir")+fsep+"x10flasher_lib"+fsep+"bin2elf").getAbsolutePath();
	}

	public static String get7z() {
		String fsep = OS.getFileSeparator();
		return new File(System.getProperty("user.dir")+fsep+"x10flasher_lib"+fsep+"7z.exe").getAbsolutePath();	
}

	public static String getFastBootPath() {
		String fsep = OS.getFileSeparator();
	   if (OS.getName().equals("windows"))
		   return new File(System.getProperty("user.dir")+fsep+"x10flasher_lib"+fsep+"fastboot.exe").getAbsolutePath();
	   else
		   return new File(System.getProperty("user.dir")+fsep+"x10flasher_lib"+fsep+"fastboot."+OS.getName()).getAbsolutePath();
	}
	
	public static String getWorkDir() {
		return System.getProperty("user.dir");
	}

	public static String getSHA256(byte[] array) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			digest.update(array, 0, array.length);
			byte[] sha256 = digest.digest();
			return HexDump.toHex(sha256);
		}
		catch(NoSuchAlgorithmException nsa) {
			throw new RuntimeException("Unable to process file for SHA-256", nsa);
		}
	}

	public static String getSHA256(File f) {
		byte[] buffer = new byte[8192];
		int read = 0;
		InputStream is=null;
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			is = new FileInputStream(f);
			while( (read = is.read(buffer)) > 0) {
				digest.update(buffer, 0, read);
			}		
			byte[] sha256 = digest.digest();
			BigInteger bigInt = new BigInteger(1, sha256);
			String output = bigInt.toString(32);
			return output.toUpperCase();
		}
		catch(IOException e) {
			throw new RuntimeException("Unable to process file for SHA-256", e);
		}
		catch(NoSuchAlgorithmException nsa) {
			throw new RuntimeException("Unable to process file for SHA-256", nsa);
		}
		finally {
			try {
				is.close();
			}
			catch(IOException e) {
				throw new RuntimeException("Unable to close input stream for SHA-256 calculation", e);
			}
		}
	}
	
	public static void copyfile(String srFile, String dtFile){
		  try{
		  File f1 = new File(srFile);
		  File f2 = new File(dtFile);
		  if (!f1.getAbsolutePath().equals(f2.getAbsolutePath())) {
			  InputStream in = new FileInputStream(f1);
			  
			  //For Append the file.
			//  OutputStream out = new FileOutputStream(f2,true);
	
			  //For Overwrite the file.
			  OutputStream out = new FileOutputStream(f2);
	
			  byte[] buf = new byte[1024];
			  int len;
			  while ((len = in.read(buf)) > 0){
			  out.write(buf, 0, len);
			  }
			  in.close();
			  out.close();
		  }
		  }
		  catch(FileNotFoundException ex){
			  System.out.println(ex.getMessage() + " in the specified directory.");
		  }
		  catch(IOException e){
			  System.out.println(e.getMessage());  
		  }
	}
	
	public static String getMD5(File f) {
		byte[] buffer = new byte[8192];
		int read = 0;
		InputStream is=null;
		try {
			MessageDigest digest = MessageDigest.getInstance("MD5");
			is = new FileInputStream(f);
			while( (read = is.read(buffer)) > 0) {
				digest.update(buffer, 0, read);
			}		
			byte[] md5sum = digest.digest();
			BigInteger bigInt = new BigInteger(1, md5sum);
			String output = bigInt.toString(16);
			return output.toUpperCase();
		}
		catch(IOException e) {
			throw new RuntimeException("Unable to process file for MD5", e);
		}
		catch(NoSuchAlgorithmException nsa) {
			throw new RuntimeException("Unable to process file for MD5", nsa);
		}
		finally {
			try {
				is.close();
			}
			catch(IOException e) {
				throw new RuntimeException("Unable to close input stream for MD5 calculation", e);
			}
		}
	}
	
	public static String getVersion() {
		return System.getProperty("os.version");
	}

	public static String getFileSeparator() {
		return System.getProperty("file.separator");
	}
	
	public static String getWinDir() {
		if (System.getenv("WINDIR")==null) return System.getenv("SYSTEMROOT");
		if (System.getenv("WINDIR").length()==0) return System.getenv("SYSTEMROOT");
		return System.getenv("WINDIR");
	}
	
	public static String getSystem32Dir() {
		return getWinDir()+getFileSeparator()+"System32";
	}

	public static Collection<File> listFileTree(File dir) {
	    Set<File> fileTree = new HashSet<File>();
	    for (File entry : dir.listFiles()) {
	        if (entry.isFile()) fileTree.add(entry);
	        else {
	        	fileTree.addAll(listFileTree(entry));
	        	fileTree.add(entry);
	        }
	    }
	    return fileTree;
	}

}
