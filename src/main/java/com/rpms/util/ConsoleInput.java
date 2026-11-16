package com.rpms.util;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Scanner;
public final class ConsoleInput {
 public static final Scanner IN=new Scanner(System.in);
 private ConsoleInput(){}
 public static String text(String label){System.out.print(label);return IN.nextLine().trim();}
 public static int integer(String label){while(true){try{return Integer.parseInt(text(label));}catch(Exception e){System.out.println("Enter a valid whole number.");}}}
 public static BigDecimal decimal(String label){while(true){try{return new BigDecimal(text(label));}catch(Exception e){System.out.println("Enter a valid number.");}}}
 public static LocalDate date(String label){while(true){try{return LocalDate.parse(text(label));}catch(Exception e){System.out.println("Use YYYY-MM-DD.");}}}
}
