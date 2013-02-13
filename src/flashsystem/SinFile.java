package flashsystem;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;
import org.logger.MyLogger;
import org.system.OS;


public class SinFile {

	byte[] ident = new byte[16];
	private byte[] readarray;
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
		readcount = fin.read(readarray);
		fin.close();
		return BytesUtil.getReply(readarray,readcount);
	}
	
	public void setChunkSize(long size) {
		long datasize = sinfile.length()-sinheader.getHeaderSize();
		nbchunks = datasize / size;
		chunksize = size;
		if (datasize%size>0) nbchunks++;
		readarray = new byte[(int)size];
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
		if (sinheader.getVersion()==1||sinheader.getVersion()==2)
			dumpImageV1_2();
		if (sinheader.getVersion()==3) {
			dumpImageV3();
		}
	}
	
	public void dumpImageV1_2() throws IOException {
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

	public void dumpImageV3() throws FileNotFoundException, IOException {
		RandomAccessFile fin = new RandomAccessFile(sinfile,"r");
		fin.seek(sinheader.getHeaderSize()+0x10+0x10);
		byte[] addr = new byte[0x44];
		boolean isaddr = true;
		HashMap map = new HashMap();
		int count = 0;
		while (isaddr) {
			fin.read(addr);
			isaddr = new String(addr).startsWith("ADDR");
			if (isaddr) {
				SinAddr a = new SinAddr();
				System.arraycopy(addr, 0, a.enregtype,0,4);
				System.arraycopy(addr, 4, a.enregsize,0,4);
				System.arraycopy(addr, 8, a.addrsrc,0,8);
				System.arraycopy(addr, 16, a.datalen,0,8);
				System.arraycopy(addr, 24, a.addrdest,0,8);
				System.arraycopy(addr, 32, a.hashtype,0,4);
				map.put(new Integer(count++), a);
			}
		}
		SinAddr a = (SinAddr)map.get(map.size()-1);
		MyLogger.getLogger().info("Generating empty container file");
		String foutname = sinfile.getAbsolutePath().substring(0, sinfile.getAbsolutePath().length()-4)+".data";
		RandomAccessFile fout = OS.generateEmptyFile(foutname, a.getDestOffset()+a.getDataLength(), (byte)0xFF);
		if (fout!=null) {
			MyLogger.getLogger().info("Container generated. Now extracting data to container");
			long startoffset = sinheader.getHeaderSize()+0x10+0x10+(map.size()*0x44);
			Iterator i = map.keySet().iterator();
			while (i.hasNext()) {
				int key = ((Integer)i.next()).intValue();
				SinAddr ad = (SinAddr)map.get(key);
				fin.seek(startoffset+ad.getSrcOffset());
				byte[] res = new byte[(int)ad.getDataLength()];
				fin.read(res);
				fout.seek(ad.getDestOffset());
				fout.write(res);
			}
			fout.close();
			fin.close();
			MyLogger.getLogger().info("Extraction finished to "+foutname);
		}
		else {
			MyLogger.getLogger().error("An error occured while generating container");
		}
	}
	
	private void processHeader() throws IOException {
		byte magic[] = new byte[4];
		int nbread;
		byte headersize[] = new byte[4];
		RandomAccessFile fin = new RandomAccessFile(sinfile,"r");
		nbread = fin.read(magic);
		if (nbread != magic.length) {
			fin.close();
			throw new IOException("Error in processHeader");			
		}
		if (HexDump.toHex(magic).equals("[03, 53, 49, 4E]")) {
			nbread = fin.read(headersize);
		}
		else {
			fin.seek(2);
			nbread = fin.read(headersize);
		}
		if(nbread != headersize.length) {
			fin.close();
			throw new IOException("Error in processHeader");
		}
		MyLogger.getLogger().debug("Header size : "+BytesUtil.getInt(headersize));
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
			read = fin.read(ident1);
			if (read==-1) throw new Exception ("End of file");
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
