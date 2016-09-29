package com.java.sftp;


import java.io.*;
import java.sql.SQLException;
import java.util.*;
import org.apache.log4j.BasicConfigurator;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class TestLog4j{

   /* Get actual class name to be printed on */
   static Logger log = Logger.getLogger(TestLog4j.class);

   public static void main(String[] args)throws IOException,SQLException{
	   //PropertyConfigurator.configure("Log4j.properties");
	   log.debug("Hello this is a debug message");
       log.info("Hello this is an info message");
   }
}