package Linux;

import java.io.FileWriter;
import java.io.InputStream;
import java.io.PrintWriter;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

public class Distributed_Setup {
 public static void add_search_server(String sh_ip, String idx_ip) {
  String session_user = "root";
  String session_user_pwd = "admin";
  String login_user = "admin";
  String login_pwd = "admin123";
  String Splunk_Home = " /opt/splunk/bin/splunk ";
  String add_server = "add search-server https://" + idx_ip + ":8089 -auth " + login_user + ":" + login_pwd + " -remoteUsername " + login_user + " -remotePassword " + login_pwd;
  String command = Splunk_Home + add_server;
  try {
   FileWriter fileWriter = new FileWriter("Distributed_Setup.txt");
   PrintWriter printWriter = new PrintWriter(fileWriter);

   java.util.Properties config = new java.util.Properties();
   config.put("StrictHostKeyChecking", "no");
   JSch jsch = new JSch();
   Session session = jsch.getSession(session_user, sh_ip, 22);
   session.setPassword(session_user_pwd);
   session.setConfig(config);
   session.connect();
   System.out.println("Connected to ip: " + sh_ip);

   Channel channel = session.openChannel("exec");

   printWriter.append(command);
   ((ChannelExec) channel).setCommand(command);
   channel.setInputStream(null);
   ((ChannelExec) channel).setErrStream(System.err);

   InputStream in = channel.getInputStream();
   channel.connect();
   byte[] tmp = new byte[1024];
   while (true) {
    while ( in .available() > 0) {
     int i = in .read(tmp, 0, 1024);
     if (i < 0) break;
     printWriter.append(new String(tmp, 0, i));
     System.out.print(new String(tmp, 0, i));
    }
    if (channel.isClosed()) {
     System.out.println("Channel Closed status: " + channel.getExitStatus());
     break;
    }
    try {
     Thread.sleep(1000);
    } catch (Exception ee) {}
   }
   channel.disconnect();
   session.disconnect();
   System.out.println(idx_ip + " added into Distributed Search");
   System.out.println();
   printWriter.close();
  } catch (Exception e) {
   e.printStackTrace();
  }
 }
 public static void remove_search_server(String sh_ip, String idx_ip) {
  String session_user = "root";
  String session_user_pwd = "admin";
  String login_user = "admin";
  String login_pwd = "admin123";
  String Splunk_Home = " /opt/splunk/bin/splunk ";
  String remove_server = Splunk_Home + "remove search-server -auth " + login_user + ":" + login_pwd + " " + idx_ip + ":8089";
  try {
   java.util.Properties config = new java.util.Properties();
   config.put("StrictHostKeyChecking", "no");
   JSch jsch = new JSch();
   Session session = jsch.getSession(session_user, sh_ip, 22);
   session.setPassword(session_user_pwd);
   session.setConfig(config);
   session.connect();
   System.out.println("Connected to ip: " + sh_ip);

   Channel channel = session.openChannel("exec");
   ((ChannelExec) channel).setCommand(remove_server);
   channel.setInputStream(null);
   ((ChannelExec) channel).setErrStream(System.err);

   InputStream in = channel.getInputStream();
   channel.connect();
   byte[] tmp = new byte[1024];
   while (true) {
    while ( in .available() > 0) {
     int i = in .read(tmp, 0, 1024);
     if (i < 0) break;
     System.out.print(new String(tmp, 0, i));
    }
    if (channel.isClosed()) {
     System.out.println("Channel Closed status: " + channel.getExitStatus());
     break;
    }
    try {
     Thread.sleep(1000);
    } catch (Exception ee) {}
   }
   channel.disconnect();
   session.disconnect();
   System.out.println(idx_ip + " removed from Distributed Search");
   System.out.println();
  } catch (Exception e) {
   e.printStackTrace();
  }
 }
 public static void main(String[] args) {
  String SH_ip = "10.0.0.1";
  String IDX1_ip = "10.0.0.2";
  String IDX2_ip = "10.0.0.3";

  //add
  add_search_server(SH_ip, IDX1_ip);
  add_search_server(SH_ip, IDX2_ip);

  // remove		
  remove_search_server(SH_ip, IDX1_ip);
  remove_search_server(SH_ip, IDX2_ip);

  System.out.println("Done-------------------");
 }
}