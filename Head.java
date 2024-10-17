import java.io.IOException;
import java.io.File;

public class Head {
    static String getHeadCommit() {
        try {
            File head = new File("git/HEAD");
            if (!head.exists()) {
                return null;
            }
            return Blob.readFileAsString(head);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    static void setHeadCommit(String commit) {
        try {
            File head = new File("git/HEAD");
            if (!head.exists()) {
                head.createNewFile();
            }
            Blob.writeFileAsString(head, commit);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
