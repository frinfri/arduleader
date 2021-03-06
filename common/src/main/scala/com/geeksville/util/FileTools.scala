package com.geeksville.util

import scala.io._
import java.io._

import Using._

object FileTools {

  /// Return extension string for this file
  def getExtension(fs: String): Option[String] = {
    val dot = fs.lastIndexOf('.')
    if (dot == -1)
      None
    else
      Some(fs.substring(dot + 1))
  }

  /// Extract all bytes from an inputstream
  /// FIXME - definitely not super efficient
  def toByteArray(src: InputStream) = Stream.continually(src.read).takeWhile(-1 !=).map(_.toByte).toArray

  /// Copy from an inputStream to an OutputStream
  /// it is the caller's responsiblity to close streams
  def copy(src: InputStream, dest: OutputStream) {
    val b = new Array[Byte](4096)
    var numread = -1
    do {
      numread = src.read(b)
      //printf("read %d bytes\n", numread)
      if (numread > 0)
        dest.write(b, 0, numread)
    } while (numread != -1)

    // Unsuprisingly this was a little slow - especially on Android
    // Source.fromInputStream(src).foreach(dest.write(_))
  }

  /// Return a list of filtered filenames
  /// FIXME Play with add-on methods
  def list(dir: File, filter: (File, String) => Boolean): Array[String] = {
    dir.list(new FilenameFilter {
      override def accept(f: File, s: String) = filter(f, s)
    })
  }

  /// If necessary create the named directory
  /// @return a File for the new directory
  def mkdirs(dirname: String): File = mkdirs(new File(dirname))

  /// If necessary create the named directory
  /// @return a File for the new directory
  def mkdirs(file: File): File = {
    if (!file.isDirectory)
      if (!file.mkdirs())
        throw new FileNotFoundException("Can't create " + file)

    file
  }

  /// Return a guaranteed unique name.   
  /// If the specified filename already exists, keep adding a short suffix until it is unique.
  /// (we assume collisions are rare)
  def uniqueFile(dir: File, baseName: String, suffix: String) = {
    def makeUnique(extra: Int): File = {
      val extraStr = if (extra == 0) "" else "_%02d".format(extra)
      val candidate = new File(dir, baseName + extraStr + suffix)
      if (!candidate.exists)
        candidate
      else
        makeUnique(extra + 1)
    }

    makeUnique(0)
  }

  type OkCallback = (File, String) => Boolean

  /// Generate a FilenameFilter suitable for use with File.listFiles
  def filenameFilter(isOkay: OkCallback) = new FilenameFilter {
    override def accept(dir: File, filename: String) = isOkay(dir, filename)
  }
}
