/*Authorized by Xiaomeng Wu, David Zhao, Yiwei Zhu Jul/9/2018 */

package gitlet;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;
import java.util.Map;
import java.util.HashSet;
import java.util.Collections;

public class Gitlet {
    //Gitlet implements all the method used in gitlet

    //the init() method
    /*Creates a new gitlet version-control system in the current directory.
    This system will automatically start with one commit: a commit that
    contains no files and has the commit message initial commit. It will
    have a single branch: master, which initially points to this initial
    commit, and master will be the current branch.*/
    void init() {
        File f = new File(".gitlet");
        if (f.exists()) {
            System.out.println("A gitlet version-control system already exists "
                    + "in the current directory.");
            return;
        } else {
            f.mkdir();
            //create a commit... branch master as asked above.
            CommitTree myCommitTree = new CommitTree();
            Main.serializeWriteCommitTree(myCommitTree);
            Main.stagingArea.parent = null;
            Main.serializeWriteStagingArea(Main.stagingArea);
        }
    }

    //delete() delete the .gitlet folder just for debugging
    void delete() {
        File f = new File(".gitlet");
        f.delete();
    }

    public void add(String fileName) {

        Main.stagingArea = Main.serializeReadStagingArea();
        Main.myCommitTree = Main.serializeReadCommitTree();
        Main.myCommitTree.removedFile.remove(fileName);
        File f = new File(fileName);
        if (!f.exists()) {
            System.out.println("File does not exist.");
            return;
        }
        Blob blobToAdd = new Blob(fileName);
        Main.serialzeWriteBlob(blobToAdd);
        Main.stagingArea.addBlob(blobToAdd);
        Main.serializeWriteStagingArea(Main.stagingArea);
        Main.serializeWriteCommitTree(Main.myCommitTree);
    }

    public void commit(String logName) {
        Main.myCommitTree = Main.serializeReadCommitTree();
        Main.stagingArea = Main.serializeReadStagingArea();
        boolean change = false;

        Commit newCommit = new Commit(logName);
        //newCommit.setSHA1ID();

        Commit tempStagingArea;
        tempStagingArea = Main.serializeReadStagingArea();

        boolean isStaged = false;
        Commit current = Main.serializeReadCommit(Main.myCommitTree.head);
        for (Object id : tempStagingArea.myBlob.keySet()) {
            if (!current.myBlob.keySet().contains(id)) {
                isStaged = true;
                break;
            }
        }
        if (current.myBlob.keySet().size() != Main.stagingArea.myBlob.keySet().size()) {
            isStaged = true;
        }
        if (!isStaged) {
            System.out.println("No changes added to the commit.");
            return;
        }
        CommitTree usableCommitTree = Main.serializeReadCommitTree();
        Commit previousCommit = Main.serializeReadCommit(usableCommitTree.head);

        for (String o : tempStagingArea.myBlob.keySet()) {
            newCommit.myBlob.put(o, tempStagingArea.myBlob.get(o));
        }
        for (Object s : newCommit.myBlob.keySet()) {
            if (!previousCommit.myBlob.containsKey(s)) {
                change = true;
                break;
            }
        }
        for (Object s : previousCommit.myBlob.keySet()) {
            if (!newCommit.myBlob.containsKey(s)) {
                change = true;
                break;
            }
        }
        if (change) {
            newCommit.parent = usableCommitTree.head;
            newCommit.setTime();
            //newCommit.setSHA1ID();

            usableCommitTree.head = Main.serializeWriteCommit(newCommit);
            //System.out.println(usableCommitTree.head);
            int index = usableCommitTree.find(usableCommitTree.currentBranch);
            usableCommitTree.allTheBranchesHash.set(index, usableCommitTree.head);

            usableCommitTree.allTheCommit.add(usableCommitTree.head);

        } else {
            return;
        }

        usableCommitTree.removedFile.clear();
        Main.serializeWriteStagingArea(tempStagingArea);
        Main.serializeWriteCommitTree(usableCommitTree);
    }


    public void checkout(String branch) {
        boolean isBranch = false;
        Commit currentCommit = Main.serializeReadCommit(Main.myCommitTree.head);
        Commit checkoutCommit;
        String branchID = new String();

        for (String branchName : Main.myCommitTree.allTheBranchesName) {
            if (branchName.equals(branch)) {
                isBranch = true;
                int index = Main.myCommitTree.allTheBranchesName.indexOf(branchName);
                branchID = Main.myCommitTree.allTheBranchesHash.get(index);
                break;
            }
        }
        if (!isBranch) {
            System.out.println("No such branch exists.");
            return;
        }
        if (branch.equals(Main.myCommitTree.currentBranch)) {
            System.out.println("No need to checkout the current branch.");
            return;
        }

        checkoutCommit = Main.serializeReadCommit(branchID);
        if (checkoutCommit == null) {
            System.out.println("checkOutCommitIsNull!");
            return;
        }

        for (Object a : checkoutCommit.myBlob.keySet()) {
            String name = (String) checkoutCommit.myBlob.get(a);
            File f = new File(name);
            if (f.exists() && !currentCommit.myBlob.containsValue(name)) {
                System.out.println("There is an untracked file in the way; "
                        + "delete it or add it first.");
                return;
            }
        }
        for (Object a : checkoutCommit.myBlob.keySet()) {
            if (!(a instanceof String)) {
                System.out.println("Wrong data type!");
                return;
            }
            Blob b;
            b = Main.serialzeReadBlob((String) a);                 // recover to old version
            byte[] newVersion = b.content;
            File newFile = new File(b.fileName);
            if (!newFile.exists()) {
                try {
                    newFile.createNewFile();
                } catch (IOException e) {
                    System.out.println("IOException when tryting to create file in checkout3");
                }
            }
            Utils.writeContents(newFile, newVersion);
        }
        for (Object b : currentCommit.myBlob.keySet()) {
            if (!checkoutCommit.myBlob.containsValue(currentCommit.myBlob.get(b))) {
                String fileName = (String) currentCommit.myBlob.get(b);
                //System.out.println("I want to delete "+fileName);
                File newFile = new File(fileName);
                newFile.delete();
            }
        }
        Main.stagingArea.myBlob = checkoutCommit.myBlob;
        Main.myCommitTree.currentBranch = branch;
        Main.myCommitTree.head = branchID;

        Main.serializeWriteCommitTree(Main.myCommitTree);
        Main.serializeWriteStagingArea(Main.stagingArea);
    }


    public void checkout(String twoDash, String filename) {
        // leads to checkout(current, two-dash, filename)

        checkout(Main.myCommitTree.head, "--", filename);     // unchecked files
    }


    public void checkout(String commitID, String twoDash, String filename) {
        Main.stagingArea = Main.serializeReadStagingArea();
        Main.myCommitTree = Main.serializeReadCommitTree();
        if (!twoDash.equals("--")) {
            System.out.println("cannot recognize command");
            return;
        }
        if (commitID.length() < 40) {
            for (String id : Main.myCommitTree.allTheCommit) {
                String toCompare = id.substring(0, commitID.length());
                if (toCompare.equals(commitID)) {
                    checkout(id, "--", filename);
                }
            }
            return;
        }
        File newFile = new File(filename);
        byte[] newContent = null;

        String commitName = ".gitlet/" + commitID + ".sha";
        File commitFile = new File(commitName);

        if (!commitFile.exists()) {
            System.out.println("No commit with that id exists.");
            return;
        }
        Commit commitLookingFor = Main.serializeReadCommit(commitID);
        for (Object blobLookingFor : commitLookingFor.myBlob.keySet()) {
            if (commitLookingFor.myBlob.get(blobLookingFor).equals(filename)) {
                File currentFile = new File(filename);
                newContent = (Main.serialzeReadBlob((String) blobLookingFor)).content;
                Utils.writeContents(currentFile, newContent);
                break;
            }
        }
        if (newContent == null) {
            System.out.println("File does not exist in that commit.");
            // get the content we need
            return;
        }


        if (newFile.exists()) {
            Utils.writeContents(newFile, newContent);
            //write content in existing file
        } else {
            try {
                if (newFile.createNewFile()) {
                    Utils.writeContents(newFile, newContent);
                    //write content into new file
                }
            } catch (IOException ex) {
                System.out.println(ex);
            }
        }
        Main.serializeWriteCommitTree(Main.myCommitTree);
        Main.serializeWriteStagingArea(Main.stagingArea);
    }

    public void branch(String newBranchName) {
        Main.myCommitTree = Main.serializeReadCommitTree();
        if (Main.myCommitTree.allTheBranchesName.contains(newBranchName)) {
            System.out.println("A branch with that name already exists.");
            return;
        }
        Main.myCommitTree.allTheBranchesName.add(newBranchName);
        Main.myCommitTree.allTheBranchesHash.add(Main.myCommitTree.head);
        Main.serializeWriteCommitTree(Main.myCommitTree);
    }

    //totest
    public void rm(String filenametoRemove) {
        Commit stagingArea = Main.serializeReadStagingArea();
        CommitTree usableCommitTree = Main.serializeReadCommitTree();
        Commit head = Main.serializeReadCommit(usableCommitTree.head);
        String blobtoRemoveID = null;
        boolean isTracked = false;
        boolean isStaged = false;
        //whether it is tracked
        for (Object id : head.myBlob.keySet()) {
            if (head.myBlob.get(id).equals(filenametoRemove)) {
                isTracked = true;
            }
        }
        //whether it is staged
        for (Object id : stagingArea.myBlob.keySet()) {
            if (stagingArea.myBlob.get(id).equals(filenametoRemove)) {
                //Not sure about this casting
                blobtoRemoveID = (String) id;
                isStaged = true;
            }
        }
        //Unstage
        if (isStaged) {
            if (stagingArea.myBlob.containsKey(blobtoRemoveID)) {
                stagingArea.myBlob.remove(blobtoRemoveID, stagingArea.myBlob.get(blobtoRemoveID));
            }
        }
        //Delete the file from the working directory
        if (isTracked) {
            File f = new File(filenametoRemove);
            Utils.restrictedDelete(f);
            //Mark the file to be untracked by the next commit.
            usableCommitTree.removedFile.add(filenametoRemove);
        }
        if (!isStaged && !isTracked) {
            System.out.println("No reason to remove the file.");
        }
        Main.serializeWriteStagingArea(stagingArea);
        Main.serializeWriteCommitTree(usableCommitTree);
    }

    //David added two lines here
    public void log() {
        CommitTree usableCommitTree = Main.serializeReadCommitTree();
        Commit cc = Main.serializeReadCommit(usableCommitTree.head);
        printCommit(cc);
        while (cc.parent != null) {
            cc = Main.serializeReadCommit(cc.parent);
            printCommit(cc);
        }
    }

    public void globallog() {
        CommitTree usableCommitTree = Main.serializeReadCommitTree();
        for (String s : usableCommitTree.allTheCommit) {
            printCommit(Main.serializeReadCommit(s));
        }
    }

    private void printCommit(Commit cc) {
        System.out.println("===");
        System.out.println("Commit " + cc.SHA1ID);
        System.out.println(cc.timeStamp);
        System.out.println(cc.logMessage);
        System.out.println();
    }

    //passed
    public void find(String message) {
        CommitTree usableCommitTree = Main.serializeReadCommitTree();
        boolean isEmpty = true;
        for (String s : usableCommitTree.allTheCommit) {
            if (Main.serializeReadCommit(s).logMessage.equals(message)) {
                System.out.println(Main.serializeReadCommit(s).SHA1ID);
                //printCommit(Main.serializeReadCommit(s));
                isEmpty = false;
            }
        }
        if (isEmpty) {
            System.out.println("Found no commit with that message.");
        }
    }

    public void status() {
        CommitTree usableCommitTree = Main.serializeReadCommitTree();
        Commit tempStagingArea = Main.serializeReadStagingArea();

        System.out.println("=== Branches ===");
        ArrayList<String> allBranchesCopy = usableCommitTree.allTheBranchesName;
        Collections.sort(allBranchesCopy); //sort the list
        for (String s : allBranchesCopy) {
            if (s.equals(usableCommitTree.currentBranch)) { //if it's current
                s = "*" + s;
            }
            System.out.println(s);
        }
        System.out.println();

        System.out.println("=== Staged Files ===");
        Commit previous = Main.serializeReadCommit(usableCommitTree.head);
        ArrayList<String> arList = new ArrayList<String>();
        //NOT SURE IF THIS CAST WILL CAUSE PROBLEM
        for (Object key : tempStagingArea.myBlob.keySet()) {
            //put all values into arList (to sort)
            //WILL THIS CASTING CAUSE AN ERROR?
            if (!previous.myBlob.keySet().contains(key)) {
                arList.add((String) tempStagingArea.myBlob.get(key));
            }
        }
        Collections.sort(arList);
        for (String s : arList) {
            System.out.println(s);
        }
        System.out.println();

        System.out.println("=== Removed Files ===");
        Collections.sort(usableCommitTree.removedFile);
        for (String s : usableCommitTree.removedFile) {
            System.out.println(s);
        }
        System.out.println();
        System.out.println("=== Modifications Not Staged For Commit ===");
        System.out.println();
        System.out.println("=== Untracked Files ===");
    }

    //passed
    public void rmbranch(String branchtoRemove) {
        CommitTree usableCommitTree = Main.serializeReadCommitTree();
        boolean hasBranch = usableCommitTree.allTheBranchesName.contains(branchtoRemove);
        if (!hasBranch) {
            System.out.println("A branch with that name does not exist.");
            return;
        }
        if (usableCommitTree.currentBranch.equals(branchtoRemove)) {
            System.out.println("Cannot remove the current branch.");
            return;
        }
        int index = usableCommitTree.find(branchtoRemove);
        usableCommitTree.allTheBranchesName.remove(index);
        usableCommitTree.allTheBranchesHash.remove(index);
        Main.serializeWriteCommitTree(usableCommitTree);
    }

    public void reset(String commitID) {
        Main.myCommitTree = Main.serializeReadCommitTree();
        Main.stagingArea = Main.serializeReadStagingArea();
        String fileName = ".gitlet/" + commitID + ".sha";
        File commitToReset = new File(fileName);
        if (!commitToReset.exists()) {
            System.out.println("No commit with that id exists.");
            return;
        }
        Commit resetCommit = Main.serializeReadCommit(commitID);
        Commit headCommit = Main.serializeReadCommit(Main.myCommitTree.head);
        for (Object blobHash : resetCommit.myBlob.keySet()) {
            Object thisBlobName = resetCommit.myBlob.get(blobHash);
            File f = new File((String) thisBlobName);
            if (f.exists() && resetCommit.myBlob.containsValue(thisBlobName)
                    && !headCommit.myBlob.containsValue(thisBlobName)
                    && !Main.stagingArea.myBlob.containsValue(thisBlobName)) {
                System.out.println("There is an untracked file in the way; "
                        + "delete it or add it first.");
                return;
            }
            //checkout(commitID,"--",(String)thisBlobName);
        }
        //delete yi bo
        for (Object o : headCommit.myBlob.keySet()) {
            Object name = headCommit.myBlob.get(o);
            File f = new File((String) name);
            f.delete();
        }

        for (Object blobHash : resetCommit.myBlob.keySet()) {
            Object thisBlobName = resetCommit.myBlob.get(blobHash);
            checkout(commitID, "--", (String) thisBlobName);
        }


        Main.stagingArea.myBlob = resetCommit.myBlob;
        Main.myCommitTree.head = commitID;
        int ind = Main.myCommitTree.allTheBranchesName
                .indexOf(Main.myCommitTree.currentBranch);
        Main.myCommitTree.allTheBranchesHash.set(ind, commitID);
        Main.serializeWriteStagingArea(Main.stagingArea);
        Main.serializeWriteCommitTree(Main.myCommitTree);
    }
    //Some functions:
    //When conflicting, replace the file's content with a mixed
    private void conflictCheckout(String gHashID, String cHashID, String filename) {
        byte[] a = "<<<<<<< HEAD\n".getBytes();
        byte[] b = new byte[0];
        if (!cHashID.equals("empty")) {
            b = Main.serialzeReadBlob(cHashID).content;
        }
        byte[] c = "=======\n".getBytes();
        byte[] d = new byte[0];
        if (!gHashID.equals("empty")) {
            d = Main.serialzeReadBlob(gHashID).content;
        }
        byte[] e = ">>>>>>>\n".getBytes();
        byte[] byteContent = combineArray(combineArray(combineArray(a, b),
                combineArray(c, d)), e);
        File newFile = new File(filename);
        Utils.writeContents(newFile, byteContent);
    }
    private String getKeyFromValue(Map<String, String> testMap, String value) {
        for (Map.Entry<String, String> entry : testMap.entrySet()) {
            if (entry.getValue().equals(value)) {
                return entry.getKey();
            }
        }
        System.out.println("Does not have this file");
        return null;
    }
    private byte[] combineArray(byte[] a, byte[] b) {
        byte[] c = new byte[a.length + b.length];
        System.arraycopy(a, 0, c, 0, a.length);
        System.arraycopy(b, 0, c, a.length, b.length);
        return c;
    }


    public void merge(String branchName) {
        CommitTree usableCommitTree = Main.serializeReadCommitTree();
        Commit tempStagingArea = Main.serializeReadStagingArea();
        Commit previous = Main.serializeReadCommit(usableCommitTree.head);
        ArrayList<String> arList = new ArrayList<>();
        for (String key : tempStagingArea.myBlob.keySet()) {
            if (!previous.myBlob.keySet().contains(key)) {
                arList.add(tempStagingArea.myBlob.get(key));
            }
        }
        if (!arList.isEmpty() || !usableCommitTree.removedFile.isEmpty()) {
            System.out.println("You have uncommitted changes.");
            return;
        }
        if (!usableCommitTree.allTheBranchesName.contains(branchName)) {
            System.out.println("A branch with that name does not exist.");
            return;
        }
        if (usableCommitTree.currentBranch.equals(branchName)) {
            System.out.println("Cannot merge a branch with itself.");
            return;
        }
        String cBranchID = usableCommitTree.allTheBranchesHash
                .get(usableCommitTree.find(usableCommitTree.currentBranch));
        Commit currentBranch = Main.serializeReadCommit(cBranchID);
        String gBranchID = usableCommitTree.allTheBranchesHash
                .get(usableCommitTree.find(branchName));
        Commit givenBranch = Main.serializeReadCommit(gBranchID);
        Set<String> commitSet = new HashSet<>();
        String tempID = givenBranch.SHA1ID;
        while (tempID != null) {
            commitSet.add(tempID);
            tempID = Main.serializeReadCommit(tempID).parent;
        }
        tempID = currentBranch.SHA1ID;
        while (tempID != null && commitSet.add(tempID)) {
            tempID = Main.serializeReadCommit(tempID).parent;
        }
        if (tempID == null) {
            System.out.println("No split point!");
            return;
        }
        Commit splitPoint = Main.serializeReadCommit(tempID);
        if (splitPoint.SHA1ID.equals(givenBranch.SHA1ID)) {
            System.out.println("Given branch is an ancestor of the current branch.");
            return;
        }
        if (splitPoint.SHA1ID.equals(currentBranch.SHA1ID)) {
            int index = usableCommitTree.find(usableCommitTree.currentBranch);
            usableCommitTree.allTheBranchesHash.set(index, givenBranch.SHA1ID);
            Main.serializeWriteCommitTree(usableCommitTree);
            System.out.println("Current branch fast-forwarded.");
            return;
        }
        Set<String> sgcAllFiles = new HashSet<>();
        for (Map.Entry<String, String> entry : splitPoint.myBlob.entrySet()) {
            sgcAllFiles.add(entry.getValue());
        }
        for (Map.Entry<String, String> entry : givenBranch.myBlob.entrySet()) {
            sgcAllFiles.add(entry.getValue());
        }
        for (Map.Entry<String, String> entry : currentBranch.myBlob.entrySet()) {
            sgcAllFiles.add(entry.getValue());
        }
        String output = mergeOutput(sgcAllFiles, splitPoint, givenBranch, currentBranch);
        if (output.equals("NoConflict")) {
            Main.myGitlet.commit("Merged " + usableCommitTree.currentBranch
                    + " with " + branchName + ".");
        } else if (output.equals("Conflict"))  {
            System.out.println("Encountered a merge conflict.");
        }

    }


    private String mergeOutput(Set<String> sgcAllFiles, Commit splitPoint,
                             Commit givenBranch, Commit currentBranch) {
        boolean hasConflict = false;
        for (String s : sgcAllFiles) {
            if (splitPoint.myBlob.containsValue(s) && givenBranch.myBlob
                    .containsValue(s) && currentBranch.myBlob.containsValue(s)) {
                if (getKeyFromValue(splitPoint.myBlob, s)
                        .equals(getKeyFromValue(currentBranch.myBlob, s))) {
                    if (!getKeyFromValue(splitPoint.myBlob, s)
                            .equals(getKeyFromValue(givenBranch.myBlob, s))) {
                        Main.myGitlet.checkout(givenBranch.SHA1ID, "--", s);
                        Main.myGitlet.add(s);
                        continue;
                    }
                }
                if (!getKeyFromValue(splitPoint.myBlob, s)
                        .equals(getKeyFromValue(currentBranch.myBlob, s))) {
                    if (!getKeyFromValue(splitPoint.myBlob, s)
                            .equals(getKeyFromValue(givenBranch.myBlob, s))) {
                        conflictCheckout(getKeyFromValue(givenBranch.myBlob, s),
                                getKeyFromValue(currentBranch.myBlob, s), s);
                        hasConflict = true;
                    }
                }
                continue;
            }
            if (splitPoint.myBlob.containsValue(s) && givenBranch.myBlob.containsValue(s)
                    && !currentBranch.myBlob.containsValue(s)) {
                if (!getKeyFromValue(splitPoint.myBlob, s)
                        .equals(getKeyFromValue(givenBranch.myBlob, s))) {
                    conflictCheckout(getKeyFromValue(givenBranch.myBlob, s), "empty", s);
                    hasConflict = true;
                    continue;
                }
            }
            if (splitPoint.myBlob.containsValue(s) && !givenBranch.myBlob.containsValue(s)
                    && currentBranch.myBlob.containsValue(s)) {
                if (getKeyFromValue(splitPoint.myBlob, s)
                        .equals(getKeyFromValue(currentBranch.myBlob, s))) {
                    Main.myGitlet.rm(s);
                    continue;
                } else {
                    conflictCheckout("empty", getKeyFromValue(currentBranch.myBlob, s), s);
                    hasConflict = true;
                    continue;
                }
            }
            if (!splitPoint.myBlob.containsValue(s) && givenBranch.myBlob.containsValue(s)
                    && currentBranch.myBlob.containsValue(s)) {
                if (!getKeyFromValue(givenBranch.myBlob, s)
                        .equals(getKeyFromValue(currentBranch.myBlob, s))) {
                    conflictCheckout(getKeyFromValue(givenBranch.myBlob, s),
                            getKeyFromValue(currentBranch.myBlob, s), s);
                    hasConflict = true;
                    continue;
                }
                continue;
            }
            if (!splitPoint.myBlob.containsValue(s) && givenBranch.myBlob.containsValue(s)
                    && !currentBranch.myBlob.containsValue(s)) {
                File f = new File(s);
                if (f.exists()) {
                    System.out.println("There is an untracked file in the way;"
                            + " delete it or add it first.");
                    return "Error";
                }
                Main.myGitlet.checkout(givenBranch.SHA1ID, "--", s);
                Main.myGitlet.add(s);
            }
        }
        if (hasConflict) {
            return "Conflict";
        } else {
            return "NoConflict";
        }
    }
}
