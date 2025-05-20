package com.fbytes.llmka.tools;

import java.io.File;

public class FileUtil {

    public static void moveToBackup(String filePath) {
        File originalFile = new File(filePath);
        File backupFile = new File(filePath + ".bak");
        File backupFileTemp = new File(filePath + ".bak.temp");

        if (backupFile.exists()) {
            if (backupFileTemp.exists()) {
                boolean res = backupFileTemp.delete();
                if (!res)
                    throw new RuntimeException("Unable to delete temporary bak file");

            }
            boolean res = backupFile.renameTo(backupFileTemp);
            if (!res)
                throw new RuntimeException("Unable to rename " + backupFile.getAbsoluteFile() + " to " + backupFileTemp.getAbsoluteFile());
        }
        try {
            boolean success = originalFile.renameTo(backupFile);
            if (!success)
                throw new RuntimeException("Unable to rename " + originalFile.getAbsoluteFile() + " to " + backupFile.getAbsoluteFile());
        } catch (Exception e) {
            backupFileTemp.renameTo(backupFile);
        }

        if (backupFileTemp.exists())
            backupFileTemp.delete();
    }
}
