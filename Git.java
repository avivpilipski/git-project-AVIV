import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.io.BufferedWriter;
import java.io.FileWriter;

public class Git {
    public static void main (String [] args) throws IOException {
        //test initialization of git repo and deletes it (then recreates it for further testing)
        initGit();
        checkInitGit();
        initGit();

        //creates two blobs with unique file names and data
        File newFile = new File ("newFile.txt");
        newFile.createNewFile();
        BufferedWriter bw = new BufferedWriter(new FileWriter(newFile));
        bw.write ("sodvunaoernoienv");
        bw.close();
        Blob blob = new Blob ("newFile.txt");
        File newFile2 = new File ("newFile2.txt");
        newFile.createNewFile();
        BufferedWriter bw2 = new BufferedWriter(new FileWriter(newFile2));
        bw2.write ("sodvunaoernoien");
        bw2.close();
        Blob blob2 = new Blob ("newFile2.txt");

        //verifies that the location of a blob is correct (git/objects)
        String blobName = blob.getBlobName();
        String desiredPath = "git/objects/"+blobName;
        File b = new File (desiredPath);
        if (b.exists()&&b.isFile())
            System.out.println ("Blob exists in the correct path.");
        
        //tests that the blob has correct hash and file contents- for compressed and non-compressed files
        String correctHash = "";
        if (blob.isCompressed()){
            correctHash = "ddd8ff69c3b86f4f8a3efcb1387a5787f464a26f";
            if (blob.getBlobName().equals(correctHash)){
                System.out.println ("blob has correct hash/name.");
            }
        }
        else{
            correctHash = "aaa8b870cdae19a38f9ad6f328dbaaf31ad38965";
            if (blob.getBlobName().equals(correctHash)){
                System.out.println ("blob has correct hash/name.");
                String BlobContents = blob.readFileAsString(b);
                String FileContents = blob.readFileAsString(newFile);
                if (BlobContents.equals(FileContents)){
                    System.out.println ("blob has correct contents.");
                }
            }
        }
        //verifies that the index was updated correctly
        String indexContents = blob.readFileAsString(new File ("git/index"));
        if (indexContents.equals(blobName+" newFile.txt" + "\n" + blob2.getBlobName()+" newFile2.txt" + "\n")){
            System.out.println ("index was updated correctly");
        }

        //deletes the blobs and original files
        b.delete();
        File b2 = new File ("git/objects/"+blob2.getBlobName());
        b2.delete();
        newFile.delete();
        newFile2.delete();
        if (!b.exists()&&!b2.exists()&&!newFile.exists()&&!newFile2.exists()){
            System.out.println ("succesfully deleted blobs and original files.");
        }

        //deletes the contents of index
        File index = new File ("git/index");
        index.delete();
        index.createNewFile();
        if (index.length()==0){
            System.out.println ("succesfully deleted the contents of index.");
        }
    }

    public static void initGit() throws IOException{
        File git = new File ("git");
        File objects = new File (git, "objects");
        File index = new File (git, "index");

        if (git.exists()&&objects.exists()&&index.exists()){
            System.out.println ("Git Repository already exists");
        }

        if (!git.exists()){
            git.mkdir();
        }

        
        if (!objects.exists()){
            objects.mkdirs();
        }

        
        if (!index.exists()){
            index.createNewFile();
        }
    }

    public static void checkInitGit(){
        Path indexPath = Paths.get("git/index");
        if (Files.exists(indexPath)){
            System.out.println ("index file exists.");
        }
        
        Path objectsPath = Paths.get("git/objects");
        if (Files.exists(objectsPath)){
            System.out.println ("objects directory exists.");
        }

        Path gitPath = Paths.get("git");
        if (Files.exists(gitPath)){
            System.out.println ("git directory exists.");
            File gitFile = new File ("git");
            if (deleteDirectory(gitFile)){
                System.out.println ("git directory removed.");
            }
        }
    }

    //recursively deletes a directory
    public static boolean deleteDirectory(File directory){
        if (directory.isDirectory()){
            File [] files = directory.listFiles();
            if (files.length>0){
                for (int i=0;i<files.length;i++){
                    deleteDirectory(files[i]);
                }
            }
        }
        return directory.delete();
    }

}