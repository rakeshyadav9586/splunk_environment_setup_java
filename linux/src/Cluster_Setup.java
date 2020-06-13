package Linux;
import java.awt.AWTException;
import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.io.InputStream;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

public class Cluster_Setup {
 public static void main(String[] args) throws AWTException, IOException {
  FileWriter fileWriter = new FileWriter("Cluster_Setup.txt");
  PrintWriter printWriter = new PrintWriter(fileWriter);

  String master = "10.0.0.1";
  String IDX1 = "10.0.0.3";
  String IDX2 = "10.0.0.4";
  String IDX3 = "10.0.0.5";
  String SH1 = "10.0.0.6";
  String SH2 = "10.0.0.7";
  String SH3 = "10.0.0.8";
  String Deployer = "10.0.0.2";

  ArrayList < String > list = new ArrayList < String > ();
  list.add(master);
  list.add(IDX1);
  list.add(IDX2);
  list.add(IDX3);
  list.add(SH1);
  list.add(SH2);
  list.add(SH3);
  list.add(Deployer);

  for (int j = 0; j < list.size(); j++) {
   String license = "Rakesh_Yadav.License";
   String user = "root";
   String user_pwd = "admin";
   String login_pwd = "admin:admin123 ";
   String Splunk_home = "/opt/splunk/bin/splunk ";
   String Splunk_Restart = " /opt/splunk/bin/splunk restart ";
   String Splunk_license_master = Splunk_home + "add license /opt/" + license + " -auth " + login_pwd;
   String Splunk_license_slave = Splunk_home + "edit licenser-localslave -master_uri https://" + list.get(0) + ":8089 -auth " + login_pwd;

   String make_cluster_master = Splunk_license_master + "&&" + Splunk_Restart + "&& " + Splunk_home + "edit cluster-config -mode master -replication_factor 3 -search_factor 2 -auth " + login_pwd + "&&" + Splunk_Restart;
   System.out.println(make_cluster_master);

   String connect_idx_master = Splunk_license_slave + "&&" + Splunk_Restart + "&& " + Splunk_home + "edit cluster-config -mode slave -master_uri https://" + list.get(0) + ":8089 -replication_port 8080 -auth " + login_pwd + "&&" + Splunk_Restart;
   System.out.println(connect_idx_master);

   String connect_sh_master_deployer = Splunk_license_slave + "&&" + Splunk_Restart + "&& " + Splunk_home + "edit cluster-config -mode searchhead -master_uri https://" + list.get(0) + ":8089 -auth " + login_pwd + "&&" + Splunk_Restart + "&& " + Splunk_home + "init shcluster-config -replication_port 8087 -mgmt_uri https://" + list.get(j) + ":8089 -conf_deploy_fetch_url https://" + list.get(7) + ":8089 -auth " + login_pwd + "&&" + Splunk_Restart;
   System.out.println(connect_sh_master_deployer);

   String sh_captain = Splunk_home + "bootstrap shcluster-captain -servers_list \"https://" + list.get(4) + ":8089,https://" + list.get(5) + ":8089,https://" + list.get(6) + ":8089\" -auth " + login_pwd;
   String connect_sh_master_deployer_captain = Splunk_license_slave + "&&" + Splunk_Restart + "&& " + Splunk_home + "edit cluster-config -mode searchhead -master_uri https://" + list.get(0) + ":8089 -auth " + login_pwd + "&&" + Splunk_Restart + "&& " + Splunk_home + "init shcluster-config -replication_port 8087 -mgmt_uri https://" + list.get(j) + ":8089 -conf_deploy_fetch_url https://" + list.get(7) + ":8089 -auth " + login_pwd + "&&" + Splunk_Restart + "&& " + sh_captain;
   System.out.println(connect_sh_master_deployer_captain);

   try {

    java.util.Properties config = new java.util.Properties();
    config.put("StrictHostKeyChecking", "no");
    JSch jsch = new JSch();
    Session session = jsch.getSession(user, list.get(j), 22);
    session.setPassword(user_pwd);
    session.setConfig(config);
    session.connect();
    System.out.println("Connected to Linux ip : " + list.get(j));
    Channel channel = session.openChannel("exec");
    if (list.get(j).equalsIgnoreCase("10.0.0.1")) {
     printWriter.append(make_cluster_master);
     ((ChannelExec) channel).setCommand(make_cluster_master);
     System.out.println("IP : " + list.get(j) + " Set as Master");
    } else if (list.get(j).equalsIgnoreCase("10.0.0.3") || list.get(j).equalsIgnoreCase("10.0.0.4") || list.get(j).equalsIgnoreCase("10.0.0.5")) {
     printWriter.append(connect_idx_master);
     ((ChannelExec) channel).setCommand(connect_idx_master);
     System.out.println("Indexer IP : " + list.get(j) + " connected with Master");
    } else if (list.get(j).equalsIgnoreCase("10.0.0.6") || list.get(j).equalsIgnoreCase("10.0.0.7")) {
     printWriter.append(connect_sh_master_deployer);
     ((ChannelExec) channel).setCommand(connect_sh_master_deployer);
     System.out.println("Search Head IP : " + list.get(j) + " connected with Master");
    } else if (list.get(j).equalsIgnoreCase("10.0.0.8")) {
     printWriter.append(connect_sh_master_deployer_captain);
     ((ChannelExec) channel).setCommand(connect_sh_master_deployer_captain);
     System.out.println("Search Head IP : " + list.get(j) + " connected with Master as well as set as Captain");
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

    System.out.println();
   } catch (Exception e) {
    e.printStackTrace();
   }
  }

  printWriter.close();
 }
}