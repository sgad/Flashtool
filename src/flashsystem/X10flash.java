package flashsystem;

import flashsystem.HexDump;
import flashsystem.io.USBFlash;
import gui.LoaderRootFilter;
import gui.LoaderSelectorGUI;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.IOException;
import org.logger.MyLogger;
import org.system.Device;
import org.system.DeviceChangedListener;
import org.system.OS;
import org.system.TextFile;

import java.util.Enumeration;
import java.util.Vector;

public class X10flash {

    private Bundle _bundle;
    private Command cmd;
    private LoaderInfo phoneprops = null;
    byte readarray65[] = new byte[0x10000];
    byte readarray4[] = new byte[0x1000];

    public X10flash(Bundle bundle) {
    	_bundle=bundle;
    }

    public void setFlashState(boolean ongoing) throws IOException,X10FlashException
    {
	    	if (ongoing) {
	    		//cmd.send(Command.CMD13,Command.TA_FLASH_STARTUP_SHUTDOWN_RESULT_ONGOING,false);
	    		cmd.send(Command.CMD13, Command.TA_EDREAM_FLASH_STARTUP_SHUTDOWN_RESULT_ONGOING,false);    		
	    	}
	    	else {
	    		cmd.send(Command.CMD13, Command.TA_EDREAM_FLASH_STARTUP_SHUTDOWN_RESULT_FINISHED,false);
	    		cmd.send(Command.CMD13, Command.TA_FLASH_STARTUP_SHUTDOWN_RESULT_FINISHED,false);
	    	}
    }

    private void sendTA(InputStream in,String name) throws FileNotFoundException, IOException,X10FlashException {
    	try {
    		TaFile ta = new TaFile(in);
    		MyLogger.getLogger().info("Flashing "+name);
			Vector<TaEntry> entries = ta.entries();
			for (int i=0;i<entries.size();i++) {
				sendTAUnit(entries.get(i));
			}
    	}
    	catch (TaParseException tae) {
    		MyLogger.getLogger().error("Error parsing TA file. Skipping");
    	}
    }

    public void sendTAUnit(TaEntry ta) throws X10FlashException, IOException {
		MyLogger.getLogger().info("Writing TA unit : "+HexDump.toHex(ta.getWordbyte()));
		if (!_bundle.simulate()) {
			cmd.send(Command.CMD13, ta.getWordbyte(),false);  
		} 	
    }

    public String dumpProperty(int prnumber, String format) throws IOException, X10FlashException
    {
    		MyLogger.getLogger().info("Start Reading property");
	        MyLogger.getLogger().debug((new StringBuilder("%%% read TA property id=")).append(prnumber).toString());
	        cmd.send(Command.CMD12, BytesUtil.getBytesWord(prnumber, 4),false);
	        MyLogger.updateProgress();
	        String reply = "";
	        if (format.equals("hex"))
	        	reply = cmd.getLastReplyHex();
	        if (format.equals("string"))
	        	reply = cmd.getLastReplyString();
	        reply = reply.replace("[", "");
	        reply = reply.replace("]", "");
	        reply = reply.replace(",", "");
			MyLogger.getLogger().info("Reading TA finished.");
			return reply;
    }

    public Vector<TaEntry> dumpProperties()
    {
    	Vector<TaEntry> v = new Vector();
    	try {
		    MyLogger.getLogger().info("Start Dumping TA");
		    MyLogger.initProgress(9789);
	        for(int i = 0; i < 4920; i++) {
	        	try {
	        		cmd.send(Command.CMD12, BytesUtil.getBytesWord(i, 4),false);
		        	String reply = cmd.getLastReplyHex();
		        	reply = reply.replace("[", "");
		        	reply = reply.replace("]", "");
		        	reply = reply.replace(",", "");
		        	if (cmd.getLastReplyLength()>0) {
		        		TaEntry ta = new TaEntry();
		        		ta.setPartition(HexDump.toHex(i));
		        		ta.addData(reply.trim());
		        		v.add(ta);
		        	}

	        	}
	        	catch (X10FlashException e) {
	        	}
	        }
	        MyLogger.initProgress(0);
	        MyLogger.getLogger().info("Dumping TA finished.");
	    }
    	catch (Exception ioe) {
    		MyLogger.initProgress(0);
    		MyLogger.getLogger().error(ioe.getMessage());
    		MyLogger.getLogger().error("Error dumping TA. Aborted");
    		closeDevice();
    	}
    	return v;
    }

    public void BackupTA() throws IOException, X10FlashException {
    	openTA(2);
    	TextFile tazone = new TextFile(OS.getWorkDir()+"/custom/ta/"+ getPhoneProperty("MSN") + "-" + org.logger.TextAreaAppender.timestamp +  ".ta","ISO8859-1");
        tazone.open(false);
    	try {
		    MyLogger.getLogger().info("Start Dumping TA");
		    MyLogger.initProgress(9789);
	        for(int i = 0; i < 4920; i++) {
	        	try {
		        	MyLogger.getLogger().debug((new StringBuilder("%%% read TA property id=")).append(i).toString());
		        	cmd.send(Command.CMD12, BytesUtil.getBytesWord(i, 4),false);
		        	String reply = cmd.getLastReplyHex();
		        	reply = reply.replace("[", "");
		        	reply = reply.replace("]", "");
		        	reply = reply.replace(",", "");
		        	if (cmd.getLastReplyLength()>0) {
		        		tazone.writeln(HexDump.toHex(i) + " " + HexDump.toHex(cmd.getLastReplyLength()) + " " + reply.trim());
		        	}
	        	}
	        	catch (X10FlashException e) {
	        	}
	        }
	        MyLogger.initProgress(0);
	        tazone.close();
	        closeTA();
	    }
    	catch (Exception ioe) {
	        tazone.close();
	        closeTA();
    		MyLogger.initProgress(0);
    		MyLogger.getLogger().error(ioe.getMessage());
    		MyLogger.getLogger().error("Error dumping TA. Aborted");
    	}
    }
    
    public void RestoreTA(String tafile) throws FileNotFoundException, IOException, X10FlashException {
    	openTA(2);
    	sendTA(new FileInputStream(tafile),"preset");
		closeTA();
		MyLogger.initProgress(0);	    
    }
    
    private void processHeader(InputStream fileinputstream) throws X10FlashException {
    	try {
			byte abyte0[] = new byte[6];
			int j = fileinputstream.read(abyte0);
			if(j != 6) {
				fileinputstream.close();
				throw new X10FlashException("Error in processHeader");
			}
			int k;
			byte abyte1[] = new byte[4];
			System.arraycopy(abyte0, 2, abyte1, 0, 4);
			k = BytesUtil.getInt(abyte1);
			abyte1 = new byte[k - 6];
			k = fileinputstream.read(abyte1);
			if(k != abyte1.length) {
				fileinputstream.close();
				throw new X10FlashException("Error in processHeader");
			}
            cmd.send(Command.CMD05,BytesUtil.concatAll(abyte0, abyte1),false);
            if (USBFlash.getLastFlags() == 0)
            	getLastError();
    	}
    	catch (IOException ioe) {
    		throw new X10FlashException("Error in processHeader : "+ioe.getMessage());
    	}
    }
 
    public void getLastError() throws IOException, X10FlashException {
            cmd.send(Command.CMD07,Command.VALNULL,false);    	
    }
    
    private void uploadImage(InputStream fileinputstream, int buffer) throws X10FlashException {
    	try {
	    	processHeader(fileinputstream);
			int readCount;
			do {
				if (buffer==0x1000)
					readCount = fileinputstream.read(readarray4);
				else
					readCount = fileinputstream.read(readarray65);
				if (readCount > 0)
					if (buffer==0x1000)
						cmd.send(Command.CMD06, BytesUtil.getReply(readarray4, readCount), (readCount==buffer));
					else
						cmd.send(Command.CMD06, BytesUtil.getReply(readarray65, readCount), (readCount==buffer));
				if (readCount!=buffer) break;
			} while(true);
			fileinputstream.close();
			if (USBFlash.getLastFlags() == 0)
				getLastError();
    	}
    	catch (Exception e) {
    		e.printStackTrace();
    		try {fileinputstream.close();}catch(Exception cl) {}
    		throw new X10FlashException (e.getMessage());
    	}
    }

    public void sendLoader() throws FileNotFoundException, IOException, X10FlashException {
		MyLogger.getLogger().info("Flashing loader");
		String LoaderHandler;
		if (_bundle.hasLoader())
			uploadImage(_bundle.getLoader().getInputStream(), 0x1000);
		else {
			File dir = new File(OS.getWorkDir()+"/loaders");
			LoaderHandler = phoneprops.getProperty("LOADER_ROOT");
			File[] filelist = dir.listFiles(new LoaderRootFilter(LoaderHandler));
			if (filelist.length>1) {
				LoaderSelectorGUI sel = new LoaderSelectorGUI(filelist);
				String loader = sel.getVersion();
				if (loader.length()>0) {
					File f = new File(OS.getWorkDir()+"/loaders/"+loader);
					FileInputStream fin = new FileInputStream(f);
					uploadImage(fin, 0x1000);
				}
			}
			else {
				FileInputStream fin = new FileInputStream(filelist[0]);
				uploadImage(fin, 0x1000);
			}
		}
		USBFlash.readS1Reply();
		hookDevice(true);
    }

    public void sendLoader(File loader) throws FileNotFoundException, IOException, X10FlashException {
		MyLogger.getLogger().info("Flashing loader");
		FileInputStream fin = new FileInputStream(loader);
		uploadImage(fin, 0x1000);
		USBFlash.readS1Reply();
		hookDevice(true);
    }

    public void sendPartition() throws FileNotFoundException, IOException, X10FlashException {		
		if (_bundle.hasPartition()) {
			BundleEntry entry = _bundle.getPartition();
			MyLogger.getLogger().info("Flashing "+entry.getName());
			closeTA();
			uploadImage(entry.getInputStream(),0x10000);
			openTA(2);
		}
    }
    
    public void sendImages() throws FileNotFoundException, IOException,X10FlashException {
		for (int i = 1;i<=_bundle.getMeta().getNbCategs();i++) {
			String categ = _bundle.getMeta().getCagorie(i);
			if (_bundle.getMeta().isCategEnabled(categ)) {
				Enumeration entries = _bundle.getMeta().getEntriesOf(categ);
				while (entries.hasMoreElements()) {
					String entry = (String)entries.nextElement();
					BundleEntry bent = _bundle.getEntry(entry);
					MyLogger.getLogger().info("Flashing "+bent.getName());
					uploadImage(bent.getInputStream(),0x10000);
					MyLogger.getLogger().debug("Flashing "+bent.getName()+" finished");
				}
			}
		}
    }

    public long getNumberPasses() {
	    Enumeration<BundleEntry> e = _bundle.allEntries();
	    long totalsize = 0;
	    while (e.hasMoreElements()) {
	    	BundleEntry entry = e.nextElement();
	    	if (entry.getName().contains("loader"))
	    		totalsize = totalsize + entry.getSize()/0x1000+1;
	    	else 
	    		totalsize = totalsize + (entry.getSize()/0x10000)*2+3;
	    }
	    return totalsize+13;
    }

    public String getPhoneProperty(String property) {
    	return phoneprops.getProperty(property);
    }

    public void openTA(int partition) throws X10FlashException, IOException{
    	cmd.send(Command.CMD09, BytesUtil.getBytesWord(partition, 1), false);
    }
    
    public void closeTA() throws X10FlashException, IOException{
    	cmd.send(Command.CMD10, Command.VALNULL, false);
    }
   
    public void sendTAFiles() throws FileNotFoundException, IOException,X10FlashException {
		Enumeration entries = _bundle.getMeta().getEntriesOf("TA");
		while (entries.hasMoreElements()) {
			String entry = (String)entries.nextElement();
			BundleEntry bent = _bundle.getEntry(entry);
			if (!bent.getName().toUpperCase().contains("SIM"))
				sendTA(bent.getInputStream(),bent.getName());
			else {
				MyLogger.getLogger().warn("This file is ignored : "+bent.getName());
			}
			MyLogger.getLogger().debug("Flashing "+bent.getName()+" finished");
		}
    }
    
    public void flashDevice() {
    	try {
		    MyLogger.getLogger().info("Start Flashing");
		    MyLogger.initProgress(getNumberPasses());
		    sendLoader();
		    if (_bundle.hasCmd25()) {
		    	MyLogger.getLogger().info("Disabling final data verification check");
		    	cmd.send((byte)0x19, new byte[] {0x00, 0x01, 0x00, 0x00, 0x00, 0x01}, false);
		    }
		    openTA(2);
		    setFlashState(true);
		    sendPartition();
		    sendTAFiles();
			sendImages();
        	setFlashState(false);
        	closeTA();
        	closeDevice(0x01);
			MyLogger.getLogger().info("Flashing finished.");
			MyLogger.getLogger().info("Please wait. Phone will reboot");
			MyLogger.getLogger().info("For flashtool, Unknown Sources and Debugging must be checked in phone settings");
			MyLogger.initProgress(0);
    	}
    	catch (Exception ioe) {
    		ioe.printStackTrace();
    		closeDevice();
    		MyLogger.getLogger().error(ioe.getMessage());
    		MyLogger.getLogger().error("Error flashing. Aborted");
    		MyLogger.initProgress(0);
    	}
    }

    public boolean openDevice() {
    	return openDevice(_bundle.simulate());
    }

    public boolean deviceFound() {
    	boolean found = false;
    	try {
			Thread.sleep(500);
			found = Device.getLastConnected(false).getPid().equals("ADDE");
		}
		catch (Exception e) {
	    	found = false;
		}
    	return found;
    }

    public void endSession() throws X10FlashException,IOException {
    	MyLogger.getLogger().info("Ending flash session");
    	cmd.send(Command.CMD04,Command.VALNULL,false);
    }

    public void endSession(int param) throws X10FlashException,IOException {
    	MyLogger.getLogger().info("Ending flash session");
    	cmd.send(Command.CMD04,BytesUtil.getBytesWord(param, 1),false);
    }

    public void closeDevice() {
    	try {
    		endSession();
    	}
    	catch (Exception e) {}
    	USBFlash.close();
    	DeviceChangedListener.pause(false);
    }

    public void closeDevice(int par) {
    	try {
    		endSession(par);
    	}
    	catch (Exception e) {}
    	USBFlash.close();
    	DeviceChangedListener.pause(false);
    }

    public void hookDevice(boolean printProps) throws X10FlashException,IOException {
		cmd.send(Command.CMD01, Command.VALNULL, false);
		phoneprops.update(cmd.getLastReplyString());
		if (getPhoneProperty("ROOTING_STATUS")==null) phoneprops.setProperty("ROOTING_STATUS", "UNROOTABLE"); 
		if (phoneprops.getProperty("VER").startsWith("r"))
			phoneprops.setProperty("ROOTING_STATUS", "ROOTED");
		if (printProps)
			MyLogger.getLogger().info("Loader : "+phoneprops.getProperty("LOADER_ROOT")+" - Version : "+phoneprops.getProperty("VER")+" / Bootloader status : "+phoneprops.getProperty("ROOTING_STATUS"));
    }
    
    public boolean openDevice(boolean simulate) {
    	if (simulate) return true;
    	boolean found=false;
    	try {
    		USBFlash.open("ADDE");
			MyLogger.getLogger().info("Reading device information");
			USBFlash.readS1Reply();
			phoneprops = new LoaderInfo(new String (USBFlash.getLastReply()));
			MyLogger.getLogger().info("Phone ready for flashmode operations.");
    	    cmd = new Command(_bundle.simulate());
    	    hookDevice(false);
    		found = true;
    	}
    	catch (Exception e){
    		e.printStackTrace();
    		found=false;
    	}
    	return found;
    }

}