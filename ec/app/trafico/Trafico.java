package ec.app.trafico;

import java.util.*;

import ec.util.*;
import ec.*;
import ec.multiobjective.MultiObjectiveFitness;
import ec.simple.*;
import ec.vector.*;

import ec.app.trafico.geometry.Circle;
import ec.app.trafico.geometry.Point;
import ec.app.trafico.geometry.LineSegment;


public class Trafico extends Problem implements SimpleProblemForm {

    private static final long serialVersionUID = 1L;


    public void setup(final EvolutionState state, final Parameter base)
    {
        super.setup(state, base);
    }



    public void evaluate(final EvolutionState state, final Individual ind, final int subpopulation, final int threadnum) {
        //System.out.println("Evaluate");
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

        System.out.println("Evalue: " + objectives[0] + " " +  objectives[1]);
        System.out.println("---------------------------------------------------");
        // System.out.println("Finish evaluate");
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
        // System.out.println("QoS");
        boolean debug;
        FloatVectorIndividual t_ind = (FloatVectorIndividual)ind;
        FloatVectorSpecies t_spe = (FloatVectorSpecies)ind.species;
        double qos = 0;

        int n=0;
        float [] resultado_greedy=t_spe.getResultadosGreedys()[16];
        while (n<t_ind.genome.length && t_ind.genome[n]==resultado_greedy[n])
            n++;
        // if (n==t_ind.genome.length){
        //     debug=true;
        //     System.out.println("DEBUG ES TRUEEE!!!!");
        // }
        // else
        //     debug=false;


        ArrayList<Circle> road_side_units = new ArrayList<Circle>();

        //for (int i = 0; i < t_ind.genome.length; i++){
            // System.out.print(t_ind.genome[i]);
            // System.out.println(",");
        //}
        // Create RSU's
        for (int i = 0; i < t_ind.genome.length; i++){
            int tipo_infraestructura = (int)Math.floor(t_ind.genome[i]);
            if (tipo_infraestructura != 0){
                // There is a RSU on the segment
                double [] centro = encontrar_centro_antena(t_ind, i, tipo_infraestructura, t_spe);

                Point center = new Point(centro[0], centro[1]);
                double radio_circulo = t_spe.getRadioAntena()[tipo_infraestructura];
                /*System.out.println("Circle");
                System.out.println(center.x);
                System.out.println(center.y);
                System.out.println(radio_circulo);*/
                road_side_units.add(new Circle(center, radio_circulo));
            }
        }

        // Iterate through the segments
        if (road_side_units.size() > 0) {
            for (int i = 0; i < t_ind.genome.length; i++){

                // Calculate each segment coverage
                double start_x = t_spe.getLatitudes()[t_spe.getPtoInicialSegmento()[i]];
                double start_y = t_spe.getLongitudes()[t_spe.getPtoInicialSegmento()[i]];
                double end_x = t_spe.getLatitudes()[t_spe.getPtoFinalSegmento()[i]];
                double end_y = t_spe.getLongitudes()[t_spe.getPtoFinalSegmento()[i]];

                LineSegment segment = new LineSegment(new Point(start_x, start_y), new Point(end_x, end_y));
                double segment_length = Point.twoPointsDistance(segment.start, segment.end);
                double divitions = 10;
                double module_section = segment_length / divitions;
                double intersections = 0;

                double coverered_distance = 0;

                //System.out.println("SEGMENT");
                //segment.print();

                double x_length = Math.abs(segment.start.x - segment.end.x) / divitions;
                double y_length = Math.abs(segment.start.y - segment.end.y) / divitions;


				Circle rsu_anterior=null;

                for (int j = 0; j < divitions; j++) {
                    double x = segment.start.x;
                    if (segment.start.x < segment.end.x) {
                        x = segment.start.x + j * x_length;
                    }else if (segment.start.x > segment.end.x) {
                        x = segment.start.x - j * x_length;
                    }

                    double y = segment.start.y;
                    if (segment.start.y < segment.end.y) {
                        y = segment.start.y + j * y_length;
                    }else if (segment.start.y > segment.end.y) {
                        y = segment.start.y - j * y_length;
                    }


                    Point aux_point = new Point(x, y);

                    if(rsu_anterior!=null && rsu_anterior.belongsToCircle(aux_point)) {
	                        intersections++;
                    }
                    else{

	                    for (Circle rsu : road_side_units) {
	                        if (rsu.belongsToCircle(aux_point)) {
	                            intersections++;
	                            rsu_anterior=rsu;
	                            break;
	                        }
	                    }
	                }
                }

                coverered_distance = intersections * module_section;
                /*System.out.print("total_distance:");
                System.out.println(Point.twoPointsDistance(segment.start, segment.end));
                System.out.print("coverered_distance:");
                System.out.println(coverered_distance);*/
                qos += t_spe.getCantidadVehiculosSegmento()[i] * (coverered_distance)/(double)(t_spe.getVelocidadSegmento()[i]*1000);
                /*System.out.println("######");
                System.out.println("ID: "+i);
                System.out.println("Largo: "+ segment_length);
                System.out.println("Velocidad: "+ t_spe.getVelocidadSegmento()[i]);
                System.out.println("Vehículos: "+ t_spe.getCantidadVehiculosSegmento()[i]);
                System.out.println("######");*/
                
            }
            //System.out.println("-----------------------------------------------------------------");
        }

        //System.out.print("QoS:");
        //System.out.println(qos);
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
        return centro;
    }

}



