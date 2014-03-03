package bloodandmithril.persistence;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.Serializable;

import biz.source_code.base64Coder.Base64Coder;

import com.badlogic.gdx.files.FileHandle;

/**
 * Utility methods for persistence
 *
 * @author Matt
 */
public class PersistenceUtil {


	/** Write the object to a Base64 string. */
	public static String encode(Serializable o) {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(baos);
			oos.writeObject(o);
			oos.close();
			return new String(Base64Coder.encode(baos.toByteArray()));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}


	/** Read the object from Base64 string. */
	@SuppressWarnings("unchecked")
	public static <T> T decode(String s) {
		try {
			byte[] data = Base64Coder.decode(s);
			ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data));
			T object = (T)ois.readObject();
			ois.close();
			return object;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}


	/** Read the object from Base64 string. */
	public static <T> T decode(FileHandle file) {
		return decode(file.readString());
	}
	
	
	/** Writes a file to disk at the root directory with a specified name and content */
	public static void writeFile(String fileName, String content) {
		try {
			PrintWriter out = new PrintWriter(new FileWriter(fileName));
			out.print(content);
			out.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	
	/** Reads and returns the content of a file */
	public static String readFile(String fileName) throws IOException {
		BufferedReader in = new BufferedReader(new FileReader(fileName));
		String text = in.readLine();
		in.close();
		return text;
	}
}