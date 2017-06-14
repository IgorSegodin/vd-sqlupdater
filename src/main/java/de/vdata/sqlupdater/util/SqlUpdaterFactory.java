package de.vdata.sqlupdater.util;

import de.vdata.sqlupdater.task.SqlUpdater;

import java.nio.file.Paths;
import java.util.Properties;

/**
 * @author isegodin
 */
public class SqlUpdaterFactory {

    private static final String DEFAULT_TEMP_FOLDER_NAME = "temp";

    public static SqlUpdater create(String pathToWorkingFolder, Properties properties) {
        PropertiesWrapper propertiesWrapper = new PropertiesWrapper(properties);

        SqlUpdater sqlUpdater = new SqlUpdater();

        sqlUpdater.setDirName(FileUtil.getPathWithoutEndSlash(propertiesWrapper.getStrict("projectpath")));
        sqlUpdater.setTempDirPath(FileUtil.getPathWithoutEndSlash(propertiesWrapper.get(
                "tempDirPath",
                Paths.get(pathToWorkingFolder, DEFAULT_TEMP_FOLDER_NAME).toString()))
        );
        sqlUpdater.setMysqlPath(FileUtil.getPathWithoutEndSlash(propertiesWrapper.get("mysqlPath")));
        sqlUpdater.setWithUpdate(propertiesWrapper.get("withUpdate", Boolean.TRUE.toString()));
        sqlUpdater.setDbHost(propertiesWrapper.get("dbHost", "localhost"));
        sqlUpdater.setDbPort(propertiesWrapper.get("dbPort", "3306"));
        sqlUpdater.setDbName(propertiesWrapper.getStrict("dbName"));
        sqlUpdater.setDbUser(propertiesWrapper.getStrict("dbUser"));
        sqlUpdater.setDbPassword(propertiesWrapper.get("dbPassword", ""));
        return sqlUpdater;
    }
}
