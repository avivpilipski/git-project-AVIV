import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;

import mickeybutil.Terminate;
import mickeybutil.PathUtil;

public class Git implements GitInterface {
    // Dear Aviv,
    // ! once a directory is staged, anything within it should not be staged
    // ! this is a limitation of how blob is currently implemented
    public void stage(String filePath) {
        try {
            new Blob(filePath, false);
        } catch (IOException e) {
            throw new Error("Error");
        }
    }

    /**
     * Generates a tree file with all the root files in it
     */
    static String generateMegaTree() {
        try {
            File git = new File("git");
            File objects = new File(git, "objects");
            File index = new File(git, "index");

            // so basically we go through index and look for stuff without a slash
            BufferedReader reader = new BufferedReader(new FileReader(index));
            StringBuffer megaTree = new StringBuffer();
            while (true) {
                var line = reader.readLine();
                if (line == null)
                    break;
                var slashesNum = line.split("/").length - 1;
                if (slashesNum == 0)
                    megaTree.append(line + "\n");
            }
            final var sha = Blob.encryptThisString(megaTree.toString());

            File outFile = new File(objects, sha);
            BufferedWriter writer = new BufferedWriter(new FileWriter(outFile));

            // reads in the tree from the last commit
            var commitHash = Head.getHeadCommit();
            String commitTreeData = "";
            if (!commitHash.equals("")) {
                var commitFile = new File(objects, commitHash);
                var commitData = CommitFile.read(commitFile);
                var commitTree = new File(objects, commitData.tree);
                commitTreeData = Blob.readFileAsString(commitTree);
            }

            writer.write(megaTree.toString() + commitTreeData);

            reader.close();
            writer.close();

            return sha;
        } catch (IOException e) {
            Terminate.exception(e);
            return null;
        }
    }

    // Generate a root `tree` file and its inner `blob` files:

    // 1. Identify the **root tree** in the Working Directory—the topmost directory
    // that encompasses all other folders and files. The SHA1 hash of this `tree`
    // file will be referenced by the `commit` file created in step 2.

    // Recall that `tree` files contain references to their immediate child blobs
    // and subtrees. Thus, its corresponding SHA1 hash will be the hash of its
    // contents and be created only when all `files` within the directory have been
    // hashed.

    // 2. Save each `blob` file (including `trees`) in the `objects` directory with
    // its corresponding SHA1 hash as the filename. The last `blob` created should
    // be the **root** **`tree`**.
    public static void stageEverything() {
        File thisDir = new File("./");
        for (var file : thisDir.listFiles()) {
            try {
                System.out.println(file.getPath());
                if (!file.getPath().equals("./git") && !file.getPath().equals("./.git"))
                    new Blob(file.getPath().substring(2), false);
            } catch (Exception e) {
                Terminate.exception(e);
            }
        }
        generateMegaTree();
    }

    public String commit(String author, String message) {
        var tree = generateMegaTree();
        CommitFile commit = new CommitFile(tree, Head.getHeadCommit(), author, message, new Date().toString());
        var commitHash = Blob.encryptThisString(commit.toString());
        File git = new File("git");
        File objects = new File(git, "objects");
        File outFile = new File(objects, commitHash);
        Blob.writeFileAsString(outFile, commit.toString());
        Head.setHeadCommit(commitHash);
        var index = new File(git, "index");
        index.delete();
        try {
            index.createNewFile();
        } catch (IOException e) {
            Terminate.exception(e);
        }
        return commitHash;
    }

    private static void handleLine(String line, String path) throws IOException {
        String[] parts = line.split(" ");
        String type = parts[0];
        String hash = parts[1];
        String name = parts[2];
        if (type.equals("tree")) {
            File treeFile = new File("git/objects/" + hash);
            String treeData = Blob.readFileAsString(treeFile);
            String[] lines = treeData.split("\n");
            for (var l : lines) {
                handleLine(l, path + "/" + name);
            }
        } else if (type.equals("blob")) {
            File blobFile = new File("git/objects/" + hash);
            String blobData = Blob.readFileAsString(blobFile);
            File outFile = new File(PathUtil.removeLeadingSlash(path + "/" + name));
            Blob.writeFileAsString(outFile, blobData);
        }
    }

    public void checkout(String hash) {
        for (var file : new File("./").listFiles()) {
            if (!file.getPath().equals("./git") && !file.getPath().equals("./.git"))
                if (file.isDirectory())
                    deleteDirectory(file);
                else
                    file.delete();
        }
        try {
            File git = new File("git");
            File objects = new File(git, "objects");
            File commitFile = new File(objects, hash);
            CommitFile commit = CommitFile.read(commitFile);
            File treeFile = new File(objects, commit.tree);
            String treeData = Blob.readFileAsString(treeFile);
            String[] lines = treeData.split("\n");
            for (var line : lines) {
                handleLine(line, "");
            }
        } catch (Exception e) {
            Terminate.exception(e);
        }
    }
    // Lovingly,
    // Michael Barr

    // public static void main(String[] args) throws IOException {
    // // test initialization of git repo and deletes it (then recreates it for
    // further
    // // testing)
    // initGit();
    // checkInitGit();
    // initGit();

    // // creates two blobs with unique file names and data
    // File newFile = new File("newFile.txt");
    // newFile.createNewFile();
    // BufferedWriter bw = new BufferedWriter(new FileWriter(newFile));
    // bw.write("sfodvunaoernoienv");
    // bw.close();
    // Blob blob = new Blob("newFile.txt", false);
    // File newFile2 = new File("newFile2.txt");
    // newFile.createNewFile();
    // BufferedWriter bw2 = new BufferedWriter(new FileWriter(newFile2));
    // bw2.write("sodvunaoernoien");
    // bw2.close();
    // Blob blob2 = new Blob("newFile2.txt", false);

    // // verifies that the location of a blob is correct (git/objects)
    // String blobName = blob.getBlobName();
    // String desiredPath = "git/objects/" + blobName;
    // File b = new File(desiredPath);
    // if (b.exists() && b.isFile())
    // System.out.println("Blob exists in the correct path.");

    // // tests that the blob has correct hash and file contents- for compressed and
    // // non-compressed files
    // String correctHash = "";
    // if (blob.isCompressed()) {
    // correctHash = "ddd8ff69c3b86f4f8a3efcb1387a5787f464a26f";
    // if (blob.getBlobName().equals(correctHash)) {
    // System.out.println("blob has correct hash/name.");
    // }
    // } else {
    // correctHash = "aaa8b870cdae19a38f9ad6f328dbaaf31ad38965";
    // if (blob.getBlobName().equals(correctHash)) {
    // System.out.println("blob has correct hash/name.");
    // String BlobContents = blob.readFileAsString(b);
    // String FileContents = blob.readFileAsString(newFile);
    // if (BlobContents.equals(FileContents)) {
    // System.out.println("blob has correct contents.");
    // }
    // }
    // }
    // // verifies that the index was updated correctly
    // String indexContents = blob.readFileAsString(new File("git/index"));
    // if (indexContents.equals(blobName + " newFile.txt" + "\n" +
    // blob2.getBlobName() + " newFile2.txt" + "\n")) {
    // System.out.println("index was updated correctly");
    // }

    // new Blob("dr", false);
    // }

    public static void initGit() throws IOException {
        File git = new File("git");
        File objects = new File(git, "objects");
        File index = new File(git, "index");

        if (git.exists() && objects.exists() && index.exists()) {
            System.out.println("Git Repository already exists");
        }

        if (!git.exists()) {
            git.mkdir();
        }

        if (!objects.exists()) {
            objects.mkdirs();
        }

        if (!index.exists()) {
            index.createNewFile();
        }

        // Dear Aviv,
        File head = new File(git, "HEAD");
        if (!head.exists())
            head.createNewFile();
        // Squigglingly,
        // Michael Barr
    }

    public static void checkInitGit() {
        Path indexPath = Paths.get("git/index");
        if (Files.exists(indexPath)) {
            System.out.println("index file exists.");
        }

        Path objectsPath = Paths.get("git/objects");
        if (Files.exists(objectsPath)) {
            System.out.println("objects directory exists.");
        }

        Path gitPath = Paths.get("git");
        if (Files.exists(gitPath)) {
            System.out.println("git directory exists.");
            File gitFile = new File("git");
            if (deleteDirectory(gitFile)) {
                System.out.println("git directory removed.");
            }
        }
    }

    // recursively deletes a directory
    public static boolean deleteDirectory(File directory) {
        if (directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files.length > 0) {
                for (int i = 0; i < files.length; i++) {
                    deleteDirectory(files[i]);
                }
            }
        }
        return directory.delete();
    }

}