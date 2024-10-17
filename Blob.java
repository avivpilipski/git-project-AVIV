import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.zip.GZIPOutputStream;

import mickeybutil.Terminate;

public class Blob {
    private String blobName;
    private boolean isBlob;
    private boolean isTree;
    private static boolean compressionAuthorization = false;

    public Blob(String fileName, boolean compressionAuthorization) throws IOException {
        if (fileName.substring(0, 2).equals("./"))
            throw new Error("./ is not allowed");

        Blob.compressionAuthorization = compressionAuthorization;

        // Check if the git/objects directory exists
        if (!Files.exists(Paths.get("git/objects"))) {
            throw new FileNotFoundException("No git or objects directories");
        }

        File ogFile = new File(fileName);

        // Check if the file exists
        if (!ogFile.exists()) {
            throw new FileNotFoundException("File does not exist: " + fileName);
        }

        // Determine if the input is a file or a directory
        if (ogFile.isFile()) {
            isBlob = true;
            // Process the file as a blob...
            String fileContents = readFileAsString(ogFile);
            if (compressionAuthorization) {
                try {
                    String str = new String(compress(fileContents), StandardCharsets.UTF_8);
                    fileContents = str;
                } catch (IOException e) {
                    System.err.println("Compression failed, proceeding with uncompressed data.");
                }
            }

            // Create a SHA1 hash and save the file in the objects directory
            String blobSHA1 = newFileName(fileContents); // Get SHA1 based on file content
            File backupFile = new File("git/objects", blobSHA1);
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(backupFile))) {
                bw.write(fileContents);
            }

            // Store original fileName and SHA1 in the index file
            String str = "blob " + backupFile.getName() + " " + ogFile.getName() + "\n";
            try (BufferedWriter bw2 = new BufferedWriter(new FileWriter("git/index", true))) {
                bw2.append(str);
            }

            blobName = backupFile.getName(); // Set the blobName to the SHA1 of the blob

        }

        else if (ogFile.isDirectory()) {
            isTree = true;
            StringBuilder treeContent = new StringBuilder();
            StringBuilder indexContent = new StringBuilder();

            // Iterate over the contents of the directory
            for (File file : ogFile.listFiles()) {
                if (file.isFile()) {
                    String fileContents = readFileAsString(file);
                    String createdHash = encryptThisString(fileContents);
                    File objectFile = new File("git/objects", createdHash);
                    BufferedWriter bw = new BufferedWriter(new FileWriter(objectFile));
                    bw.write(fileContents);
                    bw.close();
                    treeContent.append("blob ").append(createdHash).append(" ")
                            .append(file.getName())
                            .append("\n");
                    indexContent.append("blob ").append(createdHash).append(" ")
                            .append(file.getPath())
                            .append("\n");
                } else if (file.isDirectory()) {
                    // Recursively create a tree for each subdirectory
                    String[] dirContents = file.list();
                    String toHash = "";
                    for (String c : dirContents) {
                        toHash += c;
                    }
                    String newObjectSHA1 = encryptThisString(toHash);
                    File objectFile = new File("git/objects", newObjectSHA1);
                    try (BufferedWriter tempBR = new BufferedWriter(new FileWriter(objectFile))) {
                        tempBR.write(toHash);
                    }
                    Blob subTree = new Blob(file.getPath(), compressionAuthorization);
                    String treeSHA1 = subTree.getBlobName(); // Get the tree's SHA1
                    treeContent.append("tree ").append(treeSHA1).append(" ")
                            .append(file.getName())
                            .append("\n");
                }
            }

            // Save the tree content to a file
            String treeSHA1 = encryptThisString(treeContent.toString());
            File treeFile = new File("git/objects", treeSHA1);
            try (BufferedWriter treeWriter = new BufferedWriter(new FileWriter(treeFile))) {
                treeWriter.write(treeContent.toString());
            }

            // Add the tree to the index
            try (BufferedWriter indexWriter = new BufferedWriter(new FileWriter("git/index", true))) {
                // writes all the blobs in the tree to index
                indexWriter.append(indexContent);
                // adds the current tree to index
                indexWriter.append("tree " + treeSHA1 + " " + fileName + "\n");
                // all the other trees are written to index when `new Blob`ed
            }

            blobName = treeSHA1; // Set the blobName to the SHA1 of the tree

        }

        else {
            throw new IOException("The specified path is neither a file nor a directory: " + fileName);
        }
    }

    public String getBlobName() {
        return blobName;
    }

    public static byte[] compress(String data) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream(data.length());
        GZIPOutputStream gzip = new GZIPOutputStream(bos);
        gzip.write(data.getBytes());
        gzip.close();
        byte[] compressed = bos.toByteArray();
        bos.close();
        return compressed;
    }

    public boolean isCompressed() {
        return compressionAuthorization;
    }

    public String newFileName(String str) throws IOException {
        // encrypts a String using the SHA1 function
        String SHA1 = encryptThisString(str);
        return SHA1;
    }

    public static String encryptThisString(String input) {
        try {
            // getInstance() method is called with algorithm SHA-1
            MessageDigest md = MessageDigest.getInstance("SHA-1");

            // digest() method is called
            // to calculate message digest of the input string
            // returned as array of byte
            byte[] messageDigest = md.digest(input.getBytes(StandardCharsets.UTF_8));

            // Convert byte array into signum representation
            BigInteger no = new BigInteger(1, messageDigest);

            // Convert message digest into hex value
            String hashtext = no.toString(16);

            // Add preceding 0s to make it 40 digits long
            while (hashtext.length() < 40) {
                hashtext = "0" + hashtext;
            }

            // return the HashText
            return hashtext;
        }

        // For specifying wrong message digest algorithms
        catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public static String readFileAsString(File file) throws IOException {
        try {
            Path path = file.toPath();
            return new String(Files.readAllBytes(path));
        } catch (IOException e) {
            System.err.println("Failed to read file: " + file.getPath());
            throw e;
        }
    }

    public static void writeFileAsString(File file, String data) {
        try {
            Path path = file.toPath();
            var parent = file.getParentFile();
            if (parent != null && !parent.exists())
                parent.mkdirs();
            if (!Files.exists(path))
                Files.createFile(path);
            
            Files.write(path, data.getBytes());
        } catch (IOException e) {
            System.err.println("Failed to write to file: " + file.getPath());
            Terminate.exception(e);
        }
    }
}