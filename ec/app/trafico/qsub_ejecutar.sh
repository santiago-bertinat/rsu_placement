#!/bin/bash

# Nombre del trabajo
#PBS -N job_RUP

# Requerimientos
# En este caso nuestro trabajo requiere: 1 nodo con 4 procesadores disponibles, 1 hora de ejecución.
#PBS -l nodes=1:ppn=1,walltime=20:00:00

# Cola de ejecución
#PBS -q serial

# Directorio de trabajo
#PBS -d /home/renzom/mic2015-trafico/ec/app/trafico

# Correo electronico
#PBS -M renzom@fing.edu.uy

# Email
#PBS -m abe
# n: no mail will be sent.
# a: mail is sent when the job is aborted by the batch system.
# b: mail is sent when the job begins execution.
# e: mail is sent when the job terminates.

# Directorio donde se guardará la salida estándar y de error de nuestro trabajo
#PBS -e /home/renzom/mic2015-trafico/ec/app/trafico
#PBS -o /home/renzom/mic2015-trafico/ec/app/trafico


echo Job Name: $PBS_JOBNAME
echo Working directory: $PBS_O_WORKDIR
echo Queue: $PBS_QUEUE
echo Cantidad de tasks: $PBS_TASKNUM
echo Home: $PBS_O_HOME
echo Puerto del MOM: $PBS_MOMPORT
echo Nombre del usuario: $PBS_O_LOGNAME
echo Idioma: $PBS_O_LANG
echo Cookie: $PBS_JOBCOOKIE
echo Offset de numero de nodos: $PBS_NODENUM
echo Shell: $PBS_O_SHELL
echo Host: $PBS_O_HOST
echo Cola de ejecucion: $PBS_QUEUE
echo Archivo de nodos: $PBS_NODEFILE
echo Path: $PBS_O_PATH
echo
cd $PBS_O_WORKDIR
echo Current path:
pwd
echo
echo Nodos:
cat $PBS_NODEFILE
echo
echo Cantidad de nodos:
NPROCS=$(wc -l < $PBS_NODEFILE)
echo $NPROCS
echo

export CLASSPATH=/home/renzom/mic2015-trafico/
for TIPO in low normal high 
do
	echo "Ejecutando instancia tipo $TIPO"
	java ec.Evolve -file /home/renzom/mic2015-trafico/ec/app/trafico/trafico.params -p pop.subpop.0.species.ruta-adyacencias=/home/renzom/mic2015-trafico/ec/app/trafico/instancias/$TIPO.txt -p stat.solution.file=res_$TIPO/solution.stat -p stat.front=res_$TIPO/front.stat -p stat.file=res_$TIPO/out.stat 
done
