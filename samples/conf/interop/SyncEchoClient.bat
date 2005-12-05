setlocal EnableDelayedExpansion
set SANDESHA2_CLASS_PATH=.\InteropSampleClients.jar
FOR %%c in (.\lib\*.jar) DO set SANDESHA2_CLASS_PATH=!SANDESHA2_CLASS_PATH!;%%c
set SANDESHA2_CLASS_PATH=%SANDESHA2_CLASS_PATH%;.\modules\Sandesha2-0.9.mar

java -cp %SANDESHA2_CLASS_PATH% sandesha2.samples.interop.SyncEchoClient .