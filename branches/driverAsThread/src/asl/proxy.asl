// Agent boss in project delegate-tasks

/* Initial beliefs and rules */

an_agent(Ag) :-
   .all_names(L) & .length(L,X) & .random(R,0) &
   .nth(R*X,L,Ag) & .substring("driver", Ag). /*1+ = evitar agente zero*/

/* Initial goals */

/* !start. */

/* Plans */

/*@c1
+!connect[source(D)]
	: true <-
	.print(D);
	jia.proxyConnectDriver(D).
	*/	