package org.jujubeframework.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.Charset;
import java.util.*;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/**
 * zip压缩处理
 * <p>
 * copy from zeroturnaround。URL：https://github.com/zeroturnaround/zt-zip
 * <p>
 * zt-zip的逻辑在解压缩的时候，没有顾及编码问题，会出现中文乱码，下面有几个编码的注意点：
 * <ul>
 * <li>解压缩的时候，出现这种情况：上传的压缩文件是在Windows环境下压缩而成的（编码GBK），而在Linux服务器端系统编码为UTF-8。
 * 这时候正确解压编码为GBK。解压之前，调用构造来设置解压缩编码</li>
 * <li>如果是要压缩文件，则根据操作系统编码调用构造来设置压缩编码</li>
 * <li>也就是说，压缩的时候依据当前操作系统编码；而解压缩的时候要依据压缩文件来源的操作系统（即：这个文件是什么系统编码来进行压缩的，
 * 那么解压缩的时候就要使用对应的编码）</li>
 * </ul>
 *
 * @author John Li Email：jujubeframework@163.com
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@SuppressWarnings({"unchecked", "rawtypes"})
public class Zips {
    private final String PATH_SEPARATOR = "/";

    /**
     * 解压文件编码
     */
    private Charset defaultEncoding;

    /**
     * Default compression level
     */
    public final int DEFAULT_COMPRESSION_LEVEL = Deflater.DEFAULT_COMPRESSION;

    private final Logger log = LoggerFactory.getLogger(Zips.class);

    public Zips(Charset charset) {
        defaultEncoding = charset;
    }

    /**
     * Checks if the ZIP file contains the given entry.
     *
     * @param zip  ZIP file.
     * @param name entry name.
     * @return <code>true</code> if the ZIP file contains the given entry.
     */
    public boolean containsEntry(File zip, String name) {
        ZipFile zf = null;
        try {
            zf = new ZipFile(zip, defaultEncoding);
            return zf.getEntry(name) != null;
        } catch (IOException e) {
            throw rethrow(e);
        } finally {
            closeQuietly(zf);
        }
    }

    /**
     * Checks if the ZIP file contains any of the given entries.
     *
     * @param zip   ZIP file.
     * @param names entry names.
     * @return <code>true</code> if the ZIP file contains any of the given
     * entries.
     */
    public boolean containsAnyEntry(File zip, String[] names) {
        ZipFile zf = null;
        boolean result = false;
        try {
            zf = new ZipFile(zip, defaultEncoding);
            for (int i = 0; i < names.length; i++) {
                if (zf.getEntry(names[i]) != null) {
                    result = true;
                    break;
                }
            }
        } catch (IOException e) {
            throw rethrow(e);
        } finally {
            closeQuietly(zf);
        }
        return result;
    }

    /**
     * Unpacks a single entry from a ZIP file.
     *
     * @param zip  ZIP file.
     * @param name entry name.
     * @return contents of the entry or <code>null</code> if it was not found.
     */
    public byte[] unpackEntry(File zip, String name) {
        ZipFile zf = null;
        try {
            zf = new ZipFile(zip, defaultEncoding);
            return doUnpackEntry(zf, name);
        } catch (IOException e) {
            throw rethrow(e);
        } finally {
            closeQuietly(zf);
        }
    }

    /**
     * Unpacks a single entry from a ZIP file.
     *
     * @param zf   ZIP file.
     * @param name entry name.
     * @return contents of the entry or <code>null</code> if it was not found.
     */
    public byte[] unpackEntry(ZipFile zf, String name) {
        try {
            return doUnpackEntry(zf, name);
        } catch (IOException e) {
            throw rethrow(e);
        }
    }

    /**
     * Unpacks a single entry from a ZIP file.
     *
     * @param zf   ZIP file.
     * @param name entry name.
     * @return contents of the entry or <code>null</code> if it was not found.
     */
    private byte[] doUnpackEntry(ZipFile zf, String name) throws IOException {
        ZipEntry ze = zf.getEntry(name);
        if (ze == null) {
            // entry not found
            return null;
        }

        try (InputStream is = zf.getInputStream(ze)) {
            return IOUtils.toByteArray(is);
        }
    }

    /**
     * Unpacks a single file from a ZIP archive to a file.
     *
     * @param zip  ZIP file.
     * @param name entry name.
     * @param file target file to be created or overwritten.
     * @return <code>true</code> if the entry was found and unpacked,
     * <code>false</code> if the entry was not found.
     */
    public boolean unpackEntry(File zip, String name, File file) {
        ZipFile zf = null;
        try {
            zf = new ZipFile(zip, defaultEncoding);
            return doUnpackEntry(zf, name, file);
        } catch (IOException e) {
            throw rethrow(e);
        } finally {
            closeQuietly(zf);
        }
    }

    /**
     * Unpacks a single file from a ZIP archive to a file.
     *
     * @param zf   ZIP file.
     * @param name entry name.
     * @param file target file to be created or overwritten.
     * @return <code>true</code> if the entry was found and unpacked,
     * <code>false</code> if the entry was not found.
     */
    public boolean unpackEntry(ZipFile zf, String name, File file) {
        try {
            return doUnpackEntry(zf, name, file);
        } catch (IOException e) {
            throw rethrow(e);
        }
    }

    /**
     * Unpacks a single file from a ZIP archive to a file.
     *
     * @param zf   ZIP file.
     * @param name entry name.
     * @param file target file to be created or overwritten.
     * @return <code>true</code> if the entry was found and unpacked,
     * <code>false</code> if the entry was not found.
     */
    private boolean doUnpackEntry(ZipFile zf, String name, File file) throws IOException {
        ZipEntry ze = zf.getEntry(name);
        if (ze == null) {
            // entry not found
            return false;
        }

        try (InputStream in = new BufferedInputStream(zf.getInputStream(ze))) {
            FileUtil.copy(in, file);
        }
        return true;
    }

    /**
     * Reads the given ZIP file and executes the given action for each entry.
     * <p>
     * For each entry the corresponding input stream is also passed to the
     * action. If you want to stop the loop then throw a ZipBreakException.
     *
     * @param zip    input ZIP file.
     * @param action action to be called for each entry.
     * @see ZipEntryCallback
     * @see #iterate(File, ZipInfoCallback)
     */
    public void iterate(File zip, ZipEntryCallback action) {
        ZipFile zf = null;
        try {
            zf = new ZipFile(zip, defaultEncoding);

            Enumeration<ZipEntry> en = (Enumeration<ZipEntry>) zf.entries();
            while (en.hasMoreElements()) {
                ZipEntry e = en.nextElement();
                try (InputStream is = zf.getInputStream(e)) {
                    action.process(is, e);
                } catch (ZipBreakException ex) {
                    break;
                }
            }
        } catch (IOException e) {
            throw rethrow(e);
        } finally {
            closeQuietly(zf);
        }
    }

    /**
     * Reads the given ZIP file and executes the given action for each given
     * entry.
     * <p>
     * For each given entry the corresponding input stream is also passed to the
     * action. If you want to stop the loop then throw a ZipBreakException.
     *
     * @param zip        input ZIP file.
     * @param entryNames names of entries to iterate
     * @param action     action to be called for each entry.
     * @see ZipEntryCallback
     * @see #iterate(File, String[], ZipInfoCallback)
     */
    public void iterate(File zip, String[] entryNames, ZipEntryCallback action) {
        ZipFile zf = null;
        try {
            zf = new ZipFile(zip, defaultEncoding);

            for (int i = 0; i < entryNames.length; i++) {
                ZipEntry e = zf.getEntry(entryNames[i]);
                if (e == null) {
                    continue;
                }
                try (InputStream is = zf.getInputStream(e)) {
                    action.process(is, e);
                } catch (ZipBreakException ex) {
                    break;
                }
            }
        } catch (IOException e) {
            throw rethrow(e);
        } finally {
            closeQuietly(zf);
        }
    }

    /**
     * Scans the given ZIP file and executes the given action for each entry.
     * <p>
     * Only the meta-data without the actual data is read. If you want to stop
     * the loop then throw a ZipBreakException.
     *
     * @param zip    input ZIP file.
     * @param action action to be called for each entry.
     * @see ZipInfoCallback
     * @see #iterate(File, ZipEntryCallback)
     */
    public void iterate(File zip, ZipInfoCallback action) {
        ZipFile zf = null;
        try {
            zf = new ZipFile(zip, defaultEncoding);

            Enumeration en = (Enumeration<ZipEntry>) zf.entries();
            while (en.hasMoreElements()) {
                ZipEntry e = (ZipEntry) en.nextElement();
                try {
                    action.process(e);
                } catch (ZipBreakException ex) {
                    break;
                }
            }
        } catch (IOException e) {
            throw rethrow(e);
        } finally {
            closeQuietly(zf);
        }
    }

    /**
     * Scans the given ZIP file and executes the given action for each given
     * entry.
     * <p>
     * Only the meta-data without the actual data is read. If you want to stop
     * the loop then throw a ZipBreakException.
     *
     * @param zip        input ZIP file.
     * @param entryNames names of entries to iterate
     * @param action     action to be called for each entry.
     * @see ZipInfoCallback
     * @see #iterate(File, String[], ZipEntryCallback)
     */
    public void iterate(File zip, String[] entryNames, ZipInfoCallback action) {
        ZipFile zf = null;
        try {
            zf = new ZipFile(zip, defaultEncoding);

            for (int i = 0; i < entryNames.length; i++) {
                ZipEntry e = zf.getEntry(entryNames[i]);
                if (e == null) {
                    continue;
                }
                try {
                    action.process(e);
                } catch (ZipBreakException ex) {
                    break;
                }
            }
        } catch (IOException e) {
            throw rethrow(e);
        } finally {
            closeQuietly(zf);
        }
    }

    /**
     * Reads the given ZIP file and executes the given action for a single
     * entry.
     *
     * @param zip    input ZIP file.
     * @param name   entry name.
     * @param action action to be called for this entry.
     * @return <code>true</code> if the entry was found, <code>false</code> if
     * the entry was not found.
     * @see ZipEntryCallback
     */
    public boolean handle(File zip, String name, ZipEntryCallback action) {
        ZipFile zf = null;
        try {
            zf = new ZipFile(zip, defaultEncoding);

            ZipEntry ze = zf.getEntry(name);
            if (ze == null) {
                // entry not found
                return false;
            }

            try (InputStream in = new BufferedInputStream(zf.getInputStream(ze))) {
                action.process(in, ze);
            }
            return true;
        } catch (IOException e) {
            throw rethrow(e);
        } finally {
            closeQuietly(zf);
        }
    }

    /* Extracting whole ZIP files. */

    /**
     * Unpacks a ZIP file to the given directory.
     * <p>
     * The output directory must not be a file.
     *
     * @param zip       input ZIP file.
     * @param outputDir output directory (created automatically if not found).
     */
    public void unpack(File zip, final File outputDir) {
        unpack(zip, outputDir, IdentityNameMapper.INSTANCE);
    }

    /**
     * Unpacks a ZIP file to the given directory.
     * <p>
     * The output directory must not be a file.
     *
     * @param zip       input ZIP file.
     * @param outputDir output directory (created automatically if not found).
     */
    public void unpack(File zip, File outputDir, NameMapper mapper) {
        log.debug("Extracting '{}' into '{}'.", zip, outputDir);
        iterate(zip, new Unpacker(outputDir, mapper));
    }

    /**
     * Unwraps a ZIP file to the given directory shaving of root dir. If there
     * are multiple root dirs or entries in the root of zip, ZipException is
     * thrown.
     * <p>
     * The output directory must not be a file.
     *
     * @param zip       input ZIP file.
     * @param outputDir output directory (created automatically if not found).
     */
    public void unwrap(File zip, final File outputDir) {
        unwrap(zip, outputDir, IdentityNameMapper.INSTANCE);
    }

    /**
     * Unwraps a ZIP file to the given directory shaving of root dir. If there
     * are multiple root dirs or entries in the root of zip, ZipException is
     * thrown.
     * <p>
     * The output directory must not be a file.
     *
     * @param zip       input ZIP file.
     * @param outputDir output directory (created automatically if not found).
     */
    public void unwrap(File zip, File outputDir, NameMapper mapper) {
        log.debug("Unwraping '{}' into '{}'.", zip, outputDir);
        iterate(zip, new Unwraper(outputDir, mapper));
    }

    /**
     * Unpacks each ZIP entry.
     *
     * @author Rein Raudjärv
     */
    private class Unpacker implements ZipEntryCallback {

        private final File outputDir;
        private final NameMapper mapper;

        public Unpacker(File outputDir, NameMapper mapper) {
            this.outputDir = outputDir;
            this.mapper = mapper;
        }

        @Override
        public void process(InputStream in, ZipEntry zipEntry) throws IOException {
            String name = mapper.map(zipEntry.getName());
            if (name != null) {
                File file = new File(outputDir, name);
                if (zipEntry.isDirectory()) {
                    FileUtils.forceMkdir(file);
                } else {
                    FileUtils.forceMkdir(file.getParentFile());

                    if (log.isDebugEnabled() && file.exists()) {
                        log.info("Overwriting file '{}'.", zipEntry.getName());
                    }

                    FileUtil.copy(in, file);
                }
                if (log.isDebugEnabled()) {
                    log.info("unpack each zip entity. entity name:" + zipEntry.getName() + "  size:" + zipEntry.getSize());
                }
            }
        }
    }

    /**
     * Unwraps entries excluding a single parent dir. If there are multiple
     * roots ZipException is thrown.
     *
     * @author Oleg Shelajev
     */
    private class Unwraper implements ZipEntryCallback {

        private final File outputDir;
        private final NameMapper mapper;
        private String rootDir;

        public Unwraper(File outputDir, NameMapper mapper) {
            this.outputDir = outputDir;
            this.mapper = mapper;
        }

        @Override
        public void process(InputStream in, ZipEntry zipEntry) throws IOException {
            String root = getRootName(zipEntry.getName());
            if (rootDir == null) {
                rootDir = root;
            } else if (!rootDir.equals(root)) {
                throw new ZipException("Unwrapping with multiple roots is not supported, roots: " + rootDir + ", " + root);
            }

            String name = mapper.map(getUnrootedName(root, zipEntry.getName()));
            if (name != null) {
                File file = new File(outputDir, name);
                if (zipEntry.isDirectory()) {
                    FileUtils.forceMkdir(file);
                } else {
                    FileUtils.forceMkdir(file.getParentFile());

                    if (log.isDebugEnabled() && file.exists()) {
                        log.info("Overwriting file '{}'.", zipEntry.getName());
                    }

                    FileUtil.copy(in, file);
                }
            }
        }

        private String getUnrootedName(String root, String name) {
            return name.substring(root.length());
        }

        private String getRootName(String name) {
            name = name.substring(FilenameUtils.getPrefixLength(name));
            int idx = name.indexOf(PATH_SEPARATOR);
            if (idx < 0) {
                throw new ZipException("Entry " + name + " from the root of the zip is not supported");
            }
            return name.substring(0, name.indexOf(PATH_SEPARATOR));
        }
    }

    /**
     * Unpacks a ZIP file to its own location.
     * <p>
     * The ZIP file will be first renamed (using a temporary name). After the
     * extraction it will be deleted.
     *
     * @param zip input ZIP file as well as the target directory.
     * @see #unpack(File, File)
     */
    public void explode(File zip) {
        try {
            // Find a new unique name is the same directory
            File tempFile = FileUtil.getTempFileFor(zip);

            // Rename the archive
            FileUtils.moveFile(zip, tempFile);

            // Unpack it
            unpack(tempFile, zip);

            // Delete the archive
            if (!tempFile.delete()) {
                throw new IOException("Unable to delete file: " + tempFile);
            }
        } catch (IOException e) {
            throw rethrow(e);
        }
    }

    /* Compressing single entries to ZIP files. */

    /**
     * Compresses the given file into a ZIP file with single entry.
     *
     * @param file file to be compressed.
     * @return ZIP file created.
     */
    public byte[] packEntry(File file) {
        log.trace("Compressing '{}' into a ZIP file with single entry.", file);

        ByteArrayOutputStream result = new ByteArrayOutputStream();
        try (ZipOutputStream out = new ZipOutputStream(result, defaultEncoding)) {
            ZipEntry entry = new ZipEntry(file.getName());
            entry.setTime(file.lastModified());
            try (InputStream in = new BufferedInputStream(new FileInputStream(file))) {
                addEntry(entry, in, out);
            }
        } catch (IOException e) {
            throw rethrow(e);
        }
        return result.toByteArray();
    }

    /* Compressing ZIP files. */

    /**
     * Compresses the given directory and all its sub-directories into a ZIP
     * file.
     * <p>
     * The ZIP file must not be a directory and its parent directory must exist.
     * Will not include the root directory name in the archive.
     *
     * @param rootDir root directory.
     * @param zip     ZIP file that will be created or overwritten.
     */
    public void pack(File rootDir, File zip) {
        pack(rootDir, zip, DEFAULT_COMPRESSION_LEVEL);
    }

    /**
     * Compresses the given directory and all its sub-directories into a ZIP
     * file.
     * <p>
     * The ZIP file must not be a directory and its parent directory must exist.
     * Will not include the root directory name in the archive.
     *
     * @param rootDir          root directory.
     * @param zip              ZIP file that will be created or overwritten.
     * @param compressionLevel compression level
     */
    public void pack(File rootDir, File zip, int compressionLevel) {
        pack(rootDir, zip, IdentityNameMapper.INSTANCE, compressionLevel);
    }

    /**
     * Compresses the given directory and all its sub-directories into a ZIP
     * file.
     * <p>
     * The ZIP file must not be a directory and its parent directory must exist.
     * Will not include the root directory name in the archive.
     *
     * @param sourceDir     root directory.
     * @param targetZipFile ZIP file that will be created or overwritten.
     */
    public void pack(final File sourceDir, final File targetZipFile, final boolean preserveRoot) {
        if (preserveRoot) {
            final String parentName = sourceDir.getName();
            pack(sourceDir, targetZipFile, new NameMapper() {
                @Override
                public String map(String name) {
                    return parentName + PATH_SEPARATOR + name;
                }
            });
        } else {
            pack(sourceDir, targetZipFile);
        }
    }

    /**
     * Compresses the given file into a ZIP file.
     * <p>
     * The ZIP file must not be a directory and its parent directory must exist.
     *
     * @param fileToPack  file that needs to be zipped.
     * @param destZipFile ZIP file that will be created or overwritten.
     */
    public void packEntry(File fileToPack, File destZipFile) {
        packEntries(new File[]{fileToPack}, destZipFile);
    }

    /**
     * Compresses the given files into a ZIP file.
     * <p>
     * The ZIP file must not be a directory and its parent directory must exist.
     *
     * @param filesToPack files that needs to be zipped.
     * @param destZipFile ZIP file that will be created or overwritten.
     */
    public void packEntries(File[] filesToPack, File destZipFile) {
        log.debug("Compressing '{}' into '{}'.", filesToPack, destZipFile);

        ZipOutputStream out = null;
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(destZipFile);
            out = new ZipOutputStream(new BufferedOutputStream(fos), defaultEncoding);

            for (int i = 0; i < filesToPack.length; i++) {
                File fileToPack = filesToPack[i];

                ZipEntry zipEntry = new ZipEntry(fileToPack.getName());
                zipEntry.setSize(fileToPack.length());
                zipEntry.setTime(fileToPack.lastModified());
                out.putNextEntry(zipEntry);
                FileUtil.copy(fileToPack, out);
                out.closeEntry();
            }
        } catch (IOException e) {
            throw rethrow(e);
        } finally {
            closeQuietly(out);
            closeQuietly(fos);
        }
    }

    /**
     * Compresses the given directory and all its sub-directories into a ZIP
     * file.
     * <p>
     * The ZIP file must not be a directory and its parent directory must exist.
     *
     * @param sourceDir root directory.
     * @param targetZip ZIP file that will be created or overwritten.
     */
    public void pack(File sourceDir, File targetZip, NameMapper mapper) {
        pack(sourceDir, targetZip, mapper, DEFAULT_COMPRESSION_LEVEL);
    }

    /**
     * Compresses the given directory and all its sub-directories into a ZIP
     * file.
     * <p>
     * The ZIP file must not be a directory and its parent directory must exist.
     *
     * @param sourceDir        root directory.
     * @param targetZip        ZIP file that will be created or overwritten.
     * @param compressionLevel compression level
     */
    public void pack(File sourceDir, File targetZip, NameMapper mapper, int compressionLevel) {
        log.debug("Compressing '{}' into '{}'.", sourceDir, targetZip);

        File[] listFiles = sourceDir.listFiles();
        if (listFiles == null) {
            if (!sourceDir.exists()) {
                throw new ZipException("Given file '" + sourceDir + "' doesn't exist!");
            }
            throw new ZipException("Given file '" + sourceDir + "' is not a directory!");
        } else if (listFiles.length == 0) {
            throw new ZipException("Given directory '" + sourceDir + "' doesn't contain any files!");
        }
        try (ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(targetZip)), defaultEncoding)) {
            out.setLevel(compressionLevel);
            pack(sourceDir, out, mapper, "");
        } catch (IOException e) {
            throw rethrow(e);
        }
    }

    /**
     * Compresses the given directory and all its sub-directories into a ZIP
     * file.
     *
     * @param dir        root directory.
     * @param out        ZIP output stream.
     * @param mapper     call-back for renaming the entries.
     * @param pathPrefix prefix to be used for the entries.
     */
    private void pack(File dir, ZipOutputStream out, NameMapper mapper, String pathPrefix) throws IOException {
        File[] files = dir.listFiles();
        if (files == null) {
            throw new IOException("Given file is not a directory '" + dir + "'");
        }

        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            boolean isDir = file.isDirectory();
            String path = pathPrefix + file.getName();
            if (isDir) {
                path += PATH_SEPARATOR;
            }

            // Create a ZIP entry
            String name = mapper.map(path);
            if (name != null) {
                ZipEntry zipEntry = new ZipEntry(name);
                if (!isDir) {
                    zipEntry.setSize(file.length());
                    zipEntry.setTime(file.lastModified());
                }

                out.putNextEntry(zipEntry);

                if (isDir) {
                    log.info("directory :" + name);
                } else {
                    log.info("file :" + name);
                }

                // Copy the file content
                if (!isDir) {
                    FileUtil.copy(file, out);
                }

                out.closeEntry();
            }

            // Traverse the directory
            if (isDir) {
                pack(file, out, mapper, path);
            }
        }
    }

    /**
     * Repacks a provided ZIP file into a new ZIP with a given compression
     * level.
     * <p>
     *
     * @param srcZip           source ZIP file.
     * @param dstZip           destination ZIP file.
     * @param compressionLevel compression level.
     */
    public void repack(File srcZip, File dstZip, int compressionLevel) {

        log.debug("Repacking '{}' into '{}'.", srcZip, dstZip);

        RepackZipEntryCallback callback = new RepackZipEntryCallback(dstZip, compressionLevel);

        try {
            iterate(srcZip, callback);
        } finally {
            callback.closeStream();
        }
    }

    /**
     * Repacks a provided ZIP file and replaces old file with the new one.
     * <p>
     *
     * @param zip              source ZIP file to be repacked and replaced.
     * @param compressionLevel compression level.
     */
    public void repack(File zip, int compressionLevel) {
        try {
            File tmpZip = FileUtil.getTempFileFor(zip);

            repack(zip, tmpZip, compressionLevel);

            // Delete original zip
            if (!zip.delete()) {
                throw new IOException("Unable to delete the file: " + zip);
            }

            // Rename the archive
            FileUtils.moveFile(tmpZip, zip);
        } catch (IOException e) {
            throw rethrow(e);
        }
    }

    /**
     * RepackZipEntryCallback used in repacking methods.
     *
     * @author Pavel Grigorenko
     */
    private class RepackZipEntryCallback implements ZipEntryCallback {

        private ZipOutputStream out;

        private RepackZipEntryCallback(File dstZip, int compressionLevel) {
            try {
                this.out = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(dstZip)), defaultEncoding);
                this.out.setLevel(compressionLevel);
            } catch (IOException e) {
                rethrow(e);
            }
        }

        @Override
        public void process(InputStream in, ZipEntry zipEntry) throws IOException {
            if (log.isDebugEnabled()) {
                log.info("repacking. entity name:" + zipEntry.getName() + "  size:" + zipEntry.getSize());
            }
            copyEntry(zipEntry, in, out);
        }

        private void closeStream() {
            closeQuietly(out);
        }
    }

    /**
     * Compresses a given directory in its own location.
     * <p>
     * A ZIP file will be first created with a temporary name. After the
     * compressing the directory will be deleted and the ZIP file will be
     * renamed as the original directory.
     *
     * @param dir input directory as well as the target ZIP file.
     * @see #pack(File, File)
     */
    public void unexplode(File dir) {
        unexplode(dir, DEFAULT_COMPRESSION_LEVEL);
    }

    /**
     * Compresses a given directory in its own location.
     * <p>
     * A ZIP file will be first created with a temporary name. After the
     * compressing the directory will be deleted and the ZIP file will be
     * renamed as the original directory.
     *
     * @param dir              input directory as well as the target ZIP file.
     * @param compressionLevel compression level
     * @see #pack(File, File)
     */
    public void unexplode(File dir, int compressionLevel) {
        try {
            // Find a new unique name is the same directory
            File zip = FileUtil.getTempFileFor(dir);

            // Pack it
            pack(dir, zip, compressionLevel);

            // Delete the directory
            FileUtils.deleteDirectory(dir);

            // Rename the archive
            FileUtils.moveFile(zip, dir);
        } catch (IOException e) {
            throw rethrow(e);
        }
    }

    /**
     * Compresses the given entries into a new ZIP file.
     *
     * @param entries ZIP entries added.
     * @param zip     new ZIP file created.
     */
    public void pack(ZipEntrySource[] entries, File zip) {
        log.debug("Creating '{}' from {}.", zip, Arrays.asList(entries));

        ZipOutputStream out = null;
        try {
            out = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(zip)), defaultEncoding);
            for (int i = 0; i < entries.length; i++) {
                addEntry(entries[i], out);
            }
        } catch (IOException e) {
            throw rethrow(e);
        } finally {
            closeQuietly(out);
        }
    }

    /**
     * Copies an existing ZIP file and appends it with one new entry.
     *
     * @param zip     an existing ZIP file (only read).
     * @param path    new ZIP entry path.
     * @param file    new entry to be added.
     * @param destZip new ZIP file created.
     */
    public void addEntry(File zip, String path, File file, File destZip) {
        addEntry(zip, new FileSource(path, file), destZip);
    }

    /**
     * Changes a zip file, adds one new entry in-place.
     *
     * @param zip  an existing ZIP file (only read).
     * @param path new ZIP entry path.
     * @param file new entry to be added.
     */
    public void addEntry(final File zip, final String path, final File file) {
        operateInPlace(zip, new BaseInPlaceAction() {
            @Override
            public boolean act(File tmpFile) {
                addEntry(zip, path, file, tmpFile);
                return true;
            }
        });
    }

    /**
     * Copies an existing ZIP file and appends it with one new entry.
     *
     * @param zip     an existing ZIP file (only read).
     * @param path    new ZIP entry path.
     * @param bytes   new entry bytes (or <code>null</code> if directory).
     * @param destZip new ZIP file created.
     */
    public void addEntry(File zip, String path, byte[] bytes, File destZip) {
        addEntry(zip, new ByteSource(path, bytes), destZip);
    }

    /**
     * Changes a zip file, adds one new entry in-place.
     *
     * @param zip   an existing ZIP file (only read).
     * @param path  new ZIP entry path.
     * @param bytes new entry bytes (or <code>null</code> if directory).
     */
    public void addEntry(final File zip, final String path, final byte[] bytes) {
        operateInPlace(zip, new BaseInPlaceAction() {
            @Override
            public boolean act(File tmpFile) {
                addEntry(zip, path, bytes, tmpFile);
                return true;
            }
        });
    }

    /**
     * Copies an existing ZIP file and appends it with one new entry.
     *
     * @param zip     an existing ZIP file (only read).
     * @param entry   new ZIP entry appended.
     * @param destZip new ZIP file created.
     */
    public void addEntry(File zip, ZipEntrySource entry, File destZip) {
        addEntries(zip, new ZipEntrySource[]{entry}, destZip);
    }

    /**
     * Changes a zip file, adds one new entry in-place.
     *
     * @param zip   an existing ZIP file (only read).
     * @param entry new ZIP entry appended.
     */
    public void addEntry(final File zip, final ZipEntrySource entry) {
        operateInPlace(zip, new BaseInPlaceAction() {
            @Override
            public boolean act(File tmpFile) {
                addEntry(zip, entry, tmpFile);
                return true;
            }
        });
    }

    /**
     * Copies an existing ZIP file and appends it with new entries.
     *
     * @param zip     an existing ZIP file (only read).
     * @param entries new ZIP entries appended.
     * @param destZip new ZIP file created.
     */
    public void addEntries(File zip, ZipEntrySource[] entries, File destZip) {
        if (log.isDebugEnabled()) {
            log.info("Copying '" + zip + "' to '" + destZip + "' and adding " + Arrays.asList(entries) + ".");
        }

        ZipOutputStream out = null;
        try {
            out = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(destZip)), defaultEncoding);
            copyEntries(zip, out);
            for (int i = 0; i < entries.length; i++) {
                addEntry(entries[i], out);
            }
        } catch (IOException e) {
            throw rethrow(e);
        } finally {
            closeQuietly(out);
        }
    }

    /**
     * Changes a zip file it with with new entries. in-place.
     *
     * @param zip     an existing ZIP file (only read).
     * @param entries new ZIP entries appended.
     */
    public void addEntries(final File zip, final ZipEntrySource[] entries) {
        operateInPlace(zip, new BaseInPlaceAction() {
            @Override
            public boolean act(File tmpFile) {
                addEntries(zip, entries, tmpFile);
                return true;
            }
        });
    }

    /**
     * Copies an existing ZIP file and removes entry with a given path.
     *
     * @param zip     an existing ZIP file (only read)
     * @param path    path of the entry to remove
     * @param destZip new ZIP file created.
     * @since 1.7
     */
    public void removeEntry(File zip, String path, File destZip) {
        removeEntries(zip, new String[]{path}, destZip);
    }

    /**
     * Changes an existing ZIP file: removes entry with a given path.
     *
     * @param zip  an existing ZIP file
     * @param path path of the entry to remove
     * @since 1.7
     */
    public void removeEntry(final File zip, final String path) {
        operateInPlace(zip, new BaseInPlaceAction() {
            @Override
            public boolean act(File tmpFile) {
                removeEntry(zip, path, tmpFile);
                return true;
            }
        });
    }

    /**
     * Copies an existing ZIP file and removes entries with given paths.
     *
     * @param zip     an existing ZIP file (only read)
     * @param paths   paths of the entries to remove
     * @param destZip new ZIP file created.
     * @since 1.7
     */
    public void removeEntries(File zip, String[] paths, File destZip) {
        if (log.isDebugEnabled()) {
            log.info("Copying '" + zip + "' to '" + destZip + "' and removing paths " + Arrays.asList(paths) + ".");
        }

        ZipOutputStream out = null;
        try {
            out = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(destZip)), defaultEncoding);
            copyEntries(zip, out, new HashSet(Arrays.asList(paths)));
        } catch (IOException e) {
            throw rethrow(e);
        } finally {
            closeQuietly(out);
        }
    }

    /**
     * Changes an existing ZIP file: removes entries with given paths.
     *
     * @param zip   an existing ZIP file
     * @param paths paths of the entries to remove
     * @since 1.7
     */
    public void removeEntries(final File zip, final String[] paths) {
        operateInPlace(zip, new BaseInPlaceAction() {
            @Override
            public boolean act(File tmpFile) {
                removeEntries(zip, paths, tmpFile);
                return true;
            }
        });
    }

    /**
     * Copies all entries from one ZIP file to another.
     *
     * @param zip source ZIP file.
     * @param out target ZIP stream.
     */
    private void copyEntries(File zip, final ZipOutputStream out) {
        // this one doesn't call copyEntries with ignoredEntries, because that
        // has poorer performance
        final Set names = new HashSet();
        iterate(zip, new ZipEntryCallback() {
            @Override
            public void process(InputStream in, ZipEntry zipEntry) throws IOException {
                String entryName = zipEntry.getName();
                if (names.add(entryName)) {
                    copyEntry(zipEntry, in, out);
                } else if (log.isDebugEnabled()) {
                    log.info("Duplicate entry: {}", entryName);
                }
            }
        });
    }

    /**
     * Copies all entries from one ZIP file to another, ignoring entries with
     * path in ignoredEntries
     *
     * @param zip            source ZIP file.
     * @param out            target ZIP stream.
     * @param ignoredEntries paths of entries not to copy
     */
    private void copyEntries(File zip, final ZipOutputStream out, final Set ignoredEntries) {
        final Set names = new HashSet();
        final Set dirNames = filterDirEntries(zip, ignoredEntries);
        iterate(zip, new ZipEntryCallback() {
            @Override
            public void process(InputStream in, ZipEntry zipEntry) throws IOException {
                String entryName = zipEntry.getName();
                if (ignoredEntries.contains(entryName)) {
                    return;
                }

                Iterator iter = dirNames.iterator();
                while (iter.hasNext()) {
                    String dirName = (String) iter.next();
                    if (entryName.startsWith(dirName)) {
                        return;
                    }
                }

                if (names.add(entryName)) {
                    copyEntry(zipEntry, in, out);
                } else if (log.isDebugEnabled()) {
                    log.info("Duplicate entry: {}", entryName);
                }
            }
        });
    }

    /**
     * @param zip   zip file to traverse
     * @param names names of entries to filter dirs from
     * @return Set<String> names of entries that are dirs.
     */
    Set filterDirEntries(File zip, Collection names) {
        Set dirs = new HashSet();
        if (zip == null) {
            return dirs;
        }
        ZipFile zf = null;
        try {
            zf = new ZipFile(zip, defaultEncoding);
            Iterator iterator = names.iterator();
            while (iterator.hasNext()) {
                String entryName = (String) iterator.next();
                ZipEntry entry = zf.getEntry(entryName);
                if (entry.isDirectory()) {
                    dirs.add(entry.getName());
                } else if (zf.getInputStream(entry) == null) {
                    // no input stream means that this is a dir.
                    dirs.add(entry.getName() + PATH_SEPARATOR);
                }
            }

        } catch (IOException e) {
            throw rethrow(e);
        } finally {
            closeQuietly(zf);
        }
        return dirs;
    }

    /**
     * Copies an existing ZIP file and replaces a given entry in it.
     *
     * @param zip     an existing ZIP file (only read).
     * @param path    new ZIP entry path.
     * @param file    new entry.
     * @param destZip new ZIP file created.
     * @return <code>true</code> if the entry was replaced.
     */
    public boolean replaceEntry(File zip, String path, File file, File destZip) {
        return replaceEntry(zip, new FileSource(path, file), destZip);
    }

    /**
     * Changes an existing ZIP file: replaces a given entry in it.
     *
     * @param zip  an existing ZIP file.
     * @param path new ZIP entry path.
     * @param file new entry.
     * @return <code>true</code> if the entry was replaced.
     */
    public boolean replaceEntry(final File zip, final String path, final File file) {
        return operateInPlace(zip, new BaseInPlaceAction() {
            @Override
            public boolean act(File tmpFile) {
                return replaceEntry(zip, new FileSource(path, file), tmpFile);
            }
        });
    }

    /**
     * Copies an existing ZIP file and replaces a given entry in it.
     *
     * @param zip     an existing ZIP file (only read).
     * @param path    new ZIP entry path.
     * @param bytes   new entry bytes (or <code>null</code> if directory).
     * @param destZip new ZIP file created.
     * @return <code>true</code> if the entry was replaced.
     */
    public boolean replaceEntry(File zip, String path, byte[] bytes, File destZip) {
        return replaceEntry(zip, new ByteSource(path, bytes), destZip);
    }

    /**
     * Changes an existing ZIP file: replaces a given entry in it.
     *
     * @param zip   an existing ZIP file.
     * @param path  new ZIP entry path.
     * @param bytes new entry bytes (or <code>null</code> if directory).
     * @return <code>true</code> if the entry was replaced.
     */
    public boolean replaceEntry(final File zip, final String path, final byte[] bytes) {
        return operateInPlace(zip, new BaseInPlaceAction() {
            @Override
            public boolean act(File tmpFile) {
                return replaceEntry(zip, new ByteSource(path, bytes), tmpFile);
            }
        });
    }

    /**
     * Copies an existing ZIP file and replaces a given entry in it.
     *
     * @param zip     an existing ZIP file (only read).
     * @param entry   new ZIP entry.
     * @param destZip new ZIP file created.
     * @return <code>true</code> if the entry was replaced.
     */
    public boolean replaceEntry(File zip, ZipEntrySource entry, File destZip) {
        return replaceEntries(zip, new ZipEntrySource[]{entry}, destZip);
    }

    /**
     * Changes an existing ZIP file: replaces a given entry in it.
     *
     * @param zip   an existing ZIP file.
     * @param entry new ZIP entry.
     * @return <code>true</code> if the entry was replaced.
     */
    public boolean replaceEntry(final File zip, final ZipEntrySource entry) {
        return operateInPlace(zip, new BaseInPlaceAction() {
            @Override
            public boolean act(File tmpFile) {
                return replaceEntry(zip, entry, tmpFile);
            }
        });
    }

    /**
     * Copies an existing ZIP file and replaces the given entries in it.
     *
     * @param zip     an existing ZIP file (only read).
     * @param entries new ZIP entries to be replaced with.
     * @param destZip new ZIP file created.
     * @return <code>true</code> if at least one entry was replaced.
     */
    public boolean replaceEntries(File zip, ZipEntrySource[] entries, File destZip) {
        if (log.isDebugEnabled()) {
            log.info("Copying '" + zip + "' to '" + destZip + "' and replacing entries " + Arrays.asList(entries) + ".");
        }

        final Map entryByPath = byPath(entries);
        final int entryCount = entryByPath.size();
        try {
            final ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(destZip)), defaultEncoding);
            try {
                final Set names = new HashSet();
                iterate(zip, new ZipEntryCallback() {
                    @Override
                    public void process(InputStream in, ZipEntry zipEntry) throws IOException {
                        if (names.add(zipEntry.getName())) {
                            ZipEntrySource entry = (ZipEntrySource) entryByPath.remove(zipEntry.getName());
                            if (entry != null) {
                                addEntry(entry, out);
                            } else {
                                copyEntry(zipEntry, in, out);
                            }
                        } else if (log.isDebugEnabled()) {
                            log.info("Duplicate entry: {}", zipEntry.getName());
                        }
                    }
                });
            } finally {
                closeQuietly(out);
            }
        } catch (IOException e) {
            throw rethrow(e);
        }
        return entryByPath.size() < entryCount;
    }

    /**
     * Changes an existing ZIP file: replaces a given entry in it.
     *
     * @param zip     an existing ZIP file.
     * @param entries new ZIP entries to be replaced with.
     * @return <code>true</code> if at least one entry was replaced.
     */
    public boolean replaceEntries(final File zip, final ZipEntrySource[] entries) {
        return operateInPlace(zip, new BaseInPlaceAction() {
            @Override
            public boolean act(File tmpFile) {
                return replaceEntries(zip, entries, tmpFile);
            }
        });
    }

    /**
     * Copies an existing ZIP file and adds/replaces the given entries in it.
     *
     * @param zip     an existing ZIP file (only read).
     * @param entries ZIP entries to be replaced or added.
     * @param destZip new ZIP file created.
     */
    public void addOrReplaceEntries(File zip, ZipEntrySource[] entries, File destZip) {
        if (log.isDebugEnabled()) {
            log.info("Copying '" + zip + "' to '" + destZip + "' and adding/replacing entries " + Arrays.asList(entries) + ".");
        }

        final Map entryByPath = byPath(entries);
        try {
            final ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(destZip)), defaultEncoding);
            try {
                // Copy and replace entries
                final Set names = new HashSet();
                iterate(zip, new ZipEntryCallback() {
                    @Override
                    public void process(InputStream in, ZipEntry zipEntry) throws IOException {
                        if (names.add(zipEntry.getName())) {
                            ZipEntrySource entry = (ZipEntrySource) entryByPath.remove(zipEntry.getName());
                            if (entry != null) {
                                addEntry(entry, out);
                            } else {
                                copyEntry(zipEntry, in, out);
                            }
                        } else if (log.isDebugEnabled()) {
                            log.info("Duplicate entry: {}", zipEntry.getName());
                        }
                    }
                });

                // Add new entries
                for (Iterator it = entryByPath.values().iterator(); it.hasNext(); ) {
                    addEntry((ZipEntrySource) it.next(), out);
                }
            } finally {
                closeQuietly(out);
            }
        } catch (IOException e) {
            throw rethrow(e);
        }
    }

    /**
     * Changes a ZIP file: adds/replaces the given entries in it.
     *
     * @param zip     an existing ZIP file (only read).
     * @param entries ZIP entries to be replaced or added.
     */
    public void addOrReplaceEntries(final File zip, final ZipEntrySource[] entries) {
        operateInPlace(zip, new BaseInPlaceAction() {
            @Override
            public boolean act(File tmpFile) {
                addOrReplaceEntries(zip, entries, tmpFile);
                return true;
            }
        });
    }

    /**
     * @return given entries indexed by path.
     */
    Map byPath(ZipEntrySource[] entries) {
        Map result = new HashMap(entries.length);
        for (int i = 0; i < entries.length; i++) {
            ZipEntrySource source = entries[i];
            result.put(source.getPath(), source);
        }
        return result;
    }

    /**
     * @return given entries indexed by path.
     */
    Map byPath(Collection entries) {
        Map result = new HashMap(entries.size());
        Iterator iter = entries.iterator();
        while (iter.hasNext()) {
            ZipEntrySource source = (ZipEntrySource) iter.next();
            result.put(source.getPath(), source);
        }
        return result;
    }

    /**
     * Adds a given ZIP entry to a ZIP file.
     *
     * @param entry new ZIP entry.
     * @param out   target ZIP stream.
     */
    void addEntry(ZipEntrySource entry, ZipOutputStream out) throws IOException {
        out.putNextEntry(entry.getEntry());
        InputStream in = entry.getInputStream();
        if (in != null) {
            try {
                IOUtils.copy(in, out);
            } finally {
                closeQuietly(in);
            }
        }
        out.closeEntry();
    }

    /**
     * Adds a given ZIP entry to a ZIP file.
     *
     * @param zipEntry new ZIP entry.
     * @param in       contents of the ZIP entry.
     * @param out      target ZIP stream.
     */
    void addEntry(ZipEntry zipEntry, InputStream in, ZipOutputStream out) throws IOException {
        out.putNextEntry(zipEntry);
        if (in != null) {
            IOUtils.copy(in, out);
        }
        out.closeEntry();
    }

    /**
     * Copies a given ZIP entry to a ZIP file.
     *
     * @param zipEntry a ZIP entry from existing ZIP file.
     * @param in       contents of the ZIP entry.
     * @param out      target ZIP stream.
     */
    void copyEntry(ZipEntry zipEntry, InputStream in, ZipOutputStream out) throws IOException {
        ZipEntry copy = new ZipEntry(zipEntry.getName());
        copy.setTime(zipEntry.getTime());
        addEntry(copy, new BufferedInputStream(in), out);
    }

    /* Comparing two ZIP files. */

    /**
     * Compares same entry in two ZIP files (byte-by-byte).
     *
     * @param f1   first ZIP file.
     * @param f2   second ZIP file.
     * @param path name of the entry.
     * @return <code>true</code> if the contents of the entry was same in both
     * ZIP files.
     */
    public boolean entryEquals(File f1, File f2, String path) {
        return entryEquals(f1, f2, path, path);
    }

    /**
     * Compares two ZIP entries (byte-by-byte). .
     *
     * @param f1    first ZIP file.
     * @param f2    second ZIP file.
     * @param path1 name of the first entry.
     * @param path2 name of the second entry.
     * @return <code>true</code> if the contents of the entries were same.
     */
    public boolean entryEquals(File f1, File f2, String path1, String path2) {
        ZipFile zf1 = null;
        ZipFile zf2 = null;

        try {
            zf1 = new ZipFile(f1, defaultEncoding);
            zf2 = new ZipFile(f2, defaultEncoding);

            return doEntryEquals(zf1, zf2, path1, path2);
        } catch (IOException e) {
            throw rethrow(e);
        } finally {
            closeQuietly(zf1);
            closeQuietly(zf2);
        }
    }

    /**
     * Compares two ZIP entries (byte-by-byte). .
     *
     * @param zf1   first ZIP file.
     * @param zf2   second ZIP file.
     * @param path1 name of the first entry.
     * @param path2 name of the second entry.
     * @return <code>true</code> if the contents of the entries were same.
     */
    public boolean entryEquals(ZipFile zf1, ZipFile zf2, String path1, String path2) {
        try {
            return doEntryEquals(zf1, zf2, path1, path2);
        } catch (IOException e) {
            throw rethrow(e);
        }
    }

    /**
     * Compares two ZIP entries (byte-by-byte). .
     *
     * @param zf1   first ZIP file.
     * @param zf2   second ZIP file.
     * @param path1 name of the first entry.
     * @param path2 name of the second entry.
     * @return <code>true</code> if the contents of the entries were same.
     */
    private boolean doEntryEquals(ZipFile zf1, ZipFile zf2, String path1, String path2) throws IOException {
        InputStream is1 = null;
        InputStream is2 = null;
        try {
            ZipEntry e1 = zf1.getEntry(path1);
            ZipEntry e2 = zf2.getEntry(path2);

            if (e1 == null && e2 == null) {
                return true;
            }

            if (e1 == null || e2 == null) {
                return false;
            }

            is1 = zf1.getInputStream(e1);
            is2 = zf2.getInputStream(e2);
            if (is1 == null && is2 == null) {
                return true;
            }
            if (is1 == null || is2 == null) {
                return false;
            }

            return IOUtils.contentEquals(is1, is2);
        } finally {
            closeQuietly(is1);
            closeQuietly(is2);
        }
    }

    /**
     * Closes the ZIP file while ignoring any errors.
     *
     * @param zf ZIP file to be closed.
     */
    public void closeQuietly(ZipFile zf) {
        try {
            if (zf != null) {
                zf.close();
            }
        } catch (IOException e) {
        }
    }

    /**
     * Rethrow the given exception as a runtime exception.
     */
    ZipException rethrow(IOException e) {
        throw new ZipException(e);
    }

    /**
     * Simple helper to make inplace operation easier
     *
     * @author shelajev
     */
    private abstract class BaseInPlaceAction {

        /**
         * act
         *
         * @param tmpFile 文件
         * @return true if something has been changed during the action.
         */
        abstract boolean act(File tmpFile);
    }

    /**
     * This method provides a general infrastructure for in-place operations. It
     * creates temp file as a destination, then invokes the action on source and
     * destination. Then it copies the result back into src file.
     *
     * @param src    - source zip file we want to modify
     * @param action - action which actually modifies the archives
     * @return result of the action
     */
    private boolean operateInPlace(File src, BaseInPlaceAction action) {
        File tmp = null;
        try {
            tmp = File.createTempFile("zt-zip-tmp", ".zip");
            boolean result = action.act(tmp);
            if (result) {
                FileUtils.forceDelete(src);
                FileUtils.moveFile(tmp, src);
            }
            // else nothing changes
            return result;
        } catch (IOException e) {
            throw rethrow(e);
        } finally {
            FileUtils.deleteQuietly(tmp);
        }
    }

    public class ZipException extends RuntimeException {

        private static final long serialVersionUID = -2429392488218867015L;

        public ZipException(String msg) {
            super(msg);
        }

        public ZipException(Exception e) {
            super(e);
        }
    }

    static final class FileUtil {

        private FileUtil() {
        }

        /**
         * Copies the given file into an output stream.
         *
         * @param file input file (must exist).
         * @param out  output stream.
         */
        public static void copy(File file, OutputStream out) throws IOException {
            FileInputStream in = new FileInputStream(file);
            try {
                IOUtils.copy(new BufferedInputStream(in), out);
            } finally {
                closeQuietly(in);
            }
        }

        /**
         * Copies the given input stream into a file.
         * <p>
         * The target file must not be a directory and its parent must exist.
         *
         * @param in   source stream.
         * @param file output file to be created or overwritten.
         */
        public static void copy(InputStream in, File file) throws IOException {
            OutputStream out = new BufferedOutputStream(new FileOutputStream(file));
            try {
                IOUtils.copy(in, out);
            } finally {
                closeQuietly(out);
            }
        }

        /**
         * Find a non-existing file in the same directory using the same name as
         * prefix.
         *
         * @param file file used for the name and location (it is not read or
         *             written).
         * @return a non-existing file in the same directory using the same name
         * as prefix.
         */
        public static File getTempFileFor(File file) {
            File parent = file.getParentFile();
            String name = file.getName();
            File result;
            int index = 0;
            do {
                result = new File(parent, name + "_" + index++);
            } while (result.exists());
            return result;
        }

    }

    public interface ZipEntryCallback {

        /**
         * Invoked for each entry in a ZIP file.
         *
         * @param in       contents of the ZIP entry.
         * @param zipEntry ZIP entry.
         * @throws IOException io错误
         */
        void process(InputStream in, ZipEntry zipEntry) throws IOException;

    }

    /**
     * Call-back for filtering and renaming ZIP entries while packing or
     * unpacking.
     *
     * @author Rein Raudjärv
     */
    public interface NameMapper {

        /**
         * map
         *
         * @param name original name.
         * @return name to be stored in the ZIP file or the destination
         * directory, <code>null</code> means that the entry will be
         * skipped.
         */
        String map(String name);

    }

    /**
     * NOP implementation of the name mapper.
     *
     * @author Rein Raudjärv
     * @see NameMapper
     */
    static final class IdentityNameMapper implements NameMapper {

        public static final NameMapper INSTANCE = new IdentityNameMapper();

        private IdentityNameMapper() {
        }

        @Override
        public String map(String name) {
            return name;
        }

    }

    /**
     * ZIP entry with its contents.
     *
     * @author Rein Raudjärv
     */
    public interface ZipEntrySource {

        /**
         * getPath
         *
         * @return path of the given entry (not <code>null</code>).
         */
        String getPath();

        /**
         * getEntry
         *
         * @return meta-data of the given entry (not <code>null</code>).
         */
        ZipEntry getEntry();

        /**
         * getInputStream
         *
         * @return an input stream of the given entry or <code>null</code> if
         * this entry is a directory.
         * @throws IOException io错误
         */
        InputStream getInputStream() throws IOException;

    }

    public class FileSource implements ZipEntrySource {

        private final String path;
        private final File file;

        public FileSource(String path, File file) {
            this.path = path;
            this.file = file;
        }

        @Override
        public String getPath() {
            return path;
        }

        @Override
        public ZipEntry getEntry() {
            ZipEntry entry = new ZipEntry(path);
            if (!file.isDirectory()) {
                entry.setSize(file.length());
            }
            entry.setTime(file.lastModified());
            return entry;
        }

        @Override
        public InputStream getInputStream() throws IOException {
            if (file.isDirectory()) {
                return null;
            } else {
                return new BufferedInputStream(new FileInputStream(file));
            }
        }

        @Override
        public String toString() {
            return "FileSource[" + path + ", " + file + "]";
        }

    }

    public class ByteSource implements ZipEntrySource {

        private final String path;
        private final byte[] bytes;
        private final long time;

        public ByteSource(String path, byte[] bytes) {
            this(path, bytes, System.currentTimeMillis());
        }

        public ByteSource(String path, byte[] bytes, long time) {
            this.path = path;
            this.bytes = (byte[]) bytes.clone();
            this.time = time;
        }

        @Override
        public String getPath() {
            return path;
        }

        @Override
        public ZipEntry getEntry() {
            ZipEntry entry = new ZipEntry(path);
            if (bytes != null) {
                entry.setSize(bytes.length);
            }
            entry.setTime(time);
            return entry;
        }

        @Override
        public InputStream getInputStream() throws IOException {
            if (bytes == null) {
                return null;
            } else {
                return new ByteArrayInputStream(bytes);
            }
        }

        @Override
        public String toString() {
            return "ByteSource[" + path + "]";
        }

    }

    public class ZipBreakException extends RuntimeException {
        private static final long serialVersionUID = 5863129897045964421L;

        public ZipBreakException(String msg) {
            super(msg);
        }

        public ZipBreakException(Exception e) {
            super(e);
        }

        public ZipBreakException() {
            super();
        }
    }

    /**
     * Call-back for traversing ZIP entries without their contents.
     *
     * @author Rein Raudjärv
     * @see ZipEntryCallback
     */
    public interface ZipInfoCallback {

        /**
         * Invoked for each entry in a ZIP file.
         *
         * @param zipEntry ZIP entry.
         * @throws IOException io错误
         */
        void process(ZipEntry zipEntry) throws IOException;

    }

    public static void closeQuietly(final Closeable closeable) {
        try {
            if (closeable != null) {
                closeable.close();
            }
        } catch (final IOException ioe) {
            // ignore
        }
    }
}
