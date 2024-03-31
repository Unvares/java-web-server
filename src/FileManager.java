package src;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class FileManager {
    private final File rootDirectory;

    FileManager(File rootDirectory) {
        this.rootDirectory = rootDirectory;
    }

    public File getFile(String path) throws IOException {
        File requestedFile = new File(rootDirectory, path);
        validateFilePath(requestedFile);

        if (!requestedFile.exists()) {
            throw new IOException("Requested file does not exist.");
        }

        return requestedFile;
    }

    public void saveFile(InputStream fileData, String path) throws IOException {
        File targetFile = new File(rootDirectory, path);
        validateFilePath(targetFile);

        FileOutputStream out = new FileOutputStream(targetFile);
        byte[] buffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = fileData.read(buffer)) != -1) {
            out.write(buffer, 0, bytesRead);
        }
        out.close();
    }

    private void validateFilePath(File file) throws IOException {
        if (!file.getCanonicalPath().startsWith(rootDirectory.getCanonicalPath())) {
            throw new IllegalArgumentException("Access to files outside of the root directory is prohibited.");
        }
    }
}
