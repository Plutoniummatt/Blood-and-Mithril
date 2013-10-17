package spritestar.persistence;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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
}
