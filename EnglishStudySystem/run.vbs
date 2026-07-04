Set objShell = CreateObject("WScript.Shell")
Set objFSO = CreateObject("Scripting.FileSystemObject")
strAppDir = objFSO.GetFile(WScript.ScriptFullName).ParentFolder.Path
objShell.Run chr(34) & strAppDir & "\jre-minimal\bin\javaw.exe" & chr(34) & " -jar " & chr(34) & strAppDir & "\bin\EnglishStudySystem.jar" & chr(34), 0, False
