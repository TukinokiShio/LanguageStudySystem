; JSS project installer. Keep the AppId stable for upgrades.
#define MyAppName "JapStudySystem"
#define MyAppVersion "3.8.7"
#define MyAppPublisher "SHIO, Inc."
#define MyAppURL "https://github.com/TukinokiShio/LanguageStudySystem"
#define MyAppScript "run.vbs"

[Setup]
AppId={{43063371-76E3-448C-81CE-415C4C209AEC}
AppName={#MyAppName}
AppVersion={#MyAppVersion}
AppPublisher={#MyAppPublisher}
AppPublisherURL={#MyAppURL}
AppSupportURL={#MyAppURL}
AppUpdatesURL={#MyAppURL}
DefaultDirName=D:\SHIO_LANGUAGE\jss
DisableDirPage=yes
DisableProgramGroupPage=yes
PrivilegesRequired=lowest
OutputDir=..\artifacts\release\output
OutputBaseFilename=JapaneseStudySystem_{#MyAppVersion}_Setup
SetupIconFile=..\JapStudySystem\app.ico
UninstallDisplayIcon={app}\app.ico
CloseApplications=yes
RestartApplications=no
Compression=lzma2
SolidCompression=yes
WizardStyle=modern dynamic
VersionInfoVersion={#MyAppVersion}

[Languages]
Name: "english"; MessagesFile: "compiler:Default.isl"

[Tasks]
Name: "desktopicon"; Description: "Create a desktop shortcut"; GroupDescription: "Additional icons:"; Flags: unchecked

[Files]
Source: "..\JapStudySystem\app.ico"; DestDir: "{app}"; Flags: ignoreversion
Source: "..\JapStudySystem\run.bat"; DestDir: "{app}"; Flags: ignoreversion
Source: "..\JapStudySystem\run.vbs"; DestDir: "{app}"; Flags: ignoreversion
Source: "..\JapStudySystem\README.md"; DestDir: "{app}"; Flags: ignoreversion isreadme
Source: "..\JapStudySystem\bin\JapanStudySystem.jar"; DestDir: "{app}\bin"; Flags: ignoreversion
Source: "..\JapStudySystem\jre-minimal\*"; DestDir: "{app}\jre-minimal"; Flags: ignoreversion recursesubdirs createallsubdirs

[Icons]
Name: "{autoprograms}\{#MyAppName}"; Filename: "{app}\{#MyAppScript}"; WorkingDir: "{app}"; IconFilename: "{app}\app.ico"; IconIndex: 0
Name: "{autodesktop}\{#MyAppName}"; Filename: "{app}\{#MyAppScript}"; WorkingDir: "{app}"; IconFilename: "{app}\app.ico"; IconIndex: 0; Tasks: desktopicon
Name: "{group}\Uninstall {#MyAppName}"; Filename: "{uninstallexe}"

[Run]
Filename: "{app}\{#MyAppScript}"; Description: "Launch {#MyAppName}"; Flags: shellexec postinstall skipifsilent
