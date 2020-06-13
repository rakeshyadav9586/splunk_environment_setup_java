import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

public class Cluster_Push_App_general {
 public static void main(String[] args) throws IOException {
  FileWriter fileWriter = new FileWriter("Cluster_Push_App.txt");
  PrintWriter printWriter = new PrintWriter(fileWriter);

  String master = "10.0.0.1";
  String Deployer = "10.0.0.2";
  String sh = "10.0.0.8";
  String ssh_pwd = "admin";
  String ips[] = {
   master,
   Deployer
  };
  for (int j = 0; j < ips.length; j++) {
   String ta_build_name = null;
   String app_build_name = null;
   String src_path_ta = "C:\\Rakesh\\Latest_Build\\TA\\";
   String src_path_app = "C:\\Rakesh\\Latest_Build\\App\\";
   String dest_path_master = "/opt/splunk/etc/master-apps";
   String des_path_shcluster = "/opt/splunk/etc/shcluster/apps";

   File file_ta = new File("C:\\Rakesh\\Latest_Build\\TA\\");
   String[] fileList_ta = file_ta.list();
   for (String ta_name: fileList_ta)
    ta_build_name = ta_name;

   File file_app = new File("C:\\Rakesh\\Latest_Build\\App\\");
   String[] fileList_app = file_app.list();
   for (String app_name: fileList_app)
    app_build_name = app_name;

   String server_ip = null;
   String ip = master;
   if (ip.equalsIgnoreCase(ips[j])) {
    server_ip = "pscp -pw " + ssh_pwd + " " + src_path_ta + ta_build_name + " root@" + ips[j] + ":" + dest_path_master;
    System.out.println(server_ip);
   } else {
    server_ip = "pscp -pw " + ssh_pwd + " " + src_path_app + app_build_name + " root@" + ips[j] + ":" + des_path_shcluster;
    System.out.println(server_ip);
   }

   System.out.println("Copy latest build on " + ips[j]);
   try {
    String[] command = new String[3];
    command[0] = "cmd";
    command[1] = "/c";
    command[2] = server_ip;

    Process p = Runtime.getRuntime().exec(command);

    BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
    String line = reader.readLine();
    while (line != null) {
     System.out.println(line);
     line = reader.readLine();
    }
    System.out.println();
    BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
    BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));
    String Error;
    while ((Error = stdError.readLine()) != null) {
     System.out.println(Error);
    }
    while ((Error = stdInput.readLine()) != null) {
     System.out.println(Error);
    }
    System.out.println("Latest Build Copy Done !");
   } catch (Exception e) {
    e.printStackTrace();
   }
   //connect ssh and run the command
   String user = "root";
   String user_pwd = "admin";
   String login_pwd = "admin:admin123";
   String master_path = " /opt/splunk/etc/master-apps/";
   String deployer_path = " /opt/splunk/etc/shcluster/apps/";
   String splunk_home = "/opt/splunk/bin/splunk ";

   String deployer_push = "tar -xvzf" + deployer_path + app_build_name + " -C" + deployer_path + " && " + "rm -rf" + deployer_path + app_build_name + " && " + splunk_home + "apply shcluster-bundle --answer-yes -target https://" + sh + ":8089 -auth " + login_pwd + " && " + splunk_home + "show shcluster-status";
   System.out.println(deployer_push);

   String master_push = "tar -xvzf" + master_path + ta_build_name + " -C" + master_path + " && " + "rm -rf" + master_path + ta_build_name + " && " + splunk_home + "apply cluster-bundle --answer-yes -auth " + login_pwd + " && " + splunk_home + "show cluster-bundle-status -auth " + login_pwd;
   System.out.println(master_push);

   try {
    java.util.Properties config = new java.util.Properties();
    config.put("StrictHostKeyChecking", "no");
    JSch jsch = new JSch();
    Session session = jsch.getSession(user, ips[j], 22);
    session.setPassword(user_pwd);
    session.setConfig(config);
    session.connect();
    System.out.println("Connected to Linux ip : " + ips[j]);
    Channel channel = session.openChannel("exec");
    if (ips[j].equalsIgnoreCase(Deployer)) {
     printWriter.append(deployer_push);
     ((ChannelExec) channel).setCommand(deployer_push);
     System.out.println("IP : " + ips[j] + " App push successfully on SHs");

    } else if (ips[j].equalsIgnoreCase(master)) {
     printWriter.append(master_push);
     ((ChannelExec) channel).setCommand(master_push);
     System.out.println("IP : " + ips[j] + " App push successfully on IDXs");
    } else {
     System.out.println("Done");
    }

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
    System.out.println("App Push Successfully");
    System.out.println();
   } catch (Exception e) {
    e.printStackTrace();
   }
  }
  printWriter.close();
 }
}