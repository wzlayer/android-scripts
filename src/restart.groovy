#!/usr/bin/env groovy

//get adb exec
adbExec = getAdbPath();

//check connected devices
def adbDevicesCmd = "$adbExec devices"
def proc = adbDevicesCmd.execute()
proc.waitFor()

def foundDevice = false

proc.in.text.eachLine { //start at line 1 and check for a connected device
        line, number ->
            if(number > 0 && line.contains("device"))
            {
                if(!line.contains("emulator"))
                    foundDevice = true
            }
}

if(!foundDevice) {
    println("You need to connect your device via usb first! Emulators don't support screenrecord")
    System.exit(-1)
}

// //Min Sdk Level 19 is required
// checkForSdkLevel();

//If filename is provided use it, otherwise use screenrecord.mp4
if(args.length == 0) {
    executeRestartCommand("com.android.settings");
}
else {
    executeRestartCommand(args[0]);
}

private void executeRestartCommand(String pdkName) {
    def console = System.console()
    // 两条命令之间通过 ";" 隔开
    // https://blog.csdn.net/ysdaniel/article/details/6127860

    // 通过 pkgName 启动应用
    // https://stackoverflow.com/questions/4567904/how-to-start-an-application-using-android-adb-tools
    // def command = "$adbExec shell am force-stop $pdkName; $adbExec shell monkey -p $pdkName -c android.intent.category.LAUNCHER 1"
    // println("command: $command");
    // def forcestopCommand = "$adbExec shell am force-stop $pdkName"
    // def p1 = forcestopCommand.execute();
    // p1.destroy();
    // sleep(1000)
    
    // def startCommand = "$adbExec shell monkey -p $pdkName -c android.intent.category.LAUNCHER 1"
    // def p2 = startCommand.execute();
    // console.readLine("Press enter to finish screenrecording");
    //Kill process
    // p2.destroy();
    //Take some time so the file can be saved completely, if the file is corrupt after pulling, increase sleep time
    // sleep(2000);

    println "$adbExec shell am force-stop $pdkName".execute().text
    println "$adbExec shell monkey -p $pdkName -c android.intent.category.LAUNCHER 1".execute().text
}

// private void checkForSdkLevel() {
//     //screenrecord is only available on API level 19 and higher
//     def apilevelCmd = "$adbExec -d shell getprop ro.build.version.sdk"
//     proc = apilevelCmd.execute()
//     proc.waitFor()
//     def apilevel
//     proc.in.text.eachLine { apilevel = it.toInteger() }    
//     if(apilevel < 19) {
//         println("Screenrecord is not available below API Level 19")
//         System.exit(-1)
//     }
// }

private String getAdbPath() {
    def adbExec = "adb"   //Todo: check if we need adb.exe on windows
    try {
        def command = "$adbExec"    //try it plain from the path
        command.execute()
        // println("adbExec: $adbExec")
        return adbExec
    }
    catch (IOException e) {
        //next try with Android Home
        def env = System.getenv("ANDROID_HOME")
        println("adb not in path trying Android home")
        if (env != null && env.length() > 0) {
            //try it here
            try {
                adbExec = env + "/platform-tools/adb"
                def command = "$adbExec"// is actually a string
                command.execute()
                // println("adbExec: $adbExec")
                return adbExec
            }
            catch (IOException ex) {
                println("Could not find $adbExec in path and no ANDROID_HOME is set :(")
                System.exit(-1)
            }
        }
    }
}