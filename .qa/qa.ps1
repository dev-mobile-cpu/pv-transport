$global:ADB = "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe"
$global:QA = "C:\pv\pv-transport\.qa"

function Snap([string]$name) {
  & $ADB shell screencap -p /sdcard/__qa.png | Out-Null
  & $ADB pull -a /sdcard/__qa.png "$QA\$name.png" | Out-Null
  "$QA\$name.png"
}

function Dump() {
  & $ADB shell uiautomator dump /sdcard/__qa.xml 2>&1 | Out-Null
  & $ADB pull /sdcard/__qa.xml "$QA\dump.xml" 2>&1 | Out-Null
  [xml]$x = Get-Content "$QA\dump.xml" -Encoding UTF8
  $x
}

function Nodes() {
  $x = Dump
  $x.SelectNodes("//node") | ForEach-Object {
    $b = $_.bounds
    if ($b -match '\[(\d+),(\d+)\]\[(\d+),(\d+)\]') {
      $cx = [int](([int]$Matches[1] + [int]$Matches[3]) / 2)
      $cy = [int](([int]$Matches[2] + [int]$Matches[4]) / 2)
    } else { $cx = 0; $cy = 0 }
    [PSCustomObject]@{
      text = $_.text; desc = $_.'content-desc'; cls = $_.class
      clickable = $_.clickable; enabled = $_.enabled; bounds = $b; cx = $cx; cy = $cy
    }
  }
}

function Texts() { Nodes | Where-Object { $_.text -ne '' } | Select-Object text, clickable, enabled, cx, cy, bounds }

function TapText([string]$t, [int]$idx = 0) {
  $n = @(Nodes | Where-Object { $_.text -eq $t })
  if ($n.Count -eq 0) { Write-Output "NOTFOUND: $t"; return }
  $target = $n[$idx]
  & $ADB shell input tap $target.cx $target.cy | Out-Null
  Write-Output "tapped '$t' at $($target.cx),$($target.cy)"
}

function Tap([int]$x, [int]$y) { & $ADB shell input tap $x $y | Out-Null }
function SwipeUp() { & $ADB shell input swipe 540 1600 540 700 300 | Out-Null }
function SwipeDown() { & $ADB shell input swipe 540 700 540 1600 300 | Out-Null }
function TypeText([string]$s) { & $ADB shell input text $s | Out-Null }
function Back() { & $ADB shell input keyevent 4 | Out-Null }

function GoOffline() { & $ADB shell svc data disable; & $ADB shell svc wifi disable; & $ADB shell cmd connectivity airplane-mode enable }
function GoOnline() { & $ADB shell cmd connectivity airplane-mode disable; & $ADB shell svc data enable; & $ADB shell svc wifi enable }

function Logs([string]$pat = "SyncWorker|UPLOAD_DEBUG|WM-|pv.transport") {
  & $ADB logcat -d | Select-String -Pattern $pat
}
function ClearLogs() { & $ADB logcat -c }
function Relaunch() {
  & $ADB shell am force-stop com.pv.transport
  Start-Sleep -Milliseconds 500
  & $ADB shell am start -n com.pv.transport/.MainActivity | Out-Null
}
