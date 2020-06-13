package Linux;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.TimeUnit;
import java.awt.AWTException;
import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.io.InputStream;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
public class Cluster_Splunk_Reinstall {
 public static void copy_splunk_linux(String ip, String pwd, String Splunk_version) {
  String src_path = "C:\\Rakesh\\Do_not_Delete\\Splunk\\Linux\\";
  String src_path1 = "C:\\Rakesh\\Do_not_Delete\\Splunk\\";
  String dest_path = "/opt";
  String license = "Rakesh_Yadav.License";

  String server_ip = "pscp -pw " + pwd + " " + src_path + Splunk_version + " root@" + ip + ":" + dest_path + " && " + "pscp -pw " + pwd + " " + src_path1 + license + " root@" + ip + ":" + dest_path;

  System.out.print("Copy splunk & license on " + ip);

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
  } catch (Exception e) {
   e.printStackTrace();
  }
  System.out.println("Splunk copy done on " + ip);
  System.out.println();
 }
 public static void splunk_reintall_in_Linux(String ip, String pwd, String build_name) {

  String get_splunk_version = build_name.substring(7, 10);
  float splunk_version = Float.parseFloat(get_splunk_version);

  //connect ssh and run the command
  String user = "root";
  String Splunk_Home = " /opt/splunk/bin/splunk ";
  String Splunk_Stop = Splunk_Home + "stop ";
  String Splunk_Stop_msg = " echo ==================================================Splunk Stopped successfully || echo ==================================================Splunk Stop Failed ";
  String Splunk_Removed = " rm -rf /opt/splunk ";
  String Splunk_Removed_msg = " echo ==================================================Splunk Removed successfully ";
  String Splunk_untar = " tar xvzf /opt/" + build_name + " -C /opt/ ";
  String Splunk_untar_msg = " echo ==================================================Splunk Untar successfully ";
  String Splunk_new_version = Splunk_Home + "start --accept-license --answer-yes --no-prompt --seed-passwd admin123 ";
  String Splunk_old_version = Splunk_Home + "start --accept-license ";
  String Splunk_Start_msg = " echo ==================================================Splunk Started Successfully ";
  String Splunk_old_pass_update = Splunk_Home + "edit user admin -password admin123 -auth admin:changeme ";
  String Splunk_old_pass_update_msg = " echo ==================================================Splunk Credential Updated Successfully || echo ================================================== Splunk Credential Update Failed";
  String Splunk_Restart = Splunk_Home + "restart ";
  String Splunk_Restart_msg = " echo ==================================================Splunk Restarted Successfully";

  String command_Old = Splunk_Stop + "&&" + Splunk_Stop_msg + "&&" + Splunk_Removed + "&&" + Splunk_Removed_msg + "&&" + Splunk_untar + "&&" + Splunk_untar_msg + "&&" + Splunk_old_version + "&&" + Splunk_Start_msg + "&&" + Splunk_old_pass_update + "&&" + Splunk_old_pass_update_msg + "&&" + Splunk_Restart + "&&" + Splunk_Restart_msg;
  //			System.out.println(command_Old);

  String command_New = Splunk_Stop + "&&" + Splunk_Stop_msg + "&&" + Splunk_Removed + "&&" + Splunk_Removed_msg + "&&" + Splunk_untar + "&&" + Splunk_untar_msg + "&&" + Splunk_new_version + "&&" + Splunk_Start_msg;
  //			System.out.println(command_New);
  Instant start = Instant.now();
  try {
   FileWriter fileWriter = new FileWriter("Cluster_Splunk_Reinstall.txt");
   PrintWriter printWriter = new PrintWriter(fileWriter);

   java.util.Properties config = new java.util.Properties();
   config.put("StrictHostKeyChecking", "no");
   JSch jsch = new JSch();
   Session session = jsch.getSession(user, ip, 22);
   session.setPassword(pwd);
   session.setConfig(config);
   session.connect();
   System.out.println("Connected to Linux ip : " + ip);
   Channel channel = session.openChannel("exec");
   if (splunk_version > 7.0) {
    printWriter.append(command_New);
    ((ChannelExec) channel).setCommand(command_New);
   } else {
    printWriter.append(command_Old);
    ((ChannelExec) channel).setCommand(command_Old);
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
   System.out.println("Splunk Version " + build_name + " installed on Linux ip : " + ip);
   System.out.println();
   printWriter.close();
  } catch (Exception e) {
   e.printStackTrace();
  }
 }
 public static void main(String[] args) throws AWTException {
  // New :- Splunk Version >= 7.1.0

  String Splunk_6_5_0 = "splunk-6.5.0-59c8927def0f-Linux-x86_64.tgz";
  String Splunk_6_5_10 = "splunk-6.5.10-8114be174b06-Linux-x86_64.tgz";
  String Splunk_6_6_12 = "splunk-6.6.12-ff1b28d42e4c-Linux-x86_64.tgz";
  String Splunk_7_0_11 = "splunk-7.0.11-ca372bdc34bc-Linux-x86_64.tgz";
  String Splunk_7_1_9 = "splunk-7.1.9-45b25e1f9be3-Linux-x86_64.tgz";
  String Splunk_7_2_8 = "splunk-7.2.8-d613a50d43ac-Linux-x86_64.tgz";
  String Splunk_7_3_0 = "splunk-7.3.0-657388c7a488-Linux-x86_64.tgz";
  String Splunk_7_3_1_1 = "splunk-7.3.1.1-7651b7244cf2-Linux-x86_64.tgz";
  String Splunk_8_0_0 = "splunk-8.0.0-1357bef0a7f6-Linux-x86_64.tgz";
  String Splunk_8_0_1 = "splunk-8.0.1-6db836e2fb9e-Linux-x86_64.tgz";

  String mater = "10.0.0.1";
  String IDX1 = "10.0.0.2";
  String IDX2 = "10.0.0.3";
  String IDX3 = "10.0.0.4";
  String SH1 = "10.0.0.5";
  String SH2 = "10.0.0.6";
  String SH3 = "10.0.0.7";
  String Deployer = "10.0.0.8";

  ArrayList < String > list = new ArrayList < String > ();
  list.add(mater);
  list.add(IDX1);
  list.add(IDX2);
  list.add(IDX3);
  list.add(SH1);
  list.add(SH2);
  list.add(SH3);
  list.add(Deployer);
  Instant start = Instant.now();

  //All VMs
  for (int i = 0; i < list.size(); i++) {
   copy_splunk_linux(list.get(i), "admin", Splunk_7_2_8);
  }

  //All VMs

  for (int i = 0; i < list.size(); i++) {
   splunk_reintall_in_Linux(list.get(i), "admin", Splunk_7_2_8);
  }

  Instant end = Instant.now();
  Duration interval = Duration.between(start, end);
  System.out.println("Execution time in seconds: " + interval.getSeconds());

 }
}