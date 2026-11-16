package com.rpms.util;
import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
public final class ActivityLogger {
 private ActivityLogger(){}
 public static void log(String message){
  try { Files.writeString(Path.of("activity_log.txt"),LocalDateTime.now()+" | "+message+System.lineSeparator(),StandardOpenOption.CREATE,StandardOpenOption.APPEND); }
  catch(IOException e){ System.err.println("Could not write activity log: "+e.getMessage()); }
 }
}
