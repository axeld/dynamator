@echo off
rem ------------------------------------------------------------
rem testall.bat: Comprehensive build-and-test script for Windows.
rem
rem This is the authoritative build-and-test script. 
rem 
rem Description:
rem - cleans generated files
rem - builds Dynamator under JDK 1.2
rem - lints using PMD
rem - runs tests under JDK 1.2
rem - runs tests under JDK 1.1
rem - runs tests under jview
rem - runs coverage tests
rem
rem Environment variables used:
rem - %JAVA_HOME%       must refer to the installation directory
rem                     of a JDK compatible with jcoverage and pmd
rem                     (The authoritative environment uses JDK 1.4)
rem - %JAVA_1.1_HOME%   may refer to the installation directory
rem                     of a JDK 1.1.6 or greater 
rem                     (default is /jdk1.1.8)
rem - %JAVA_1.2_HOME%   may refer to the installation directory
rem                     of a JDK 1.2 or greater 
rem                     (default is /jdk1.2.2)
rem
rem Requirements:
rem - File build.properties must define the following properties:
rem   - pmd.home: install directory for PMD
rem   - jcoverage.home: install directory for JCoverage
rem - jview must be available (should come with Windows)
rem
rem Tested under:
rem - Windows XP
rem
rem Quirks
rem - Ant's ant.bat script prepends %JAVA_HOME%/lib/tools.jar
rem   to the classpath, which messes up the compile (1.2 trying
rem   to run the 1.4 compiler). Therefore invocations of the
rem   ant build and test scripts set JAVA_HOME instead of JAVACMD. 
rem   This means that on an error condition, JAVA_HOME may be 
rem   incorrectly set.
rem - Ant sets errorlevel=1 when the default build.xml target is run,
rem   so the compile call uses the 'jar' target.
rem - ant-contrib needs to be rebuilt using the JDK 1.2 compiler
rem   to be used by this script.
rem - jakarta-oro needs to be modified to run under JDK 1.1 
rem   to be used by this script.
rem - ant.bat is incompatible with jview, so it is bypassed for 
rem   the jview test.
rem ------------------------------------------------------------
@
rem ------------------------------------------------------------
rem Clear the DOS error level
rem ------------------------------------------------------------
@
ver>nul:
@
rem ------------------------------------------------------------
rem Set working variables
rem ------------------------------------------------------------
@
if "%JAVA_HOME%"=="" goto no_java_home
set _JAVA_1.1_HOME=
set _JAVA_1.2_HOME=
set JAVACMD=
if not "%JAVA_1.1_HOME%"=="" set _JAVA_1.1_HOME=%JAVA_1.1_HOME%
if not "%JAVA_1.2_HOME%"=="" set _JAVA_1.2_HOME=%JAVA_1.2_HOME%
if "%_JAVA_1.1_HOME%"=="" set _JAVA_1.1_HOME=\jdk1.1.8
if "%_JAVA_1.2_HOME%"=="" set _JAVA_1.2_HOME=\jdk1.2.2
if not exist "%_JAVA_1.1_HOME%" goto no_java_1
if not exist "%_JAVA_1.2_HOME%" goto no_java_2
@
echo ------------------------------------------------------------
echo Clean
echo ------------------------------------------------------------
@
call ant clean
@
echo ------------------------------------------------------------
echo Build using JDK 1.2
echo ------------------------------------------------------------
@
set _DYNAMATOR_JAVA_HOME_SAVE_=%JAVA_HOME%
set JAVA_HOME=%_JAVA_1.2_HOME%
call ant require-jdk-1.2
if errorlevel 1 goto skip_compile
call ant jar
:skip_compile
set JAVA_HOME=%_DYNAMATOR_JAVA_HOME_SAVE_%
set _DYNAMATOR_JAVA_HOME_SAVE_=
if errorlevel 1 goto _end_
@
echo ------------------------------------------------------------
echo Lint
echo ------------------------------------------------------------
@
call ant -f pmd.xml
@
echo ------------------------------------------------------------
echo Test using JDK 1.2
echo ------------------------------------------------------------
@
set _DYNAMATOR_JAVA_HOME_SAVE_=%JAVA_HOME%
set JAVA_HOME=%_JAVA_1.2_HOME%
call ant -f test.xml 
set JAVA_HOME=%_DYNAMATOR_JAVA_HOME_SAVE_%
set _DYNAMATOR_JAVA_HOME_SAVE_=
if errorlevel 1 goto _end_
@
echo ------------------------------------------------------------
echo Test using JDK 1.1
echo ------------------------------------------------------------
@
set _DYNAMATOR_JAVA_HOME_SAVE_=%JAVA_HOME%
set JAVA_HOME=%_JAVA_1.1_HOME%
call ant -f test.xml
set JAVA_HOME=%_DYNAMATOR_JAVA_HOME_SAVE_%
set _DYNAMATOR_JAVA_HOME_SAVE_=
if errorlevel 1 goto _end_
@
echo ------------------------------------------------------------
echo Test using jview
echo ------------------------------------------------------------
@
set _DYNAMATOR_CLASSPATH_SAVE_=%CLASSPATH%
set LOCALCLASSPATH=
echo %errorlevel%
for %%f in ("%ANT_HOME%\lib\*.jar") do call "%ANT_HOME%\bin\lcp.bat" %%f
set CLASSPATH=%LOCALCLASSPATH%;%CLASSPATH%
jview /d:ant.home=%ANT_HOME% org.apache.tools.ant.Main -f test.xml regression
set CLASSPATH=%_DYNAMATOR_CLASSPATH_SAVE_%
set _DYNAMATOR_CLASSPATH_SAVE_=
if errorlevel 1 goto _end_
@
echo ------------------------------------------------------------
echo Run coverage tests
echo ------------------------------------------------------------
@
call ant -f jcoverage.xml
if errorlevel 1 goto _end_
@
rem ============================================================
rem ============================================================
rem ============================================================
@
rem ------------------------------------------------------------
rem Error handling
rem ------------------------------------------------------------
@
goto _end_

:no_java_home
echo You must set environment variable JAVA_HOME.
goto _end_

:no_java_1
echo You must set environment variable JAVA_1.1_HOME to a JDK 1.1 distribution.
goto _end_

:no_java_2
echo You must set environment variable JAVA_1.2_HOME to a JDK 1.2 distribution.
goto _end_

:_end_
