/*Authorized by Xiaomeng Wu, David Zhao, Yiwei Zhu Jul/9/2018 */

package gitlet;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class Commit implements Serializable {  //consider as the staging area itself
    String SHA1ID; //the id itself
    String logMessage; //its log message
    String timeStamp; //its time stamp
    String parent;  //its parent's SHA1ID
    //    List myBlob = new ArrayList<String>(); //contains all the Blobs of the commit
    Map<String, String> myBlob = new HashMap<>();

    Commit(String log) {
        SHA1ID = null;
        logMessage = log;
    }

    public void setSHA1ID() {

        String aa = Main.serializeWriteCommit(this);
        SHA1ID = gitlet.Utils.sha1(aa);
    }

    public void setLogMessage(String m) {
        logMessage = m;
    }

    public void setParent(Commit papa) {
        parent = papa.SHA1ID;
    }

    public void setTime() {
        timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                .format(Calendar.getInstance().getTime());
    }

    public void addBlob(Blob b) {
        if (myBlob.containsKey(b.SHA1ID)) {
            return;
        }
        String toMove = new String();
        for (Object o : myBlob.keySet()) {
            if (b.fileName.equals(myBlob.get(o))) {
                toMove = (String) o;
            }
        }
        myBlob.remove(toMove);
        myBlob.put(b.SHA1ID, b.fileName);
        b.isTracked = true;
    }

    public void addBlob(Commit c) {                  // case for stagingArea pushing
        myBlob = c.myBlob;
    }
}
