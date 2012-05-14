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
	byte[] parts = new byte[65535];
	byte spare;
	File sinfile;
	
	public SinFile(String file) throws FileNotFoundException,IOException {
		sinfile = new File(file);
		processHeader();
	}

	public String getFile() {
		return sinfile.getAbsolutePath();
	}
	
	public String getImage() {
		return sinfile.getAbsolutePath().replaceAll(".sin",".data");
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
	
	public byte getSpare() {
		return spare;
	}

}
