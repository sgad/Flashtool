package org.system;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Enumeration;
import java.util.Vector;

import org.logger.MyLogger;

import flashsystem.BytesUtil;
import flashsystem.HexDump;

public class ElfParser {

	File data;
	byte[] e_ident = new byte[16];
    byte[] e_type = new byte[2];
    byte[] e_machine = new byte[2];
    byte[] e_version = new byte[4];
    byte[] e_entry = new byte[4];
    byte[] e_phoff = new byte[4];
    byte[] e_shoff = new byte[4];
    byte[] e_flags = new byte[4];
    byte[] e_ehsize = new byte[2];
    byte[] e_phentsize = new byte[2];
    byte[] e_phnum = new byte[2];
    byte[] e_shentsize = new byte[2];
    byte[] e_shnum = new byte[2];
    byte[] e_shstrndx = new byte[2]; 
    byte[] p_offset = new byte[4];
    byte[] p_filesz = new byte[4];
    Vector<ProgramInfo> programs = new Vector();
	
	/*Elf32_Addr      Unsigned 32-bit program address
	Elf32_Half      Unsigned 16-bit field
	Elf32_Off       Unsigned 32-bit file offset
	Elf32_Sword     Signed 32-bit field or integer
	Elf32_Word      Unsigned 32-bit field or integer*/
	
	public ElfParser(String file) throws IOException {
		data = new File(file);
		RandomAccessFile fin = new RandomAccessFile(data,"r");
		fin.seek(28);
		fin.read(e_phoff);
		fin.seek(42);
		fin.read(e_phentsize);
		fin.read(e_phnum);
		revert(e_phoff);
		revert(e_phentsize);
		revert(e_phnum);
		for (int i=0;i<BytesUtil.getInt(e_phnum);i++) {
			fin.seek(BytesUtil.getInt(e_phoff)+4+(i*BytesUtil.getInt(e_phentsize)));
			fin.read(p_offset);
			revert(p_offset);
			fin.seek(BytesUtil.getInt(e_phoff)+16+(i*BytesUtil.getInt(e_phentsize)));
			fin.read(p_filesz);
			revert(p_filesz);			
			ProgramInfo pinfo = new ProgramInfo(BytesUtil.getInt(p_offset),BytesUtil.getInt(p_filesz),Integer.toString(i+1));
			programs.add(pinfo);
		}
		fin.close();
	}
	
	private void revert(byte[] array) {
		for (int i = 0, j = array.length - 1; i < j; i++, j--)  
		{  
		    byte b = array[i];  
		    array[i] = array[j];  
		    array[j] = b;  
		}
	}
	
	private class ProgramInfo {
		
		int p_offset;
		int p_size;
		String p_name;
		
		ProgramInfo (int offset, int size,String name) {
			p_offset = offset;
			p_size = size;
			p_name = name;
		}
		
		public int getSize() {
			return p_size;
		}
		
		public int getOffset() {
			return p_offset;
		}
		
		public String getName() {
			return p_name;
		}
	
	}

	public int getNbParts() {
		return programs.size();
	}
	
	public String getFolder() {
		return data.getParentFile().getAbsolutePath();
	}
	
	public void unpack() throws FileNotFoundException,IOException {
		RandomAccessFile fin = new RandomAccessFile(data,"r");
		Enumeration<ProgramInfo> e = programs.elements();
		while (e.hasMoreElements()) {
			ProgramInfo p = e.nextElement();
			fin.seek(p.getOffset());
			byte[] image = new byte[p.getSize()];
			fin.read(image);
			String ext = "."+p.getName();
			byte[] ident = new byte[352];
			if (image.length<352)
				System.arraycopy(image, 0, ident, 0, image.length);
			else
				System.arraycopy(image, 0, ident, 0, 352);
			String identHex = HexDump.toHex(ident);
			if (identHex.contains("[1F, 8B"))
				ext = ".ramdisk.gz";
			if (identHex.contains("[00, 00, A0, E1"))
				ext = ".Image";
			if (identHex.contains("53, 31, 5F, 52, 50, 4D"))
				ext = ".rpm.bin";
			MyLogger.getLogger().info("Extracting part " + p.getName() + " to " +data.getAbsoluteFile()+ext);
			File f = new File(data.getAbsoluteFile()+ext);
			FileOutputStream fout = new FileOutputStream(f);
			fout.write(image);
			image=null;
			fout.flush();
			fout.close();
		}
		fin.close();
		MyLogger.getLogger().info("ELF Extraction finished");
	}
	
}
