package net.sn0wix_.files.config;

public class Config {
    public String fmcsAccount = "";
    public String fmcsPassword = "";
    public String ftpAccount = "";
    public String ftpAccountPassword = "";
    public String rapidApiKey = "";
    public String serverUrl = "https://freemcserver.p.rapidapi.com/v4/server/000000";
    public String directoryToBackup = "";
    public int maxBackups = 5;
    public int backupIntervalInHours = 168;
    public int ftpPort = 21;
    public int delayBetweenRequests = 1100;
    public boolean printOutput = true;
    public boolean backupWhileRunning = true;
    public boolean checkFmcsApiKey = true;
    public boolean checkFtpDns = true;
    public boolean passiveFtp = true;
    public String ftpMode = "binary";
    public boolean openConsole = true;
    public boolean darkMode = false;
    public boolean popupAfterFinish = false;
    public boolean popupAfterSuccessfulDownload = true;
    public boolean closeConsoleUponExit = false;
    public boolean closeConsoleWhenDownloaded = false;
    public boolean closeConsoleWhenChecking = false;


    public String fmcsApiKey = "NO-TOKEN";
    public String userAgent = "";
    public String ftpServer = "";
    public boolean shouldBackup = false;
    public String lastCheck = "";
}
