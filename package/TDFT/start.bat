@echo off

java -version >nul 2>&1 || (
    echo It seems like java hasn't been installed on your system.
    echo This application requires at least Java 8!
    exit /B 1
)

if exist tdfp-gui.jar (
    set file=tdfp-gui.jar
) else (
    if exist tdfp-cli.jar (
        set file=tdfp-cli.jar
    ) else (
        echo Failed to find TDFP file!
        exit /B 1
    )
)

rem Execute application
java -jar %file% --plugin="./plugins/"