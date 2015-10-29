import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;

/**
 * convert a series of rgb files into one
 */
public class convert {
	public static void main(String[] args) throws IOException {
		int videolength = 600;
		int idx = 0;
		byte[] bytes = new byte[videolength * 352 * 288 * 3];
		String foldername = args[0];
		File root = new File(foldername);
		// System.out.println(foldername);
		File contents[] = root.listFiles();
		for (File f : contents) {
			// System.out.println(f.getName());
			String extensionname = getExtensionName(f.getName());
			if (extensionname.equals("rgb")) {
				System.out.println(f.getName());
				InputStream is = new FileInputStream(f);
				byte[] b = new byte[352 * 288 * 3];
				is.read(b, 0, b.length);
				for (int i = 0; i < 352 * 288 * 3; i++) {
					bytes[i + idx] = b[i];
				}
				idx += 352 * 288 * 3;

			}

		}
		File writename = new File("output.rgb");
		writename.createNewFile(); 
		FileOutputStream fos = new FileOutputStream(writename, true);
		fos.write(bytes, 0, bytes.length);
		fos.flush(); 
		fos.close(); 
	}

	public static String getExtensionName(String filename) {
		if ((filename != null) && (filename.length() > 0)) {
			int dot = filename.lastIndexOf('.');
			if ((dot > -1) && (dot < (filename.length() - 1))) {
				return filename.substring(dot + 1);
			}
		}
		return filename;
	}
}
