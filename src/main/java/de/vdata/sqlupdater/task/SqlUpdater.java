package de.vdata.sqlupdater.task;


import java.io.File;
import java.io.InputStream;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.List;

import de.vdata.sqlupdater.entity.SqlLogEntity;


public class SqlUpdater {

    private String dirName;
    private String dbHost = "localhost";
    private String dbPort = "3306";
    private String dbName = "portal";
    private String dbUser = "root";
    private String dbPassword = "";
    private String mysqlPath = "";
    private String tempDirPath = "";
    private boolean withUpdate = true;
    private static final String MYSQL_INSECURE_PASSWORD_WARNING = "Warning: Using a password on the command line interface can be insecure.";

    private static final Comparator<File> fileComparator = Comparator.comparing(File::getName);

    private Connection resolveJDBC() throws Exception {
        Class.forName("com.mysql.jdbc.Driver");
        String url = "jdbc:mysql://" + this.dbHost + ":" + this.dbPort + "/" + this.dbName;
        return DriverManager.getConnection(url, this.dbUser, this.dbPassword);
    }

    private String getFilePathForDB(File file) {
        return file.getAbsolutePath().replace(this.dirName, "");
    }

    public void execute() {
        try {
            startExecution();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startExecution()
            throws Exception {
        Connection connection = null;
        try {
            connection = resolveJDBC();
            List<File> newScripts = getNotExecutedScripts(connection);
            newScripts.sort(fileComparator);
            File tempDir = new File(this.tempDirPath + getFileSeparator() + "temp");
            tempDir.mkdir();
            copyFiles(newScripts, tempDir);
            List<SqlLogEntity> logsEntity = new ArrayList<>();
            for (File file : newScripts) {
                logsEntity.add(executeScript(file, tempDir.getAbsolutePath(), this.withUpdate));
            }
            writeToLog(connection, logsEntity);
            deleteDir(tempDir);
            if (this.withUpdate) {
                int error = 0;
                int ok = 0;
                for (SqlLogEntity sqlLogEntity : logsEntity) {
                    if ("ok".equalsIgnoreCase(sqlLogEntity.getState())) {
                        ok++;
                    } else {
                        error++;
                    }
                }
                System.out.println("Ok executed scripts: " + ok);
                System.out.println("Error executed scripts: " + error);
            }
        } finally {
            if (connection != null) {
                connection.close();
            }
        }
    }

    private SqlLogEntity executeScript(File file, String tempDir, boolean withUpdate) {
        SqlLogEntity entity = new SqlLogEntity();
        entity.setExecuteDate(Calendar.getInstance().getTime());
        entity.setScriptName(getFilePathForDB(file));
        entity.setScriptSize(file.length());
        if (withUpdate) {
            try {
                Runtime runtime = Runtime.getRuntime();
                List<String> cmd = new ArrayList<>();
                if (!"".equals(this.mysqlPath)) {
                    cmd.add(this.mysqlPath + getFileSeparator() + "mysql");
                } else {
                    cmd.add("mysql");
                }
                if (!"".equals(this.dbHost)) {
                    cmd.add("-h" + this.dbHost);
                }
                if (!"".equals(this.dbPort)) {
                    cmd.add("-P" + this.dbPort);
                }
                cmd.add(this.dbName);
                cmd.add("-u" + this.dbUser);
                if (!"".equals(this.dbPassword)) {
                    cmd.add("-p" + this.dbPassword);
                }
                String[] cmdStr = new String[cmd.size()];
                cmd.toArray(cmdStr);
                Process proc = runtime.exec(cmdStr);


                String sourceCommand = ("source " + tempDir + getFileSeparator() + file.getName()).replace('\\', '/');
                proc.getOutputStream().write(sourceCommand.getBytes("ISO-8859-1"));
                System.out.println(file.getAbsolutePath());
                proc.getOutputStream().close();
                proc.waitFor();
                InputStream in = proc.getErrorStream();
                byte[] bs = new byte[in.available()];
                in.read(bs);
                String messageFromMysql = new String(bs).replaceAll(getLineSeparator(), "").replace(MYSQL_INSECURE_PASSWORD_WARNING, "");
                if (messageFromMysql.length() > 0) {
                    entity.setState(messageFromMysql);
                    System.out.println("Error: " + messageFromMysql);
                } else {
                    entity.setState("OK");
                    System.out.println(file.getName() + " execute ok");
                }
                in.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            entity.setState("unknown status");
        }
        return entity;
    }

    private List<File> getNotExecutedScripts(Connection connection)
            throws SQLException {
        List<File> result = new ArrayList();
        File sqlDirectory = new File(this.dirName + getFileSeparator() + "sql");
        List<File> allScripts = new ArrayList();
        getFileList(sqlDirectory, allScripts);
        List<SqlLogEntity> sqlLogEntitys = new ArrayList();
        sqlLogEntitys = parseLog(connection);
        for (File file : allScripts) {
            boolean executed = false;
            for (SqlLogEntity sqlLogEntity : sqlLogEntitys) {
                if ((sqlLogEntity.getScriptName().equals(getFilePathForDB(file))) && (sqlLogEntity.getScriptSize() == file.length())) {
                    executed = true;
                    break;
                }
            }
            if (!executed) {
                result.add(file);
            }
        }
        return result;
    }

    private void writeToLog(Connection connection, List<SqlLogEntity> sqlLogEntitys)
            throws SQLException {
        System.out.println("Write to log " + sqlLogEntitys.size());
        for (SqlLogEntity logEntity : sqlLogEntitys) {
            PreparedStatement pstmt = connection.prepareStatement("INSERT INTO executed_sqls_log VALUES(null, ?, ?, ?, ?)");
            pstmt.setObject(1, logEntity.getExecuteDate());
            pstmt.setObject(2, logEntity.getScriptName());
            pstmt.setObject(3, logEntity.getState());
            pstmt.setObject(4, Long.valueOf(logEntity.getScriptSize()));
            pstmt.executeUpdate();
            pstmt.close();
        }
    }

    private List<SqlLogEntity> parseLog(Connection connection)
            throws SQLException {
        List<SqlLogEntity> sqlLogEntitys = new ArrayList();
        PreparedStatement pstmt = connection.prepareStatement("SELECT log.* FROM executed_sqls_log log INNER JOIN (SELECT script_name, MAX(execute_date) AS max_execute_date FROM executed_sqls_log GROUP BY script_name) max_execute_date ON log.script_name=max_execute_date.script_name AND log.execute_date=max_execute_date.max_execute_date");
        ResultSet rs = pstmt.executeQuery();
        while (rs.next()) {
            sqlLogEntitys.add(parseSqlLogEntity(rs));
        }
        rs.close();
        pstmt.close();
        return sqlLogEntitys;
    }

    private SqlLogEntity parseSqlLogEntity(ResultSet rs)
            throws SQLException {
        SqlLogEntity entity = new SqlLogEntity();
        entity.setScriptName(rs.getString("script_name"));
        entity.setExecuteDate(rs.getDate("execute_date"));
        entity.setState(rs.getString("state"));
        entity.setScriptSize(rs.getLong("script_size"));
        return entity;
    }

    private static void getFileList(File dir, List<File> files) {
        try {
            File[] fileList = dir.listFiles();
            for (int i = 0; i < fileList.length; i++) {
                if (fileList[i].isDirectory()) {
                    getFileList(fileList[i], files);
                } else if (fileList[i].getName().endsWith(".sql")) {
                    files.add(fileList[i]);
                }
            }
        } catch (Exception e) {
            System.out.println("Wrong project directory! " + dir.getAbsolutePath());
        }
    }

    private void copyFiles(List<File> files, File dir)
            throws Exception {
        for (File file : files) {
            copyFile(file, dir);
        }
    }

    private void copyFile(File file, File dir)
            throws Exception {
        Files.copy(Paths.get(file.getPath(), new String[0]), Paths.get(dir.getAbsolutePath() + getFileSeparator() + file.getName(), new String[0]), new CopyOption[]{StandardCopyOption.REPLACE_EXISTING});
    }

    private void deleteDir(File file) {
        if (!file.exists()) {
            return;
        }
        if (file.isDirectory()) {
            for (File f : file.listFiles()) {
                deleteDir(f);
            }
            file.delete();
        } else {
            file.delete();
        }
    }

    private static String getFileSeparator() {
        return File.separator;
    }

    private static String getLineSeparator() {
        return System.lineSeparator();
    }

    public void setDirName(String dirName) {
        this.dirName = dirName;
    }

    public void setDbHost(String dbHost) {
        this.dbHost = dbHost;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    public void setDbUser(String dbUser) {
        this.dbUser = dbUser;
    }

    public void setDbPassword(String dbPassword) {
        this.dbPassword = dbPassword;
    }

    public void setMysqlPath(String mysqlPath) {
        this.mysqlPath = mysqlPath;
    }

    public void setTempDirPath(String tempDirPath) {
        this.tempDirPath = tempDirPath;
    }

    public void setDbPort(String dbPort) {
        this.dbPort = dbPort;
    }

    public void setWithUpdate(String withUpdate) {
        this.withUpdate = (("true".equals(withUpdate)) || ("".equals(withUpdate)));
    }
}