from pylab import *

#Initialize dictionary to parse the pareto fronts
objectives=["Cost","QoS"]

results= {}
for obj in objectives:
  results[obj]=[]

#Load the pareto fronts from the files
# path_to_front1 = "front_comparison.stat"

# f = open (path_to_front1)
# lines = f.readlines()
# for line in lines:
#   tokens=line.split()
#   results[objectives[0]].append(float(tokens[0]))
#   results[objectives[1]].append(float(tokens[1]))
# f.close()

# scatter(results[objectives[0]],results[objectives[1]],label="NSGA-II", color="blue")

# results= {}
# for obj in objectives:
#   results[obj]=[]

path_to_front2 = "knapsack_results.stat"

f = open (path_to_front2)
lines = f.readlines()
for line in lines:
  tokens=line.split()
  results[objectives[0]].append(float(tokens[0]))
  results[objectives[1]].append(float(tokens[1]))
f.close()

scatter(results[objectives[0]],results[objectives[1]],label="Knapsack", color="green")

results= {}
for obj in objectives:
  results[obj]=[]

path_to_front2 = "front.stat"

f = open (path_to_front2)
lines = f.readlines()
for line in lines:
  tokens=line.split()
  results[objectives[0]].append(float(tokens[0]))
  results[objectives[1]].append(float(tokens[1]))
f.close()

scatter(results[objectives[0]],results[objectives[1]],label="NSGA-II", color="red")


grid(True)
xlabel (objectives[0])
ylabel (objectives[1])
legend(loc=0,ncol=3 ,prop={'size':10},scatterpoints = 1)
savefig("PF.png")

# print "Greedy Costo"
# print  "Costo:%f QoS:%f"%(greedy_costo[0],greedy_costo[1])
# print "Greedy QoS"
# print  "Costo:%f QoS:%f"%(greedy_qos[0],greedy_qos[1])
# print "Extremo Inicial FP"
# print "Costo:%f QoS:%f"%(results["cost"][0], results["QoS"][0])
# print "Extremo Final FP"
# print "Costo:%f QoS:%f"%(results["cost"][-1], results["QoS"][-1])


