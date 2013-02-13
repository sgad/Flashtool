package org.system;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.math.BigInteger;
import java.security.Key;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.crypto.Cipher;

import org.logger.MyLogger;

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
	
	public static void unyaffs(String yaffsfile, String folder) {
		try {
			File f = new File(folder);
			if (!f.exists()) f.mkdir();
			else if (f.isFile()) throw new IOException("destination must be a folder");
			ProcessBuilderWrapper command = new ProcessBuilderWrapper(new String[] {getWorkDir()+File.separator+"x10flasher_lib"+File.separator+"unyaffs."+getName(),yaffsfile,folder},false);
		}
		catch (Exception e) {
			MyLogger.getLogger().warn("Failed : "+e.getMessage());
		}
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

	public static String getSHA1(byte[] array) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-1");
			digest.update(array, 0, array.length);
			byte[] sha1 = digest.digest();
			return HexDump.toHex(sha1);
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
			  MyLogger.getLogger().error(ex.getMessage() + " in the specified directory.");
		  }
		  catch(IOException e){
			  MyLogger.getLogger().error(e.getMessage());  
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

	public static void decrypt(File in) throws Exception {
		KeyFactory keyFactory = KeyFactory.getInstance("RSA");
		FileInputStream keyfis = new FileInputStream(OS.getWorkDir()+File.separator+"custom"+File.separator+"root"+File.separator+"keys"+File.separator+"privkey");
		byte[] encKey = new byte[keyfis.available()];
		keyfis.read(encKey);
		keyfis.close();
		PKCS8EncodedKeySpec privKeySpec = new PKCS8EncodedKeySpec(encKey);
		PrivateKey privKey = keyFactory.generatePrivate(privKeySpec);
		encryptDecryptFile(in.getAbsolutePath(),in.getAbsolutePath()+".dec",privKey, Cipher.DECRYPT_MODE);
	}

	public static void encrypt(File in) throws Exception {
		KeyFactory keyFactory = KeyFactory.getInstance("RSA");
		FileInputStream keyfis = new FileInputStream(OS.getWorkDir()+File.separator+"custom"+File.separator+"root"+File.separator+"keys"+File.separator+"pubkey");
		byte[] decKey = new byte[keyfis.available()];
		keyfis.read(decKey);
		keyfis.close();
		X509EncodedKeySpec pubKeySpec = new X509EncodedKeySpec(decKey);
		PublicKey pubKey = keyFactory.generatePublic(pubKeySpec);
		encryptDecryptFile(in.getAbsolutePath(),in.getAbsolutePath()+".enc",pubKey, Cipher.ENCRYPT_MODE);
	}

	public static void encryptDecryptFile(String srcFileName, String destFileName, Key key, int cipherMode) throws Exception
	{
		if (srcFileName.endsWith(".enc")) {
			destFileName=srcFileName.substring(0, srcFileName.lastIndexOf(".enc"));
		}
		OutputStream outputWriter = null;
		InputStream inputReader = null;
		if (cipherMode == Cipher.ENCRYPT_MODE)
			MyLogger.getLogger().info("Encrypting "+srcFileName+" to "+destFileName);
		else
			MyLogger.getLogger().info("Decrypting "+srcFileName+" to "+destFileName);
		try
		{
			Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
			String textLine = null;
			
			byte[] buf = cipherMode == Cipher.ENCRYPT_MODE? new byte[100] : new byte[128];
			int bufl;
			// init the Cipher object for Encryption...
			cipher.init(cipherMode, key);
			
			// start FileIO
			outputWriter = new FileOutputStream(destFileName);
			inputReader = new FileInputStream(srcFileName);
			while ( (bufl = inputReader.read(buf)) != -1)
			{
				byte[] encText = null;
				if (cipherMode == Cipher.ENCRYPT_MODE)
					encText = encrypt(copyBytes(buf,bufl),(PublicKey)key);
				else
					encText = decrypt(copyBytes(buf,bufl),(PrivateKey)key);
				outputWriter.write(encText);
			}
			outputWriter.flush();
		}
		catch (Exception e)
		{
			throw e;
		}
		finally
		{
			try
			{
				if (outputWriter != null)
					outputWriter.close();
				if (inputReader != null)
					inputReader.close();
			}
			catch (Exception e)
			{} 
		}
	}
	
	public static byte[] copyBytes(byte[] arr, int length)
	{
		byte[] newArr = null;
		if (arr.length == length)
			newArr = arr;
		else
		{
			newArr = new byte[length];
			for (int i = 0; i < length; i++)
			{
				newArr[i] = (byte) arr[i];
			}
		}
		return newArr;
	}
	
	public static byte[] encrypt(byte[] text, PublicKey key) throws Exception
	{
		byte[] cipherText = null;
		try
		{
			// get an RSA cipher object and print the provider
			Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
			
			// encrypt the plaintext using the public key
			cipher.init(Cipher.ENCRYPT_MODE, key );
			cipherText = cipher.doFinal(text);
		}
		catch (Exception e)
		{
			throw e;
		}
		return cipherText;
	}
	
	public static byte[] decrypt(byte[] text, PrivateKey key) throws Exception
	{
		byte[] dectyptedText = null;
		try
		{
			// decrypt the text using the private key
			Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
			cipher.init(Cipher.DECRYPT_MODE, key);
			try {
				dectyptedText = cipher.doFinal(text);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		catch (Exception e)
		{
			throw e;
		}
		return dectyptedText;
	}

	public static RandomAccessFile generateEmptyFile(String fname, long size, byte fill) {
		// To fill the empty file with FF values		
		try {
			byte[] empty = new byte[65*1024];
			for (int i=0; i<empty.length;i++)
				empty[i] = fill;		
			// Creation of empty file
			File f = new File(fname);
			f.delete();
			FileOutputStream fout = new FileOutputStream(f);
			for (long i = 0; i<size/empty.length; i++) {
				fout.write(empty);
			}
			for (long i = 0; i<size%empty.length; i++) {
				fout.write(fill);
			}
			fout.flush();
			fout.close();
			RandomAccessFile fo = new RandomAccessFile(f,"rw");
			return fo;
		}
		catch (Exception e) {
			e.printStackTrace();
			MyLogger.getLogger().error(e.getMessage());
			return null;
		}
	}

}
