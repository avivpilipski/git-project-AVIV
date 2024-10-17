public interface GitInterface {    
    /**
     * Adds a file to the next commit
     *
     * @param path The path to the file or directory to be staged.
     */
    void stage(String path);

    /**
     * Creates a file defining the changes made to the repository
     *
     * @param author  The author's username
     * @param message Text field for the commit 
     * @return The commit's hash
     */
    String commit(String author, String message);

    /**
     * Restores the repository to it's state at a given commit
     *
     * @param hash The hash of the commit to check out
     */
    void checkout(String hash);
}
