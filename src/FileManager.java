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

  public File getContent(String path) throws IOException {
    File requestedContent = new File(rootDirectory, path);
    validatePath(requestedContent);

    if (!requestedContent.exists()) {
      throw new NoSuchFileException("Requested file does not exist.");
    }

    return requestedContent;
  }

  public byte[] getContentAsBytes(String path) throws IOException {
    File file = getContent(path);
    byte[] data = Files.readAllBytes(file.toPath());
    return data;
  }

  public String getMimeType(String path) throws IOException {
    String mimeType = Files.probeContentType(Paths.get(path));
    if (mimeType == null) {
      throw new IllegalArgumentException("File extension is missing or MIME type could not be determined.");
    }

    return mimeType;
  }

  public void saveFile(InputStream fileData, String path) throws IOException {
    File targetFile = new File(rootDirectory, path);
    validatePath(targetFile);

    FileOutputStream out = new FileOutputStream(targetFile);
    byte[] buffer = new byte[1024];
    int bytesRead;
    while ((bytesRead = fileData.read(buffer)) != -1) {
      out.write(buffer, 0, bytesRead);
    }
    out.close();
  }

  private void validatePath(File file) throws IOException {
    if (!file.getCanonicalPath().startsWith(rootDirectory.getCanonicalPath())) {
      throw new AccessDeniedException("Access to files outside of the root directory is prohibited.");
    }
  }

  public boolean isDirectory(String path) throws IOException {
    File file = new File(rootDirectory, path);
    validatePath(file);
    return file.isDirectory() ? true : false;
  }
}
