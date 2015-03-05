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
        int tipo_infraestructura;
        double centro[];
        double xcentro, ycentro,radio_circulo, alpha, beta, dist_extremo_centro,xini,yini,xfin,yfin, m, lambda;
        boolean ini_dentro, fin_dentro;
        //Recorro el individuo, segmento a segmento
        for (int i=0;i<t_ind.genome.length;i++){
            tipo_infraestructura=(int) Math.floor(t_ind.genome[i]);
            if (tipo_infraestructura!=0){ //Si no hay antena no aporta qos
                centro=encontrar_centro_antena(t_ind, i, tipo_infraestructura, t_spe);
                //Circulo de cobertura
                xcentro=centro[0];
                ycentro=centro[1];
                radio_circulo=t_spe.getRadioAntena()[tipo_infraestructura];
                //Veo la cobertura en cada uno de los segmentos
                for (int k=0;k<t_ind.genome.length;k++){

                    xini=t_spe.getLatitudes()[t_spe.getPtoInicialSegmento()[k]];
                    yini=t_spe.getLongitudes()[t_spe.getPtoInicialSegmento()[k]];
                    xfin =  t_spe.getLatitudes()[t_spe.getPtoFinalSegmento()[k]];
                    yfin = t_spe.getLongitudes()[t_spe.getPtoFinalSegmento()[k]];

                    if (k==i){
                        //Separo el calculo de la cobertura sobre el propio segmento
                        qos+= t_spe.getCantidadVehiculosSegmento()[k] *Math.min(radio_circulo, distancia_entre_dos_puntos(xcentro,ycentro,xini,yini)) + Math.min(radio_circulo, distancia_entre_dos_puntos(xcentro,ycentro,xfin,yfin));

                    }
                    else{
                        //Discrimino segun los extremos esten dentro o fuera del circulo
                        ini_dentro = radio_circulo < distancia_entre_dos_puntos(xcentro, ycentro, xini, yini);
                        fin_dentro = radio_circulo < distancia_entre_dos_puntos(xcentro, ycentro, xfin, yfin);
                        if (ini_dentro && fin_dentro){
                            //El segmento esta completamente cubierto
                            qos+= distancia_entre_dos_puntos(xini,yini,xfin,yfin)*t_spe.getCantidadVehiculosSegmento()[k];
                        }
                        else if (ini_dentro || fin_dentro){
                            //Hay un punto adentro y uno afuera
                            if (ini_dentro){
                                //El punto de adentro es el inicial
                                alpha=angulo_entre_rectas(xcentro,ycentro,xini,yini,xini,yini,xfin,yfin);
                                dist_extremo_centro= distancia_entre_dos_puntos(xcentro,ycentro,xini,yini);
                            }else{
                                //El punto de adentro es el final
                                alpha=angulo_entre_rectas(xcentro,ycentro,xini,yini,xini,yini,xfin,yfin);
                                dist_extremo_centro= distancia_entre_dos_puntos(xcentro,ycentro,xini,yini);
                            }

                            beta= Math.asin(dist_extremo_centro*Math.sin(alpha)/radio_circulo);
                            qos+=t_spe.getCantidadVehiculosSegmento()[k] * (Math.sin(180-alpha-beta)*radio_circulo/Math.sin(alpha));
                        }
                        else if (distancia_punto_recta(xcentro,ycentro,xini,yini,xfin,yfin) < radio_circulo){
                            //Hay dos puntos de interseccion
                            m=distancia_punto_recta(xcentro,ycentro,xini,yini,xfin,yfin);
                            lambda=Math.sqrt(Math.pow(radio_circulo,2) - Math.pow(m,2));
                            qos+=t_spe.getCantidadVehiculosSegmento()[k] * (2*lambda);
                        }
                }
            }

            }    

        }


        return qos;
    }


    public double [] encontrar_centro_antena (FloatVectorIndividual t_ind, int indice, int tipo_infraestructura, FloatVectorSpecies t_spe){
        
        double lat_ini= t_spe.getLatitudes()[t_spe.getPtoInicialSegmento()[indice]];
        double lng_ini= t_spe.getLongitudes()[t_spe.getPtoInicialSegmento()[indice]];
        double lat_fin= t_spe.getLatitudes()[t_spe.getPtoFinalSegmento()[indice]];
        double lng_fin= t_spe.getLongitudes()[t_spe.getPtoFinalSegmento()[indice]];

        double a=(lng_fin-lng_ini)/(lat_fin-lat_ini);
        double lambda = t_ind.genome[indice]-tipo_infraestructura;
        double [] centro = new double [2];
        centro[0] = lambda*(1/(Math.sqrt(1+Math.pow(a,2))));
        centro[1] = lambda*(a/(Math.sqrt(1+Math.pow(a,2))));   
        return centro;  
    }


    public double distancia_entre_dos_puntos(double xa, double ya, double xb, double yb){
        return Math.sqrt((xa-xb)*(xa-xb) + (ya-yb)*(ya-yb));
    }

    public double distancia_punto_recta(double xp,double yp,double x1,double y1,double x2,double y2){
        double a= Math.abs((y2-y1)*xp - (x2-x1)*yp + x2*y1 - y2*x1);
        double b= Math.pow(y2-y1,2)+ Math.pow(x2-x1,2);
        return a/Math.sqrt(b);
    }

    public double angulo_entre_rectas(double x1,double y1,double x2,double y2, double x3,double y3,double x4,double y4){
        double m1=(y2-y1)/(x2-x1);
        double m2=(y4-y3)/(x4-x3);
        return Math.atan(Math.abs((m2-m1)/(1+(m1*m2))));
    }


}



