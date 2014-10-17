package ca.fraggergames.ffxivextract.helpers;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;

import ca.fraggergames.ffxivextract.models.SqPack_DatFile;
import ca.fraggergames.ffxivextract.models.SqPack_IndexFile;
import ca.fraggergames.ffxivextract.models.SqPack_IndexFile.SqPack_File;
import ca.fraggergames.ffxivextract.models.SqPack_IndexFile.SqPack_Folder;
import ca.fraggergames.ffxivextract.storage.HashDatabase;

public class EXD_Searcher {

	public static void saveEXL()
	{
		InputStream in;
		BufferedWriter writer = null;
		
		boolean readingName = true;
		
		try {
			in  = new FileInputStream("C:\\Users\\Filip\\Desktop\\root.exl.dat");
			writer = new BufferedWriter(new FileWriter("./exddump2.txt"));
			
			while(true)
			{
				int b = in.read();
				
				if (b == -1)
					break;
				
				if (b == ',' || b == 0x0D)
				{
					if (b == 0x0D)
						in.read();
					if (readingName)
						writer.append("\r\n");
					readingName = !readingName;
				}
	
				System.out.println((char)b);
				
				if (readingName)
					writer.append((char)b);
			}
			in.close();
			writer.close();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
	}
	
	public static void saveEXDNames(SqPack_IndexFile currentIndexFile, SqPack_DatFile currentDatFile)
	{
		BufferedWriter writer = null;
		try {writer = new BufferedWriter(new FileWriter("./exddump.txt"));
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		int numSaved = 0;
		String string = "TEXT_";
			for (int i = 0; i < currentIndexFile.getPackFolders().length; i++) {			
				SqPack_Folder f = currentIndexFile.getPackFolders()[i];
				for (int j = 0; j < f.getFiles().length; j++) {
					SqPack_File fi = f.getFiles()[j];
					byte[] data;
					try {
						data = currentDatFile.extractFile(fi.dataoffset, null);
						if (data == null)
							continue;
						
						for (int i2 = 0; i2 < data.length - string.length(); i2++) {
							boolean exitFile = false;
							for (int j2 = 0; j2 < string.length(); j2++) {
								if (data[i2 + j2] == string.charAt(j2)) {
									if (j2 == string.length() - 1) {																								
										
										//Look for end
										int endString = 0;
										int underScoreCount = 0;
										for (int endSearch = i2; endSearch < data.length - string.length(); endSearch++)
										{																					
											if (data[endSearch] == '_')											
												underScoreCount++;
											
											if (underScoreCount >= 3)
											{
												endString = endSearch;
												break;
											}
										}										
	
										//Hack for last file
										if (endString == 0)
											endString = data.length-1;
										
										//Get full path
										String fullpath = new String(data, i2, endString-i2);
										fullpath = HashDatabase.getFolder(f.getId()) + "/" + fullpath.toLowerCase();
																				
										writer.write(fullpath + ".exh\r\n");
										writer.write(fullpath + "_0_en.exd\r\n");
										writer.write(fullpath + "_0_ja.exd\r\n");
										writer.write(fullpath + "_0_de.exd\r\n");
										writer.write(fullpath + "_0_fr.exd\r\n");
										System.out.println("=> "+ fullpath);
										exitFile = true;							
										numSaved++;
																										
									} else
										continue;
								} else
									break;
							}	
							if (exitFile)
								break;
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
	
				}								
			}
			System.out.println("Saved " + numSaved +"names.");
			try {
				writer.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	
	public static void createEXDFiles(String path)
	{
		try {
			BufferedReader reader = new BufferedReader(new FileReader(path));
			BufferedWriter writer = new BufferedWriter(new FileWriter(path+"out.txt"));
			
			while(true){
				String in = reader.readLine();
				if (in == null)
					break;
				in.replace("\n", "");
				in.replace("\r", "");
				writer.write(in + ".exh\r\n");
				writer.write(in + "_0_en.exd\r\n");
				writer.write(in + "_0_ja.exd\r\n");
				writer.write(in + "_0_de.exd\r\n");
				writer.write(in + "_0_fr.exd\r\n");
			}
			reader.close();
			writer.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
