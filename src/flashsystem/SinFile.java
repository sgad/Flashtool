package flashsystem;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Vector;
import org.logger.MyLogger;


public class SinFile {

	byte[] ident = new byte[16];
    static byte readarray64[] = new byte[0x10000];
    static byte readarray4[] = new byte[0x1000];
	File sinfile;
	byte[] yaffs2 = {0x03, 0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, (byte)0xFF, (byte)0xFF, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
	String datatype = null;
	long nbchunks = 0;
	long chunksize = 0;
	SinFileHeader sinheader;
	
	public SinFileHeader getSinHeader() {
		return sinheader;
	}
	
	public SinFile(String file) throws FileNotFoundException,IOException {
		sinfile = new File(file);
		processHeader();
		datatype = getDatatype();
		if (sinheader.hasPartitionInfo()) {
			if (sinheader.getVersion()==1) {
				int cursize = sinheader.getHashBlocks().get(1).getLength();
				sinheader.setBlockSize(cursize);
				if (sinheader.getPartitionType()==0x0A) {
					for (int i=0;i<sinheader.getHashBlocks().size();i++) {
						sinheader.getHashBlocks().get(i).setSpare(cursize%131072);
					}
				}
					
			}
			else
				sinheader.setBlockSize(512);
		}
	}

	public String getLongFileName() {
		return sinfile.getAbsolutePath();
	}
	
	public String getShortFileName() {
		return sinfile.getName();
	}
	
	public String getHeaderFileName() {
		String path = sinfile.getAbsolutePath(); 
		return path.substring(0, path.length()-3)+"header";
	}
	
	public byte[] getHeaderBytes() {
		return sinheader.getHeader();
	}	
	
	public byte[] getChunckBytes(int chunkId) throws IOException {
		RandomAccessFile fin = new RandomAccessFile(sinfile,"r");
		long offset = sinheader.getHeaderSize()+(chunkId*chunksize);
		fin.seek(offset);
		int readcount=0;
		if (readarray64.length==chunksize) {
			readcount = fin.read(readarray64);
			fin.close();
			return BytesUtil.getReply(readarray64,readcount);
		}
		else {
			readcount = fin.read(readarray4);
			fin.close();
			return BytesUtil.getReply(readarray4,readcount);
		}
	}
	
	public void setChunkSize(long size) {
		long datasize = sinfile.length()-sinheader.getHeaderSize();
		nbchunks = datasize / size;
		chunksize = size;
		if (datasize%size>0) nbchunks++;
	}
	
	public long getChunkSize() {
		return chunksize;
	}
	
	public long getNbChunks() {
		return nbchunks;
	}
	
	public String getImageFileName() throws IOException {
		String path = sinfile.getAbsolutePath(); 
		return path.substring(0, path.length()-3)+getDataType();
	}
	
	public String getPartInfoFileName() {
		String path = sinfile.getAbsolutePath(); 
		return path.substring(0, path.length()-3)+"partinfo";		
	}
	
	public void dumpHeader() throws IOException {
		MyLogger.getLogger().info("Extracting "+getShortFileName() + " header to " + getHeaderFileName());
		FileOutputStream fout = new FileOutputStream(new File(getHeaderFileName()));
		fout.write(sinheader.getHeader());
		fout.flush();
		fout.close();
		MyLogger.getLogger().info("HEADER Extraction finished");
	}
	
	public void dumpImage() throws IOException {
				try {
					// First I write partition info bytearray in a .partinfo file
					if (sinheader.hasPartitionInfo()) {
						FileOutputStream foutpart = new FileOutputStream(new File(getPartInfoFileName()));
						foutpart.write(sinheader.getPartitionInfo());
						foutpart.flush();
						foutpart.close();
					}
		
					// To fill the empty file with FF values
					byte[] empty = new byte[65*1024];
					for (int i=0; i<empty.length;i++)
						empty[i] = (byte)0xFF;		
					// Creation of empty file
					File f = new File(getImageFileName());
					f.delete();
					RandomAccessFile fout = new RandomAccessFile(f,"rw");
					MyLogger.getLogger().info("Generating container file");
					MyLogger.getLogger().info("Output file size : " + sinheader.getOutfileLengthString());
					for (long i = 0; i<sinheader.getOutfileLength()/empty.length; i++) {
						fout.write(empty);
					}
					for (long i = 0; i<sinheader.getOutfileLength()%empty.length; i++) {
						fout.write(0xFF);
					}
					MyLogger.getLogger().info("Finished Generating container file");
					RandomAccessFile findata = new RandomAccessFile(sinfile,"r");		
					// Positionning in files
					MyLogger.getLogger().info("Extracting data into container");
					findata.seek(sinheader.getHeaderSize());
					Vector<SinHashBlock> blocks = sinheader.getHashBlocks();
					for (int i=0;i<blocks.size();i++) {
						SinHashBlock b = blocks.elementAt(i);
						byte[] data = new byte[b.getLength()];
						findata.read(data);
						b.validate(data);
						fout.seek(blocks.size()==1?0:b.getOffset());
						fout.write(data);
					}
					fout.close();
					findata.close();
					MyLogger.getLogger().info("Data Extraction finished");
				}
				catch (Exception e) {
					MyLogger.getLogger().error("Error while extracting data : "+e.getMessage());
					e.printStackTrace();
				}
	}

	private void processHeader() throws IOException {
		int nbread;
		byte headersize[] = new byte[4];
		RandomAccessFile fin = new RandomAccessFile(sinfile,"r");
		fin.seek(2);
		nbread = fin.read(headersize);
		if(nbread != headersize.length) {
			fin.close();
			throw new IOException("Error in processHeader");
		}
		byte[] header = new byte[BytesUtil.getInt(headersize)];
		fin.seek(0);
		nbread = fin.read(header);
		sinheader = new SinFileHeader(header);
		if (sinheader.hasPartitionInfo()) {
			fin.seek(sinheader.getHeaderSize());
			byte[] part = new byte[sinheader.getPartitionInfoLength()];
			fin.read(part);
			sinheader.setPartitionInfo(part);
		}
		fin.close();
		
    }

	public byte[] getPartitionInfoBytes() throws IOException {
		return sinheader.getPartitionInfo();
	}

	public String getDatatype() throws IOException {
		RandomAccessFile fin = new RandomAccessFile(sinfile,"r");
		fin.seek(sinheader.getHeaderSize()+sinheader.getPartitionInfo().length);
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
				read = fin.read(ident);
				if (read==-1) throw new Exception ("End of file");
				for (int i=0;i<read;i++) {
					if (ident[i]!=0) {
						isnull=false;
					}
				}
			}
			fin.seek(fin.getFilePointer()-16+0x38);
			byte[] ident1 = new byte[2];
			fin.read(ident1);
			fin.close();
			if (HexDump.toHex(ident1).contains("53, EF")) return "ext4";
			return "unknown";
		} catch (Exception e) {
			fin.close();
			return "unknown";
		}
	}

	public String getDataType() throws IOException {
		return datatype;
	}

	public byte getSpareBytes() {
		return sinheader.getPartitionType();
	}

}
