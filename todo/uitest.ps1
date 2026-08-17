$ADB = "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe"

function UiXml {
    & $ADB shell uiautomator dump /sdcard/u.xml 2>&1 | Out-Null
    return ((& $ADB shell cat /sdcard/u.xml) -join "")
}

function UiTexts {
    $xml = UiXml
    ($xml -split 'text="') | ForEach-Object {
        if ($_ -match '^([^"]{1,80})"') { $matches[1] }
    } | Where-Object { $_ -ne "" } | Select-Object -Unique
}

function NodeBounds([string]$text, [string]$xml) {
    if (-not $xml) { $xml = UiXml }
    $esc = [regex]::Escape($text)
    if ($xml -match "text=`"$esc`"[^>]*bounds=`"\[(\d+),(\d+)\]\[(\d+),(\d+)\]`"") {
        return @([int]$matches[1], [int]$matches[2], [int]$matches[3], [int]$matches[4])
    }
    return $null
}

function TapText([string]$text, [int]$dy = 0) {
    $b = NodeBounds $text
    if ($null -eq $b) { Write-Output "MISS: $text"; return $false }
    $x = [int](($b[0] + $b[2]) / 2)
    $y = [int](($b[1] + $b[3]) / 2) + $dy
    & $ADB shell input tap $x $y
    Write-Output "TAP '$text' @ $x,$y"
    return $true
}

function TypeText([string]$s) {
    $safe = $s -replace ' ', '%s'
    & $ADB shell input text "$safe"
}

function ClearField() {
    & $ADB shell input keyevent 123 | Out-Null
    1..25 | ForEach-Object { & $ADB shell input keyevent 67 | Out-Null }
}

function Shot([string]$name) {
    & $ADB shell screencap -p /sdcard/s.png
    & $ADB pull /sdcard/s.png "C:\pv\pv-transport\todo\shots\$name.png" | Out-Null
    Write-Output "shot: $name"
}

function Focus() {
    (& $ADB shell "dumpsys window | grep -E 'mCurrentFocus'")
}

function CrashCheck() {
    $l = & $ADB logcat -d -b crash 2>&1 | Select-String -Pattern "com.pv.transport"
    if ($l) { Write-Output "CRASH FOUND:"; $l } else { Write-Output "no crash" }
}
