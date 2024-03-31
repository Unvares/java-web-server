package src;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;

public class FileManager {
    private final File rootDirectory;

    FileManager(File rootDirectory) {
        this.rootDirectory = rootDirectory;
    }

    public File getFile(String path) throws IOException {
        File requestedFile = new File(rootDirectory, path);
        validateFilePath(requestedFile);

        if (!requestedFile.exists()) {
            throw new NoSuchFileException("Requested file does not exist.");
        }

        return requestedFile;
    }

    public byte[] getFileAsByteArray(String path) throws IOException {
        File file = getFile(path);
        return Files.readAllBytes(file.toPath());
    }

    public String getMimeType(String path) throws IOException {
        String extension = "";

        int i = path.lastIndexOf('.');
        if (i > 0) {
            extension = path.substring(i + 1);
        }

        String mimeType = Files.probeContentType(Paths.get(extension));

        if (extension.isEmpty() || mimeType == null) {
            throw new IllegalArgumentException("File extension is missing or MIME type could not be determined.");
        }

        return mimeType;
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
            throw new AccessDeniedException("Access to files outside of the root directory is prohibited.");
        }
    }
}
