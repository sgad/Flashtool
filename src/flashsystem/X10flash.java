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
	    		MyLogger.getLogger().info("Opening TA for writting");
	    		cmd.send(Command.CMD13,Command.TA_FLASH_STARTUP_SHUTDOWN_RESULT_ONGOING,false);
	    		cmd.send(Command.CMD13, Command.TA_EDREAM_FLASH_STARTUP_SHUTDOWN_RESULT_ONGOING,false);    		
	    	}
	    	else {
	    		MyLogger.getLogger().info("Closing TA");
	    		cmd.send(Command.CMD13, Command.TA_FLASH_STARTUP_SHUTDOWN_RESULT_FINISHED,false);
	        	cmd.send(Command.CMD13, Command.TA_EDREAM_FLASH_STARTUP_SHUTDOWN_RESULT_FINISHED,false);
	    	}
    }

    private void sendTA(InputStream in,String name) throws FileNotFoundException, IOException,X10FlashException {
    	try {
    		TaFile ta = new TaFile(in);
    		MyLogger.getLogger().info("Flashing "+name);
			Vector<TaEntry> entries = ta.entries();
			for (int i=0;i<entries.size();i++) {
				MyLogger.getLogger().info("Writting TA value : "+HexDump.toHex(entries.get(i).getWordbyte()));
				if (!_bundle.simulate()) {
					cmd.send(Command.CMD13, entries.get(i).getWordbyte(),false);  
				}
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

    public String dumpPropertyHex(int prnumber) throws IOException, X10FlashException
    {
    		MyLogger.getLogger().info("Start Reading property");
	        MyLogger.getLogger().debug((new StringBuilder("%%% read TA property id=")).append(prnumber).toString());
	        cmd.send(Command.CMD12, BytesUtil.getBytesWord(prnumber, 4),false);
	        MyLogger.updateProgress();
	        String reply = cmd.getLastReplyHex();
	        reply = reply.replace("[", "");
	        reply = reply.replace("]", "");
	        reply = reply.replace(",", "");
			MyLogger.getLogger().info("Reading TA finished.");
			return reply;
    }

    public String dumpPropertyString(int prnumber) throws IOException, X10FlashException
    {
    		MyLogger.getLogger().info("Start Reading TA");
	        MyLogger.getLogger().debug((new StringBuilder("%%% read TA property id=")).append(prnumber).toString());
	        cmd.send(Command.CMD12, BytesUtil.getBytesWord(prnumber, 4),false);
	        MyLogger.updateProgress();
	        String reply = cmd.getLastReplyString();
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
    		DeviceChangedListener.pause(false);
    		closeDevice();
    	}
    	return v;
    }

    public void BackupTA() throws IOException, X10FlashException
    {
    	TextFile tazone = new TextFile(OS.getWorkDir()+"/custom/ta/"+ getPhoneProperty("MSN") + ".ta","ISO8859-1");
        tazone.open(false);
    	try {
		    MyLogger.getLogger().info("Start Dumping TA");
		    MyLogger.initProgress(9789);
	        for(int i = 0; i < 4920; i++) {
	        	try {
	        	MyLogger.getLogger().debug((new StringBuilder("%%% read TA property id=")).append(i).toString());
	        	cmd.send(Command.CMD12, BytesUtil.getBytesWord(i, 4),false);
	        	String reply = cmd.getLastReplyHex();
	        	String replyS = cmd.getLastReplyString();
	        	reply = reply.replace("[", "");
	        	reply = reply.replace("]", "");
	        	reply = reply.replace(",", "");
	        	if (cmd.getLastReplyLength()>0) {
	        		tazone.writeln(HexDump.toHex(i) + " " + HexDump.toHex(cmd.getLastReplyLength()) + " " + reply.trim());
	        	}
	        } catch (X10FlashException e) {
        	}
	        }
	        
	        MyLogger.initProgress(0);
	        tazone.close();
			MyLogger.getLogger().info("Dumping TA finished.");
			DeviceChangedListener.pause(false);
			closeDevice();
	    }
    	catch (Exception ioe) {
	        tazone.close();
    		MyLogger.initProgress(0);
    		MyLogger.getLogger().error(ioe.getMessage());
    		MyLogger.getLogger().error("Error dumping TA. Aborted");
    		DeviceChangedListener.pause(false);
    		closeDevice();
    	}
    }    
    
    public void RestoreTA() throws FileNotFoundException, IOException, X10FlashException {
    	sendTA(new FileInputStream(OS.getWorkDir()+"/custom/ta/"+ getPhoneProperty("MSN") + ".ta"),"preset");
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
			if (phoneprops.getProperty("LOADER_ROOT") != null ) {
					LoaderHandler = phoneprops.getProperty("LOADER_ROOT");
				}
			else {
					LoaderHandler = phoneprops.getProperty("S1_ROOT");
				}
			String[] LoaderHandler2 = LoaderHandler.split("\\,");
			String LoaderSub = null;
			for (int x=0; x<LoaderHandler2.length; x++) {
				File tempf = new File(OS.getWorkDir()+"/loaders/"+LoaderHandler2[x]+".sin");
				if (tempf.exists()) {LoaderSub = LoaderHandler2[x]; }
			}
			File[] filelist = dir.listFiles(new LoaderRootFilter(LoaderSub));
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
    }

    public void sendSystemAndUserData() throws FileNotFoundException,IOException, X10FlashException {
        Enumeration<BundleEntry> e = _bundle.systemdataEntries();
    	while (e.hasMoreElements()) {
    		BundleEntry entry = e.nextElement();
    		MyLogger.getLogger().info("Flashing "+entry.getName());
    		uploadImage(entry.getInputStream(),0x10000);
    	}    	
    }
    
    public void sendImages() throws FileNotFoundException, IOException,X10FlashException {            
    		Enumeration<BundleEntry> e = _bundle.entries();
        	while (e.hasMoreElements()) {
        		BundleEntry entry = e.nextElement();
        		MyLogger.getLogger().info("Flashing "+entry.getName());
        		uploadImage(entry.getInputStream(),0x10000);
        		MyLogger.getLogger().debug("Flashing "+entry.getName()+" finished");
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

    public void init() throws X10FlashException,FileNotFoundException, IOException {
		cmd.send(Command.CMD09, Command.VAL2, false);
        cmd.send(Command.CMD10, Command.VALNULL, false);
        sendLoader();		
		cmd.send(Command.CMD01, Command.VALNULL, false);
		phoneprops.update(cmd.getLastReplyString());
		if (getPhoneProperty("ROOTING_STATUS")==null) phoneprops.setProperty("ROOTING_STATUS", "UNROOTABLE"); 
		if (phoneprops.getProperty("VER").startsWith("r"))
			phoneprops.setProperty("ROOTING_STATUS", "ROOTED");
		MyLogger.getLogger().info("Loader : "+phoneprops.getProperty("LOADER_ROOT")+" - Version : "+phoneprops.getProperty("VER")+" / Bootloader status : "+phoneprops.getProperty("ROOTING_STATUS"));
        cmd.send(Command.CMD09, Command.VAL2,false);    	
    }
  
    public void flashDevice() {
    	try {
		    MyLogger.getLogger().info("Start Flashing");
		    MyLogger.initProgress(getNumberPasses());

		    init();

			sendImages();
        	sendSystemAndUserData();
    
        	if (_bundle.hasTA()) {
        		setFlashState(true);
        		if (_bundle.hasPreset()) sendTA(_bundle.getPreset().getInputStream(),"preset");
        		//if (_bundle.hasSimlock()) sendTA(_bundle.getSimlock().getInputStream(),"simlock");
        		setFlashState(false);
        	}
            
			cmd.send(Command.CMD10,Command.VALNULL,false);            
			
	
			closeDevice();
			
			MyLogger.getLogger().info("Flashing finished.");
			MyLogger.getLogger().info("Please wait. Phone will reboot");
			MyLogger.getLogger().info("For flashtool, Unknown Sources and Debugging must be checked in phone settings");
			MyLogger.initProgress(0);
		    DeviceChangedListener.pause(false);
    	}
    	catch (Exception ioe) {
    		closeDevice();
    		MyLogger.getLogger().error(ioe.getMessage());
    		MyLogger.getLogger().error("Error flashing. Aborted");
    		MyLogger.initProgress(0);
    		DeviceChangedListener.pause(false);
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
    	cmd.send(Command.CMD04,Command.VALNULL,false);
    }
    
    public void closeDevice() {
    	try {
    		endSession();
    	}
    	catch (Exception e) {}
    	USBFlash.close();
    }
    
    public void hookDevice() throws X10FlashException,IOException {
		cmd.send(Command.CMD01, Command.VALNULL, false);
		phoneprops.update(new String (USBFlash.getLastReply()));
		if (getPhoneProperty("ROOTING_STATUS")==null) phoneprops.setProperty("ROOTING_STATUS", "UNROOTABLE"); 
		if (phoneprops.getProperty("VER").startsWith("r"))
			phoneprops.setProperty("ROOTING_STATUS", "ROOTED");
    }
    
    public boolean openDevice(boolean simulate) {
    	if (simulate) return true;
    	boolean found=false;
    	try {
    		USBFlash.open();
    		MyLogger.getLogger().info("Phone ready for flashmode operations.");
    		phoneprops = new LoaderInfo(new String (USBFlash.getLastReply()));
    	    cmd = new Command(_bundle.simulate());
    	    hookDevice();
    		found = true;
    	}
    	catch (Exception e){
    		found=false;
    	}
    	return found;
    }

}
