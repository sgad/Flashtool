package flashsystem;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.logger.MyLogger;

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
	
	public SinFile(String file) throws FileNotFoundException,IOException {
		sinfile = new File(file);
		processHeader();
	}

	public String getFile() {
		return sinfile.getAbsolutePath();
	}
	
	public String getImage() throws IOException {
		return sinfile.getAbsolutePath().replaceAll(".sin","."+getIdent());
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
		MyLogger.getLogger().info("SIN Extraction finished");
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

	public String getIdent() throws IOException {
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
			fin.close();
			return "";
		} catch (IOException e) {
			fin.close();
			return "";
		}
		
	}

	public byte getSpare() {
		return spare;
	}

}
