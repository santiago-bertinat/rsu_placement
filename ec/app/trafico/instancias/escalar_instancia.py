import random
f=open("adyacencias.txt")
fn=open("normal.txt","w")
fh=open("high.txt","w")
fl=open("low.txt","w")

for line in f.readlines():
	tokens=line.strip().split(" ")
	trafico_normal=int(tokens[2])
	factor=random.uniform(0.0,0.2)
	trafico_high=round(trafico_normal+trafico_normal*factor)
	trafico_low=round(trafico_normal-(trafico_normal*factor))
	for archivo in [fn,fh,fl]:
		archivo.write("%s %s "%(tokens[0],tokens[1]))
	fn.write("%d"%trafico_normal)
	fh.write("%d"%trafico_high)
	fl.write("%d"%trafico_low)
	for archivo in [fn,fh,fl]:
		archivo.write(" %s\n"%tokens[3])
fn.close()
fh.close()
fl.close()
f.close()
	
		
				


