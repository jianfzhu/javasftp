package com.java.sftp;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.jcraft.jsch.ChannelSftp;

public class TestSFTP {
	public static void main(String[] args){
		String host="10.40.100.204";
		String username="G360682";
		String password="G360682";
		int port=22;
		SftpManager SftpM = new SftpManager();
		try {
			ChannelSftp sftp = SftpM.connect(host, username, password, port);
			List<String> l = SftpM.listFiles(sftp, "ISS_FIFO_DDSR/backup", ".csv");
			System.out.println(l);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	
	}

}
