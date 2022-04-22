package net.ME1312.Galaxi.Library;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public final class Directories {
    private Directories() {}

    /**
     * Copy a Directory
     *
     * @param from Source
     * @param to Destination
     */
    public static void copy(File from, File to) {
        if (from.isDirectory() && !Files.isSymbolicLink(from.toPath())) {
            if (!to.exists()) {
                to.mkdirs();
            }

            String files[] = from.list();

            for (String file : files) {
                File srcFile = new File(from, file);
                File destFile = new File(to, file);

                copy(srcFile, destFile);
            }
        } else {
            try {
                if (!to.exists()) Files.copy(from.toPath(), to.toPath(), LinkOption.NOFOLLOW_LINKS);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Search a Directory
     *
     * @param folder Location
     * @return List of all found file paths
     */
    public static List<String> search(File folder) {
        return search(folder, folder);
    }

    private static List<String> search(File origin, File file) {
        List<String> list = new LinkedList<String>();
        if (file.isFile()) {
            if (origin == file) {
                list.add(file.getName());
            } else {
                list.add(file.getAbsolutePath().substring(origin.getAbsolutePath().length()+1));
            }
        }
        if (file.isDirectory()) for (File next : file.listFiles()) {
            list.addAll(search(origin, next));
        }
        return list;
    }

    /**
     * Zip a Directory
     *
     * @param folder Location
     * @param zip Zip Data Stream
     */
    public static void zip(File folder, OutputStream zip) {
        File dir = (folder.isFile())?folder.getParentFile():folder;
        byte[] buffer = new byte[4096];

        try (ZipOutputStream zos = new ZipOutputStream(zip)) {
            for (String next : search(folder)) {
                zos.putNextEntry(new ZipEntry(next.replace(File.separatorChar, '/')));
                try (FileInputStream in = new FileInputStream(dir.getAbsolutePath() + File.separator + next)) {
                    int len;
                    while ((len = in.read(buffer)) != -1) {
                        zos.write(buffer, 0, len);
                    }
                }
            }
            zos.closeEntry();
        } catch(IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Unzip a Directory
     *
     * @param zip Zip Data Stream
     * @param folder Parent Location
     */
    public static void unzip(InputStream zip, File folder) {
        byte[] buffer = new byte[4096];
        try (ZipInputStream zis = new ZipInputStream(zip)) {
            ZipEntry ze;
            while ((ze = zis.getNextEntry()) != null) {
                File newFile = new File(folder + File.separator + ze.getName().replace('/', File.separatorChar));
                if (newFile.exists()) {
                    if (newFile.isDirectory()) {
                        delete(newFile);
                    } else {
                        newFile.delete();
                    }
                }
                if (ze.isDirectory()) {
                    newFile.mkdirs();
                    continue;
                } else if (!newFile.getParentFile().exists()) {
                    newFile.getParentFile().mkdirs();
                }
                try (FileOutputStream fos = new FileOutputStream(newFile)) {
                    int len;
                    while ((len = zis.read(buffer)) != -1) {
                        fos.write(buffer, 0, len);
                    }
                }
            }
            zis.closeEntry();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Delete Directory
     *
     * @param folder Location
     */
    public static void delete(File folder) {
        File[] files = folder.listFiles();
        if(files!=null) {
            for(File f : files) {
                if(f.isDirectory() && !Files.isSymbolicLink(f.toPath())) {
                    delete(f);
                } else try {
                    Files.delete(f.toPath());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        folder.delete();
    }
}
