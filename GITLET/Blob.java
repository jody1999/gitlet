package gitlet;

import java.io.File;
import java.io.Serializable;

public class Blob implements Serializable {
    String SHA1ID;
    String fileName;
    boolean isTracked;
    byte[] content;

    Blob() {
        fileName = "Justfortrial";
    }

    Blob(String fileName) {
        File f = new File(fileName);                        // find the file
        if (f.exists()) {
            this.fileName = fileName;
            content = Utils.readContents(f);
            byte[] fn = fileName.getBytes();
            byte[] toHash = Main.combineArray(fn, content);
            SHA1ID = gitlet.Utils.sha1(toHash);
            isTracked = true;
        } else {
            System.out.println("File do not exist when initialize blob");
        }
    }
}
