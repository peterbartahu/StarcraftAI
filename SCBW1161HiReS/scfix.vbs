Set WshShell = WScript.CreateObject("WScript.Shell") 
set svc=GetObject("winmgmts:root\default:StdRegProv") 
Wscript.Quit(svc.setStringValue(&H80000002, "SOFTWARE\Blizzard Entertainment\Starcraft", "Program", WshShell.currentDirectory & "\Starcraft.exe"))