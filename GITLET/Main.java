/*Authorized by Xiaomeng Wu, David Zhao, Yiwei Zhu Jul/9/2018*/

package gitlet;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;

/* Driver class for Gitlet, the tiny stupid version-control system.
   @author
*/
public class Main {
    /* Usage: java gitlet.Main ARGS, where ARGS contains
       <COMMAND> <OPERAND> .... */
    public static void main(String[] args) {
        myGitlet = new Gitlet();
        if (args[0].equals("init")) {
            myGitlet.init();
        }
        if (args[0].equals("add")) {
            myGitlet.add(args[1]);
        }
        if (args[0].equals("log") && args.length == 1) {
            myGitlet.log();
        }
        if (args.length == 1 && args[0].equals("global-log")) {
            myGitlet.globallog();
        }
        if (args[0].equals("commit")) {
            if (args.length == 1 || args[1].equals("")) {
                System.out.println("Please enter a commit message.");
                return;
            }
            String logMessage = new String();
            for (int i = 1; i < args.length - 1; ++i) {
                logMessage += args[i];
                logMessage += " ";
            }
            logMessage += args[args.length - 1];
            myGitlet.commit(logMessage);
        }

        if (args[0].equals("branch")) {
            myGitlet.branch(args[1]);
        }
        if (args.length == 1 && args[0].equals("status")) {
            myGitlet.status();
        }
        if (args.length == 2 && args[0].equals("rm-branch")) {
            myGitlet.rmbranch(args[1]);
        }
        if (args.length == 2 && args[0].equals("rm")) {
            myGitlet.rm(args[1]);
        }
        if (args.length == 2 && args[0].equals("reset")) {
            myGitlet.reset(args[1]);
        }
        if (args.length > 0 && args[0].equals("find")) {
            String findMessage = new String();
            if (args.length < 2) {
                return;
            } else {
                for (int i = 1; i < args.length - 1; ++i) {
                    findMessage += args[i];
                    findMessage += " ";
                }
                findMessage += args[args.length - 1];
                myGitlet.find(findMessage);
            }
        }
        if (args[0].equals("checkout")) {
            myCommitTree = serializeReadCommitTree();
            stagingArea = serializeReadStagingArea();
            if ((!args[1].equals("--")) && args.length == 2) {
                myGitlet.checkout(args[1]);
                return;
            }
            if (args.length == 3 && args[1].equals("--")) {
                myGitlet.checkout("--", args[2]);
            } else if (args.length == 4 && args[2].equals("--")) {
                myGitlet.checkout(args[1], "--", args[3]);
            } else {
                System.out.println("Incorrect operands.");
            }
        }
        if (args.length == 2 && args[0].equals("merge")) {
            myGitlet.merge(args[1]);
        }
    }

    //We have a Gitlet class instance here.
    public static Gitlet myGitlet;
    public static Commit stagingArea = new Commit("This is staging area!");
    public static CommitTree myCommitTree;


    //bunch of serialize and deserialze
    public static String serializeWriteCommit(Commit obj) {
        String fileName;
        byte[] one = obj.timeStamp.getBytes();
        byte[] two = obj.logMessage.getBytes();
        byte[] toHash;
        if (obj.parent != null) {
            byte[] three = obj.parent.getBytes();
            toHash = combineArray(one, combineArray(two, three));
        } else {
            toHash = combineArray(one, two);
        }
        String sha = Utils.sha1(toHash);
        obj.SHA1ID = sha;
        try {
            File f = new File(".gitlet/" + sha + ".sha");
            ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(f));
            out.writeObject(obj);
            out.close();
        } catch (IOException e) {
            System.out.println("IOException");
        }
        return sha;
    }

    public static Commit serializeReadCommit(String hashID) {
        String fileName = ".gitlet/" + hashID + ".sha";
        Commit obj;
        File f = new File(fileName);
        if (!f.exists()) {
            System.out.println("not exist");
            return null;
        }
        try {
            ObjectInputStream in = new ObjectInputStream(new FileInputStream(f));
            obj = (Commit) in.readObject();
            in.close();
            return obj;
        } catch (ClassNotFoundException e) {
            System.out.print("No Such File");
        } catch (IOException e) {
            System.out.println("IOexception");
        }
        return null;
    }

    public static void serializeWriteCommitTree(CommitTree obj) {
        File f = new File(".gitlet/CommitTree.cmt");
        try {
            ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(f));
            out.writeObject(obj);
            out.close();
        } catch (IOException e) {
            System.out.println("IOexception in serialize committree");
        }
    }

    public static CommitTree serializeReadCommitTree() {
        File f = new File(".gitlet/CommitTree.cmt");
        CommitTree obj;
        try {
            ObjectInputStream in = new ObjectInputStream(new FileInputStream(f));
            obj = (CommitTree) in.readObject();
            in.close();
            return obj;
        } catch (ClassNotFoundException e) {
            System.out.println("ClassNotFoundException in sarialize read committree");
        } catch (IOException e) {
            System.out.println("IOE in serialze read committree");
        }
        return null;
    }

    public static void serializeWriteStagingArea(Commit obj) {
        File f = new File(".gitlet/StagingArea.sta");
        try {
            ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(f));
            out.writeObject(obj);
            out.close();
        } catch (IOException e) {
            System.out.println("IOexception in serialize stagingarea");
        }
    }

    public static Commit serializeReadStagingArea() {
        File f = new File(".gitlet/StagingArea.sta");
        Commit obj;
        try {
            ObjectInputStream in = new ObjectInputStream(new FileInputStream(f));
            obj = (Commit) in.readObject();
            in.close();
            return obj;
        } catch (ClassNotFoundException e) {
            System.out.println("ClassNotFoundException in sarialize read staging area");
        } catch (IOException e) {
            System.out.println("IOE in serialze read staging area");
        }
        return null;
    }

    public static void serialzeWriteBlob(Blob obj) {
        String fileName = ".gitlet/" + obj.SHA1ID + ".sha";
        //System.out.println(fileName);

        File f = new File(fileName);
        try {
            ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(f));
            out.writeObject(obj);
            out.close();
        } catch (IOException e) {
            System.out.println("IOexception in serialize writeBlob");
        }
    }

    public static Blob serialzeReadBlob(String hashID) {
        if (hashID == null) {
            System.out.println("No input in serialzeReadBlob!");
            return null;
        }
        String fileName = ".gitlet/" + hashID + ".sha";
        File f = new File(fileName);
        Blob obj = new Blob();
        try {
            ObjectInputStream in = new ObjectInputStream(new FileInputStream(f));
            obj = (Blob) in.readObject();
            in.close();
            return obj;
        } catch (ClassNotFoundException e) {
            System.out.println("ClassNotFoundException in sarialize readBlob");
        } catch (IOException e) {
            System.out.println("IOE in serialze read blob");
        }
        return null;
    }

    public static byte[] combineArray(byte[] a, byte[] b) {
        byte[] c = new byte[a.length + b.length];
        System.arraycopy(a, 0, c, 0, a.length);
        System.arraycopy(b, 0, c, a.length, b.length);
        return c;
    }
}
