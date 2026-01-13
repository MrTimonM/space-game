@echo off
powershell -NoExit -Command ^
"cd 'F:\space v2\space_game'; ^
javac -d bin src/*.java; ^
if ($LASTEXITCODE -eq 0) { ^
    cd bin; ^
    java SpaceGame ^
}"
