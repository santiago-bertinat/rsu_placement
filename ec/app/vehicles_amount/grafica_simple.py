from pylab import *

#Greedys
greedy_costo=[4133.099945068359, 22321.579525473975]
greedy_qos=[9072.799896240234,29467.391112470326]


#Initialize dictionary to parse the pareto fronts
objectives=["cost","QoS"]

results= {}
for obj in objectives:
	results[obj]=[]


#Load the pareto fronts from the files
path_to_file="front.stat"
f=open (path_to_file)
lines= f.readlines()
for line in lines:
	tokens=line.split()
	results[objectives[0]].append(float(tokens[0]))
	results[objectives[1]].append(float(tokens[1]))
f.close()

scatter(results[objectives[0]],results[objectives[1]],label="NSGA-II", color="blue")




scatter(greedy_costo[0],greedy_costo[1],color="red",marker="o")
annotate("greedy_cost",(greedy_costo[0],greedy_costo[1]))

scatter(greedy_qos[0],greedy_qos[1],color="red",marker="o")
annotate("greedy_QoS",(greedy_qos[0],greedy_qos[1]))


grid(True)
xlabel (objectives[0])
ylabel (objectives[1])
legend(loc=0,ncol=3 ,prop={'size':10},scatterpoints = 1)
savefig("PF.png")

print "Greedy Costo"
print  "Costo:%f QoS:%f"%(greedy_costo[0],greedy_costo[1])
print "Greedy QoS"
print  "Costo:%f QoS:%f"%(greedy_qos[0],greedy_qos[1])
print "Extremo Inicial FP"
print "Costo:%f QoS:%f"%(results["cost"][0], results["QoS"][0])
print "Extremo Final FP"
print "Costo:%f QoS:%f"%(results["cost"][-1], results["QoS"][-1])


for i,resultado in enumerate(results["QoS"]):
	if (resultado>greedy_qos[1]):
		print i

#print "FP Con igual precio que greedy costo"
#print "Costo:%f QoS:%f"%(results["cost"][28], results["QoS"][28])

print "FP Con igual qos que greedy qos"
print "Costo:%f QoS:%f"%(results["cost"][21], results["QoS"][21])
