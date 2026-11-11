@echo off
if not exist build\classes mkdir build\classes
for /r src %%f in (*.java) do echo %%f>> fuentes.txt
javac -encoding UTF-8 -d build\classes @fuentes.txt
if errorlevel 1 pause & exit /b 1
del fuentes.txt
jar cfe BibliotecaUNAB.jar cl.biblioteca.Main -C build\classes .
echo Compilacion lista: BibliotecaUNAB.jar
pause
