@echo off
powershell -NoExit -Command ^
"cd 'F:\space v2\space_game'; ^
Remove-Item -Path 'bin\*.class' -Force -ErrorAction SilentlyContinue; ^
javac -encoding UTF-8 -d bin -sourcepath src src\*.java; ^
if ($LASTEXITCODE -eq 0) { ^
    cd bin; ^
    java GameWindow ^
}"
