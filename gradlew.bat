@rem Gradle wrapper launch script for Windows
@rem Copyright © 2015-2021 the original authors. Apache License 2.0.

@if "%DEBUG%"=="" @echo off
@rem Set local scope
setlocal

set APP_NAME=Gradle
set APP_BASE_NAME=%~n0
set APP_HOME=%~dp0

set CLASSPATH=%APP_HOME%gradle\wrapper\gradle-wrapper.jar

if defined JAVA_HOME goto findJavaFromJavaHome
set JAVA_EXE=java.exe
goto execute

:findJavaFromJavaHome
set JAVA_HOME=%JAVA_HOME:"=%
set JAVA_EXE=%JAVA_HOME%/bin/java.exe

:execute
"%JAVA_EXE%" -classpath "%CLASSPATH%" org.gradle.wrapper.GradleWrapperMain %*

:end
endlocal
