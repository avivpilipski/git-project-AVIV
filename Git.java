import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Git {
    public static void main (String [] args) throws IOException {
        initGit();
        checkInitGit();
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