package bloodandmithril.persistence;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import bloodandmithril.core.Copyright;

/**
 * Helper class for writing zip files
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public class ZipHelper {

	private ZipOutputStream out;

	/**
	 * Constructor
	 */
	public ZipHelper(String path, String name) {
		try {
			out = new ZipOutputStream(new FileOutputStream(path + name));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}


	/**
	 * Adds a file in the specified directory with the specified name filled with the specified content
	 */
	public ZipHelper addFile(String path, String name, String content, boolean failureTolerant) {
		try {
			out.putNextEntry(new ZipEntry(path + name));
			out.write(content.getBytes());
			out.closeEntry();
		} catch (Exception e) {
			if (!failureTolerant) {
				throw new RuntimeException(e);
			}
		}

		return this;
	}


	/**
	 * Finalize the zip creation by writing it to disk
	 */
	public void makeZip() {
		try {
			out.close();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	

	/** Reads a zip entry as a string */
	public static String readEntry(ZipFile zip, String zipEntry) {
		try {
			ZipEntry entry = zip.getEntry(zipEntry);
			BufferedReader fIn = new BufferedReader(new InputStreamReader(zip.getInputStream(entry), "UTF-8"));

			char[] chars = new char[(int)entry.getSize()];
			fIn.read(chars);

			return new String(chars);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	
	/** Reads a zip entry as a string */
	public static String readEntry(ZipFile zip, ZipEntry zipEntry) {
		try {
			BufferedReader fIn = new BufferedReader(new InputStreamReader(zip.getInputStream(zipEntry), "UTF-8"));

			char[] chars = new char[(int)zipEntry.getSize()];
			fIn.read(chars);

			return new String(chars);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	
	/** Reads all entries */
	public static Enumeration<? extends ZipEntry> readAllEntries(ZipFile zip) {
		return zip.entries();
	}
}