package flashsystem;

import flashsystem.HexDump;
import flashsystem.io.USBFlash;
import gui.LoaderRootFilter;
import gui.LoaderSelectorGUI;
import gui.deviceSelectGui;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.IOException;
import org.logger.MyLogger;
import org.system.Device;
import org.system.DeviceChangedListener;
import org.system.Devices;
import org.system.OS;
import org.system.TextFile;

import java.util.Enumeration;
import java.util.Properties;
import java.util.Vector;

public class X10flash {

    private Bundle _bundle;
    private Command cmd;
    private LoaderInfo phoneprops = null;

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
    
    private void processHeader(SinFile sin) throws X10FlashException {
    	try {
    		MyLogger.getLogger().info("    Checking header");
	    	cmd.send(Command.CMD05,sin.getHeaderBytes(),false);
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
			for (int j=0;j<sin.getNbChunks();j++) {
				cmd.send(Command.CMD06, sin.getChunckBytes(j), (sin.getNbChunks()<(j+1)));
			}
			if (USBFlash.getLastFlags() == 0)
				getLastError();
    	}
    	catch (Exception e) {
    		e.printStackTrace();
    		throw new X10FlashException (e.getMessage());
    	}
    }

    public void sendLoader() throws FileNotFoundException, IOException, X10FlashException {
		MyLogger.getLogger().info("Processing loader");
		String LoaderHandler;
		if (_bundle.hasLoader()) {
			SinFile sin = new SinFile(_bundle.getLoader().getAbsolutePath());
			sin.setChunkSize(0x1000);
			uploadImage(sin);
		}
		else {
			File dir = new File(OS.getWorkDir()+"/loaders");
			LoaderHandler = phoneprops.getProperty("LOADER_ROOT");
			File[] filelist = dir.listFiles(new LoaderRootFilter(LoaderHandler));
			if (filelist.length>1) {
				LoaderSelectorGUI sel = new LoaderSelectorGUI(filelist);
				String loader = sel.getVersion();
				if (loader.length()>0) {
					SinFile sin = new SinFile(OS.getWorkDir()+"/loaders/"+loader);
					sin.setChunkSize(0x1000);
					uploadImage(sin);
				}
			}
			else {
				if (filelist.length==1) {
					SinFile sin = new SinFile(filelist[0].getAbsolutePath());
					sin.setChunkSize(0x1000);
					uploadImage(sin);
				}
				else {
	        		String devid="";
	        		deviceSelectGui devsel = new deviceSelectGui(null);
	        		devid = devsel.getDevice(new Properties());
	        		SinFile sin = new SinFile(OS.getWorkDir()+"/devices/"+devid+"/loader.sin");
	        		sin.setChunkSize(0x1000);
					uploadImage(sin);
					FileInputStream fin = new FileInputStream(new File(OS.getWorkDir()+"/devices/"+devid+"/loader.sin"));
					FileOutputStream fout = new FileOutputStream(OS.getWorkDir()+"/loaders/"+new File(phoneprops.getProperty("LOADER_ROOT")+".sin"));
					try {
						byte b[] = new byte[1];
						int read = fin.read(b);
						while (read == 1) {
							fout.write(b);
							read = fin.read(b);
						}
					}
					catch (Exception e) {
					}
					fout.flush();
					fout.close();
					fin.close();
				}
			}
		}
		USBFlash.readS1Reply(5000);
		hookDevice(true);
    }

    public void sendLoader(File loader) throws FileNotFoundException, IOException, X10FlashException {
		MyLogger.getLogger().info("Processing loader");
		SinFile sin = new SinFile(loader.getAbsolutePath());
		sin.setChunkSize(0x1000);
		uploadImage(sin);
		USBFlash.readS1Reply(5000);
		hookDevice(true);
    }

    public void sendPartition() throws FileNotFoundException, IOException, X10FlashException {		
		if (_bundle.hasPartition()) {
			BundleEntry entry = _bundle.getPartition();
			MyLogger.getLogger().info("Processing "+entry.getName());
			closeTA();
			SinFile sin = new SinFile(entry.getAbsolutePath());
			sin.setChunkSize(0x10000);
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
					sin.setChunkSize(0x10000);
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
    	cmd.send(Command.CMD09, BytesUtil.getBytesWord(partition, 1), false);
    }
    
    public void closeTA() throws X10FlashException, IOException{
    	cmd.send(Command.CMD10, Command.VALNULL, false);
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
    
    public void flashDevice() {
    	try {
		    MyLogger.getLogger().info("Start Flashing");
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
    	MyLogger.initProgress(_bundle.getMaxProgress());
    	boolean found=false;
    	try {
    		USBFlash.open("ADDE");
    		try {
			MyLogger.getLogger().info("Reading device information");
			USBFlash.readS1Reply();
			phoneprops = new LoaderInfo(new String (USBFlash.getLastReply()));
			MyLogger.getLogger().info("Phone ready for flashmode operations.");
    		}
    		catch (Exception e) {
    			e.printStackTrace();
    			MyLogger.getLogger().info("Unable to read from phone after having opened it.");
    			MyLogger.getLogger().info("trying to continue anyway");
    		}
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