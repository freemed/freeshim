; Licensed to the FreeMED (ASF) under one or more
; contributor license agreements.  See the NOTICE file distributed with
; this work for additional information regarding copyright ownership.
; The ASF licenses this file to You under the Apache License, Version 2.0
; (the "License"); you may not use this file except in compliance with
; the License.  You may obtain a copy of the License at
;
;     http://www.apache.org/licenses/LICENSE-2.0
;
; Unless required by applicable law or agreed to in writing, software
; distributed under the License is distributed on an "AS IS" BASIS,
; WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
; See the License for the specific language governing permissions and
; limitations under the License.

; Tomcat script for Nullsoft Installer
; $Id: tomcat.nsi 899134 2010-01-14 09:44:28Z rjung $

  ;Compression options
  CRCCheck on
  SetCompressor /SOLID lzma

  Name "FreeSHIM"
  !define VERSION 0.1.1.2

  ;Product information
  VIAddVersionKey ProductName "FreeSHIM"
  VIAddVersionKey CompanyName "FreeMED Software Foundation"
  VIAddVersionKey LegalCopyright "Copyright (c) 1999-2011 FreeMED Software Foundation"
  VIAddVersionKey FileDescription "FreeSHIM Installer"
  VIAddVersionKey FileVersion "1.0"
  VIAddVersionKey ProductVersion "0.1.1"
  VIAddVersionKey Comments "freeshim.org"
  VIAddVersionKey InternalName "freeshim-0.1.1.2-win32.exe"
  VIProductVersion ${VERSION}
  !define TOMCAT_VERSION 6.0.32

!include "MUI.nsh"
!include "StrFunc.nsh"
${StrRep}
  Var "JavaHome"



;--------------------------------
;Configuration

  !define MUI_HEADERIMAGE
  !define MUI_HEADERIMAGE_RIGHT
  !define MUI_HEADERIMAGE_BITMAP header.bmp
  !define MUI_WELCOMEFINISHPAGE_BITMAP side_left.bmp 
  !define MUI_FINISHPAGE_SHOWREADME "$INSTDIR\webapps\ROOT\RELEASE-NOTES.txt"
  !define MUI_FINISHPAGE_RUN $INSTDIR\bin\tomcat6w.exe
  !define MUI_FINISHPAGE_RUN_PARAMETERS //MR//Tomcat6
  !define MUI_FINISHPAGE_NOREBOOTSUPPORT

  !define MUI_ABORTWARNING

  !define TEMP1 $R0
  !define TEMP2 $R1

  !define MUI_ICON tomcat.ico
  !define MUI_UNICON tomcat.ico

  ;General
  OutFile freeshim-${version}-win32-setup.exe

  ;Install Options pages
  LangString TEXT_JVM_TITLE ${LANG_ENGLISH} "Java Virtual Machine"
  LangString TEXT_JVM_SUBTITLE ${LANG_ENGLISH} "Java Virtual Machine path selection."
  LangString TEXT_JVM_PAGETITLE ${LANG_ENGLISH} ": Java Virtual Machine path selection"

  LangString TEXT_CONF_TITLE ${LANG_ENGLISH} "Configuration"
  LangString TEXT_CONF_SUBTITLE ${LANG_ENGLISH} "Tomcat basic configuration."
  LangString TEXT_CONF_PAGETITLE ${LANG_ENGLISH} ": Configuration Options"

  ;Install Page order
  !insertmacro MUI_PAGE_WELCOME
  !insertmacro MUI_PAGE_LICENSE INSTALLLICENSE
  !insertmacro MUI_PAGE_COMPONENTS
  !insertmacro MUI_PAGE_DIRECTORY
  Page custom SetConfiguration Void "$(TEXT_CONF_PAGETITLE)"
  Page custom SetChooseJVM Void "$(TEXT_JVM_PAGETITLE)"
  !insertmacro MUI_PAGE_INSTFILES
  Page custom CheckUserType
  !insertmacro MUI_PAGE_FINISH

  ;Uninstall Page order
  !insertmacro MUI_UNPAGE_CONFIRM
  !insertmacro MUI_UNPAGE_INSTFILES

  ;License dialog
  ;LicenseData License.rtf

  ;Component-selection page
    ;Descriptions
    LangString DESC_SecTomcat ${LANG_ENGLISH} "Install the Tomcat Servlet container."
    LangString DESC_SecTomcatCore ${LANG_ENGLISH} "Install the Tomcat Servlet container core."
    LangString DESC_SecTomcatService ${LANG_ENGLISH} "Automatically start Tomcat when the computer is started."
    LangString DESC_SecTomcatNative ${LANG_ENGLISH} "Install APR based Tomcat native .dll for better performance and scalability in production environments."
;    LangString DESC_SecTomcatSource ${LANG_ENGLISH} "Install the Tomcat source code."
    LangString DESC_SecMenu ${LANG_ENGLISH} "Create a Start Menu program group for Tomcat."
    LangString DESC_SecDocs ${LANG_ENGLISH} "Install the Tomcat documentation bundle. This includes documentation on the servlet container and its configuration options, on the Jasper JSP page compiler, as well as on the native webserver connectors."
    LangString DESC_SecManager ${LANG_ENGLISH} "Install the Tomcat Manager administrative web application."
    LangString DESC_SecHostManager ${LANG_ENGLISH} "Install the Tomcat Host Manager administrative web application."
    LangString DESC_SecSHIM ${LANG_ENGLISH} "Install the FreeSHIM application."
    LangString DESC_SecAdmin ${LANG_ENGLISH} "Installs the administration web application.";
;    LangString DESC_SecWebapps ${LANG_ENGLISH} "Installs other utility web applications (WebDAV, balancer, etc)."

  ;Language
  !insertmacro MUI_LANGUAGE English

  ;Folder-select dialog
  InstallDir "$PROGRAMFILES\FreeMED\FreeSHIM"

  ;Install types
  InstType Normal
  InstType Minimum
  InstType Full

  ; Main registry key
  InstallDirRegKey HKLM "SOFTWARE\FreeMED\FreeSHIM" ""

  !insertmacro MUI_RESERVEFILE_INSTALLOPTIONS
  ReserveFile "jvm.ini"
  ReserveFile "config.ini"

;--------------------------------
;Installer Sections

SubSection "Tomcat" SecTomcat

Section "Core" SecTomcatCore

  SectionIn 1 2 3 RO

  IfSilent +2 0
  Call checkJvm

  SetOutPath $INSTDIR
  File tomcat.ico
  File apache-tomcat-${TOMCAT_VERSION}\LICENSE
  File apache-tomcat-${TOMCAT_VERSION}\NOTICE
  SetOutPath $INSTDIR\lib
  File /r apache-tomcat-${TOMCAT_VERSION}\lib\*.*
  SetOutPath $INSTDIR\logs
  File /nonfatal /r apache-tomcat-${TOMCAT_VERSION}\logs\*.*
  SetOutPath $INSTDIR\work
  File /nonfatal /r apache-tomcat-${TOMCAT_VERSION}\work\*.*
  SetOutPath $INSTDIR\temp
  File /nonfatal /r apache-tomcat-${TOMCAT_VERSION}\temp\*.*
  SetOutPath $INSTDIR\bin
  File apache-tomcat-${TOMCAT_VERSION}\bin\bootstrap.jar
  File apache-tomcat-${TOMCAT_VERSION}\bin\tomcat-juli.jar
  SetOutPath $INSTDIR\conf
  File apache-tomcat-${TOMCAT_VERSION}\conf\*.*
  SetOutPath $INSTDIR\webapps\ROOT
  File /r apache-tomcat-${TOMCAT_VERSION}\webapps\ROOT\*.*

  Call configure
  Call findJavaPath
  Pop $2

  IfSilent +2 0
  !insertmacro MUI_INSTALLOPTIONS_READ $2 "jvm.ini" "Field 2" "State"

  StrCpy "$JavaHome" $2
  Call findJVMPath
  Pop $2

  DetailPrint "Using Jvm: $2"

  SetOutPath $INSTDIR\bin
  File apache-tomcat-${TOMCAT_VERSION}\bin\tomcat6w.exe

  File /oname=tomcat6.exe apache-tomcat-${TOMCAT_VERSION}\bin\tomcat6.exe

  ; Get the current platform x86 / AMD64 / IA64
  ;Call FindCpuType
  ;Pop $0
  ;StrCmp $0 "x86" 0 +2
  ;File /oname=tomcat6.exe apache-tomcat-${TOMCAT_VERSION}\bin\tomcat6.exe
  ;StrCmp $0 "x64" 0 +2
  ;File /oname=tomcat6.exe apache-tomcat-${TOMCAT_VERSION}\bin\x64\tomcat6.exe
  ;StrCmp $0 "i64" 0 +2
  ;File /oname=tomcat6.exe apache-tomcat-${TOMCAT_VERSION}\bin\i64\tomcat6.exe

  InstallRetry:
  ClearErrors
  nsExec::ExecToLog '"$INSTDIR\bin\tomcat6.exe" //IS//Tomcat6 --DisplayName "Apache Tomcat 6" --Description "Apache Tomcat @VERSION@ Server - http://tomcat.apache.org/" --LogPath "$INSTDIR\logs" --Install "$INSTDIR\bin\tomcat6.exe" --Jvm "$2" --StartPath "$INSTDIR" --StopPath "$INSTDIR"'
  Pop $0
  StrCmp $0 "0" InstallOk
    MessageBox MB_ABORTRETRYIGNORE|MB_ICONSTOP \
      "Failed to install Tomcat6 service.$\r$\nCheck your settings and permissions.$\r$\nIgnore and continue anyway (not recommended)?" \
       /SD IDIGNORE IDIGNORE InstallOk IDRETRY InstallRetry
  Quit
  InstallOk:
  ClearErrors

SectionEnd

Section "Service" SecTomcatService

  SectionIn 1 2 3

  IfSilent 0 +3
  Call findJavaPath
  Pop $2

  IfSilent +2 0
  !insertmacro MUI_INSTALLOPTIONS_READ $2 "jvm.ini" "Field 2" "State"

  StrCpy "$JavaHome" $2
  Call findJVMPath
  Pop $2

  nsExec::ExecToLog '"$INSTDIR\bin\tomcat6.exe" //US//Tomcat6 --Startup auto'
  ; Behave like Apache Httpd (put the icon in tray on login)
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Run" "ApacheTomcatMonitor" '"$INSTDIR\bin\tomcat6w.exe" //MS//Tomcat6'

  ClearErrors

SectionEnd

Section "Native" SecTomcatNative

  SectionIn 1 2 3 RO

  SetOutPath $INSTDIR\bin

  ; Topaz tablet HSB driver
  File ..\..\..\shim-drivers\shim-driver-signature-topaz\native\win32\SigUsb.dll

  File apache-tomcat-${TOMCAT_VERSION}\bin\tcnative-1.dll

  ;Call FindCpuType
  ;Pop $0
  ;StrCmp $0 "x86" 0 +2
  ;File apache-tomcat-${TOMCAT_VERSION}\bin\tcnative-1.dll
  ;StrCmp $0 "x64" 0 +2
  ;File /oname=tcnative-1.dll apache-tomcat-${TOMCAT_VERSION}\bin\x64\tcnative-1.dll
  ;StrCmp $0 "i64" 0 +2
  ;File /oname=tcnative-1.dll apache-tomcat-${TOMCAT_VERSION}\bin\i64\tcnative-1.dll

  ClearErrors

SectionEnd

;Section "Source Code" SecTomcatSource
;
;  SectionIn 3
;  SetOutPath $INSTDIR
;  File /r src
;
;SectionEnd

SubSectionEnd

Section "Start Menu Items" SecMenu

  SectionIn 1 2 3

  !insertmacro MUI_INSTALLOPTIONS_READ $2 "jvm.ini" "Field 2" "State"

  SetOutPath "$SMPROGRAMS\Apache Tomcat @VERSION_MAJOR_MINOR@"

  CreateShortCut "$SMPROGRAMS\Apache Tomcat @VERSION_MAJOR_MINOR@\Tomcat Home Page.lnk" \
                 "http://tomcat.apache.org/"

  CreateShortCut "$SMPROGRAMS\Apache Tomcat @VERSION_MAJOR_MINOR@\Welcome.lnk" \
                 "http://localhost:$R0/"

;  IfFileExists "$INSTDIR\webapps\admin" 0 NoAdminApp
;
;  CreateShortCut "$SMPROGRAMS\Apache Tomcat @VERSION_MAJOR_MINOR@\Tomcat Administration.lnk" \
;                 "http://localhost:$R0/admin/"
;NoAdminApp:

  IfFileExists "$INSTDIR\webapps\manager" 0 NoManagerApp

  CreateShortCut "$SMPROGRAMS\FreeSHIM\Tomcat Manager.lnk" \
                 "http://localhost:$R0/manager/html"

NoManagerApp:

  IfFileExists "$INSTDIR\webapps\webapps\tomcat-docs" 0 NoDocumentaion

  CreateShortCut "$SMPROGRAMS\FreeSHIM\Tomcat Documentation.lnk" \
                 "$INSTDIR\webapps\tomcat-docs\index.html"

NoDocumentaion:

  CreateShortCut "$SMPROGRAMS\FreeSHIM\Uninstall FreeSHIM.lnk" \
                 "$INSTDIR\Uninstall.exe"

  CreateShortCut "$SMPROGRAMS\FreeSHIM\Program Directory.lnk" \
                 "$INSTDIR"

  CreateShortCut "$SMPROGRAMS\FreeSHIM\Monitor Tomcat.lnk" \
                 "$INSTDIR\bin\tomcat6w.exe" \
                 '//MS//Tomcat6' \
                 "$INSTDIR\tomcat.ico" 0 SW_SHOWNORMAL

  CreateShortCut "$SMPROGRAMS\FreeSHIM\Configure Tomcat.lnk" \
                 "$INSTDIR\bin\tomcat6w.exe" \
                 '//ES//Tomcat6' \
                 "$INSTDIR\tomcat.ico" 0 SW_SHOWNORMAL

SectionEnd

Section "Documentation" SecDocs

  SectionIn 1 3
  SetOutPath $INSTDIR\webapps\docs
  File /r apache-tomcat-${TOMCAT_VERSION}\webapps\docs\*.*

SectionEnd

Section "Manager" SecManager

  SectionIn 1 3

  SetOverwrite on
  SetOutPath $INSTDIR\webapps\manager
  File /r apache-tomcat-${TOMCAT_VERSION}\webapps\manager\*.*

SectionEnd

Section "Host Manager" SecHostManager

  SectionIn 3

  SetOverwrite on
  SetOutPath $INSTDIR\webapps\host-manager
  File /r apache-tomcat-${TOMCAT_VERSION}\webapps\host-manager\*.*

SectionEnd

Section "SHIM" SecSHIM

  SectionIn 1 2 3 RO

  SetOverwrite on
  SetOutPath $INSTDIR\webapps
  File /r ..\..\target\shim.war

SectionEnd

;Section "Administration" SecAdmin
;
;  SectionIn 3
;
;  SetOutPath $INSTDIR\webapps
;  File /r webapps\admin
;  SetOutPath $INSTDIR\conf\Catalina\localhost
;  File conf\Catalina\localhost\admin.xml
;
;SectionEnd

;Section "Webapps" SecWebapps
;
;  SectionIn 3
;
;  SetOutPath $INSTDIR\webapps
;  File /nonfatal /r webapps\balancer
;  File /nonfatal /r webapps\webdav
;
;SectionEnd

;Section "Compatibility" SecCompat
;
;  SetOutPath $INSTDIR
;  File /oname=bin\jmx.jar ..\compat\bin\jmx.jar
;  File /oname=common\endorsed\xercesImpl.jar ..\compat\common\endorsed\xercesImpl.jar
;  File /oname=common\endorsed\xml-apis.jar  ..\compat\common\endorsed\xml-apis.jar
;
;SectionEnd

Section -post
  nsExec::ExecToLog '"$INSTDIR\bin\tomcat6.exe" //US//Tomcat6 --Classpath "$INSTDIR\bin\bootstrap.jar" --StartClass org.apache.catalina.startup.Bootstrap --StopClass org.apache.catalina.startup.Bootstrap --StartParams start --StopParams stop  --StartMode jvm --StopMode jvm'
  nsExec::ExecToLog '"$INSTDIR\bin\tomcat6.exe" //US//Tomcat6 --JvmOptions "-Dcatalina.home=$INSTDIR#-Dcatalina.base=$INSTDIR#-Djava.endorsed.dirs=$INSTDIR\endorsed#-Djava.io.tmpdir=$INSTDIR\temp#-Djava.util.logging.manager=org.apache.juli.ClassLoaderLogManager#-Djava.util.logging.config.file=$INSTDIR\conf\logging.properties" --StdOutput auto --StdError auto'

  WriteUninstaller "$INSTDIR\Uninstall.exe"

  WriteRegStr HKLM "SOFTWARE\FreeMED\FreeSHIM" "InstallPath" $INSTDIR
  WriteRegStr HKLM "SOFTWARE\FreeMED\FreeSHIM" "Version" @VERSION@
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\FreeSHIM" \
                   "DisplayName" "FreeSHIM (remove only)"
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\FreeSHIM" \
                   "UninstallString" '"$INSTDIR\Uninstall.exe"'

SectionEnd

Function .onInit
  ;Reset install dir for 64-bit
  ExpandEnvStrings $0 "%PROGRAMW6432%"
  StrCmp $0 "%PROGRAMW6432%" +2 0
  StrCpy $INSTDIR "$0\FreeMED\FreeSHIM"

  ;Extract Install Options INI Files
  !insertmacro MUI_INSTALLOPTIONS_EXTRACT "config.ini"
  !insertmacro MUI_INSTALLOPTIONS_EXTRACT "jvm.ini"

FunctionEnd

Function SetChooseJVM
  !insertmacro MUI_HEADER_TEXT "$(TEXT_JVM_TITLE)" "$(TEXT_JVM_SUBTITLE)"
  Call findJavaPath
  Pop $3
  !insertmacro MUI_INSTALLOPTIONS_WRITE "jvm.ini" "Field 2" "State" $3
  !insertmacro MUI_INSTALLOPTIONS_DISPLAY "jvm.ini"
FunctionEnd

Function SetConfiguration
  !insertmacro MUI_HEADER_TEXT "$(TEXT_CONF_TITLE)" "$(TEXT_CONF_SUBTITLE)"

  SectionGetFlags ${SecManager} $0
  IntOp $0 $0 & ${SF_SELECTED}
  IntCmp $0 0 0 Enable Enable
  SectionGetFlags ${SecHostManager} $0
  IntOp $0 $0 & ${SF_SELECTED}
  IntCmp $0 0 Disable 0 0

Enable:
  ; Enable the user and password controls if the manager or host-manager app is
  ; being installed
  !insertmacro MUI_INSTALLOPTIONS_READ $0 "config.ini" "Field 5" "HWND"
  !insertmacro MUI_INSTALLOPTIONS_WRITE "config.ini" "Field 5" "Flags" ""
  EnableWindow $0 1
  !insertmacro MUI_INSTALLOPTIONS_READ $0 "config.ini" "Field 7" "HWND"
  !insertmacro MUI_INSTALLOPTIONS_WRITE "config.ini" "Field 7" "Flags" ""
  EnableWindow $0 1
  Goto Display

Disable:
  ; Disable the user and password controls if neither the manager nor
  ; host-manager app is being installed
  !insertmacro MUI_INSTALLOPTIONS_READ $0 "config.ini" "Field 5" "HWND"
  !insertmacro MUI_INSTALLOPTIONS_WRITE "config.ini" "Field 5" "Flags" "DISABLED"
  EnableWindow $0 0
  !insertmacro MUI_INSTALLOPTIONS_READ $0 "config.ini" "Field 7" "HWND"
  !insertmacro MUI_INSTALLOPTIONS_WRITE "config.ini" "Field 7" "Flags" "DISABLED"
  EnableWindow $0 0
  ; Clear the values
  !insertmacro MUI_INSTALLOPTIONS_WRITE "config.ini" "Field 5" "State" ""
  !insertmacro MUI_INSTALLOPTIONS_WRITE "config.ini" "Field 7" "State" ""

Display:
  !insertmacro MUI_INSTALLOPTIONS_DISPLAY "config.ini"

FunctionEnd

Function Void
FunctionEnd

;--------------------------------
;Descriptions

!insertmacro MUI_FUNCTION_DESCRIPTION_BEGIN
  !insertmacro MUI_DESCRIPTION_TEXT ${SecTomcat} $(DESC_SecTomcat)
  !insertmacro MUI_DESCRIPTION_TEXT ${SecTomcatCore} $(DESC_SecTomcatCore)
  !insertmacro MUI_DESCRIPTION_TEXT ${SecTomcatService} $(DESC_SecTomcatService)
  !insertmacro MUI_DESCRIPTION_TEXT ${SecTomcatNative} $(DESC_SecTomcatNative)
;  !insertmacro MUI_DESCRIPTION_TEXT ${SecTomcatSource} $(DESC_SecTomcatSource)
;  !insertmacro MUI_DESCRIPTION_TEXT ${SecCompat} $(DESC_SecCompat)
  !insertmacro MUI_DESCRIPTION_TEXT ${SecMenu} $(DESC_SecMenu)
  !insertmacro MUI_DESCRIPTION_TEXT ${SecDocs} $(DESC_SecDocs)
  !insertmacro MUI_DESCRIPTION_TEXT ${SecManager} $(DESC_SecManager)
  !insertmacro MUI_DESCRIPTION_TEXT ${SecHostManager} $(DESC_SecHostManager)
  !insertmacro MUI_DESCRIPTION_TEXT ${SecSHIM} $(DESC_SecSHIM)
;  !insertmacro MUI_DESCRIPTION_TEXT ${SecAdmin} $(DESC_SecAdmin)
;  !insertmacro MUI_DESCRIPTION_TEXT ${SecWebapps} $(DESC_SecWebapps)
!insertmacro MUI_FUNCTION_DESCRIPTION_END

; =====================
; FindCpuType Function
; =====================
;
; Find the CPU used on the system, and put the result on the top of the
; stack
;
Function FindCpuType

  ClearErrors
  ; Default CPU is always x86
  StrCpy $1 "x86"
  ExpandEnvStrings $0 "%PROCESSOR_ARCHITEW6432%"
  StrCmp $0 "%PROCESSOR_ARCHITEW6432%" +5 0
  StrCmp $0 "IA64" 0 +3
  StrCpy $1 "i64"
  Goto FoundCpu
  StrCpy $1 "x64"

FoundCpu:
  ; Put the result in the stack
  Push $1

FunctionEnd

; =====================
; CheckUserType Function
; =====================
;
; Check the user type, and warn if it's not an administrator.
; Taken from Examples/UserInfo that ships with NSIS.
Function CheckUserType
  ClearErrors
  UserInfo::GetName
  IfErrors Win9x
  Pop $0
  UserInfo::GetAccountType
  Pop $1
  StrCmp $1 "Admin" 0 +3
    ; This is OK, do nothing
    Goto done

    MessageBox MB_OK|MB_ICONEXCLAMATION 'Note: the current user is not an administrator. \
               To run FreeSHIM as a Windows service, you must be an administrator. \
               You can still run FreeSHIM from the command-line as this type of user.'
    Goto done

  Win9x:
    # This one means you don't need to care about admin or
    # not admin because Windows 9x doesn't either
    MessageBox MB_OK "Error! This DLL can't run under Windows 9x!"

  done:
FunctionEnd


; =====================
; FindJavaPath Function
; =====================
;
; Find the JAVA_HOME used on the system, and put the result on the top of the
; stack
; Will return an empty string if the path cannot be determined
;
Function findJavaPath

  ;ClearErrors

  ;ReadEnvStr $1 JAVA_HOME

  ;IfErrors 0 FoundJDK

  ClearErrors

  ; Use the 64-bit registry on 64-bit machines
  ExpandEnvStrings $0 "%PROGRAMW6432%"
  StrCmp $0 "%PROGRAMW6432%" +2 0
  SetRegView 64

  ReadRegStr $2 HKLM "SOFTWARE\JavaSoft\Java Runtime Environment" "CurrentVersion"
  ReadRegStr $1 HKLM "SOFTWARE\JavaSoft\Java Runtime Environment\$2" "JavaHome"
  ReadRegStr $3 HKLM "SOFTWARE\JavaSoft\Java Runtime Environment\$2" "RuntimeLib"

  ;FoundJDK:

  IfErrors 0 NoErrors
  StrCpy $1 ""

NoErrors:

  ClearErrors

  ; Put the result in the stack
  Push $1

FunctionEnd


; ====================
; FindJVMPath Function
; ====================
;
; Find the full JVM path, and put the result on top of the stack
; Argument: JVM base path (result of findJavaPath)
; Will return an empty string if the path cannot be determined
;
Function findJVMPath

  ClearErrors
  
  ;Step one: Is this a JRE path (Program Files\Java\XXX)
  StrCpy $1 "$JavaHome"
  
  StrCpy $2 "$1\bin\hotspot\jvm.dll"
  IfFileExists "$2" FoundJvmDll
  StrCpy $2 "$1\bin\server\jvm.dll"
  IfFileExists "$2" FoundJvmDll
  StrCpy $2 "$1\bin\client\jvm.dll"  
  IfFileExists "$2" FoundJvmDll
  StrCpy $2 "$1\bin\classic\jvm.dll"
  IfFileExists "$2" FoundJvmDll

  ;Step two: Is this a JDK path (Program Files\XXX\jre)
  StrCpy $1 "$JavaHome\jre"
  
  StrCpy $2 "$1\bin\hotspot\jvm.dll"
  IfFileExists "$2" FoundJvmDll
  StrCpy $2 "$1\bin\server\jvm.dll"
  IfFileExists "$2" FoundJvmDll
  StrCpy $2 "$1\bin\client\jvm.dll"  
  IfFileExists "$2" FoundJvmDll
  StrCpy $2 "$1\bin\classic\jvm.dll"
  IfFileExists "$2" FoundJvmDll

  ClearErrors
  ;Step tree: Read defaults from registry
  
  ReadRegStr $1 HKLM "SOFTWARE\JavaSoft\Java Runtime Environment" "CurrentVersion"
  ReadRegStr $2 HKLM "SOFTWARE\JavaSoft\Java Runtime Environment\$1" "RuntimeLib"
  
  IfErrors 0 FoundJvmDll
  StrCpy $2 ""

  FoundJvmDll:
  ClearErrors

  ; Put the result in the stack
  Push $2

FunctionEnd


; ====================
; CheckJvm Function
; ====================
;
Function checkJvm

  !insertmacro MUI_INSTALLOPTIONS_READ $3 "jvm.ini" "Field 2" "State"
  IfFileExists "$3\bin\java.exe" NoErrors1
  MessageBox MB_OK|MB_ICONSTOP "No Java Virtual Machine found in folder:$\r$\n$3"
  Quit
NoErrors1:
  StrCpy "$JavaHome" $3
  Call findJVMPath
  Pop $4
  StrCmp $4 "" 0 NoErrors2
  MessageBox MB_OK|MB_ICONSTOP "No Java Virtual Machine found in folder:$\r$\n$3"
  Quit
NoErrors2:

FunctionEnd

; ==================
; Configure Function
; ==================
;
; Display the configuration dialog boxes, read the values entered by the user,
; and build the configuration files
;
Function configure

  !insertmacro MUI_INSTALLOPTIONS_READ $R0 "config.ini" "Field 2" "State"
  !insertmacro MUI_INSTALLOPTIONS_READ $R1 "config.ini" "Field 5" "State"
  !insertmacro MUI_INSTALLOPTIONS_READ $R2 "config.ini" "Field 7" "State"

  IfSilent 0 +2
  StrCpy $R0 '8180'

  StrCpy $R4 'port="$R0"'
  StrCpy $R5 ''

  IfSilent Silent 0

  ; Escape XML
  Push $R1
  Call xmlEscape
  Pop $R1
  Push $R2
  Call xmlEscape
  Pop $R2
  
  StrCmp $R1 "" +4 0  ; Blank user - do not add anything to tomcat-users.xml
  StrCmp $R2 "" +3 0  ; Blank password - do not add anything to tomcat-users.xml
  StrCpy $R5 '<user name="$R1" password="$R2" roles="admin,manager,default" />'
  DetailPrint 'Admin user added: "$R1"'
  
Silent:
  DetailPrint 'HTTP/1.1 Connector configured on port "$R0"'

  SetOutPath $TEMP
  File /r confinstall

  ; Build final server.xml
  Delete "$INSTDIR\conf\server.xml"
  FileOpen $R9 "$INSTDIR\conf\server.xml" w

  Push "$TEMP\confinstall\server_1.xml"
  Call copyFile
  FileWrite $R9 $R4
  Push "$TEMP\confinstall\server_2.xml"
  Call copyFile

  FileClose $R9

  DetailPrint "server.xml written"

  ; Build final tomcat-users.xml
  
  Delete "$INSTDIR\conf\tomcat-users.xml"
  FileOpen $R9 "$INSTDIR\conf\tomcat-users.xml" w
  ; File will be written using current windows codepage
  System::Call 'Kernel32::GetACP() i .r18'
  StrCmp $R8 "932" 0 +3
    ; Special case where Java uses non-standard name for character set
    FileWrite $R9 "<?xml version='1.0' encoding='ms$R8'?>$\r$\n"
    Goto +2
    FileWrite $R9 "<?xml version='1.0' encoding='cp$R8'?>$\r$\n"
  Push "$TEMP\confinstall\tomcat-users_1.xml"
  Call copyFile
  FileWrite $R9 $R5
  Push "$TEMP\confinstall\tomcat-users_2.xml"
  Call copyFile

  FileClose $R9

  DetailPrint "tomcat-users.xml written"

  RMDir /r "$TEMP\confinstall"

FunctionEnd


Function xmlEscape
  Pop $0
  ${StrRep} $0 $0 "&" "&amp;"
  ${StrRep} $0 $0 "$\"" "&quot;"
  ${StrRep} $0 $0 "<" "&lt;"
  ${StrRep} $0 $0 ">" "&gt;"
  Push $0
FunctionEnd


; =================
; CopyFile Function
; =================
;
; Copy specified file contents to $R9
;
Function copyFile

  ClearErrors

  Pop $0

  FileOpen $1 $0 r

 NoError:

  FileRead $1 $2
  IfErrors EOF 0
  FileWrite $R9 $2

  IfErrors 0 NoError

 EOF:

  FileClose $1

  ClearErrors

FunctionEnd


;--------------------------------
;Uninstaller Section

Section Uninstall

  Delete "$INSTDIR\modern.exe"
  Delete "$INSTDIR\Uninstall.exe"

  ; Stop FreeSHIM service monitor if running
  nsExec::ExecToLog '"$INSTDIR\bin\tomcat6w.exe" //MQ//Tomcat6'
  ; Delete Tomcat service
  nsExec::ExecToLog '"$INSTDIR\bin\tomcat6.exe" //DS//Tomcat6'
  ClearErrors

  DeleteRegKey HKCR "JSPFile"
  DeleteRegKey HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\FreeSHIM"
  DeleteRegKey HKLM "SOFTWARE\FreeMED\FreeSHIM"
  DeleteRegValue HKLM "Software\Microsoft\Windows\CurrentVersion\Run" "ApacheTomcatMonitor"
  RMDir /r "$SMPROGRAMS\FreeSHIM"
  Delete "$INSTDIR\tomcat.ico"
  Delete "$INSTDIR\LICENSE"
  Delete "$INSTDIR\NOTICE"
  RMDir /r "$INSTDIR\bin"
  RMDir /r "$INSTDIR\lib"
  Delete "$INSTDIR\conf\*.dtd"
  RMDir "$INSTDIR\logs"
;  RMDir /r "$INSTDIR\webapps\balancer"
  RMDir /r "$INSTDIR\webapps\docs"
  RMDir /r "$INSTDIR\webapps\shim"
;  RMDir /r "$INSTDIR\webapps\webdav"
  RMDir /r "$INSTDIR\work"
  RMDir /r "$INSTDIR\temp"
  RMDir "$INSTDIR"

  IfSilent Removed 0

  ; if $INSTDIR was removed, skip these next ones
  IfFileExists "$INSTDIR" 0 Removed 
    MessageBox MB_YESNO|MB_ICONQUESTION \
      "Remove all files in your Tomcat @VERSION_MAJOR_MINOR@ directory? (If you have anything  \
 you created that you want to keep, click No)" IDNO Removed
    RMDir /r "$INSTDIR\webapps\ROOT" ; this would be skipped if the user hits no
    RMDir "$INSTDIR\webapps"
    Delete "$INSTDIR\*.*" 
    RMDir /r "$INSTDIR"
    Sleep 500
    IfFileExists "$INSTDIR" 0 Removed 
      MessageBox MB_OK|MB_ICONEXCLAMATION \
                 "Note: $INSTDIR could not be removed."
  Removed:

SectionEnd

;eof
