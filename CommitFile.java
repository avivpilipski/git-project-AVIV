import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

import mickeybutil.Terminate;

public class CommitFile {
    public String tree;
    public String parent;
    public String author;
    public String date;
    public String message;

    public CommitFile(String tree, String parent, String author, String message, String date) {
        this.tree = tree;
        this.parent = parent;
        this.author = author;
        this.date = date;
        this.message = message;
    }

    static CommitFile read(File file) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            var treeLine = reader.readLine();
            var parentLine = reader.readLine();
            var authorLine = reader.readLine();
            var dateLine = reader.readLine();
            var messageLine = reader.readLine();

            Terminate.assertEq(treeLine.substring(0, 5), "tree ");
            var tree = treeLine.substring(5);
            Terminate.assertEq(parentLine.substring(0, 7), "parent ");
            var parent = parentLine.substring(7);
            Terminate.assertEq(authorLine.substring(0, 7), "author ");
            var author = authorLine.substring(7);
            Terminate.assertEq(dateLine.substring(0, 5), "date ");
            var date = dateLine.substring(9);
            Terminate.assertEq(messageLine.substring(0, 8), "message ");
            var message = messageLine.substring(8);
            reader.close();
            return new CommitFile(tree, parent, author, message, date);
        } catch (Exception e) {
            Terminate.exception(e);
        }
        return null;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("tree " + tree + "\n");
        sb.append("parent " + parent + "\n");
        sb.append("author " + author + "\n");
        sb.append("date " + date + "\n");
        sb.append("message " + message + "\n");
        return sb.toString();
    }

    static void write(File file, CommitFile commit) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            writer.write(commit.toString());
            writer.close();
        } catch (Exception e) {
            Terminate.exception(e);
        }
    }
}
