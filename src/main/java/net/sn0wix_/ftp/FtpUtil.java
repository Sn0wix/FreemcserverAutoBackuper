package net.sn0wix_.ftp;

import net.sn0wix_.Main;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

import java.io.*;
import java.util.Arrays;

public class FtpUtil {

    public static void connectAndDownload(String user, String password, String domain, int port) {
        FTPClient ftpClient = new FTPClient();

        try {
            ftpClient.connect(domain, port);

            int replyCode = ftpClient.getReplyCode();
            if (!FTPReply.isPositiveCompletion(replyCode)) {
                System.err.println("Ftp connection failed. Server reply code: " + replyCode);
                ftpClient.disconnect();
                return;
            }
            if (ftpClient.login(user, password)) {
                if (Main.CONFIG.printOutput) {
                    System.out.println("Ftp login successful!");
                }


                switch (Main.CONFIG.ftpMode.toLowerCase()) {
                    case "ascii":
                        ftpClient.setFileType(FTP.ASCII_FILE_TYPE);
                        break;
                    case "ebcdic":
                        ftpClient.setFileType(FTP.EBCDIC_FILE_TYPE);
                        break;
                    case "local":
                        ftpClient.setFileType(FTP.LOCAL_FILE_TYPE);
                        break;
                    default:
                        ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
                        break;
                }


                if (Main.CONFIG.passiveFtp) {
                    ftpClient.enterLocalPassiveMode();
                }


                if (Main.CONFIG.printOutput) {
                    System.out.println(Arrays.toString(ftpClient.listDirectories()));
                    System.out.println(Arrays.toString(ftpClient.listFiles()));
                    System.out.println("Saving to + " + Main.CONFIG_FILE.currentBackupLocation + File.separator + Main.CONFIG.directoryToBackup);
                }

                downloadDirectory(ftpClient, Main.CONFIG.directoryToBackup, "", Main.CONFIG_FILE.currentBackupLocation + File.separator);

                ftpClient.logout();
            } else {
                System.err.println("Ftp login failed!");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                ftpClient.disconnect();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void downloadDirectory(FTPClient ftpClient, String parentDir, String currentDir, String saveDir) throws IOException {
        String dirToList = parentDir;
        if (!currentDir.isEmpty()) {
            dirToList += "/" + currentDir;
        }

        FTPFile[] subFiles = ftpClient.listFiles(dirToList);

        if (subFiles != null) {
            for (FTPFile aFile : subFiles) {
                String currentFileName = aFile.getName();
                if (currentFileName.equals(".") || currentFileName.equals("..")) {
                    continue;
                }
                String filePath = parentDir + "/" + currentDir + "/"
                        + currentFileName;
                if (currentDir.isEmpty()) {
                    filePath = parentDir + "/" + currentFileName;
                }

                String newDirPath = saveDir + parentDir + File.separator
                        + currentDir + File.separator + currentFileName;
                if (currentDir.isEmpty()) {
                    newDirPath = saveDir + parentDir + File.separator
                            + currentFileName;
                }

                if (aFile.isDirectory()) {
                    File newDir = new File(newDirPath);
                    boolean created = newDir.mkdirs();
                    if (created) {
                        if (Main.CONFIG.printOutput) {
                            System.out.println("CREATED directory: " + newDirPath);
                        }
                    } else {
                        System.err.println("COULD NOT create directory: " + newDirPath);
                    }

                    downloadDirectory(ftpClient, dirToList, currentFileName,
                            saveDir);
                } else {
                    boolean success = downloadSingleFile(ftpClient, filePath, newDirPath);
                    if (success) {
                        if (Main.CONFIG.printOutput) {
                            System.out.println("DOWNLOADED file: " + filePath);
                        }
                    } else {
                        System.err.println("COULD NOT download file: "
                                + filePath);
                    }
                }
            }
        }
    }


    public static boolean downloadSingleFile(FTPClient ftpClient, String remoteFilePath, String savePath) {
        File downloadFile = new File(savePath);

        File parentDir = downloadFile.getParentFile();
        if (!parentDir.exists()) {
            parentDir.mkdirs();
        }

        try {
            BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(downloadFile));
            boolean bl = ftpClient.retrieveFile(remoteFilePath, outputStream);
            outputStream.close();
            return bl;
        } catch (Exception e) {
            System.err.println("Error when downloading file " + remoteFilePath);
            e.printStackTrace();
        }

        return false;
    }
}
