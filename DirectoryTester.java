import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class DirectoryTester {
    public static void main(String[] args) throws IOException {
        Git.deleteDirectory(new File("./git"));
        Git.initGit();
        Git.stageEverything();
        var git = new Git();
        var commit = git.commit("me", "test commit");
        git.checkout(commit);
    }

    // Helper method to create a file with specified content
    private static void createFile(File file, String content) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(content);
        } catch (IOException e) {
            System.err.println("Failed to create file: " + file.getAbsolutePath());
        }
    }
}
