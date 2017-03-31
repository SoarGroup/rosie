import numpy as np
import matplotlib.pyplot as plt

# evenly sampled time at 200ms intervals
t = (0,3,6,9)#np.arange(0, 9, 3);
pC = (16, 17, 18, 19)
pI = (50, 53, 57, 58)
pT = (950,820,600,550)
plt.yscale('log')
plt.title('Agent Efficiency by Task Order')
plt.ylabel('Avg. Time(msecs)', fontsize=18)
plt.xlabel('Learning order position', fontsize=18)

# red dashes, blue squares and green triangles
perC, = plt.plot(t, pC, 'bo')
perI, =plt.plot(t, pI, 'rs')
perT, =plt.plot(t, pT, 'g^')
plt.legend((perC, perI, perT), ('per cycle','per interaction','per task'), loc=1, fontsize=18 )
plt.savefig('taskeff.eps', format='eps', dpi= 300)
plt.close()
