package flashsystem;

public class SinFileHeader {

	private byte[] version = new byte[1]; 
	private byte[] nextHeader = new byte[1];;
	private byte[] headersize = new byte[4];
	private byte[] partitionType = new byte[1];;
	private byte[] sinreserved = new byte[4];
	private byte[] hashlistsize = new byte[4];
	private byte[] hashlength = new byte[1];
	private boolean hasPartinfo = false;
	int partinfolength;
	long outfilelength;
	private byte[] partitioninfo;
	int bksize=512;
	byte[] header;
	
	public SinFileHeader(byte[] header) {
		version[0] = header[0];
		nextHeader[0] = header[1];
		headersize[0] = header[2];
		headersize[1] = header[3];
		headersize[2] = header[4];
		headersize[3] = header[5];
		partitionType[0] = header[6];
		sinreserved[0] = header[7];
		sinreserved[1] = header[8];
		sinreserved[2] = header[9];
		sinreserved[3] = header[10];
		hashlistsize[0] = header[11];
		hashlistsize[1] = header[12];
		hashlistsize[2] = header[13];
		hashlistsize[3] = header[14];
	}

	public void setFirstBlock(byte[] block) {
		hashlength[0] = block[8];
		byte[] datalength = new byte[4];
		byte[] offset = new byte[4];
		datalength[0]=block[4];
		datalength[1]=block[5];
		datalength[2]=block[6];
		datalength[3]=block[7];
		partinfolength=BytesUtil.getInt(datalength);
		if ((partinfolength==0x10) && (getNbHashBlocks()>1)) hasPartinfo = true;
		else {
			outfilelength=partinfolength;
			partinfolength=0;
			partitioninfo = new byte[1];
		}
	}
	
	public int getPartitionInfoLength() {
		return partinfolength;
	}
	
	public boolean hasPartitionInfo() {
		return hasPartinfo;
	}
	
	public void setPartitionInfo(byte[] partinfo) {
		partitioninfo = partinfo;
		byte[] nbblocks = new byte[4];
		nbblocks[0] = partinfo[15];
		nbblocks[1] = partinfo[14];
		nbblocks[2] = partinfo[13];
		nbblocks[3] = partinfo[12];
		outfilelength = BytesUtil.getLong(nbblocks)*bksize;
	}
	
	public byte[] getPartitionInfo() {
		return partitioninfo;
	}
	
	public long getOutfileLength() {
		return outfilelength;
	}
	
	public int getNbHashBlocks() {
		return getHashListSize()/(BytesUtil.getInt(hashlength)+9);
	}
	
	public int getVersion() {
		return BytesUtil.getInt(version);
	}
	
	public int getNextHeader() {
		return BytesUtil.getInt(nextHeader);
	}
	
	public byte getPartitionType() {
		return partitionType[0];
	}
	
	public void setHeader(byte[] pheader) {
		header = pheader;
	}

	public byte[] getHeader() {
		return header;
	}
	
	public String getPartypeString() {
		if (partitionType[0]==0x09)
			return "Without spare";
		if (partitionType[0]==0x0A)
			return "With spare";
		return "unknown";
	}
	
	public int getHashListSize() {
		return BytesUtil.getInt(hashlistsize);
	}

	public int getHeaderSize() {
		return BytesUtil.getInt(headersize);
	}

}
