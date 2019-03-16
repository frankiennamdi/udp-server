package com.franklin.sample.udp.test;

import org.assertj.core.util.Lists;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

class TestSupport {

  static List<String> unzip(String zipFile, String outputFolder) throws IOException {
    byte[] buffer = new byte[1024];
    File folder = new File(outputFolder);
    List<String> files = Lists.newArrayList();
    if (!folder.exists()) {
      folder.mkdir();
    }
    ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile));
    ZipEntry ze = zis.getNextEntry();
    while (ze != null) {
      String fileName = ze.getName();
      File newFile = new File(outputFolder + File.separator + fileName);
      new File(newFile.getParent()).mkdirs();
      files.add(newFile.getAbsolutePath());
      FileOutputStream fos = new FileOutputStream(newFile);
      int len;
      while ((len = zis.read(buffer)) > 0) {
        fos.write(buffer, 0, len);
      }
      fos.close();
      ze = zis.getNextEntry();
    }
    zis.closeEntry();
    zis.close();
    return files;
  }
}
