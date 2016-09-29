package com.java.sftp;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelSftp.LsEntry;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

/**
 * ����JSch��ʵ��SFTP���ء��ϴ��ļ�����
 * 
 * @author 
 * @version 1.0
 */
public class SftpManager {
	private static final Logger logger = LoggerFactory.getLogger(SftpManager.class);
	public static final String SFTP_PROTOCAL = "sftp";

	/**
	 * Password authorization
	 * 
	 * @param host
	 *            ����IP
	 * @param username
	 *            ������½�û���
	 * @param password
	 *            ������½����
	 * @param port
	 *            ����ssh��½�˿ڣ����port <= 0ȡĬ��ֵ(22)
	 * @return sftp
	 * @throws Exception
	 * @see http://www.jcraft.com/jsch/
	 */
	public static ChannelSftp connect(String host, String username, String password, int port) throws Exception {
		Channel channel = null;
		ChannelSftp sftp = null;
		JSch jsch = new JSch();

		Session session = createSession(jsch, host, username, port);
		// ���õ�½����������
		session.setPassword(password);
		// ���õ�½��ʱʱ��
		session.connect(15000);
		logger.info("Session connected to " + host + ".");
		try {
			// ����sftpͨ��ͨ��
			channel = (Channel) session.openChannel(SFTP_PROTOCAL);
			channel.connect(1000);
			logger.info("Channel created to " + host + ".");
			sftp = (ChannelSftp) channel;
		} catch (JSchException e) {
			logger.error("exception when channel create.", e);
		}
		return sftp;
	}

	/**
	 * Private/public key authorization (������Կ��ʽ��½)
	 * 
	 * @param username
	 *            ������½�û���(user account)
	 * @param host
	 *            ����IP(server host)
	 * @param port
	 *            ����ssh��½�˿�(ssh port), ���port<=0, ȡĬ��ֵ22
	 * @param privateKey
	 *            ��Կ�ļ�·��(the path of key file.)
	 * @param passphrase
	 *            ��Կ������(the password of key file.)
	 * @return sftp
	 * @throws Exception
	 * @see http://www.jcraft.com/jsch/
	 */
	public static ChannelSftp connect(String username, String host, int port, String privateKey, String passphrase)
			throws Exception {
		Channel channel = null;
		ChannelSftp sftp = null;
		JSch jsch = new JSch();

		// ������Կ������ ,֧����Կ�ķ�ʽ��½
		if (StringUtils.isNotEmpty(privateKey)) {
			if (StringUtils.isNotEmpty(passphrase)) {
				// ���ô��������Կ
				jsch.addIdentity(privateKey, passphrase);
			} else {
				// ���ò����������Կ
				jsch.addIdentity(privateKey);
			}
		}
		Session session = createSession(jsch, host, username, port);
		// ���õ�½��ʱʱ��
		session.connect(15000);
		logger.info("Session connected to " + host + ".");
		try {
			// ����sftpͨ��ͨ��
			channel = (Channel) session.openChannel(SFTP_PROTOCAL);
			channel.connect(1000);
			logger.info("Channel created to " + host + ".");
			sftp = (ChannelSftp) channel;
		} catch (JSchException e) {
			logger.error("exception when channel create.", e);
		}
		return sftp;
	}

	/**
	 * upload all the files to the server<br/>
	 * �������ļ���Ϊ srcFile ���ļ��ϴ���Ŀ�������, Ŀ���ļ���Ϊ dest,<br/>
	 * �� destΪĿ¼����Ŀ���ļ�������srcFile�ļ�����ͬ. ����Ĭ�ϵĴ���ģʽ�� OVERWRITE
	 * 
	 * @param sftp
	 * @param srcFile
	 *            �����ļ��ľ���·��
	 * @param dest
	 *            Ŀ���ļ��ľ���·��
	 */
	public static void upload(ChannelSftp sftp, String srcFile, String dest) {
		try {
			File file = new File(srcFile);
			if (file.isDirectory()) {
				sftp.cd(srcFile);
				for (String fileName : file.list()) {
					sftp.put(srcFile + SystemUtils.FILE_SEPARATOR + fileName, dest);
				}
			}
			sftp.put(srcFile, dest);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * upload all the files to the server<br/>
	 * ��fileList�еı����ļ��ϴ���Ŀ�������, Ŀ��Ŀ¼·��Ϊ destPath,<br/>
	 * destPath������Ŀ¼��Ŀ���ļ�������Դ�ļ�����ͬ. ����Ĭ�ϵĴ���ģʽ�� OVERWRITE
	 * 
	 * @param sftp
	 * @param fileList
	 *            Ҫ�ϴ���Ŀ����������ļ��ľ���·��
	 * @param destPath
	 *            Ŀ���ļ��ľ���·��, һ����Ŀ¼, ���Ŀ¼���������Զ�����
	 * @throws SftpException
	 */
	public static void upload(ChannelSftp sftp, List<String> fileList, String destPath) throws SftpException {
		try {
			sftp.cd(destPath);
		} catch (Exception e) {
			sftp.mkdir(destPath);
		}
		for (String srcFile : fileList) {
			upload(sftp, srcFile, destPath);
		}
	}

	/**
	 * ʹ��sftp�����ļ�
	 * 
	 * @param sftp
	 * @param srcPath
	 *            sftp��������Դ�ļ���·��, ������Ŀ¼
	 * @param saveFile
	 *            ���غ��ļ��Ĵ洢·��, ��ΪĿ¼, ���ļ�������Ŀ��������ϵ��ļ�����ͬ
	 * @param srcfile
	 *            Ŀ��������ϵ��ļ�, ����ΪĿ¼
	 */
	public static void download(ChannelSftp sftp, String srcPath, String saveFile, String srcfile) {
		try {
			sftp.cd(srcPath);
			File file = new File(saveFile);
			if (file.isDirectory()) {
				sftp.get(srcfile, new FileOutputStream(file + SystemUtils.FILE_SEPARATOR + srcfile));
			} else {
				sftp.get(srcfile, new FileOutputStream(file));
			}
		} catch (Exception e) {
			logger.error("download file: {} error", srcPath + SystemUtils.FILE_SEPARATOR + srcfile, e);
		}
	}

	/**
	 * ʹ��sftp����Ŀ���������ĳ��Ŀ¼��ָ�����͵��ļ�, �õ����ļ����� sftp�������ϵ���ͬ
	 * 
	 * @param sftp
	 * @param srcPath
	 *            sftp��������ԴĿ¼��·��, ������Ŀ¼
	 * @param savePath
	 *            ���غ��ļ��洢��Ŀ¼·��, һ����Ŀ¼, ������������Զ�����
	 * @param fileTypes
	 *            ָ�����͵��ļ�, �ļ��ĺ�׺����ɵ��ַ�������
	 */
	public static void download(ChannelSftp sftp, String srcPath, String savePath, String... fileTypes) {
		List<String> fileList = new ArrayList<String>();
		try {
			sftp.cd(srcPath);
			createDir(savePath);
			if (fileTypes.length == 0) {
				// �г�������Ŀ¼�����е��ļ��б�
				fileList = listFiles(sftp, srcPath, "*");
				downloadFileList(sftp, srcPath, savePath, fileList);
				return;
			}
			for (String type : fileTypes) {
				fileList = listFiles(sftp, srcPath, "*" + type);
				parseAndUpdateDB(sftp, srcPath, savePath, fileList);
			}
		} catch (Exception e) {
			logger.error("download all file in path = '" + srcPath + "' and type in " + Arrays.asList(fileTypes)
					+ " error", e);
		}

	}

	private static File createDir(String savePath) throws Exception {
		File localPath = new File(savePath);
		if (!localPath.exists() && !localPath.isFile()) {
			if (!localPath.mkdir()) {
				throw new Exception(localPath + " directory can not create.");
			}
		}
		return localPath;
	}

	/**
	 * sftp����Ŀ���������srcPathĿ¼������ָ�����ļ�.<br/>
	 * �����ش洢·���´����������������ļ�,�Լ������ز����Ǹ��ļ�.<br/>
	 * 
	 * @param sftp
	 * @param savePath
	 *            �ļ����ص����ش洢��·��,������Ŀ¼
	 * @param fileList
	 *            ָ����Ҫ���ص��ļ����б�
	 * @throws SftpException
	 * @throws FileNotFoundException
	 */
	public static void downloadFileList(ChannelSftp sftp, String srcPath, String savePath, List<String> fileList)
			throws SftpException, FileNotFoundException {
		sftp.cd(srcPath);
		for (String srcFile : fileList) {
			logger.info("srcFile: " + srcFile);
			String localPath = savePath + SystemUtils.FILE_SEPARATOR + srcFile;
			sftp.get(srcFile, localPath);
		}
	}

	/**
	 * sftp����Ŀ�������������ָ�����ļ�, �������ļ�������.<br/>
	 * �����ش洢·���´����������������ļ�, �����(������)���ļ�.<br/>
	 * 
	 * @param sftp
	 * @param srcPath
	 *            sftp��Դ�ļ���Ŀ¼
	 * @param savePath
	 *            �ļ����ص����ش洢��·��,������Ŀ¼
	 * @param fileList
	 *            ָ����Ҫ���ص��ļ��б�
	 * @throws FileNotFoundException
	 * @throws SftpException
	 */
	private static void parseAndUpdateDB(ChannelSftp sftp, String srcPath, String savePath, List<String> fileList)
			throws FileNotFoundException, SftpException {
		sftp.cd(srcPath);
		for (String srcFile : fileList) {
			String localPath = savePath + SystemUtils.FILE_SEPARATOR + srcFile;
			File localFile = new File(localPath);
			// savePath·���������ļ��������ļ�����, ��������ļ�
			if (localFile.exists() && localFile.isFile()) {
				continue;
			}

			logger.info("start downloading file: [" + srcFile + "], parseAndUpdate to DB");
			sftp.get(srcFile, localPath);
			//updateDB(localFile);
		}
	}

	/**
	 * ��ȡsrcPath·������regex��ʽָ�����ļ��б�
	 * 
	 * @param sftp
	 * @param srcPath
	 *            sftp�������ϵ�Ŀ¼
	 * @param regex
	 *            ��Ҫƥ����ļ���
	 * @return
	 * @throws SftpException
	 */
	@SuppressWarnings("unchecked")
	public static List<String> listFiles(ChannelSftp sftp, String srcPath, String regex) throws SftpException {
		List<String> fileList = new ArrayList<String>();
		sftp.cd(srcPath); // ���srcPath����Ŀ¼����׳��쳣
		if ("".equals(regex) || regex == null) {
			regex = "*";
		}
		Vector<LsEntry> sftpFile = sftp.ls(regex);
		String fileName = null;
		for (LsEntry lsEntry : sftpFile) {
			fileName = lsEntry.getFilename();
			fileList.add(fileName);
		}
		return fileList;
	}

	/**
	 * ɾ���ļ�
	 * 
	 * @param dirPath
	 *            Ҫɾ���ļ�����Ŀ¼
	 * @param file
	 *            Ҫɾ�����ļ�
	 * @param sftp
	 * @throws SftpException
	 */
	public static void delete(String dirPath, String file, ChannelSftp sftp) throws SftpException {
		String now = sftp.pwd();
		sftp.cd(dirPath);
		sftp.rm(file);
		sftp.cd(now);
	}

	/**
	 * Disconnect with server
	 */
	public static void disconnect(ChannelSftp sftp) {
		try {
			if (sftp != null) {
				if (sftp.isConnected()) {
					sftp.disconnect();
				} else if (sftp.isClosed()) {
					logger.info("sftp is closed already");
				}
				if (null != sftp.getSession()) {
					sftp.getSession().disconnect();
				}
			}
		} catch (JSchException e) {
			// Ignore
		}

	}

	private static Session createSession(JSch jsch, String host, String username, int port) throws Exception {
		Session session = null;
		if (port <= 0) {
			// ���ӷ�����������Ĭ�϶˿�
			session = jsch.getSession(username, host);
		} else {
			// ����ָ���Ķ˿����ӷ�����
			session = jsch.getSession(username, host, port);
		}
		// ������������Ӳ��ϣ����׳��쳣
		if (session == null) {
			throw new Exception(host + "session is null");
		}
		// ���õ�һ�ε�½��ʱ����ʾ����ѡֵ��(ask | yes | no)
		session.setConfig("StrictHostKeyChecking", "no");
		return session;
	}

	public static void main(String[] args) throws Exception {
		
		
		
	}
}
