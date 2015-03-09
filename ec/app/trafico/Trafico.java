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
        double xcentro, ycentro,radio_circulo, alpha, beta, dist_extremo_centro,xini,yini,xfin,yfin, m, lambda, dAC,dBC,dAQ,dQB,dAB;
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
                System.out.println("Con centro "+xcentro+" "+ycentro + " y radio "+radio_circulo+" cubro a: ");
                //Veo la cobertura en cada uno de los segmentos
                for (int k=0;k<t_ind.genome.length;k++){

                    xini=t_spe.getLatitudes()[t_spe.getPtoInicialSegmento()[k]];
                    yini=t_spe.getLongitudes()[t_spe.getPtoInicialSegmento()[k]];
                    xfin =  t_spe.getLatitudes()[t_spe.getPtoFinalSegmento()[k]];
                    yfin = t_spe.getLongitudes()[t_spe.getPtoFinalSegmento()[k]];

                    //DEBUG
                    //System.out.println("Centro "+xcentro+" "+ycentro+" y Radio: "+radio_circulo);

                    if (k==i){
                        //Separo el calculo de la cobertura sobre el propio segmento
                        qos+= t_spe.getCantidadVehiculosSegmento()[k] *Math.min(radio_circulo, distancia_entre_dos_puntos(xcentro,ycentro,xini,yini)) + Math.min(radio_circulo, distancia_entre_dos_puntos(xcentro,ycentro,xfin,yfin));

                    }
                    else{
                        //Discrimino segun los extremos esten dentro o fuera del circulo
                        ini_dentro = radio_circulo > distancia_entre_dos_puntos(xcentro, ycentro, xini, yini);
                        fin_dentro = radio_circulo > distancia_entre_dos_puntos(xcentro, ycentro, xfin, yfin);
                        if (ini_dentro && fin_dentro){
                            //El segmento esta completamente cubierto
                            qos+= distancia_entre_dos_puntos(xini,yini,xfin,yfin)*t_spe.getCantidadVehiculosSegmento()[k];
                            System.out.println("Segmento " + t_spe.getPtoInicialSegmento()[k]+ " "+ t_spe.getPtoFinalSegmento()[k] + " (T) - "+ xini+ " "+ yini+" "+xfin+" "+yfin);
                        }
                        else if (ini_dentro || fin_dentro){
                            //Hay un punto adentro y uno afuera
                            System.out.println("Segmento " + t_spe.getPtoInicialSegmento()[k]+ " "+ t_spe.getPtoFinalSegmento()[k] + " (1P) - "+ xini+ " "+ yini+" "+xfin+" "+yfin);

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
                            //La recta intersecta el circulo, falta ver si el segmento tambien
                            m=distancia_punto_recta(xcentro,ycentro,xini,yini,xfin,yfin);
                            dAC=distancia_entre_dos_puntos(xcentro,ycentro,xini,yini);
                            dBC=distancia_entre_dos_puntos(xcentro,ycentro,xfin,yfin);
                            dAB=distancia_entre_dos_puntos(xini,yini,xfin,yfin);
                            dAQ=Math.sqrt(Math.pow(dAC,2)-Math.pow(m,2));
                            dQB=Math.sqrt(Math.pow(dBC,2)-Math.pow(m,2));
                            if (dAQ<dAB && dQB<dAB){
                                System.out.println("Segmento " + t_spe.getPtoInicialSegmento()[k]+ " "+ t_spe.getPtoFinalSegmento()[k] + " (2P) - "+ xini+ " "+ yini+" "+xfin+" "+yfin);
                                //El segmento intersecta el circulo
                                lambda=Math.sqrt(Math.pow(radio_circulo,2) - Math.pow(m,2));
                                qos+=t_spe.getCantidadVehiculosSegmento()[k] * (2*lambda);
                            }
                        }
                    }
                }
                System.out.println("-----------------------------------------------------------------------------------");
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
        centro[0] = lat_ini + Math.sqrt((lng_fin-lng_ini)*(lng_fin-lng_ini) + (lat_fin-lat_ini)*(lat_fin-lat_ini))*lambda*(1/(Math.sqrt(1+Math.pow(a,2))));
        centro[1] = lng_ini + Math.sqrt((lng_fin-lng_ini)*(lng_fin-lng_ini) + (lat_fin-lat_ini)*(lat_fin-lat_ini))*lambda*(a/(Math.sqrt(1+Math.pow(a,2))));
        //DEBUG
        //System.out.println("El centro de ("+lat_ini+" "+lng_ini+") y ("+lat_fin+" "+lng_fin+") es ("+centro[0]+" "+centro[1]+") con lambda "+lambda);   
        return centro;  
    }


    public double distancia_entre_dos_puntos(double xa, double ya, double xb, double yb){
        //return Math.sqrt((xa-xb)*(xa-xb) + (ya-yb)*(ya-yb));

        double deglen=110.25*1000;
        double x= xa-xb;
        double y= (ya-yb)*Math.cos(xb);
        return deglen* Math.sqrt(x*x + y*y);

    }

    public double distancia_punto_recta(double xp,double yp,double x1,double y1,double x2,double y2){
        // double a= Math.abs((y2-y1)*xp - (x2-x1)*yp + x2*y1 - y2*x1);
        // double b= Math.pow(y2-y1,2)+ Math.pow(x2-x1,2);
        // return a/Math.sqrt(b);

        // double deglen=110.25*1000;
        // yp=yp*Math.cos(xp);
        // y1=y1*Math.cos(xp);
        // y2=y2*Math.cos(xp);
        // double a= Math.abs((y2-y1)*xp - (x2-x1)*yp + x2*y1 - y2*x1);
        // double b= Math.pow(y2-y1,2)+ Math.pow(x2-x1,2);
        //return deglen*a/Math.sqrt(b);

        //Usando la formula de Heron
        double d1p, d2p,d12;
        d1p=distancia_entre_dos_puntos(xp,yp,x1,y1);
        d2p=distancia_entre_dos_puntos(xp,yp,x2,y2);
        d12=distancia_entre_dos_puntos(x1,y1,x2,y2);

        double s = (d1p+d2p+d12)/2;
        double area= Math.sqrt(s*(s-d1p)*(s-d2p)*(s-d12));
        return 2*area/d12;
    }

    public double angulo_entre_rectas(double x1,double y1,double x2,double y2, double x3,double y3,double x4,double y4){
        double m1=(y2-y1)/(x2-x1);
        double m2=(y4-y3)/(x4-x3);
        return Math.atan(Math.abs((m2-m1)/(1+(m1*m2))));
    }


}



