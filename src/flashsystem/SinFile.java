package flashsystem;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.logger.MyLogger;
import org.system.OS;
import org.system.ProcessBuilderWrapper;

public class SinFile {

	int headersize;
	int partitioninfosize=0x10;
	byte[] header;
	byte[] partinfo = new byte[0x10];
	byte[] ident = new byte[16];
	byte[] parts = new byte[65535];
	byte spare;
	File sinfile;
	byte[] yaffs2 = {0x03, 0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, (byte)0xFF, (byte)0xFF, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
	String datatype = null;
	
	public SinFile(String file) throws FileNotFoundException,IOException {
		sinfile = new File(file);
		processHeader();
		datatype = getDatatype();
	}

	public String getFile() {
		return sinfile.getAbsolutePath();
	}
	
	public String getName() {
		return sinfile.getName();
	}
	
	public String getHeader() {
		String path = sinfile.getAbsolutePath(); 
		return path.substring(0, path.length()-3)+"header";
	}
	
	public String getImage() throws IOException {
		String path = sinfile.getAbsolutePath(); 
		return path.substring(0, path.length()-3)+getIdent();
	}
	
	public String getPartInfoName() {
		String path = sinfile.getAbsolutePath(); 
		return path.substring(0, path.length()-3)+"partinfo";		
	}
	
	public void dumpHeader() throws IOException {
		MyLogger.getLogger().info("Extracting "+getFile() + " header to " + getHeader());
		FileOutputStream fout = new FileOutputStream(new File(getHeader()));
		fout.write(header);
		fout.flush();
		fout.close();
		MyLogger.getLogger().info("HEADER Extraction finished");
	}
	
	public void dumpImage() throws IOException {
		MyLogger.getLogger().info("Extracting "+getFile() + " content to " + getImage());
		FileInputStream fin = new FileInputStream(sinfile);
		int read; 
		read = fin.read(header);
		if (read!=headersize) {
			fin.close();
			throw new IOException("Error in retrieving header");
		}
		read = fin.read(partinfo);
		if (read!=partinfo.length) {
			fin.close();
			throw new IOException("Error in retrieving partinfo");
		}
		FileOutputStream foutpart = new FileOutputStream(new File(this.getPartInfoName()));
		foutpart.write(partinfo);
		foutpart.flush();
		foutpart.close();
		FileOutputStream fout = new FileOutputStream(new File(getImage()));
		try {
			while (true) {
				read = fin.read(parts);
				fout.write(parts, 0, read);
			}
		}
		catch (Exception e) {
		}
		fout.flush();
		fout.close();
		fin.close();
		MyLogger.getLogger().info("DATA Extraction finished");
	}

	private void processHeader() throws IOException {
		FileInputStream fileinputstream = new FileInputStream(sinfile);	
		byte abyte0[] = new byte[6];
		int j = fileinputstream.read(abyte0);
		if(j != 6) {
			fileinputstream.close();
			throw new IOException("Error in processHeader");
		}
		int k;
		byte abyte1[] = new byte[4];
		System.arraycopy(abyte0, 2, abyte1, 0, 4);
		k = BytesUtil.getInt(abyte1);
		abyte1 = new byte[k - 6];
		k = fileinputstream.read(abyte1);
		if(k != abyte1.length) {
			fileinputstream.close();
			throw new IOException("Error in processHeader");
		}
		spare = abyte1[0];
		headersize = BytesUtil.concatAll(abyte0, abyte1).length;
		header = new byte[headersize];
		fileinputstream.close();
    }

	public byte[] getPartitionInfo() throws IOException {
		FileInputStream fin = new FileInputStream(sinfile);
		int read = fin.read(header);
		read = fin.read(partinfo);
		if (read!=partinfo.length) {
			fin.close();
			throw new IOException("Error in retrieving partinfo");
		}
		fin.close();
		return partinfo;
	}

	public String getDatatype() throws IOException {
		FileInputStream fin = new FileInputStream(sinfile);
		int read = fin.read(header);
		read = fin.read(partinfo);
		if (read!=partinfo.length) {
			fin.close();
			throw new IOException("Error in retrieving partinfo");
		}
		try {
			read = fin.read(ident);
			if (read!=ident.length) {
				fin.close();
				throw new IOException("Error in retrieving data type");
			}
			String result = new String(ident);
			String yaffs = new String(yaffs2);
			if (result.equals(yaffs)) return "yaffs2";
			if (result.contains("ELF")) return "elf";
			boolean isnull = true;
			int count=0;
			for (int i=0;i<ident.length;i++) {
				if (ident[i]!=0) {
					isnull=false;
				}
			}
			while (isnull) {
				byte b = (byte)fin.read();
				if (b!=0) isnull=false;
			}
			byte[] ident1 = new byte[52];
			fin.read(ident1);
			ident1 = new byte[4];
			fin.read(ident1);
			fin.close();
			if (HexDump.toHex(ident1).contains("53, EF")) return "ext4";
			return "unknown";
		} catch (IOException e) {
			fin.close();
			return "unknown";
		}
	}

	public String getIdent() throws IOException {
		return datatype;
	}

	public byte getSpare() {
		return spare;
	}

}
