@echo off

set username=gwx978100
set password=HTIPL#123


"C:/Program Files/Huawei/DevEco Studio 2.1.0.240/jbr/bin/java" -jar "C:/Users/gWX978100/AppData/Local/Huawei/Sdk/toolchains/lib/hapsigntoolv2.jar" sign -mode remote -privatekey "HOS Application Provision Dev" -inputFile entry/build/outputs/hap/debug/entry-debug-rich-unsigned.hap -outputFile entry/build/outputs/hap/debug/entry-debug-signed.hap -username %username% -password %password% -signAlg SHA256withECDSA -profile AppProvision.PROFILE

echo .
echo HAP signed Successfully

hdc app install -r entry/build/outputs/hap/debug/entry-debug-signed.hap

echo .
echo HAP installed successfully
