@echo off
setlocal

REM Minimal reproducer for javac NPE in Types.interfaceCandidates()
REM Requires: JDK 21 and ASM 9.x jar (e.g., asm-9.7.1.jar)
REM
REM The bug: javac crashes when an interface .class file has a method with
REM ACC_BRIDGE | ACC_ABSTRACT | ACC_SYNTHETIC flags, and overload resolution
REM requires checking isFunctionalInterface() on that interface.

set ASM_JAR=asm-9.7.1.jar

echo === Step 1: Compile library sources ===
if exist classes rmdir /s /q classes
mkdir classes
javac -d classes lib\I.java lib\Sub.java lib\Util.java
if errorlevel 1 goto :error

echo === Step 2: Patch I.class (add ACC_BRIDGE+ACC_SYNTHETIC to typeof) ===
javac -cp %ASM_JAR% PatchFlags.java
if errorlevel 1 (
    echo ERROR: Could not compile PatchFlags. Make sure %ASM_JAR% is in the current directory.
    goto :error
)
java -cp %ASM_JAR%;. PatchFlags classes\I.class
if errorlevel 1 goto :error

echo === Step 3: Compile test against patched classes (should crash) ===
javac -cp classes Test.java
if errorlevel 1 (
    echo.
    echo *** javac crashed as expected ***
) else (
    echo.
    echo *** Bug did NOT reproduce - javac succeeded ***
)

goto :end
:error
echo Build failed
:end
endlocal
