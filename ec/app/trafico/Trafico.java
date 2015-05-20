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
        double qos_sin_penalizar;
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
                //System.out.println("Con centro "+xcentro+" "+ycentro + " y radio "+radio_circulo+" cubro a: ");
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
                        qos_sin_penalizar=t_spe.getCantidadVehiculosSegmento()[k] *(Math.min(radio_circulo, distancia_entre_dos_puntos(xcentro,ycentro,xini,yini)) + Math.min(radio_circulo, distancia_entre_dos_puntos(xcentro,ycentro,xfin,yfin)))/(double)(t_spe.getVelocidadSegmento()[k]*1000);
                        qos+= qos_sin_penalizar*factor_penalizacion(t_ind,k, t_spe);

                    }
                    else{
                        //Discrimino segun los extremos esten dentro o fuera del circulo
                        ini_dentro = radio_circulo > distancia_entre_dos_puntos(xcentro, ycentro, xini, yini);
                        fin_dentro = radio_circulo > distancia_entre_dos_puntos(xcentro, ycentro, xfin, yfin);
                        if (ini_dentro && fin_dentro){
                            //El segmento esta completamente cubierto
                            qos_sin_penalizar= distancia_entre_dos_puntos(xini,yini,xfin,yfin)*t_spe.getCantidadVehiculosSegmento()[k]/(double)(t_spe.getVelocidadSegmento()[k]*1000);
                            qos+= qos_sin_penalizar*factor_penalizacion(t_ind,k, t_spe);
                            //System.out.println("Segmento " + t_spe.getPtoInicialSegmento()[k]+ " "+ t_spe.getPtoFinalSegmento()[k] + " (T) - "+ xini+ " "+ yini+" "+xfin+" "+yfin);
                        }
                        else if (ini_dentro || fin_dentro){
                            //Hay un punto adentro y uno afuera
                            //System.out.println("Segmento " + t_spe.getPtoInicialSegmento()[k]+ " "+ t_spe.getPtoFinalSegmento()[k] + " (1P) - "+ xini+ " "+ yini+" "+xfin+" "+yfin);

                            if (ini_dentro){
                                //El punto de adentro es el inicial
                                alpha=angulo_entre_rectas(xcentro,ycentro,xini,yini,xini,yini,xfin,yfin);
                                dist_extremo_centro= distancia_entre_dos_puntos(xcentro,ycentro,xini,yini);
                            }else{
                                //El punto de adentro es el final
                                alpha=angulo_entre_rectas(xcentro,ycentro,xfin,yfin,xfin,yfin,xini,yini);
                                dist_extremo_centro= distancia_entre_dos_puntos(xcentro,ycentro,xfin,yfin);
                            }

                            if (alpha!=0){
                                //Hay angulo entre las rectas
                                beta= Math.asin(dist_extremo_centro*Math.sin(alpha)/(double)radio_circulo);
                                //System.out.println("Alpha: "+alpha);
                                //System.out.println("Beta: "+ beta);
                                //System.out.println("Dist_ext_centro "+ dist_extremo_centro);
                                //System.out.println("Cuenta final: "+ t_spe.getCantidadVehiculosSegmento()[k] * (Math.sin(Math.PI-alpha-beta)*radio_circulo/Math.sin(alpha))/(t_spe.getVelocidadSegmento()[k]*1000));
                                qos_sin_penalizar=t_spe.getCantidadVehiculosSegmento()[k] * (Math.sin(Math.PI-alpha-beta)*radio_circulo/Math.sin(alpha))/(double)(t_spe.getVelocidadSegmento()[k]*1000);
                                qos+= qos_sin_penalizar*factor_penalizacion(t_ind,k, t_spe);
                            }
                            else{
                                //Los 3 puntos están alineados
                                if (ini_dentro){
                                    qos_sin_penalizar=t_spe.getCantidadVehiculosSegmento()[k]*(radio_circulo-distancia_entre_dos_puntos(xcentro,ycentro,xini,yini))/(double)(t_spe.getVelocidadSegmento()[k]*1000);
                                    qos+= qos_sin_penalizar*factor_penalizacion(t_ind,k, t_spe);
                                }
                                else{
                                    qos_sin_penalizar=t_spe.getCantidadVehiculosSegmento()[k]*(radio_circulo-distancia_entre_dos_puntos(xcentro,ycentro,xfin,yfin))/(double)(t_spe.getVelocidadSegmento()[k]*1000);
                                    qos+= qos_sin_penalizar*factor_penalizacion(t_ind,k, t_spe);

                                }
                            }
                            
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
                                //System.out.println("Segmento " + t_spe.getPtoInicialSegmento()[k]+ " "+ t_spe.getPtoFinalSegmento()[k] + " (2P) - "+ xini+ " "+ yini+" "+xfin+" "+yfin);
                                //El segmento intersecta el circulo
                                lambda=Math.sqrt(Math.pow(radio_circulo,2) - Math.pow(m,2));
                                qos_sin_penalizar=t_spe.getCantidadVehiculosSegmento()[k] * (2*lambda)/(double)(t_spe.getVelocidadSegmento()[k]*1000);
                                qos+= qos_sin_penalizar*factor_penalizacion(t_ind,k, t_spe);

                            }
                        }
                    }
                    //System.out.println(k+" "+qos);

                }
                //System.out.println("-----------------------------------------------------------------------------------");
            }    

        }
        return qos;
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


    public double distancia_entre_dos_puntos(double xa, double ya, double xb, double yb){

        double deglen=110.25*1000;
        double x= xa-xb;
        double y= (ya-yb)*Math.cos(xb);
        return deglen* Math.sqrt(x*x + y*y);

    }

    public double distancia_punto_recta(double xp,double yp,double x1,double y1,double x2,double y2){

        //Usando la formula de Heron
        double d1p, d2p,d12;
        d1p=distancia_entre_dos_puntos(xp,yp,x1,y1);
        d2p=distancia_entre_dos_puntos(xp,yp,x2,y2);
        d12=distancia_entre_dos_puntos(x1,y1,x2,y2);

        double s = (d1p+d2p+d12)/(double)2;
        double area= Math.sqrt(s*(s-d1p)*(s-d2p)*(s-d12));
        return 2*area/(double)d12;
    }

    public double angulo_entre_rectas(double x1,double y1,double x2,double y2, double x3,double y3,double x4,double y4){
        double m1=(y2-y1)/(double)(x2-x1);
        double m2=(y4-y3)/(double)(x4-x3);
        return Math.atan(Math.abs((m2-m1)/(double)(1+(m1*m2))));
    }

    public double area_interseccion_circulos(double radius1, double radius2, double distance){
        double r = radius1;
        double r_max = radius2;
        double d = distance;
        if(r_max < r){
            // swap
            r = radius2;
            r_max = radius1;
        }
        double part1 = r*r*Math.acos((d*d + r*r - r_max*r_max)/(double)(2*d*r));
        double part2 = r_max*r_max*Math.acos((d*d + r_max*r_max - r*r)/(double)(2*d*r_max));
        double part3 = 0.5*Math.sqrt((-d+r+r_max)*(d+r-r_max)*(d-r+r_max)*(d+r+r_max));

        double intersectionArea = part1 + part2 - part3;
        return intersectionArea;
    }

    public double factor_penalizacion(FloatVectorIndividual t_ind, int indice, FloatVectorSpecies t_spe){
        //QoSp=QoS * FACTOR_PENALIZACION
        //FACTOR_PENALIZACION = 1 - (sum area de intersecciones/(area_circulo * #intersecciones))

        int tipo_infraestructura_principal=(int) Math.floor(t_ind.genome[indice]);
        double [] centro_principal=encontrar_centro_antena(t_ind, indice, tipo_infraestructura_principal, t_spe);
        double xcentro_principal=centro_principal[0];
        double ycentro_principal=centro_principal[1];
        double radio_principal=t_spe.getRadioAntena()[tipo_infraestructura_principal];
        double area_principal=2*Math.PI*(radio_principal*radio_principal);
        double cantidad_de_intersecciones=0;
        double suma_area_intersecciones=0;

        for (int i=0;i<t_ind.genome.length;i++){
            if (i!=indice){
                int tipo_infraestructura=(int) Math.floor(t_ind.genome[i]);
                double [] centro=encontrar_centro_antena(t_ind, i, tipo_infraestructura, t_spe);
                //Circulo de cobertura
                double xcentro=centro[0];
                double ycentro=centro[1];
                double radio=t_spe.getRadioAntena()[tipo_infraestructura];
                double distancia_entre_centros=distancia_entre_dos_puntos(xcentro_principal,ycentro_principal,xcentro,ycentro);
                //Si la distancia entre los centros es mayor que la suma de los radios->los circulos no se intersectan
                if (radio+radio_principal>distancia_entre_centros){
                    double area_interseccion=area_interseccion_circulos(radio_principal,radio,distancia_entre_centros);
                    if (area_interseccion>0.000001){
                        //Condicion de guarda por si el calculo de la interseccion no es preciso
                        cantidad_de_intersecciones+=1;
                        suma_area_intersecciones+=area_interseccion;
                    }
                }
            }
        }

        double factor = 1 - (suma_area_intersecciones/(double)(cantidad_de_intersecciones*area_principal));
        return factor;
    }


}



