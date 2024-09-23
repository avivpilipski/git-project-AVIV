import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.zip.GZIPOutputStream;
import java.io.IOException;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;


public class Blob {
    private String blobName;
    private static boolean compressionAuthorization = false;

    public Blob (String fileName) throws IOException{
        if (!Files.exists(Paths.get("git/objects")))
            throw new FileNotFoundException("no git or objects directories");
        if (!Files.exists(Paths.get(fileName)))
            throw new FileNotFoundException("file does not exist");

        File ogFile = new File (fileName);

        //compressing the data if toggled on
        String fileContents = readFileAsString(ogFile);
        if (compressionAuthorization == true){
            try {
                fileContents = compress(fileContents).toString();
            } catch (IOException e) {
                System.err.println("Compression failed, proceeding with uncompressed data.");
            }
        }

        //calling SHA1 on the file and putting that SHA1ed file into the objects directory
       
        File backupFile = new File ("git/objects", newFileName(fileContents));
        BufferedWriter bw = new BufferedWriter(new FileWriter(backupFile));
        bw.write (fileContents);
        bw.close();

        //storing original fileName and SHA1 of file into the index File
        String str = backupFile.getName() + " " + ogFile.getName() + "\n";
        BufferedWriter bw2 = new BufferedWriter(new FileWriter("git/index", true));
        bw2.append (str);
        bw2.close();

        blobName = backupFile.getName();
    }

    public String getBlobName(){
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

    public boolean isCompressed(){
        return compressionAuthorization;
    }
    
    public String newFileName (String str) throws IOException{
        //encrypts a String using the SHA1 function
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

    public String readFileAsString(File file) throws IOException {
        try {
            Path path = file.toPath();
            return new String(Files.readAllBytes(path));
        } catch (IOException e) {
            System.err.println("Failed to read file: " + file.getPath());
            throw e;
        }
    }
}