package com.example.dangkhoa.placestogo.Utils;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by dangkhoa on 10/02/2018.
 */

public class FileUtil {

    //"storage/emulated/0"
    public static final String ROOT_DIR = Environment.getExternalStorageDirectory().getPath();

    public static final String PICTURES = ROOT_DIR + "/Pictures";
    public static final String DCIM = ROOT_DIR + "/DCIM";

    /**
     * Search a directory and return a list of all **directories** contained inside
     *
     * @param directory
     * @return
     */
    public static ArrayList<String> getDirectoryPaths(String directory) {
        ArrayList<String> pathArray = new ArrayList<>();
        File file = new File(directory);
        File[] listfiles = file.listFiles();

        for (int i = 0; i < listfiles.length; i++) {
            if (listfiles[i].isDirectory()) {

                String dir = listfiles[i].getAbsolutePath();

                ArrayList<String> fileList = getFilePaths(dir);

                // just add directory that has size > 0
                if (fileList.size() > 0) {
                    pathArray.add(listfiles[i].getAbsolutePath());
                }
            }
        }
        return pathArray;
    }

    /**
     * Search a directory and return a list of all **files** contained inside
     *
     * @param directory
     * @return
     */
    public static ArrayList<String> getFilePaths(String directory) {
        ArrayList<String> pathArray = new ArrayList<>();
        File file = new File(directory);
        File[] listfiles = file.listFiles();
        for (int i = 0; i < listfiles.length; i++) {
            if (listfiles[i].isFile()) {
                pathArray.add(listfiles[i].getAbsolutePath());
            }
        }
        return pathArray;
    }

    /**
     * Retrieve directory name from the directory path (e.g. /aaa/bbb/directory_name)
     *
     * @param path
     * @return
     */
    public static String getDirectoryName(String path) {
        return path.substring(path.lastIndexOf('/') + 1);
    }

    /**
     * Get all file paths within several directories
     *
     * @param directories (a list of directories)
     * @return
     */
    public static ArrayList<String> getFilePathsInManyDirectories(ArrayList<String> directories) {
        ArrayList<String> pathList = new ArrayList<>();

        for (int i = 0; i < directories.size(); i++) {
            ArrayList<String> resultList = getFilePaths(directories.get(i));

            for (int j = 0; j < resultList.size(); j++) {
                String path = resultList.get(j);

                String fileType = path.substring(path.lastIndexOf('.') + 1);
                // check for image type
                if (fileType.equals("png") || fileType.equals("jpg")) {
                    // if the path is already in pathList, don't add it again
                    if (!pathList.contains(path)) {
                        pathList.add(path);
                    }
                }
            }
        }
        return pathList;
    }
}
