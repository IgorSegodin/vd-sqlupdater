package de.vdata.sqlupdater;

import de.vdata.sqlupdater.task.SqlUpdater;
import de.vdata.sqlupdater.util.FileUtil;
import de.vdata.sqlupdater.util.UpdaterFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * @author i.segodin
 */
public class App {

    private static final String PROPERTY_EXAMPLE_FILE_NAME = "sql-updater.properties.example";
    private static final String PROPERTY_FILE_NAME = "sql-updater.properties";


    public static void main(String[] args) {
        URL jarLocation = App.class.getProtectionDomain().getCodeSource().getLocation();
        String pathToWorkingFolder = FileUtil.getPathToFolder(jarLocation.getFile());

        File propertiesFile = Paths.get(pathToWorkingFolder, PROPERTY_FILE_NAME).toFile();

        if (!propertiesFile.exists()) {
            /* Create property file stub if it does not exist */
            initializePropertyFileFlow(propertiesFile);
            System.out.println("Please, fill property file: " + propertiesFile.getPath());
            return;
        }

        Properties properties = loadProperties(propertiesFile);

        SqlUpdater sqlUpdater = UpdaterFactory.create(pathToWorkingFolder, properties);

        sqlUpdater.execute();
    }

    private static void initializePropertyFileFlow(File propertiesFile) {
        try {
            try (InputStream propertyExampleInputStream = App.class.getClassLoader().getResourceAsStream(PROPERTY_EXAMPLE_FILE_NAME)) {
                try (FileOutputStream propertiesFileOutputStream = new FileOutputStream(propertiesFile)) {
                    FileUtil.writeStream(propertyExampleInputStream, propertiesFileOutputStream);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static Properties loadProperties(File propertiesFile) {
        try {
            Properties properties = new Properties();
            properties.load(new FileInputStream(propertiesFile));
            return properties;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
