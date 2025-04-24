import core.ThsCore;
import thsCrack.fjc;

import java.io.FileInputStream;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {

        thsbase64 thsb64=new thsbase64();
        System.out.println(new String(thsb64.encode("12345678901234567890\n\r".getBytes())));
        System.out.println(new String(thsb64.decode("mExloMvOolf5mMeLmls1oWBFPEawMs==".getBytes())));

        System.exit(0);
        ThsCore thsCore = new ThsCore();
        //创建一个新的连接到同花顺服务器
        if (!thsCore.connectThsServer()) {
            System.exit(0);
        }

        //查看是否存在passport.dat
        try (FileInputStream fis = new FileInputStream("D:\\passport.dat");) {
            //直接使用passport登录
            thsCore.passportLogin(fis.readAllBytes());
        } catch (Exception exception) {
            //发送相关的设备初始化信息
            if (!thsCore.sendDeviceInfo()) {
                System.out.println("发送初始化设备信息失败");
            }
            String phoneNumber = "your phone";
            //发送登录验证包
            thsCore.sendSmsLogin(phoneNumber);
            Scanner scanner = new Scanner(System.in);
            while (true) {
                System.out.print("输入短信验证码:");
                String checkCode = scanner.next();
                if (thsCore.verifyCheckCode(phoneNumber, checkCode, 4)) break;
            }
        }

    }
}