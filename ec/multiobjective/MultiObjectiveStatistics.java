/*
  Copyright 2010 by Sean Luke and George Mason University
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/

package ec.multiobjective;

import java.util.ArrayList;
import java.util.Arrays;
import ec.EvolutionState;
import ec.Individual;
import ec.Subpopulation;
import ec.multiobjective.MultiObjectiveFitness;
import ec.simple.SimpleStatistics;
import ec.util.*;
import ec.vector.*;
import java.io.*;

/* 
 * MultiObjectiveStatistics.java
 * 
 * Created: Thu Feb 04 2010
 * By: Faisal Abidi and Sean Luke
 *
 */

/*
 * MultiObjectiveStatistics are a SimpleStatistics subclass which overrides the finalStatistics
 * method to output the current Pareto Front in various ways:
 *
 * <ul>
 * <li><p>Every individual in the Pareto Front is written to the end of the statistics log.
 * <li><p>A summary of the objective values of the Pareto Front is written to stdout.
 * <li><p>The objective values of the Pareto Front are written in tabular form to a special
 * Pareto Front file specified with the parameters below.  This file can be easily read by
 * gnuplot or Excel etc. to display the Front (if it's 2D or perhaps 3D).
 * 
 * <p>
 * <b>Parameters</b><br>
 * <table>
 * <tr>
 * <td valign=top><i>base</i>.<tt>front</tt><br>
 * <font size=-1>String (a filename)</font></td>
 * <td valign=top>(The Pareto Front file, if any)</td>
 * </tr>
 * </table>
 */

public class MultiObjectiveStatistics extends SimpleStatistics
    {   
    /** front file parameter */
    public static final String P_MODULO_FRONT_FILE = "modulo.front";
    public static final String P_PARETO_FRONT_FILE = "front";
    public static final String P_SILENT_FRONT_FILE = "silent.front";
    public static final String P_SOLUTION_FILE="solution.file";
        
    public boolean silentFront;

    /** The pareto front log */
    public int solutionLog=0;
    public int frontLog = 0;  // stdout by default
    public int moduloFront;

    public void setup(final EvolutionState state, final Parameter base)
        {
        super.setup(state,base);

        silentFront = state.parameters.getBoolean(base.push(P_SILENT), null, false);
        // yes, we're stating it a second time.  It's correct logic.
        silentFront = state.parameters.getBoolean(base.push(P_SILENT_FRONT_FILE), null, silentFront);
        

        moduloFront = state.parameters.getInt(base.push(P_MODULO_FRONT_FILE),null, moduloFront);


        File frontFile = state.parameters.getFile(base.push(P_PARETO_FRONT_FILE),null);
        File solutionFile = state.parameters.getFile(base.push(P_SOLUTION_FILE),null);
        try
            {
            solutionLog = state.output.addLog(solutionFile, !compress, compress);
            }
        catch (IOException i)
            {
            state.output.fatal("An IOException occurred while trying to create the log " + solutionLog + ":\n" + i);
            }

        if (silentFront)
            {
            frontLog = Output.NO_LOGS;
            }
        else if (frontFile!=null)
            {
            try
                {
                frontLog = state.output.addLog(frontFile, !compress, compress);
                }
            catch (IOException i)
                {
                state.output.fatal("An IOException occurred while trying to create the log " + frontFile + ":\n" + i);
                }
            }
        else state.output.warning("No Pareto Front statistics file specified, printing to stdout at end.", base.push(P_PARETO_FRONT_FILE));
        }


    /** Logs the best individual of the generation. */
    public void postEvaluationStatistics(final EvolutionState state)
        {

        super.postEvaluationStatistics(state);

        // if (state.generation%moduloFront==0){
        //     for (int s = 0; s < state.population.subpops.length; s++)
        //         {
        //         MultiObjectiveFitness typicalFitness = (MultiObjectiveFitness)(state.population.subpops[s].individuals[0].fitness);
        //         //if (doFinal) state.output.println("\n\nPareto Front of Subpopulation " + s, statisticslog);

        //         // build front
        //         ArrayList front = typicalFitness.partitionIntoParetoFront(state.population.subpops[s].individuals, null, null);

        //         // sort by objective[0]
        //         Object[] sortedFront = front.toArray();
        //         QuickSort.qsort(sortedFront, new SortComparator()
        //             {
        //             public boolean lt(Object a, Object b)
        //                 {
        //                 return (((MultiObjectiveFitness) (((Individual) a).fitness)).getObjective(0) < 
        //                     (((MultiObjectiveFitness) ((Individual) b).fitness)).getObjective(0));
        //                 }
                    
        //             public boolean gt(Object a, Object b)
        //                 {
        //                 return (((MultiObjectiveFitness) (((Individual) a).fitness)).getObjective(0) > 
        //                     ((MultiObjectiveFitness) (((Individual) b).fitness)).getObjective(0));
        //                 }
        //             });
                            
                    
        //         // write short version of front out to disk
        //         if (!silentFront)
        //             {
        //             if (state.population.subpops.length > 1)
        //                 state.output.println("Subpopulation " + s, frontLog);
        //             //Imprimir la generacion 
        //             state.output.println("Generacion: "+ state.generation , frontLog);
        //             for (int i = 0; i < sortedFront.length; i++)
        //                 {
        //                 Individual ind = (Individual)(sortedFront[i]);
        //                 MultiObjectiveFitness mof = (MultiObjectiveFitness) (ind.fitness);
        //                 double[] objectives = mof.getObjectives();
            
        //                 String line = "";
        //                 for (int f = 0; f < objectives.length; f++)
        //                     line += (objectives[f] + " ");
        //                 state.output.println(line, frontLog);
        //                 }
        //             }
        //      }
        // }
        }   











    /** Logs the best individual of the run. */
    public void finalStatistics(final EvolutionState state, final int result)
        {
        bypassFinalStatistics(state, result);  // just call super.super.finalStatistics(...)

        if (doFinal) state.output.println("\n\n\n PARETO FRONTS", statisticslog);
        for (int s = 0; s < state.population.subpops.length; s++)
            {
            MultiObjectiveFitness typicalFitness = (MultiObjectiveFitness)(state.population.subpops[s].individuals[0].fitness);
            if (doFinal) state.output.println("\n\nPareto Front of Subpopulation " + s, statisticslog);

            // build front
            ArrayList front = typicalFitness.partitionIntoParetoFront(state.population.subpops[s].individuals, null, null);

            // sort by objective[0]
            Object[] sortedFront = front.toArray();
            QuickSort.qsort(sortedFront, new SortComparator()
                {
                public boolean lt(Object a, Object b)
                    {
                    return (((MultiObjectiveFitness) (((Individual) a).fitness)).getObjective(0) < 
                        (((MultiObjectiveFitness) ((Individual) b).fitness)).getObjective(0));
                    }
                
                public boolean gt(Object a, Object b)
                    {
                    return (((MultiObjectiveFitness) (((Individual) a).fitness)).getObjective(0) > 
                        ((MultiObjectiveFitness) (((Individual) b).fitness)).getObjective(0));
                    }
                });
                        
            // print out front to statistics log
            if (doFinal)
                for (int i = 0; i < sortedFront.length; i++)
                    ((Individual)(sortedFront[i])).printIndividualForHumans(state, statisticslog);
                
            // write short version of front out to disk
            if (!silentFront)
                {
                if (state.population.subpops.length > 1)
                    state.output.println("Subpopulation " + s, frontLog);
                for (int i = 0; i < sortedFront.length; i++)
                    {
                    Individual ind = (Individual)(sortedFront[i]);
                    MultiObjectiveFitness mof = (MultiObjectiveFitness) (ind.fitness);
                    double[] objectives = mof.getObjectives();
                    FloatVectorIndividual t_ind = (FloatVectorIndividual)ind;
                    FloatVectorSpecies t_spe=(FloatVectorSpecies)ind.species;
        
                    //Imprimo la ubicacion de antenas
                    state.output.println("SOLUCION DEL FRENTE DE PARETO "+i,solutionLog);
                    int tipo_infraestructura;
                    double [] centro;
                    for (int j=0;j<t_ind.genome.length;j++){
                        tipo_infraestructura=(int) Math.floor(t_ind.genome[j]);
                        if (tipo_infraestructura!=0){
                            centro=encontrar_centro_antena(t_ind, j, tipo_infraestructura, t_spe);
                            double radio_circulo=t_spe.getRadioAntena()[tipo_infraestructura];
                            double posicion=t_ind.genome[j]-tipo_infraestructura;
                            state.output.println(j+","+radio_circulo+","+posicion+","+t_spe.getPtoInicialSegmento()[j]+","+t_spe.getPtoFinalSegmento()[j]+","+t_spe.getLongitudes()[t_spe.getPtoInicialSegmento()[j]]+","+t_spe.getLatitudes()[t_spe.getPtoInicialSegmento()[j]]+","+t_spe.getLongitudes()[t_spe.getPtoFinalSegmento()[j]]+","+t_spe.getLatitudes()[t_spe.getPtoFinalSegmento()[j]]+","+centro[1]+","+centro[0], solutionLog);
                        }     
                    }    
                    state.output.println("",solutionLog);
                    state.output.println("",solutionLog);

                    String line = "";
                    for (int f = 0; f < objectives.length; f++)
                        line += (objectives[f] + " ");
                    state.output.println(line, frontLog);
                    }
                }
             }


        }

        public double [] encontrar_centro_antena (FloatVectorIndividual t_ind, int indice, int tipo_infraestructura, FloatVectorSpecies t_spe){
            
            double lat_ini= t_spe.getLatitudes()[t_spe.getPtoInicialSegmento()[indice]];
            double lng_ini= t_spe.getLongitudes()[t_spe.getPtoInicialSegmento()[indice]];
            double lat_fin= t_spe.getLatitudes()[t_spe.getPtoFinalSegmento()[indice]];
            double lng_fin= t_spe.getLongitudes()[t_spe.getPtoFinalSegmento()[indice]];

            double a=(lng_fin-lng_ini)/(double)(lat_fin-lat_ini);
            double lambda = t_ind.genome[indice]-tipo_infraestructura;
            double [] centro = new double [2];
            if (lat_fin>lat_ini)
                centro[0]= lat_ini + Math.sqrt((lng_fin-lng_ini)*(lng_fin-lng_ini) + (lat_fin-lat_ini)*(lat_fin-lat_ini))*lambda/(double)Math.sqrt(1+(a*a));
            else
                centro[0]= lat_ini - Math.sqrt((lng_fin-lng_ini)*(lng_fin-lng_ini) + (lat_fin-lat_ini)*(lat_fin-lat_ini))*lambda/(double)Math.sqrt(1+(a*a));
            
            if (lng_fin>lng_ini)
                centro[1]= lng_ini + Math.abs(Math.sqrt((lng_fin-lng_ini)*(lng_fin-lng_ini) + (lat_fin-lat_ini)*(lat_fin-lat_ini))*a*lambda/(double)Math.sqrt(1+(a*a)));
            else
                centro[1]= lng_ini - Math.abs(Math.sqrt((lng_fin-lng_ini)*(lng_fin-lng_ini) + (lat_fin-lat_ini)*(lat_fin-lat_ini))*a*lambda/(double)Math.sqrt(1+(a*a)));
                
            //DEBUG
            //System.out.println("El centro de ("+lat_ini+" "+lng_ini+") y ("+lat_fin+" "+lng_fin+") es ("+centro[0]+" "+centro[1]+") con lambda "+lambda);   
            return centro;    
        }
    }
