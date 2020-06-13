import java.awt.AWTException;
import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.concurrent.TimeUnit;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.interactions.Actions;
import java.io.InputStream;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

public class Splunk_Fresh_install {

 public static void copy_splunk_linux(String ip, String pwd, String Splunk_version) {
  String src_path = "C:\\Rakesh\\Do_not_Delete\\Splunk\\Linux\\";
  String src_path1 = "C:\\Rakesh\\Do_not_Delete\\Splunk\\";
  String license = "Rakesh_Yadav.License";
  String dest_path = "/opt";

  String server_ip = "pscp -pw " + pwd + " " + src_path + Splunk_version + " root@" + ip + ":" + dest_path + " && " + "pscp -pw " + pwd + " " + src_path1 + license + " root@" + ip + ":" + dest_path;
  System.out.print("====================Copy splunk and License on " + ip + "====================");
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
  System.out.println("Splunk and License copy done on " + ip);
  System.out.println();
 }
 public static void splunk_restart(String ip, String pwd) {
  String user = "root";
  String Splunk_Home = " /opt/splunk/bin/splunk ";
  String Splunk_Restart = Splunk_Home + "restart ";
  String Splunk_Restart_msg = " echo ==================================================Splunk Restarted Successfully";
  String Restart = Splunk_Restart + "&&" + Splunk_Restart_msg;
  try {
   JSch jsch = new JSch();
   Session session = jsch.getSession(user, ip, 22);
   session.setPassword(pwd);
   java.util.Properties config = new java.util.Properties();
   config.put("StrictHostKeyChecking", "no");
   session.setConfig(config);
   session.connect();
   System.out.println("Going to Restart Linux ip : " + ip);
   Channel channel = session.openChannel("exec");

   ((ChannelExec) channel).setCommand(Restart);
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
  } catch (Exception e) {
   e.printStackTrace();
  }
 }

 public static void splunk_Fresh_install_in_Linux(String ip, String pwd, String build_name) {
  String get_version = build_name.substring(7, 10);
  float version = Float.parseFloat(get_version);

  //connect ssh and run the command
  String user = "root";
  String license = "Rakesh_Yadav_16_Aug_2020.License";
  String Splunk_untar = " tar xvzf /opt/" + build_name + " -C /opt/ ";
  String Splunk_untar_msg = " echo ==================================================Splunk Untar successfully ";
  String Splunk_new_version = " /opt/splunk/bin/splunk start --accept-license --answer-yes --no-prompt --seed-passwd admin123 ";
  String Splunk_old_version = " /opt/splunk/bin/splunk start --accept-license ";
  String Splunk_Start_msg = " echo ==================================================Splunk Started Successfully ";
  String Splunk_new_license = " /opt/splunk/bin/splunk add license /opt/" + license + " -auth admin:admin123 ";
  String Splunk_old_license = " /opt/splunk/bin/splunk add license /opt/" + license + " -auth admin:changeme ";
  String Splunk_license_msg = " echo ==================================================Splunk License added successfully || echo ==================================================Splunk License adding Failed ";
  String Splunk_old_pass_update = " /opt/splunk/bin/splunk edit user admin -password admin123 -auth admin:changeme ";
  String Splunk_old_pass_update_msg = " echo ==================================================Splunk Credential Updated Successfully || echo ================================================== Splunk Credential Update Failed";

  String command_Old = Splunk_untar + "&&" + Splunk_untar_msg + "&&" + Splunk_old_version + "&&" + Splunk_Start_msg + "&&" + Splunk_old_license + "&&" + Splunk_license_msg + "&&" + Splunk_old_pass_update + "&&" + Splunk_old_pass_update_msg;
  String command_New = Splunk_untar + "&&" + Splunk_untar_msg + "&&" + Splunk_new_version + "&&" + Splunk_Start_msg + "&&" + Splunk_new_license + "&&" + Splunk_license_msg;

  try {
   FileWriter fileWriter = new FileWriter("Splunk_Fresh_install.txt");
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
   if (version > 7.0) {
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
   System.out.println(build_name + " installed on Linux ip : " + ip);
   System.out.println();

   String host = ip.substring(5);
   String get_os = host.substring(3);
   int vm_ip = Integer.parseInt(get_os);
   String os = null;
   if (vm_ip == 20 || vm_ip == 21 || vm_ip == 22) {
    os = "centOS";
   } else if (vm_ip == 23 || vm_ip == 24 || vm_ip == 25) {
    os = "Ubuntu";
   } else if (vm_ip == 26 || vm_ip == 27 || vm_ip == 28) {
    os = "SUSE";
   }
   System.setProperty("webdriver.chrome.driver", "..\\Driver\\chromedriver.exe");
   WebDriver driver = new ChromeDriver();
   Actions action = new Actions(driver);
   driver.manage().window().maximize();
   driver.manage().timeouts().implicitlyWait(60, TimeUnit.SECONDS);
   driver.manage().timeouts().pageLoadTimeout(60, TimeUnit.SECONDS);

   driver.get("http://" + ip + ":8000");
   driver.findElement(By.id("username")).sendKeys("admin");
   driver.findElement(By.id("password")).sendKeys("admin123");
   action.sendKeys(Keys.TAB).perform();
   action.sendKeys(Keys.ENTER).perform();
   Thread.sleep(3);
   driver.navigate().refresh();
   Thread.sleep(3);
   String title = driver.getTitle();
   String splunk_version = title.substring(14);
   driver.get("http://" + ip + ":8000/en-US/manager/launcher/server/settings/settings?action=edit");

   driver.findElement(By.id("serverName_id")).clear();
   driver.findElement(By.id("serverName_id")).sendKeys(host + "_" + os + "_" + splunk_version);

   driver.findElement(By.id("host_id")).clear();
   driver.findElement(By.id("host_id")).sendKeys(host + "_" + os + "_" + splunk_version);

   driver.findElement(By.xpath("//button[@type='submit']")).click();
   Thread.sleep(3);
   //			driver.get("http://"+ip+":8000/en-US/manager/launcher/control");
   //			driver.findElement(By.id("restart-splunk-button")).click();
   //			driver.switchTo().alert().accept();			

   System.out.println("Server Name & Host Name Updated Successfully for IP: " + ip);
   driver.close();
   driver.quit();
   splunk_restart(ip, pwd);
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

  //Copy splunk

  copy_splunk_linux("10.0.0.1", "admin", Splunk_7_3_0);

  //Fresh install

  splunk_Fresh_install_in_Linux("10.0.0.1", "admin", Splunk_7_3_0);
  //			}
 }