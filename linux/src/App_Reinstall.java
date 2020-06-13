import java.awt.AWTException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.InputStream;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

public class App_Reinstall {
 public static void App_reintall_in_Linux(String ip, String pwd) {
  String build_name = null;
  String src_path = "C:\\Rakesh\\Latest_Build\\";
  String dest_path = "/opt/splunk/etc/apps";

  File file = new File(src_path);
  File[] files = file.listFiles(new FilenameFilter() {
   @Override
   public boolean accept(File dir, String name) {
    return name.endsWith(".spl");
   }
  });
  for (File name: files) {
   build_name = name.getName();
  }

  String server_ip = "pscp -pw " + pwd + " " + src_path + build_name + " root@" + ip + ":" + dest_path;
  System.out.println(server_ip);
  System.out.print("Started Copy latest build " + build_name + " on " + ip);
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
  //connect ssh and run the command
  String user = "root";
  String splunk_stop = "/opt/splunk/bin/splunk stop ";
  String remove_log = " rm -rf /opt/splunk/var/log/splunk/splunk_rapid_diag.log ";
  String remove_task = " rm -rf /opt/splunk/var/run/splunk/splunk_rapid_diag ";
  String remove_app = " rm -rf /opt/splunk/etc/apps/splunk_rapid_diag ";
  String untar_spl = " tar xvzf /opt/splunk/etc/apps/" + build_name + " -C /opt/splunk/etc/apps/ ";
  String remove_spl = " rm -rf /opt/splunk/etc/apps/" + build_name + " ";
  String splunk_start = " /opt/splunk/bin/splunk start";
  String command = splunk_stop + "&&" + remove_log + "&&" + remove_task + "&&" + remove_app + "&&" + untar_spl + "&&" + remove_spl + "&&" + splunk_start;
  System.out.println(command);
  try {
   FileWriter fileWriter = new FileWriter("App_Reinstall.txt");
   PrintWriter printWriter = new PrintWriter(fileWriter);

   java.util.Properties config = new java.util.Properties();
   config.put("StrictHostKeyChecking", "no");
   JSch jsch = new JSch();
   Session session = jsch.getSession(user, ip, 22);
   session.setPassword(pwd);
   session.setConfig(config);
   session.connect();
   System.out.println("Connected");

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
   System.out.println(build_name + ": installed on Linux ip : " + ip);
   printWriter.close();
  } catch (Exception e) {
   e.printStackTrace();
  }
 }
 /**
  * @param args
  * @throws AWTException
  */
 public static void main(String[] args) throws AWTException {

  App_reintall_in_Linux("10.0.0.0", "admin");
 }
}