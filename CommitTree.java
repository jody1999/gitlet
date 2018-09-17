/*Authorized by Xiaomeng Wu, David Zhao, Yiwei Zhu Jul/9/2018 */

package gitlet;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class CommitTree implements Serializable {
    ArrayList<String> allTheBranchesName; //master included
    ArrayList<String> allTheBranchesHash;
    String head;
    String currentBranch;
    Set<String> allTheCommit = new HashSet<>();
    ArrayList<String> removedFile = new ArrayList<>();

    CommitTree() {
        allTheBranchesHash = new ArrayList<>();
        allTheBranchesName = new ArrayList<>();
        Commit master = new Commit("initial commit");
        master.parent = null;
        master.setTime();
        allTheBranchesName.add("master");
        currentBranch = "master";
        String toAdd = Main.serializeWriteCommit(master);
        allTheBranchesHash.add(toAdd);
        head = allTheBranchesHash.get(find("master"));
        allTheCommit.add(toAdd);
    }

    public int find(String name) {
        for (int i = 0; i < allTheBranchesName.size(); ++i) {
            if (allTheBranchesName.get(i).equals(name)) {
                return i;
            }
        }
        return -1;
    }
}
