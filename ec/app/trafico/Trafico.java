package ec.app.trafico;

import ec.util.*;
import ec.*;
import ec.multiobjective.MultiObjectiveFitness;
import ec.simple.*;
import ec.vector.*;



public class Trafico extends Problem implements SimpleProblemForm
{

    private static final long serialVersionUID = 1L;


    public void setup(final EvolutionState state, final Parameter base)
    {
        super.setup(state, base);
    }



    public void evaluate(final EvolutionState state,
        final Individual ind,
        final int subpopulation,
        final int threadnum)
        {
        if( !( ind instanceof FloatVectorIndividual ) )
            state.output.fatal( "The individuals for this problem should be FloatVectorIndividuals." );

        FloatVectorIndividual temp = (FloatVectorIndividual)ind;
        float[] genome = temp.genome;
        int numDecisionVars = genome.length;

        double[] objectives = ((MultiObjectiveFitness)ind.fitness).getObjectives();

        objectives[0] = costo(ind);
        objectives[1]  = qos(ind);
        
        ((MultiObjectiveFitness)ind.fitness).setObjectives(state, objectives);
        ind.evaluated = true;
        }


    public double costo(Individual ind){

        FloatVectorIndividual t_ind = (FloatVectorIndividual)ind;
        FloatVectorSpecies t_spe = (FloatVectorSpecies)ind.species;

        double costo=0;
        int tipo_infraestructura;
        for (int i=0;i<t_ind.genome.length;i++){
            tipo_infraestructura=(int) Math.floor(t_ind.genome[i]);
            costo+=t_spe.getPrecioAntena()[tipo_infraestructura];
        }
        return costo;
    
    }

    public double qos (Individual ind){
        FloatVectorIndividual t_ind = (FloatVectorIndividual)ind;
        FloatVectorSpecies t_spe = (FloatVectorSpecies)ind.species;
        double qos=0;

        float [] vehiculos_atendidos_antena = new float [(int)Math.round(maxGene[0])];
        contar_vehiculos (t_ind, t_spe, &vehiculos_atendidos_antena);


        float di;
        float velocidadi;
        for (int i=1;i<(int)Math.round(maxGene[0]);i++){
            di=getRadioAntenas()[i];
            //velocidadi=????
            //qos+=(di/velocidadi)*vehiculos_atendidos_antena[i];
        }
        return qos;
    }


    public void (FloatVectorIndividual t_ind, FloatVectorSpecies t_spe, float* vehiculos_atendidos_antena){
        for (int i=0;i<t_ind.genome.length;i++){
            

            
        }
    }

}



