package flashsystem;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

import org.logger.MyLogger;
import org.system.OS;
import org.system.ProcessBuilderWrapper;

public class SinFile {

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
		// First I write partition info bytearray in a .partinfo file
		FileOutputStream foutpart = new FileOutputStream(new File(this.getPartInfoName()));
		foutpart.write(partinfo);
		foutpart.flush();
		foutpart.close();
		
		// Data Extraction (usually yaffs2, elf or ext4 object)
		MyLogger.getLogger().info("Extracting "+getFile() + " content to " + getImage());
		RandomAccessFile fin = new RandomAccessFile(sinfile,"r");
		// seek to start offset of data
		fin.seek(header.length+partinfo.length);
		int read; 
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
		int nbread;
		int headersize;
		RandomAccessFile fin = new RandomAccessFile(sinfile,"r");
		byte hsize[] = new byte[4];
		fin.seek(2);
		nbread = fin.read(hsize);
		if(nbread != 4) {
			fin.close();
			throw new IOException("Error in processHeader");
		}
		headersize = BytesUtil.getInt(hsize);
		header = new byte[headersize];
		fin.seek(0);
		nbread = fin.read(header);
		if(nbread != headersize) {
			fin.close();
			throw new IOException("Error in processHeader");
		}
		spare = header[6];
		nbread = fin.read(partinfo);
		if(nbread != partinfo.length) {
			fin.close();
			throw new IOException("Error in processHeader");
		}
		fin.close();
    }

	public byte[] getPartitionInfo() throws IOException {
		return partinfo;
	}

	public String getDatatype() throws IOException {
		RandomAccessFile fin = new RandomAccessFile(sinfile,"r");
		fin.seek(header.length+partinfo.length);
		int read;
		try {
			read = fin.read(ident);
			if (read!=ident.length) {
				fin.close();
				throw new IOException("Error in retrieving data type");
			}
			String result = new String(ident);
			String yaffs = new String(yaffs2);
			if (result.equals(yaffs)) {
				fin.close();
				return "yaffs2";
			}
			if (result.contains("ELF")) {
				fin.close();
				return "elf";
			}
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
