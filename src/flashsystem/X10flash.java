package flashsystem;

import flashsystem.HexDump;
import flashsystem.io.USBFlash;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.IOException;
import org.logger.MyLogger;
import org.system.Device;
import org.system.DeviceChangedListener;
import org.system.DeviceEntry;
import org.system.Devices;
import org.system.OS;
import org.system.TextFile;
import java.util.Enumeration;
import java.util.Vector;

public class X10flash {

    private Bundle _bundle;
    private Command cmd;
    private LoaderInfo phoneprops = null;
    private String firstRead = "";
    private String cmd01string = "";
    private boolean taopen = false;
    private boolean modded_loader=false;
    private String currentdevice = "";
    private int maxpacketsize = 0;
    private String serial = "";

    public X10flash(Bundle bundle) {
    	_bundle=bundle;
    }

    public void setFlashState(boolean ongoing) throws IOException,X10FlashException
    {
	    	if (ongoing) {
	    		openTA(2);
	    		cmd.send(Command.CMD13,Command.TA_FLASH_STARTUP_SHUTDOWN_RESULT_ONGOING,false);
	    		closeTA();
	    		openTA(2);
	    		cmd.send(Command.CMD13, Command.TA_EDREAM_FLASH_STARTUP_SHUTDOWN_RESULT_ONGOING,false);
	    		closeTA();
	    		openTA(2);
	    	}
	    	else {
	    		closeTA();
	    		openTA(2);
	    		cmd.send(Command.CMD13, Command.TA_FLASH_STARTUP_SHUTDOWN_RESULT_FINISHED,false);
	    		closeTA();
	    		openTA(2);
	    		cmd.send(Command.CMD13, Command.TA_EDREAM_FLASH_STARTUP_SHUTDOWN_RESULT_FINISHED,false);
	    		closeTA();
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

    public TaEntry dumpProperty(int unit) throws IOException, X10FlashException
    {
    		String sunit = HexDump.toHex(BytesUtil.getBytesWord(unit, 4));
    		sunit = sunit.replace("[", "");
    		sunit = sunit.replace("]", "");
    		sunit = sunit.replace(",", "");
    		sunit = sunit.replace(" ", "");
    		MyLogger.getLogger().info("Start Reading unit "+sunit);
	        MyLogger.getLogger().debug((new StringBuilder("%%% read TA property id=")).append(unit).toString());
	        try {
	        	cmd.send(Command.CMD12, BytesUtil.getBytesWord(unit, 4),false);
	        	MyLogger.getLogger().info("Reading TA finished.");
	        }
	        catch (X10FlashException e) {
	        	MyLogger.getLogger().info("Reading TA finished.");
	        	return null;
	        }
	        if (cmd.getLastReplyLength()>0) {
        		TaEntry ta = new TaEntry();
        		ta.setPartition(HexDump.toHex(unit));
	        	String reply = cmd.getLastReplyHex();
	        	reply = reply.replace("[", "");
	        	reply = reply.replace("]", "");
	        	reply = reply.replace(",", "");
        		ta.addData(reply.trim());
        		return ta;
    		}
			return null;
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
    
    private void processHeader(SinFile sin) throws X10FlashException {
    	try {
    		MyLogger.getLogger().info("    Checking header");
    		int nbparts = sin.getSinHeader().getHeaderSize() / (int)sin.getChunkSize();
    		int remain = sin.getSinHeader().getHeaderSize() % (int)sin.getChunkSize();
    		byte[] looppart = new byte[(int)sin.getChunkSize()];
    		for (int i=0;i<nbparts;i++) {
    			System.arraycopy(sin.getHeaderBytes(), (int)(i*sin.getChunkSize()), looppart, 0, (int)sin.getChunkSize());
    			cmd.send(Command.CMD05,looppart,true);
        		if (USBFlash.getLastFlags() == 0)
        			getLastError();
    		}
    		byte[] remaining = new byte[remain];
			System.arraycopy(sin.getHeaderBytes(), sin.getSinHeader().getHeaderSize()-remain, remaining, 0, remain);
    		cmd.send(Command.CMD05,remaining,false);
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
    
    private void uploadImage(SinFile sin) throws X10FlashException {
    	try {
	    	processHeader(sin);
	    	MyLogger.getLogger().info("    Flashing data");
	    	MyLogger.getLogger().debug("Number of parts to send : "+sin.getNbChunks()+" / Part size : "+sin.getChunkSize());
			for (int j=0;j<sin.getNbChunks();j++) {
				int cur = j+1;
				MyLogger.getLogger().debug("Sending part "+cur+" of "+sin.getNbChunks());
				cmd.send(Command.CMD06, sin.getChunckBytes(j), !((j+1)==sin.getNbChunks()));
			}
			if (USBFlash.getLastFlags() == 0)
				getLastError();
    	}
    	catch (Exception e) {
    		e.printStackTrace();
    		throw new X10FlashException (e.getMessage());
    	}
    }

    private String getDefaultLoader() {
    	int nbfound = 0;
    	String loader = "";
    	Enumeration<Object> e = Devices.listDevices(true);
    	while (e.hasMoreElements()) {
    		DeviceEntry current = Devices.getDevice((String)e.nextElement());
    		if (current.getRecognition().contains(currentdevice)) {
    			nbfound++;
    			if (modded_loader) {
    				loader = current.getLoaderUnlocked();
    			}
    			else {
    				loader=current.getLoader();
    			}
    		}
    	}
    	if ((nbfound == 0) || (nbfound > 1)) 
    		return "";
    	if (modded_loader)
			MyLogger.getLogger().info("Using an unofficial loader");
    	return OS.getWorkDir()+loader.substring(1, loader.length());
    }

    public void sendLoader() throws FileNotFoundException, IOException, X10FlashException {
    	String loader = "";
		MyLogger.getLogger().info("Processing loader");
		if (!modded_loader) {
			if (_bundle.hasLoader()) {
				loader = _bundle.getLoader().getAbsolutePath();
			}
			else {
				loader = getDefaultLoader();
			}
		}
		else {
			loader = getDefaultLoader();
			if (!new File(loader).exists()) loader="";
			if (loader.length()==0)
				if (_bundle.hasLoader()) {
					MyLogger.getLogger().info("Device loader has not been identified. Using the one from the bundle");
					loader = _bundle.getLoader().getAbsolutePath();
				}
		}
		
		if (loader.length()==0) throw new X10FlashException("No loader found for this device");
		SinFile sin = new SinFile(loader);
		if (sin.getSinHeader().getVersion()>=2)
			sin.setChunkSize(0x10000);
		else
			sin.setChunkSize(0x1000);
		uploadImage(sin);
		USBFlash.readS1Reply(true);
		hookDevice(true);
    }

    public void sendPartition() throws FileNotFoundException, IOException, X10FlashException {		
		if (_bundle.hasPartition()) {
			BundleEntry entry = _bundle.getPartition();
			MyLogger.getLogger().info("Processing "+entry.getName());
			closeTA();
			SinFile sin = new SinFile(entry.getAbsolutePath());
			sin.setChunkSize(maxpacketsize);
			uploadImage(sin);
			openTA(2);
		}
    }
    
    public void sendImages() throws FileNotFoundException, IOException,X10FlashException {
		for (int i = 1;i<=_bundle.getMeta().getNbCategs();i++) {
			String categ = _bundle.getMeta().getCagorie(i);
			if (_bundle.getMeta().isCategEnabled(categ)) {
				Enumeration entries = _bundle.getMeta().getEntriesOf(categ,true);
				while (entries.hasMoreElements()) {
					String entry = (String)entries.nextElement();
					BundleEntry bent = _bundle.getEntry(entry);
					MyLogger.getLogger().info("Processing "+bent.getName());
					SinFile sin = new SinFile(bent.getAbsolutePath());
					sin.setChunkSize(maxpacketsize);
					uploadImage(sin);
					MyLogger.getLogger().debug("Flashing "+bent.getName()+" finished");
				}
			}
		}
    }

    public String getPhoneProperty(String property) {
    	return phoneprops.getProperty(property);
    }

    public void openTA(int partition) throws X10FlashException, IOException{
    	if (!taopen)
    		cmd.send(Command.CMD09, BytesUtil.getBytesWord(partition, 1), false);
    	taopen = true;
    }
    
    public void closeTA() throws X10FlashException, IOException{
    	if (taopen)
    		cmd.send(Command.CMD10, Command.VALNULL, false);
    	taopen = false;
    }
   
    public void sendTAFiles() throws FileNotFoundException, IOException,X10FlashException {
		Enumeration entries = _bundle.getMeta().getEntriesOf("TA",true);
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
    
    public void getDevInfo() throws IOException, X10FlashException {
    	cmd.send(Command.CMD12, Command.TA_DEVID1, false);
    	String info = "Current device : "+cmd.getLastReplyString();
    	currentdevice = cmd.getLastReplyString();
    	cmd.send(Command.CMD12, Command.TA_DEVID2, false);
    	info = info + " - "+cmd.getLastReplyString();
    	serial = cmd.getLastReplyString();
    	cmd.send(Command.CMD12, Command.TA_DEVID3, false);
    	info = info + " - "+cmd.getLastReplyString();
    	cmd.send(Command.CMD12, Command.TA_DEVID4, false);
    	info = info + " - "+cmd.getLastReplyString();
    	cmd.send(Command.CMD12, Command.TA_DEVID5, false);
    	info = info + " - "+cmd.getLastReplyString();
    	MyLogger.getLogger().info(info);
    }
    
    public void flashDevice() {
    	try {
		    MyLogger.getLogger().info("Start Flashing");
		    sendLoader();
		    maxpacketsize=Integer.parseInt(phoneprops.getProperty("MAX_PKT_SZ"),16);
		    if (_bundle.hasCmd25()) {
		    	MyLogger.getLogger().info("Disabling final data verification check");
		    	cmd.send(Command.CMD25, Command.DISABLEFINALVERIF, false);
		    }
		    setFlashState(true);
		    sendPartition();
		    sendTAFiles();
			sendImages();
        	setFlashState(false);
        	closeDevice(0x01);
			MyLogger.getLogger().info("Flashing finished.");
			MyLogger.getLogger().info("Please unplug and start your phone");
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

    public Bundle getBundle() {
    	return _bundle;
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
		cmd01string = cmd.getLastReplyString();
		MyLogger.getLogger().debug(cmd01string);
		phoneprops.update(cmd01string);
		if (getPhoneProperty("ROOTING_STATUS")==null) phoneprops.setProperty("ROOTING_STATUS", "UNROOTABLE"); 
		if (phoneprops.getProperty("VER").startsWith("r"))
			phoneprops.setProperty("ROOTING_STATUS", "ROOTED");
		if (printProps) {
			MyLogger.getLogger().debug("After loader command reply (hook) : "+cmd01string);
			MyLogger.getLogger().info("Loader : "+phoneprops.getProperty("LOADER_ROOT")+" - Version : "+phoneprops.getProperty("VER")+" / Bootloader status : "+phoneprops.getProperty("ROOTING_STATUS"));
		}
		else
			MyLogger.getLogger().debug("First command reply (hook) : "+cmd01string);
    }

    public boolean openDevice(boolean simulate) {
    	if (simulate) return true;
    	MyLogger.initProgress(_bundle.getMaxProgress());
    	boolean found=false;
    	try {
    		USBFlash.open("ADDE");
    		try {
				MyLogger.getLogger().info("Reading device information");
				USBFlash.readS1Reply(true);
				firstRead = new String (USBFlash.getLastReply());
				phoneprops = new LoaderInfo(firstRead);
				if (phoneprops.getProperty("VER").startsWith("r"))
					modded_loader=true;
				MyLogger.getLogger().debug(firstRead);
				
    		}
    		catch (Exception e) {
    			e.printStackTrace();
    			MyLogger.getLogger().info("Unable to read from phone after having opened it.");
    			MyLogger.getLogger().info("trying to continue anyway");
    		}
    	    cmd = new Command(_bundle.simulate());
    	    hookDevice(false);
    	    MyLogger.getLogger().info("Phone ready for flashmode operations.");
		    openTA(2);
		    getDevInfo();
		    closeTA();
    	    found = true;
    	}
    	catch (Exception e){
    		e.printStackTrace();
    		found=false;
    	}
    	return found;
    }

    public String getSerial() {
    	return serial;
    }
}